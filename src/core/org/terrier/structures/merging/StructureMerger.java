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
 * The Original Code is StructureMerger.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author) 
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures.merging;

import gnu.trove.TIntIntHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.AbstractPostingOutputStream;
import org.terrier.structures.BasicDocumentIndexEntry;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.FSOMapFileLexiconOutputStream;
import org.terrier.structures.FieldDocumentIndexEntry;
import org.terrier.structures.FieldLexiconEntry;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.LexiconOutputStream;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.SimpleBitIndexPointer;
import org.terrier.structures.SimpleDocumentIndexEntry;
import org.terrier.structures.bit.DirectInvertedOutputStream;
import org.terrier.structures.bit.FieldDirectInvertedOutputStream;
import org.terrier.structures.indexing.CompressingMetaIndexBuilder;
import org.terrier.structures.indexing.CompressionFactory;
import org.terrier.structures.indexing.DocumentIndexBuilder;
import org.terrier.structures.indexing.LexiconBuilder;
import org.terrier.structures.indexing.MetaIndexBuilder;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.Posting;
import org.terrier.structures.postings.PostingIdComparator;
import org.terrier.structures.postings.bit.BasicIterablePosting;
import org.terrier.structures.postings.bit.FieldIterablePosting;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;

/**
 * This class merges the structures created by Terrier, so that
 * we use fewer and larger inverted and direct files.
 * <p>
 * <b>Properties:</b>&lt;ul&gt;
  * <li><tt>lexicon.use.hash</tt> - build a lexicon hash file for new index. Set to <tt>true</tt> by default.</li>
 * <li><tt>merge.direct</tt> - merge the direct indices if both indices have them. Set to <tt>true</tt> by default.</li>
 * @author Vassilis Plachouras and Craig Macdonald
  */
public class StructureMerger {
	
	/** the logger used */
	protected static final Logger logger = LoggerFactory.getLogger(StructureMerger.class);	
	/** 
	 * A hashmap for converting the codes of terms appearing only in the 
	 * vocabulary of the second set of data structures into a new set of 
	 * term codes for the merged set of data structures.
	 */
	protected TIntIntHashMap termcodeHashmap = null;
	protected boolean keepTermCodeMap = false;
	
	/** The number of documents in the merged structures. */
	protected int numberOfDocuments;
	
	/** The number of pointers in the merged structures. */
	protected long numberOfPointers;
	
	/** The number of terms in the collection. */
	protected int numberOfTerms;

	
	protected CompressionConfiguration compressionDirectConfig;
	protected CompressionConfiguration compressionInvertedConfig;
	
	protected boolean MetaReverse = Boolean.parseBoolean(ApplicationSetup.getProperty("merger.meta.reverse", "true"));
	
	/** source index 1 */	
	protected IndexOnDisk srcIndex1; 
	/** source index 2 */
	protected IndexOnDisk srcIndex2; 
	/** destination index */
	protected IndexOnDisk destIndex;

	/** class to use to write direct file */	
	protected Class<? extends DirectInvertedOutputStream> directFileOutputStreamClass = DirectInvertedOutputStream.class;
	protected Class<? extends DirectInvertedOutputStream> fieldDirectFileOutputStreamClass = FieldDirectInvertedOutputStream.class;
	
	protected final int fieldCount;
	
