/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is FatUtils.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.matching;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.daat.FatCandidateResultSet;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.FieldLexiconEntry;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.collections.MapEntry;
import org.terrier.structures.postings.BasicPostingImpl;
import org.terrier.structures.postings.BlockFieldPostingImpl;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.BlockPostingImpl;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.FieldPostingImpl;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.IterablePostingImpl;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.Files;
import org.terrier.utility.io.DebuggingDataInput;
import org.terrier.utility.io.DebuggingDataOutput;
import org.terrier.utility.io.WrappedIOException;

/** Various utilities for the dealing with {@link FatResultSet}s.
 * @author Craig Macdonald
 * @since 4.0
 */
public class FatUtils {

	private static final byte VERSION = 2;
	private static final boolean DEBUG = false;
	
	static Logger logger = LoggerFactory.getLogger(FatUtils.class);
	
	public static FatResultSet recreate(FatResultSet frs) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		frs.write(dos);
		FatResultSet rtr = new FatCandidateResultSet();
		rtr.readFields(new DataInputStream(new ByteArrayInputStream(baos.toByteArray())));
		return rtr;
	}
	
	public static void readFields(FatResultSet frs, DataInput in) throws IOException
	{
		if (DEBUG)
			in = new DebuggingDataInput(in);
		int i =-1;
		int resultSize = -1;
		int j = -1;
		int termCount = -1;
		try{
			if (in.readByte() != VERSION)
			{
				throw new IOException("Version mismatch");
			}
			
			CollectionStatistics collStats = new CollectionStatistics();
			collStats.readFields(in);
			frs.setCollectionStatistics(collStats);
			
			final boolean fields = collStats.getNumberOfFields() > 0;
			final int fieldCount = collStats.getNumberOfFields();
				
			//read number of query terms
			termCount = in.readInt();
			if (termCount == 0)
			{
				frs.setResultSize(0);
				final int[] docids 		= new int[0];
				final double[] scores 		= new double[0];
				final short[] occurrences = new short[0];
				final WritablePosting[][] postings = new WritablePosting[0][];				
				frs.setScores(scores);
				frs.setDocids(docids);
				frs.setPostings(postings);
				frs.setOccurrences(occurrences);
				
				System.err.println("No found terms for this query");
				return;
			}
			
			//read the classes to use
			String statsClassName = in.readUTF();
			
			//hack for some older fat result versions
			if (statsClassName.equals("org.terrier.structures.FieldIndex$FieldIndexLexiconEntry"))
				statsClassName = FieldLexiconEntry.class.getName();

			Class<? extends EntryStatistics> statisticsClass = Class.forName(statsClassName).asSubclass(EntryStatistics.class);
			Class<? extends WritablePosting> postingClass = Class.forName(in.readUTF()).asSubclass(WritablePosting.class);
			
			
			//read terms and entry statistics
			final EntryStatistics[] entryStats = new EntryStatistics[termCount];
			final String[] queryTerms = new String[termCount];
			final double[] keyFrequencies = new double[termCount];
			for(j=0;j<termCount;j++)
			{
				queryTerms[j] = in.readUTF();
				final EntryStatistics le = fields
					? statisticsClass.getConstructor(Integer.TYPE).newInstance(fieldCount)
					: statisticsClass.newInstance();
				entryStats[j] = le;
				keyFrequencies[j] = in.readDouble();
				((Writable)le).readFields(in);
			}
			frs.setEntryStatistics(entryStats);
			frs.setKeyFrequencies(keyFrequencies);
			frs.setQueryTerms(queryTerms);
			
			
			//read the number of documents
			resultSize = in.readInt();
			//size the arrays
			final int[] docids 		= new int[resultSize];
			final double[] scores 		= new double[resultSize];
			final short[] occurrences = new short[resultSize];
			final WritablePosting[][] postings = new WritablePosting[resultSize][];
			
			//for each document
			for (i = 0; i < resultSize; i++)
			{
				//read: docid, scores, occurrences
				docids[i] = in.readInt();
				scores[i] = in.readDouble();
				occurrences[i] = in.readShort();
				final int docLen = in.readInt();
				final int[] fieldLens;
				if (fields)
				{
					fieldLens = new int[fieldCount];
					for(int fi=0;fi<fieldCount;fi++)
						fieldLens[fi] = in.readInt();
					//System.err.println("docid="+docids[i] + " lf=" + org.terrier.utility.ArrayUtils.join(fieldLens, ","));
				}
				else
				{
					fieldLens = null;
				}
				
				postings[i] = new WritablePosting[termCount];
				
				//boolean anyPostings = false;
				
				//for each term
				for (j = 0; j < termCount; j++) {
					//read the id of the posting, and use Posting.read(in);
					boolean hasPosting = in.readBoolean();
					if (! hasPosting)
						continue;
					//anyPostings = true;
					WritablePosting p = postingClass.newInstance();
					p.readFields(in);
					//check that the posting we read is assigned to the docid
					assert docids[i] == p.getId();
					
					p.setDocumentLength(docLen);
					if (fields)
						((FieldPosting)p).setFieldLengths(fieldLens);
					postings[i][j] = p;
				}
				//leave at least one posting lying around for each document, even if it is empty
//				if (! anyPostings)
//				{
//					postings[i][0] = postingClass.newInstance();
//					postings[i][0].setId(0);
//					postings[i][0].setDocumentLength(0);
//				}
//				if (docids[i] == 65936)
//				{
//					System.err.println("Reading 65936: " + postings[i][0].toString());
//				}
			}
		frs.setScores(scores);
		frs.setDocids(docids);
		frs.setPostings(postings);
		frs.setOccurrences(occurrences);
		}catch (EOFException eofe) {
			logger.error("EOF within FatUtils.read()", eofe);
			throw eofe;
		}catch (IOException ioe) {
			logger.error("EOF within FatUtils.read()", ioe);
			throw ioe;			
		}catch (Exception e) {
			throw new WrappedIOException("Problem reading document at rank " + i + " of " + resultSize + ", term " + j + " of " + termCount, e);
		}
	}
	
	public static void write(FatResultSet frs, DataOutput out) throws IOException
	{
		if (DEBUG)
			out = new DebuggingDataOutput(out);
		out.writeByte(VERSION);
		
		final CollectionStatistics collStats = frs.getCollectionStatistics();
		final EntryStatistics[] entryStats = frs.getEntryStatistics();
		final String[] queryTerms = frs.getQueryTerms();
		final double[] keyFrequency = frs.getKeyFrequencies();
		final WritablePosting[][] postings = frs.getPostings();
		final int[] docids = frs.getDocids();
		final double[] scores = frs.getScores();
		final short[] occurrences = frs.getOccurrences();
		
		
		collStats.write(out);
		final boolean fields = collStats.getNumberOfFields() > 0;
		final int fieldCount = collStats.getNumberOfFields();		
		final int queryTermCount = queryTerms.length;
		
		//write out the number of query terms
		out.writeInt(queryTermCount);
		if(queryTermCount == 0)
			return;
	
		//write out the classes
		out.writeUTF(entryStats[0].getClass().getName());
		out.writeUTF(firstPosting(postings).getClass().getName());
		
		
		//write out query terms
		//write out the entry statistics
		for (int i = 0; i < queryTermCount; i++){
			out.writeUTF(queryTerms[i]);
			out.writeDouble(keyFrequency[i]);
			((Writable)entryStats[i]).write(out);
		}
		
		//write out the number of documents
		out.writeInt(docids.length);
		int i = 0;
		//for each document
		long notNullPostings = 0;
		for (i = 0; i < docids.length; i ++) {
			//write out the docid to out 
			out.writeInt(docids[i]);
			//write out the score
			out.writeDouble(scores[i]);
			//write out the occurrences
			out.writeShort(occurrences[i]);
			
			//write out the document length, and possible field lengths			
			WritablePosting firstPosting = firstPosting(postings[i]);
			assert firstPosting != null : "Docid " + docids[i] + " with score " + scores[i] + " has no matching postings";
			out.writeInt(firstPosting.getDocumentLength());			
			if (fields)
			{
				final int[] fieldLengths = ((FieldPosting)firstPosting).getFieldLengths();
				assert fieldLengths.length == fieldCount;
				for(int fi=0;fi<fieldCount;fi++)
					out.writeInt(fieldLengths[fi]);
			}
			
			//for each posting that is not null
			assert postings[i].length == queryTermCount;
			for (WritablePosting p : postings[i]) {
				//write the id of the posting, and use Posting.write(out)
				out.writeBoolean(p != null);
				if (p != null)
				{
					notNullPostings++;
					p.write(out);
				}
			}
		}
		logger.info(docids.length +" documents, "+queryTermCount+" terms, mean postings per document: " + ((double)notNullPostings/(double)docids.length) );
	}
	
	public static Index makeIndex(FatResultSet frs)
	{
		final int termCount = frs.getQueryTerms().length;
		final int docCount = frs.getResultSize();
		final int[] docids = frs.getDocids();
		final String[] queryTerms = frs.getQueryTerms();
		final EntryStatistics[] entryStats = frs.getEntryStatistics();
		final WritablePosting[][] postings = new WritablePosting[docCount][];
		final CollectionStatistics collStats = frs.getCollectionStatistics();
		System.arraycopy(frs.getPostings(), 0, postings, 0, docCount);		
		final Map<String,EntryStatistics> statsMap = new HashMap<String,EntryStatistics>();		
		
		final boolean fields = frs.getCollectionStatistics().getNumberOfFields() > 0;
		final int fieldCount = frs.getCollectionStatistics().getNumberOfFields();
		final boolean blocks = firstPosting(postings) instanceof BlockPosting;
		
		//make maps based on the terms
		for (int i=0;i<termCount;i++)
		{
			boolean anyPostings = firstPosting(postings, i) != null;
			if (anyPostings)
			{				
				((LexiconEntry) entryStats[i]).setTermId(i);
				statsMap.put(queryTerms[i], entryStats[i]);
				//System.err.println("set: "+ queryTerms[i]  + entryStats[i] + " => " + i);
			}
			else
			{
				//TODO: optimise this by knowing at creation/loading of resultset.
				logger.warn("Ignoring term " + queryTerms[i] + " as it has no non-null postings in the FatResultSet");
			}
		}
		for (int di=0;di<docCount;di++)
		{
			boolean any = false;
			if (postings[di] == null)
				postings[di] = new WritablePosting[termCount];
			else
				for(WritablePosting p : postings[di])
				{
					if (p != null)
					{
						any = true;
						break;
					}				
				}
			if (! any)
				postings[di][0] = blocks 
					? fields 
						? new BlockFieldPostingImpl(docids[di], 0, new int[0], fieldCount)
						: new BlockPostingImpl(docids[di], 0, new int[0])
					: fields
						? new FieldPostingImpl(docids[di], 0, fieldCount)
						: new BasicPostingImpl(docids[di], 0);
		}
		
		//sort the postings by id.
		Arrays.sort(postings, new Comparator<WritablePosting[]>()
		{			
			@Override
			public int compare(WritablePosting[] p1, WritablePosting[] p2) {
				final int x = firstPosting(p1).getId();
				final int y = firstPosting(p2).getId();
				return (x < y) ? -1 : ((x == y) ? 0 : 1);
			}			
		});
		
		final Lexicon<String> lex = new Lexicon<String>() {

			@Override
			public LexiconEntry getLexiconEntry(String term) {
				return (LexiconEntry) statsMap.get(term);
			}

			@Override
			public Entry<String, LexiconEntry> getLexiconEntry(int termid) {
				throw new UnsupportedOperationException();
			}
			@Override
			public Entry<String, LexiconEntry> getIthLexiconEntry(int index) {
				throw new UnsupportedOperationException();
			}
			@Override
			public void close() throws IOException {}
			@Override
			public Iterator<Entry<String, LexiconEntry>> iterator() {
				throw new UnsupportedOperationException();
			}
			@Override
			public int numberOfEntries() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Iterator<Entry<String, LexiconEntry>> getLexiconEntryRange(
					String from, String to) {
				throw new UnsupportedOperationException();
			}
		};
		
		final PostingIndex<Pointer> inv = new PostingIndex<Pointer>()
		{
			@Override
			public void close() throws IOException {}

			@Override
			public IterablePosting getPostings(Pointer lEntry)
					throws IOException 
			{
				final int term = ((LexiconEntry)lEntry).getTermId();
				//System.err.println("read: " + lEntry + " => " + term);
				if (blocks && fields)
					return new BFIterablePostingFromWritablePostingSlice(postings, term);
				else if (fields)
					return new FIterablePostingFromWritablePostingSlice(postings, term);
				else if (blocks)
					return new BIterablePostingFromWritablePostingSlice(postings, term);
				else 
					return new IterablePostingFromWritablePostingSlice(postings, term);
			}			
		};
		
		return new Index(0l, 0l, 0l){

			@Override
			public PostingIndex<Pointer> getInvertedIndex() {
				return inv;
			}

			@Override
			public Lexicon<String> getLexicon() {
				return lex;
			}

			@Override
			public CollectionStatistics getCollectionStatistics() {
				return collStats;
			}

			@Override
			public void close() throws IOException {
				
			}

			@Override
			public void flush() throws IOException {
				
			}

			@Override
			public PostingIndex<?> getDirectIndex() {
				return null;
			}

			@Override
			public DocumentIndex getDocumentIndex() {
				return null;
			}

			@Override
			public Object getIndexStructure(String structureName) {
				return null;
			}

			@Override
			public Object getIndexStructureInputStream(String structureName) {
				return null;
			}

			@Override
			public MetaIndex getMetaIndex() {
				return null;
			}

			@Override
			public String toString() {
				return this.getClass().getSimpleName();
			}
			
		};
	}
	
	static class IterablePostingFromWritablePostingSlice extends IterablePostingImpl
	{		
		
		final WritablePosting[][] postings; //document, term
		final int slice;
		WritablePosting current;
		int index;
		
		public IterablePostingFromWritablePostingSlice(WritablePosting[][] postings, int slice)
		{
			this.index = -1;
			this.postings = postings;
			this.slice = slice;
		}
		
		@Override
		public int next() throws IOException {
			index++;
			if (index >= postings.length)
				return EOL;
			current = postings[index][slice];
			while(current == null)
			{
				index++;
				if (index >= postings.length)
					return EOL;
				current = postings[index][slice];
			}
			return current.getId();
		}

		@Override
		public boolean endOfPostings() {
			return index < postings.length;
		}

		@Override
		public int getId() {
			return  current.getId();
		}

		@Override
		public int getFrequency() {
			return current.getFrequency();
		}

		@Override
		public int getDocumentLength() {
			return current.getDocumentLength();
		}

		@Override
		public void setId(int id) {
			current.setId(id);
		}

		@Override
		public WritablePosting asWritablePosting() {
			return current.asWritablePosting();
		}

		@Override
		public void close() throws IOException {}
		
	}
	
	static class BIterablePostingFromWritablePostingSlice extends IterablePostingFromWritablePostingSlice implements BlockPosting
	{
		public BIterablePostingFromWritablePostingSlice(
				WritablePosting[][] postings, int slice) {
			super(postings, slice);
		}

		@Override
		public int[] getPositions() {
			return ((BlockPosting)current).getPositions(); 
		}		
	}
	
	
	static class FIterablePostingFromWritablePostingSlice extends IterablePostingFromWritablePostingSlice implements FieldPosting
	{
		public FIterablePostingFromWritablePostingSlice(
				WritablePosting[][] postings, int slice) {
			super(postings, slice);
		}

		@Override
		public int[] getFieldFrequencies() {
			return ((FieldPosting)current).getFieldFrequencies();
		}

		@Override
		public int[] getFieldLengths() {
			return ((FieldPosting)current).getFieldLengths();
		}

		@Override
		public void setFieldLengths(int[] newLengths) {
			((FieldPosting)current).setFieldLengths(newLengths);
		}	
	}
	
	static class BFIterablePostingFromWritablePostingSlice extends BIterablePostingFromWritablePostingSlice implements FieldPosting
	{

		public BFIterablePostingFromWritablePostingSlice(
				WritablePosting[][] postings, int slice) {
			super(postings, slice);
		}

		@Override
		public int[] getFieldFrequencies() {
			return ((FieldPosting)current).getFieldFrequencies();
		}

		@Override
		public int[] getFieldLengths() {
			return ((FieldPosting)current).getFieldLengths();
		}

		@Override
		public void setFieldLengths(int[] newLengths) {
			((FieldPosting)current).setFieldLengths(newLengths);
		}		
		
		/** Makes a human readable form of this posting */
		@Override
		public String toString()
		{
			return "(" + getId() + "," + getFrequency() + ",F[" + ArrayUtils.join(getFieldFrequencies(), ",")
				+ "],B[" + ArrayUtils.join(getPositions(), ",") + "])";
		}
	}

	protected static WritablePosting firstPosting(WritablePosting[][] postings)
	{
		for(int i=0;i<postings.length;i++)
		{
			if (postings[i] == null)
				continue;
			for(int j=0;j<postings[i].length;j++)
				if (postings[i][j] != null)
					return postings[i][j];
		}
		return null;
	}
	
	protected static WritablePosting firstPosting(WritablePosting[][] postings, int termIndex)
	{
		for(int i=0;i<postings.length;i++)
		{
			if (postings[i] == null)
				continue;
			assert termIndex < postings[i].length;
			if (postings[i][termIndex] != null)
				return postings[i][termIndex];
		}
		return null;
	}
	
	public static WritablePosting firstPosting(WritablePosting[] writablePostings) {
		if (writablePostings == null)
			return null;
		for(WritablePosting p : writablePostings)
			if (p != null)
				return p;
		return null;
	}
	
	public static String getInfo(FatResultSet frs) {
		return frs.getResultSize() + " documents for " 
			+ frs.getQueryTerms().length + " query terms. Posting type is "
			+ firstPosting(frs.getPostings()).getClass().getSimpleName();
	}
	
	public static void dump(FatResultSet frs) {
		String[] qts = frs.getQueryTerms();
		EntryStatistics[] es = frs.getEntryStatistics();
		double[] ks = frs.getKeyFrequencies();
		System.out.println(ks.length + " query terms:");
		for(int i=0;i<qts.length;i++)
		{
			System.out.println("\t"+qts[i]+ "^"+ ks[i] + " : " + es[i]);
		}
		
		System.out.println( frs.getResultSize() + " documents for " 
			+ frs.getQueryTerms().length + " query terms. Posting type is "
			+ firstPosting(frs.getPostings()).getClass().getSimpleName());
		
		final int[] docids = frs.getDocids();
		final double[] scores = frs.getScores();
		final WritablePosting[][] postings = frs.getPostings();
		final int termCount = postings[0].length;
		final boolean fields = firstPosting(postings) instanceof FieldPosting;
		
		for(int i=0;i<frs.getResultSize();i++)
		{
			System.out.print("rank "+i+" docid " + docids[i] + " score=" + scores[i] + ' ');
			for(int j=0;j<termCount;j++)
			{
				if (postings[i][j] == null)
				{
					System.out.print("j="+j+":null ");
					continue;
				}
				System.out.print(postings[i][j].toString());
				System.out.print(" l=" + postings[i][j].getDocumentLength());
				if (fields)
					System.out.print(" lf=" + ArrayUtils.join(((FieldPosting) postings[i][j]).getFieldLengths(), ","));
				System.out.print(' ');
			}
			System.out.println();
		}
	}
	
	interface CloseableIterator<E> extends Closeable, Iterator<E>{};
	
	public static Iterator<Map.Entry<String,FatResultSet>> readFatResultSet(String filename) throws IOException
	{
		final DataInputStream dis = new DataInputStream(Files.openFileStream(filename));
		return new CloseableIterator<Map.Entry<String,FatResultSet>> () {

			boolean more = true;
			
			@Override
			public boolean hasNext() {
				return more;
			}

			@Override
			public Entry<String, FatResultSet> next() {
				try{
					String qid = dis.readUTF();
					FatResultSet frs = new FatCandidateResultSet();
					frs.readFields(dis);
					return new MapEntry<String,FatResultSet>(qid, frs);
				} catch (IOException e) {
					more = false;
					return null;
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public void close() throws IOException {
				dis.close();
			}
			
		};
	}
	
	public static void main(String[] args) throws IOException
	{
		if (args.length != 2)
		{
			System.err.println("Usage: " +FatUtils.class.getName() + " {--info|--dump} results.fat.gz");
			return;
			
		}
		final boolean dump = args[0].equals("--dump");
		FatResultSet frs = new FatCandidateResultSet();
		DataInputStream dis = new DataInputStream(Files.openFileStream(args[1]));
		int queryCount = 0;
		while(true)
		{
			try
			{
				String qid = dis.readUTF();
				System.err.println("Now reading query " + qid);
				queryCount++;
				frs.readFields(dis);
				if (! dump)
					System.out.println(qid + " " + getInfo(frs));
				else
				{
					System.out.println("Query " + qid);
					dump(frs);
					System.out.println();
				}
			}
			catch (EOFException e) {
				break;
			}
		}
		System.out.println("Total " + queryCount + " queries");
	}
	
}