	protected String basicInvertedIndexPostingIteratorClass = BasicIterablePosting.class.getName();
	protected String fieldInvertedIndexPostingIteratorClass = FieldIterablePosting.class.getName();
	protected String basicDirectIndexPostingIteratorClass = BasicIterablePosting.class.getName();
	protected String fieldDirectIndexPostingIteratorClass = FieldIterablePosting.class.getName();
	/**
	 * constructor
	 * @param _srcIndex1
	 * @param _srcIndex2
	 * @param _destIndex
	 */
	public StructureMerger(IndexOnDisk _srcIndex1, IndexOnDisk _srcIndex2, IndexOnDisk _destIndex)
	{
		this.srcIndex1 = _srcIndex1;
		this.srcIndex2 = _srcIndex2;
		this.destIndex = _destIndex;
		numberOfDocuments = 0;
		numberOfPointers = 0;
		numberOfTerms = 0;
		
		final int srcFieldCount1 = srcIndex1.getIntIndexProperty("index.inverted.fields.count", 0);
		final int srcFieldCount2 = srcIndex2.getIntIndexProperty("index.inverted.fields.count", 0);
		if (srcFieldCount1 != srcFieldCount2)
		{
			throw new Error("FieldCounts in source indices must match");
		}
		String[] fieldNames = ArrayUtils.parseCommaDelimitedString(srcIndex1.getIndexProperty("index.inverted.fields.names", ""));
		assert srcFieldCount1 == fieldNames.length;
		
		fieldCount = srcFieldCount1;
		compressionDirectConfig = CompressionFactory.getCompressionConfiguration("direct", fieldNames, 0,0);
		compressionInvertedConfig = CompressionFactory.getCompressionConfiguration("inverted", fieldNames, 0,0);
	}
	


	
	/**
	 * Sets the output index. This index should have no documents
	 * @param _outputIndex the index to be merged to
	 */
	public void setOutputIndex(IndexOnDisk _outputIndex) {
		this.destIndex = _outputIndex;
		//invertedFileOutput = _outputName;
	}
	

	/**
	 * Merges the two lexicons into one. After this stage, the offsets in the
	 * lexicon are ot correct. They will be updated only after creating the 
	 * inverted file.
	 */
	@SuppressWarnings("unchecked")
	protected void mergeInvertedFiles() {
		try {
			//getting the number of entries in the first document index, 
			//in order to assign the correct docids to the documents 
			//of the second inverted file.
			
			int numberOfDocs1 = srcIndex1.getCollectionStatistics().getNumberOfDocuments();
			int numberOfDocs2 = srcIndex2.getCollectionStatistics().getNumberOfDocuments();
						
			numberOfDocuments = numberOfDocs1 + numberOfDocs2;
			
			
			final int srcFieldCount1 = srcIndex1.getIntIndexProperty("index.inverted.fields.count", 0);
			final int srcFieldCount2 = srcIndex1.getIntIndexProperty("index.inverted.fields.count", 0);
			if (srcFieldCount1 != srcFieldCount2)
			{
				throw new Error("FieldCounts in source indices must match");
			}
			
			final int fieldCount = srcFieldCount1;
			
			//creating a new map between new and old term codes
			if (keepTermCodeMap)
				termcodeHashmap = new TIntIntHashMap();

			//setting the input streams
			Iterator<Map.Entry<String,LexiconEntry>> lexInStream1 = 
				(Iterator<Map.Entry<String,LexiconEntry>>)srcIndex1.getIndexStructureInputStream("lexicon");
			Iterator<Map.Entry<String,LexiconEntry>> lexInStream2 = 
				(Iterator<Map.Entry<String,LexiconEntry>>)srcIndex2.getIndexStructureInputStream("lexicon");
			
			for(String property : new String[] {"index.inverted.fields.names", "max.term.length", "index.lexicon-keyfactory.class", "index.lexicon-keyfactory.parameter_values",
					"index.lexicon-keyfactory.parameter_types", "index.lexicon-valuefactory.class", "index.lexicon-valuefactory.parameter_values",
					"index.lexicon-valuefactory.parameter_types"} )
			{
				destIndex.setIndexProperty(property, srcIndex1.getIndexProperty(property, null));
			}
			
			FixedSizeWriteableFactory<LexiconEntry> lvf = 
				(FixedSizeWriteableFactory<LexiconEntry>)srcIndex1.getIndexStructure("lexicon-valuefactory");
				
			//setting the output stream
			LexiconOutputStream<String> lexOutStream = 
				new FSOMapFileLexiconOutputStream(destIndex, "lexicon", (Class <FixedSizeWriteableFactory<LexiconEntry>>) lvf.getClass());

			int newCodes = keepTermCodeMap
					 ? (int)srcIndex1.getCollectionStatistics().getNumberOfUniqueTerms()
					 : 0;
			
			PostingIndex<Pointer> inverted1 = (PostingIndex<Pointer>) srcIndex1.getInvertedIndex();
			PostingIndex<Pointer> inverted2 = (PostingIndex<Pointer>) srcIndex2.getInvertedIndex();
			
			AbstractPostingOutputStream invOS = null;
			try{
				invOS = compressionInvertedConfig.getPostingOutputStream(((IndexOnDisk) destIndex).getPath() + ApplicationSetup.FILE_SEPARATOR +  
						((IndexOnDisk) destIndex).getPrefix() + ".inverted"+ compressionInvertedConfig.getStructureFileExtension());
				
			} catch (Exception e) {
				logger.error("Couldn't create specified DirectInvertedOutputStream", e);
				lexOutStream.close();
				return;
			}


			boolean hasMore1 = false;
			boolean hasMore2 = false;
			String term1;
			String term2;
			Map.Entry<String,LexiconEntry> lee1 = null;
			Map.Entry<String,LexiconEntry> lee2 = null;
			hasMore1 = lexInStream1.hasNext();
			if (hasMore1)
				lee1 = lexInStream1.next();
			hasMore2 = lexInStream2.hasNext();
			if (hasMore2)
				lee2 = lexInStream2.next();
			while (hasMore1 && hasMore2) {
		
				term1 = lee1.getKey();
				term2 = lee2.getKey();
				
				int lexicographicalCompare = term1.compareTo(term2);
				if (lexicographicalCompare < 0) {
					//write to inverted file postings for the term that only occurs in 1st index
					BitIndexPointer newPointer = invOS.writePostings(inverted1.getPostings(lee1.getValue()));
					lee1.getValue().setPointer(newPointer);
					numberOfPointers+=newPointer.getNumberOfEntries();
					if (! keepTermCodeMap)
						lee1.getValue().setTermId(newCodes++);
					lexOutStream.writeNextEntry(term1, lee1.getValue());
					hasMore1 = lexInStream1.hasNext();
					if (hasMore1)
						lee1 = lexInStream1.next();
					
					
				} else if (lexicographicalCompare > 0) {
					//write to inverted file postings for the term that only occurs in 2nd index
					//docids are transformed as we go.
					BitIndexPointer newPointer = 
						invOS.writePostings(inverted2.getPostings(lee2.getValue()), -(numberOfDocs1+1));
					lee2.getValue().setPointer(newPointer);
					numberOfPointers+=newPointer.getNumberOfEntries();
					
					int newCode = newCodes++;
					if (keepTermCodeMap)
						termcodeHashmap.put(lee2.getValue().getTermId(), newCode);
					lee2.getValue().setTermId(newCode);
					lexOutStream.writeNextEntry(term2, lee2.getValue());
					hasMore2 = lexInStream2.hasNext();
					if (hasMore2)
						lee2 = lexInStream2.next();
				} else {
					//write to postings for a term that occurs in both indices
					
					//1. postings from the first index are unchanged
					IterablePosting ip1 = inverted1.getPostings(lee1.getValue());
					BitIndexPointer newPointer1 = invOS.writePostings(ip1);
					
					//2. postings from the 2nd index have their docids transformed
					IterablePosting ip2 = inverted2.getPostings(lee2.getValue());
					BitIndexPointer newPointer2 = invOS.writePostings(ip2, ip1.getId() - numberOfDocs1);
					
					numberOfPointers+= newPointer1.getNumberOfEntries() + newPointer2.getNumberOfEntries();
						
					//don't set numberOfEntries, as LexiconEntry.add() will take care of this.
					lee1.getValue().setPointer(newPointer1);
					if (keepTermCodeMap)
						termcodeHashmap.put(lee2.getValue().getTermId(), lee1.getValue().getTermId());
					else
						lee1.getValue().setTermId(newCodes++);
					
					lee1.getValue().add(lee2.getValue());
					lexOutStream.writeNextEntry(term1, lee1.getValue());
					
					hasMore1 = lexInStream1.hasNext();
					if (hasMore1)
						lee1 = lexInStream1.next();
					
					hasMore2 = lexInStream2.hasNext();
					if (hasMore2)
						lee2 = lexInStream2.next();
				}
			}
			
			if (hasMore1) {
				lee2 = null;
				while (hasMore1) {
					//write to inverted file as well.
					BitIndexPointer newPointer = invOS.writePostings(
							inverted1.getPostings(lee1.getValue()));
					lee1.getValue().setPointer(newPointer);
					if (! keepTermCodeMap)
						lee1.getValue().setTermId(newCodes++);
					numberOfPointers+=newPointer.getNumberOfEntries();
					lexOutStream.writeNextEntry(lee1.getKey(), lee1.getValue());
					hasMore1 = lexInStream1.hasNext();
					if (hasMore1)
						lee1 = lexInStream1.next();
				}
			} else if (hasMore2) {
				lee1 = null;
				while (hasMore2) {
					//write to inverted file as well.
					BitIndexPointer newPointer = invOS.writePostings(
							inverted2.getPostings(lee2.getValue()), -(numberOfDocs1+1));
					lee2.getValue().setPointer(newPointer);
					numberOfPointers+=newPointer.getNumberOfEntries();
					int newCode = newCodes++;
					if (keepTermCodeMap)
						termcodeHashmap.put(lee2.getValue().getTermId(), newCode);
					lee2.getValue().setTermId(newCode);
					lexOutStream.writeNextEntry(lee2.getKey(), lee2.getValue());
					hasMore2 = lexInStream2.hasNext();
					if (hasMore2)
						lee2 = lexInStream2.next();
				}		
			}
			IndexUtil.close(lexInStream1);
			IndexUtil.close(lexInStream2);
			

			inverted1.close();
			inverted2.close();
			invOS.close();
			
			destIndex.setIndexProperty("num.Documents", ""+numberOfDocuments);
			destIndex.addIndexStructure(
						"inverted",
						compressionInvertedConfig.getStructureClass().getName(),
						"org.terrier.structures.IndexOnDisk,java.lang.String,org.terrier.structures.DocumentIndex,java.lang.Class", 
						"index,structureName,document,"+ 
						compressionInvertedConfig.getPostingIteratorClass().getName() );
	        destIndex.addIndexStructureInputStream(
	                    "inverted",
	                    compressionInvertedConfig.getStructureInputStreamClass().getName(),
	                    "org.terrier.structures.IndexOnDisk,java.lang.String,java.util.Iterator,java.lang.Class",
	                    "index,structureName,lexicon-entry-inputstream,"+
	                    compressionInvertedConfig.getPostingIteratorClass().getName());
	        destIndex.setIndexProperty("index.inverted.fields.count", ""+fieldCount);
			lexOutStream.close();
			if (fieldCount > 0)
			{
				destIndex.addIndexStructure("lexicon-valuefactory", FieldLexiconEntry.Factory.class.getName(), "java.lang.String", "${index.inverted.fields.count}");
			}
			destIndex.flush();
								
		} catch(IOException ioe) {
			logger.error("IOException while merging lexicons and inverted files.", ioe);
		}
	}


	/**
	 * Merges the two direct files and the corresponding document id files.
	 */
	@SuppressWarnings("unchecked")
	protected void mergeDirectFiles() {
		try {
			final DocumentIndexBuilder docidOutput = new DocumentIndexBuilder(destIndex, "document");
			
			final String[] metaTags = ArrayUtils.parseCommaDelimitedString(srcIndex1.getIndexProperty("index.meta.key-names", "docno"));
			final int[] metaTagLengths = ArrayUtils.parseCommaDelimitedInts(srcIndex1.getIndexProperty("index.meta.value-lengths", "20"));
			final String[] metaReverseTags = MetaReverse
				? ArrayUtils.parseCommaDelimitedString(srcIndex1.getIndexProperty("index.meta.reverse-key-names", ""))
				: new String[0];
			final MetaIndexBuilder metaBuilder = new CompressingMetaIndexBuilder(destIndex, metaTags, metaTagLengths, metaReverseTags);
		
			if (! srcIndex1.getIndexProperty("index.meta.key-names", "docno").equals(srcIndex2.getIndexProperty("index.meta.key-names", "docno")))
			{
				metaBuilder.close();
				throw new Error("Meta fields in source indices must match");
			}
			final BitIndexPointer emptyPointer = new SimpleBitIndexPointer();
			
				
			final int srcFieldCount1 = srcIndex1.getIntIndexProperty("index.direct.fields.count", 0);
			final int srcFieldCount2 = srcIndex1.getIntIndexProperty("index.direct.fields.count", 0);
			if (srcFieldCount1 != srcFieldCount2)
			{
				metaBuilder.close();
				throw new Error("FieldCounts in source indices must match");
			}
			
			final int fieldCount = srcFieldCount1;
			
			
			for(String property : new String[] {"index.direct.fields.names","index.direct.fields.count" } )
			{
				destIndex.setIndexProperty(property, srcIndex1.getIndexProperty(property, null));
			}
			
			AbstractPostingOutputStream dfOutput = null;
			try{
				dfOutput = compressionDirectConfig.getPostingOutputStream(destIndex.getPath() + ApplicationSetup.FILE_SEPARATOR +  
					destIndex.getPrefix() + ".direct" + compressionDirectConfig.getStructureFileExtension());
			} catch (Exception e) {
				metaBuilder.close();
				throw new Error("Couldn't create specified DirectInvertedOutputStream", e);
			}
			
			
			final Iterator<DocumentIndexEntry> docidInput1 = (Iterator<DocumentIndexEntry>)srcIndex1.getIndexStructureInputStream("document");
			final PostingIndexInputStream dfInput1 = (PostingIndexInputStream)srcIndex1.getIndexStructureInputStream("direct");
			final MetaIndex metaInput1 = srcIndex1.getMetaIndex();
			
			int sourceDocid = 0;
			//traversing the direct index, without any change
			while(docidInput1.hasNext())
			{
				BitIndexPointer pointerDF = emptyPointer;
				DocumentIndexEntry die = docidInput1.next();
				if (die.getDocumentLength() > 0)
				{
					pointerDF = dfOutput.writePostings(dfInput1.next());
				}
				die.setBitIndexPointer(pointerDF);
				docidOutput.addEntryToBuffer(die);
				metaBuilder.writeDocumentEntry(metaInput1.getAllItems(sourceDocid));
				sourceDocid++;
			}
			dfInput1.close();
			metaInput1.close();
			IndexUtil.close(docidInput1);
			final Iterator<DocumentIndexEntry> docidInput2 = (Iterator<DocumentIndexEntry>)srcIndex2.getIndexStructureInputStream("document");
			final PostingIndexInputStream dfInput2 = (PostingIndexInputStream)srcIndex2.getIndexStructureInputStream("direct");
			final MetaIndex metaInput2 = srcIndex2.getMetaIndex();
			
			sourceDocid = 0;
			while (docidInput2.hasNext())
			{
				DocumentIndexEntry die = docidInput2.next();
			
				BitIndexPointer pointerDF = emptyPointer;
				if (die.getDocumentLength() > 0)
				{
					final IterablePosting postings = dfInput2.next();
					
					List<Posting> postingList = new ArrayList<Posting>();
					while(postings.next() != IterablePosting.EOL)
					{
						final Posting p = postings.asWritablePosting();
						p.setId(termcodeHashmap.get(postings.getId()));
						postingList.add(p);
					}
					Collections.sort(postingList, new PostingIdComparator());
					pointerDF = dfOutput.writePostings(postingList.iterator());
				}
				die.setBitIndexPointer(pointerDF);
				docidOutput.addEntryToBuffer(die);
				metaBuilder.writeDocumentEntry(metaInput2.getAllItems(sourceDocid));
				sourceDocid++;
			}
			dfInput2.close();
			IndexUtil.close(docidInput2);
			metaInput2.close();
			
			metaBuilder.close();
			dfOutput.close();
			docidOutput.finishedCollections();
			docidOutput.close();

			compressionDirectConfig.writeIndexProperties(destIndex, "document-inputstream");
			
			if (fieldCount > 0)
			{
				destIndex.addIndexStructure("document-factory", FieldDocumentIndexEntry.Factory.class.getName(), "java.lang.String", "${index.direct.fields.count}");
			}
			else
			{
				destIndex.addIndexStructure("document-factory", BasicDocumentIndexEntry.Factory.class.getName(), "", "");
			}
			destIndex.flush();
			
		} catch(IOException ioe) {
			logger.error("IOException while merging df and docid files.", ioe);
		}
	}
	
	protected static Class<?>[] getInterfaces(Object o)
	{
		List<Class<?>> list = new ArrayList<Class<?>>();
		Class<?> c = o.getClass();
		while(! c.equals(Object.class))
		{
			for(Class<?> i : c.getInterfaces())
			{
				list.add(i);
			}
			c = c.getSuperclass();
		}
		return list.toArray(new Class[0]);
	}

	
	/**
	 * Merges the two document index files, and the meta files.
	 */
	@SuppressWarnings("unchecked")
	protected void mergeDocumentIndexFiles() {
		try {
			//the output docid file
			final DocumentIndexBuilder docidOutput = new DocumentIndexBuilder(destIndex, "document");
			final String[] metaTags = ArrayUtils.parseCommaDelimitedString(srcIndex1.getIndexProperty("index.meta.key-names", "docno"));
			final int[] metaTagLengths = ArrayUtils.parseCommaDelimitedInts(srcIndex1.getIndexProperty("index.meta.value-lengths", "20"));
			final String[] metaReverseTags = MetaReverse
				? ArrayUtils.parseCommaDelimitedString(srcIndex1.getIndexProperty("index.meta.reverse-key-names", ""))
				: new String[0];
			final MetaIndexBuilder metaBuilder = new CompressingMetaIndexBuilder(destIndex, metaTags, metaTagLengths, metaReverseTags);
		
			if (! srcIndex1.getIndexProperty("index.meta.key-names", "docno").equals(srcIndex2.getIndexProperty("index.meta.key-names", "docno")))
			{
				metaBuilder.close();
				throw new Error("Meta fields in source indices must match");
			}
			
			//opening the first set of files.
			final Iterator<DocumentIndexEntry> docidInput1 = (Iterator<DocumentIndexEntry>)srcIndex1.getIndexStructureInputStream("document");
			final Iterator<String[]> metaInput1 = (Iterator<String[]>)srcIndex1.getIndexStructureInputStream("meta");
			
			int srcFieldCount1 = srcIndex1.getIntIndexProperty("index.inverted.fields.count", 0);
			int srcFieldCount2 = srcIndex2.getIntIndexProperty("index.inverted.fields.count", 0);
			if (srcFieldCount1 != srcFieldCount2)
			{
				metaBuilder.close();
				throw new Error("FieldCounts in source indices must match");
			}
			if (srcIndex1.getIndexProperty("index.document-factory.class", "").equals("org.terrier.structures.SimpleDocumentIndexEntry$Factory")
				|| srcIndex1.getIndexProperty("index.document-factory.class", "").equals("org.terrier.structures.BasicDocumentIndexEntry$Factory"))
			{
				//for some reason, the source document index has not fields. so we shouldn't assume that fields are being used.
				srcFieldCount1 = 0;
			}
			final int fieldCount = srcFieldCount1;
			
			//traversing the first set of files, without any change
			while(docidInput1.hasNext())
			{
				metaInput1.hasNext();
				DocumentIndexEntry die = docidInput1.next();
				DocumentIndexEntry dieNew = (fieldCount > 0) ? die : new SimpleDocumentIndexEntry(die);
				docidOutput.addEntryToBuffer(dieNew);
				metaBuilder.writeDocumentEntry(metaInput1.next());
			}
			
			final Iterator<DocumentIndexEntry> docidInput2 = (Iterator<DocumentIndexEntry>)srcIndex2.getIndexStructureInputStream("document");
			final Iterator<String[]> metaInput2 = (Iterator<String[]>)srcIndex2.getIndexStructureInputStream("meta");
			//traversing the 2nd set of files, without any change
			while(docidInput2.hasNext())
			{
				metaInput2.hasNext();
				DocumentIndexEntry die = docidInput2.next();
				DocumentIndexEntry dieNew = (fieldCount > 0) ? die : new SimpleDocumentIndexEntry(die);
				docidOutput.addEntryToBuffer(dieNew);
				metaBuilder.writeDocumentEntry(metaInput2.next());
			}
			
			docidOutput.finishedCollections();
			docidOutput.close();
			metaBuilder.close();
			IndexUtil.close(docidInput1);
			IndexUtil.close(docidInput2);
			IndexUtil.close(metaInput1);
			IndexUtil.close(metaInput2);
			//destIndex.setIndexProperty("index.inverted.fields.count", ""+ fieldCount);
			if (fieldCount > 0)
			{
				destIndex.addIndexStructure("document-factory", FieldDocumentIndexEntry.Factory.class.getName(), "java.lang.String", "${index.inverted.fields.count}");
			}
			else
			{
				destIndex.addIndexStructure("document-factory", SimpleDocumentIndexEntry.Factory.class.getName(), "", "");
			}
			destIndex.flush();
			
		} catch(IOException ioe) {
			logger.error("IOException while merging docid files.", ioe);
		}
	}

	

	/** 
	 * creates the final term code to offset file, and the lexicon hash if enabled.
	 */
	protected void createLexidFile() {
		LexiconBuilder.optimise(destIndex, "lexicon");
	}
	
	/**
	 * Merges the structures created by terrier.
	 */
	public void mergeStructures() {
		final boolean bothInverted = srcIndex1.hasIndexStructure("inverted") && srcIndex2.hasIndexStructure("inverted");
		final boolean bothDirect = srcIndex1.hasIndexStructure("direct") && srcIndex2.hasIndexStructure("direct");
		final boolean bothLexicon = srcIndex1.hasIndexStructure("lexicon") && srcIndex2.hasIndexStructure("lexicon");
		final long t1 = System.currentTimeMillis();
		keepTermCodeMap = bothDirect;
		long t2 = 0;
		long t3 = 0;
		long t4 = 0;
		if (bothInverted)
		{
			mergeInvertedFiles();
			t2 = System.currentTimeMillis();
	        logger.info("merged inverted files in " + ((t2-t1)/1000.0d));
		}
		else if (bothLexicon)
		{
			new LexiconMerger(srcIndex1, srcIndex2, destIndex).mergeLexicons();
			t2 = System.currentTimeMillis();
    	    logger.info("merged lexicons in " + ((t2-t1)/1000.0d));
		}
		else
		{
			logger.warn("No inverted or lexicon - no merging of lexicons took place");
			t2 = System.currentTimeMillis();
		}
		
		if (bothInverted || bothLexicon)
		{
			createLexidFile();
			t3 = System.currentTimeMillis();
			logger.debug("created lexid file and lex hash in " + ((t3-t2)/1000.0d));
		}
		t3 = System.currentTimeMillis();

		if (! bothDirect || ApplicationSetup.getProperty("merge.direct","true").equals("false"))
		{	
			mergeDocumentIndexFiles();
			t4 = System.currentTimeMillis();
			logger.info("merged documentindex files in " + ((t4-t3)/1000.0d));
		} 
		else 
		{
			mergeDirectFiles();	
			t4 = System.currentTimeMillis();
			logger.info("merged direct files in " + ((t4-t3)/1000.0d));
		}
	
		if (keepTermCodeMap)
		{
			//save up some memory
			termcodeHashmap.clear();
			termcodeHashmap = null;
		}
	}

	/** Usage: java org.terrier.structures.merging.StructureMerger [binary bits] [inverted file 1] [inverted file 2] [output inverted file] <p>
      * Binary bits concerns the number of fields in use in the index. */
	public static void main(String[] args) throws Exception {
		
		if (args.length != 6)
		{
			logger.error("usage: java org.terrier.structures.merging.StructureMerger srcPath1 srcPrefix1 srcPath2 srcPrefix2 destPath1 destPrefix1 ");
			logger.error("Exiting ...");
			return;
		}
		
		Index.setIndexLoadingProfileAsRetrieval(false);
		IndexOnDisk indexSrc1 = Index.createIndex(args[0], args[1]);
		IndexOnDisk indexSrc2 = Index.createIndex(args[2], args[3]);
		IndexOnDisk indexDest = Index.createNewIndex(args[4], args[5]);
		
		StructureMerger sMerger = new StructureMerger(indexSrc1, indexSrc2, indexDest);
		long start = System.currentTimeMillis();
		logger.info("started at " + (new Date()));
		if (ApplicationSetup.getProperty("merger.onlylexicons","false").equals("true")) {
			System.err.println("Use LexiconMerger");
			return;
		} else if (ApplicationSetup.getProperty("merger.onlydocids","false").equals("true")) {
			sMerger.mergeDocumentIndexFiles();
		} else {
			sMerger.mergeStructures();
		}
		indexSrc1.close();
		indexSrc2.close();
		indexDest.close();
		
		logger.info("finished at " + (new Date()));
		long end = System.currentTimeMillis();
		logger.info("time elapsed: " + ((end-start)*1.0d/1000.0d) + " sec.");
	}




}

