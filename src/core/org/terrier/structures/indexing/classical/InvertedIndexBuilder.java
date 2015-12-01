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
 * The Original Code is InvertedIndexBuilder.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures.indexing.classical;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.AbstractPostingOutputStream;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.FSOMapFileLexicon;
import org.terrier.structures.FSOMapFileLexiconOutputStream;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.LexiconOutputStream;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.SimpleBitIndexPointer;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;
import org.terrier.structures.postings.ArrayOfBasicIterablePosting;
import org.terrier.structures.postings.ArrayOfFieldIterablePosting;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.terrier.utility.Rounding;
/**
 * Builds an inverted index. It optionally saves term-field information as well. 
 * <p><b>Algorithm:</b>
 * <ol>
 * <li>While there are terms left:
 *  <ol>
 *  <li>Read M term ids from lexicon, in lexicographical order</li>
 *  <li>Read the occurrences of these M terms into memory from the direct file</li>
 *  <li>Write the occurrences of these M terms to the inverted file</li>
 *  </ol>
 * <li>Rewrite the lexicon, removing block frequencies, and adding inverted file offsets</li>
 * <li>Write the collection statistics</li>
 * </ol>
 * <p><b>Lexicon term selection:</b>
 * There are two strategies of selecting the number of terms to read from the lexicon. The trade-off here
 * is to read a small enough number of terms into memory such that the occurrences of all those terms from
 * the direct file can fit in memory. On the other hand, the less terms that are read implies more iterations,
 * which is I/O expensive, as the entire direct file has to be read for every iteration.<br>
 * The two strategies are:
 * <ul>
 * <li>Read a fixed number of terms on each iterations - this corresponds to the property
 *  <tt>invertedfile.processterms</tt></li>
 * <li>Read a fixed number of occurrences (pointers) on each iteration. The number of pointers can be determined
 *  using the sum of frequencies of each term from the lexicon. This corresponds to the property
 *  <tt>invertedfile.processpointers</tt>. 
 * </li></ul>
 * By default, the 2nd strategy is chosen, unless the <tt>invertedfile.processpointers</tt> has a zero
 * value specified.<P>
 * Properties:
 * <ul>
 *  <li><tt>invertedfile.processterms</tt>- the number of terms to process in each iteration. Defaults to 75,000</li>
 *  <li><tt>invertedfile.processpointers</tt> - the number of pointers to process in each iteration. Defaults to 20,000,000</li>
 * </ul>
 * @author Craig Macdonald &amp; Vassilis Plachouras
  */
public class InvertedIndexBuilder {

	/** class to be used as a lexiconoutpustream. set by this and child classes */
	protected Class<?> lexiconOutputStream = null;


	/** The logger used */
	protected static final Logger logger = LoggerFactory.getLogger(InvertedIndexBuilder.class);
	
	/**
	 * A tuple containing a integer (termid) and a long pointer
	 * @author Richard McCreadie
	 *
	 */
	protected static class IntLongTuple
	{
		final int Terms;
		final long Pointers;
		IntLongTuple(int a, long b)
		{
			Terms = a;
			Pointers = b;
		}
	}
	
	protected int fieldCount = 0;

	/** Indicates whether field information is used. */
	protected boolean useFieldInformation;
	
	protected IndexOnDisk index = null;
	
	protected String structureName = null;
	
	/** The number of pointers to be processed in an interation. This directly corresponds to the
	  * property <tt>invertedfile.processpointers</tt>. If this property is set and > 0, then each
	  * iteration of the inverted index creation will be done to a set number of pointers, not a set
	  * number of terms, overriding <tt>invertedfile.processterms</tt>. Default is 20000000. */
	protected long numberOfPointersPerIteration = Long.parseLong(
		ApplicationSetup.getProperty("invertedfile.processpointers", "20000000"));
	
	/**
	 * The underlying bit file.
	 */
	//protected BitOut file;
	protected AbstractPostingOutputStream file;
	protected  CompressionConfiguration compressionConfig;

	/**
	 * contructor
	 * @param i
	 * @param _structureName
	 */
	public InvertedIndexBuilder(IndexOnDisk i, String _structureName, CompressionConfiguration compressionConfig)
	{
		this.index = i;
		this.structureName = _structureName;
		this.compressionConfig = compressionConfig;
		
		try{
			file = compressionConfig.getPostingOutputStream(
				index.getPath() + ApplicationSetup.FILE_SEPARATOR + index.getPrefix() + "." + structureName + compressionConfig.getStructureFileExtension());
		} catch (Exception ioe) {
			logger.error("creating PostingOutputStream for writing the inverted file : ", ioe);
		}
		lexiconOutputStream = LexiconOutputStream.class;
	}



	/**
	 * Closes the underlying bit file.
	 */
	public void close() throws IOException {
		if (file != null)
			file.close();
		
		index.close();
	}

	/**
	 * Creates the inverted index using the already created direct index,
	 * document index and lexicon.
	 */
	@SuppressWarnings("unchecked")
	public void createInvertedIndex() {
		try {
			Runtime r = Runtime.getRuntime();
			logger.debug("creating inverted index");
			final String LexiconFilename = index.getPath() + "/" + index.getPrefix() + ".lexicon";
			
			//final int _numberOfDocuments = index.getCollectionStatistics().getNumberOfDocuments();
		
			long assumedNumberOfPointers = Long.parseLong(index.getIndexProperty("num.Pointers", "0"));				
			long _numberOfTokens = 0;
			long _numberOfPointers = 0;
			int _numberOfUniqueTerms = index.getCollectionStatistics().getNumberOfUniqueTerms();
			
			fieldCount = index.getIntIndexProperty("index.direct.fields.count", 0);
			this.useFieldInformation = fieldCount > 0;
			Iterator<Map.Entry<String,LexiconEntry>> lexiconStream = 
				(Iterator<Map.Entry<String,LexiconEntry>>)index.getIndexStructureInputStream("lexicon");
		
			//A temporary file for storing the updated lexicon file, after
			// creating the inverted file
			DataOutputStream dos = new DataOutputStream(Files.writeFileStream(LexiconFilename.concat(".tmp2")));

			//if the set number of terms to process is higher than the
			// available,
			if (processTerms > _numberOfUniqueTerms)
				processTerms = (int) _numberOfUniqueTerms;
			long startProcessingLexicon = 0;
			long startTraversingDirectFile = 0;
			long startWritingInvertedFile = 0;
			long numberOfPointersThisIteration = 0;
			
			int i=0; int iterationCounter = 0;
			// generate a message guessing iteration counts
			String iteration_message_suffix = null;
			if (numberOfPointersPerIteration > 0 || processTerms ==0)
			{
				if (assumedNumberOfPointers > 0)
				{
					iteration_message_suffix = " of "
						+ ((assumedNumberOfPointers % numberOfPointersPerIteration ==0 )
							? (assumedNumberOfPointers/numberOfPointersPerIteration)
							: 1+(assumedNumberOfPointers/numberOfPointersPerIteration))
						+ " iterations";
				}
				else
				{
					iteration_message_suffix = "";
				}
			}
			else
			{
				iteration_message_suffix = " of "
					+ ((_numberOfUniqueTerms % processTerms ==0 ) 
						? (_numberOfUniqueTerms/processTerms)
						: 1+(_numberOfUniqueTerms/processTerms))
					+ " iterations";
			}

			if (numberOfPointersPerIteration == 0)
			{
				logger.warn("Using old-fashioned number of terms strategy. Please consider setting invertedfile.processpointers for forward compatible use");
			}
		
			while(i<_numberOfUniqueTerms)
			{
				iterationCounter++;
				TIntIntHashMap codesHashMap = null;
				TIntArrayList[][] tmpStorage = null;
				IntLongTuple results = null;
				
				logger.info("Iteration "+iterationCounter+iteration_message_suffix);
				
				//traverse the lexicon looking to determine the first N() terms
				//this can be done two ways: for the first X terms
				//OR for the first Y pointers
				//ie either N=X, or N=fn(Y)
				
				startProcessingLexicon = System.currentTimeMillis();
				
				if (numberOfPointersPerIteration > 0)
				{//we've been configured to run with a given number of pointers
					if (logger.isDebugEnabled())
						logger.debug("Scanning lexicon for "+ numberOfPointersPerIteration + " pointers");
				
					/* this is less speed efficient, as we have no way to guess how many
					 * terms it will take to fill the given number of pointers. 
					 * The advantage is that memory consumption is more directly correlated
					 * to number of pointers than number of terms, so when indexing tricky
					 * collections, it is easier to find a number of pointers that can fit
					 * in memory */
					 
					codesHashMap = new TIntIntHashMap();
					ArrayList<TIntArrayList[]> tmpStorageStorage = new ArrayList<TIntArrayList[]>();
					results = scanLexiconForPointers(
						numberOfPointersPerIteration, 
						lexiconStream,
						codesHashMap,
						tmpStorageStorage);
					tmpStorage = (TIntArrayList[][]) tmpStorageStorage.toArray(
						new TIntArrayList[0][0]);
					
				}
				else//we're running with a given number of terms
				{
					if (logger.isDebugEnabled())
						logger.debug("Scanning lexicon for " + processTerms+" terms");
					tmpStorage = new TIntArrayList[processTerms][];
					codesHashMap = new TIntIntHashMap(processTerms);
					results = scanLexiconForTerms(
						processTerms,
						lexiconStream,
						codesHashMap,
						tmpStorage);
				}
				
				processTerms = results.Terms;//no of terms to process on this iteration
				numberOfPointersThisIteration = results.Pointers;
				_numberOfPointers += results.Pointers;//no of pointers to process on this iteration
				logger.debug("Selected " + results.Terms + " terms, " + results.Pointers + " pointers for this iteration");
				
				if (results.Terms == 0)
				{
					logger.warn("No terms found this iteration - presuming end of iteration cycle (perhaps some lexicon terms are empty)");
					break;
				}
				i += processTerms;
				
				if (logger.isDebugEnabled())
					logger.debug("time to process part of lexicon: " + ((System.currentTimeMillis()- startProcessingLexicon) / 1000D));
				
				
				displayMemoryUsage(r);	
				
				//Scan the direct file looking for those terms
				startTraversingDirectFile = System.currentTimeMillis();
				traverseDirectFile(codesHashMap, tmpStorage);
				if (logger.isDebugEnabled())
					logger.debug("time to traverse direct file: " + ((System.currentTimeMillis() - startTraversingDirectFile) / 1000D));
				
				displayMemoryUsage(r);			
	
				//write the inverted file for this part of the lexicon, ie processTerms number of terms
				startWritingInvertedFile = System.currentTimeMillis();
				_numberOfTokens += writeInvertedFilePart(dos, tmpStorage, processTerms);
				if (logger.isDebugEnabled())
					logger.debug("time to write inverted file: "
					 + ((System.currentTimeMillis()- startWritingInvertedFile) / 1000D));
				
							
				displayMemoryUsage(r);
	
				if (logger.isDebugEnabled()) {
					logger.debug(
							"time to perform one iteration: "
								+ ((System.currentTimeMillis() - startProcessingLexicon)
									/ 1000D));
					logger.debug(
						"number of pointers processed: "
							+ numberOfPointersThisIteration);	
				}
				
				
				tmpStorage  = null; 
				codesHashMap.clear(); 
				codesHashMap = null;
			}
			
			
			
			file.close();
			file = null;
			IndexUtil.close(lexiconStream);
			dos.close();
			
//			this.numberOfDocuments = _numberOfDocuments;
//			this.numberOfTokens = _numberOfTokens;
//			this.numberOfUniqueTerms = _numberOfUniqueTerms;
//			this.numberOfPointers = _numberOfPointers;

			//finalising the lexicon file with the updated information
			//on the frequencies and the offsets
			//reading the original lexicon
			lexiconStream = (Iterator<Map.Entry<String,LexiconEntry>>)index.getIndexStructureInputStream("lexicon");
			
			
			//the updated lexicon
			LexiconOutputStream<String> los = getLexOutputStream("tmplexicon");
			
			//the temporary data containing the offsets
			DataInputStream dis = new DataInputStream(Files.openFileStream(LexiconFilename.concat(".tmp2")));
			BitIndexPointer pin = new SimpleBitIndexPointer();
			while(lexiconStream.hasNext())
			{
				Map.Entry<String,LexiconEntry> lee = lexiconStream.next();
				LexiconEntry value = lee.getValue();
				pin.readFields(dis);
				value.setPointer(pin);
				los.writeNextEntry(lee.getKey(), value);
			}
			IndexUtil.close(lexiconStream);
			los.close();
			dis.close();
			Files.delete(LexiconFilename.concat(".tmp2"));
			FSOMapFileLexicon.deleteMapFileLexicon("lexicon", index.getPath(),index.getPrefix());
			FSOMapFileLexicon.renameMapFileLexicon(
					"tmplexicon", index.getPath(), index.getPrefix(), 
					"lexicon", index.getPath(), index.getPrefix());
				
			compressionConfig.writeIndexProperties(index, "lexicon-entry-inputstream");
			//should be already set, but in case their not
			index.setIndexProperty("num.Terms", ""+_numberOfUniqueTerms);
			index.setIndexProperty("num.Tokens", ""+_numberOfTokens);
			index.setIndexProperty("num.Pointers", ""+_numberOfPointers);
			index.flush();
			System.gc();
			
		} catch (IOException ioe) {
			logger.error("IOException occured during creating the inverted file. Stack trace follows.", ioe);
		}
	}
	
	protected TIntArrayList[] createPointerForTerm(LexiconEntry le)
	{
		TIntArrayList[] tmpArray = new TIntArrayList[2 + fieldCount];
		final int tmpNT = le.getDocumentFrequency();
		for(int i = 0; i < fieldCount+2; i++)
			tmpArray[i] = new TIntArrayList(tmpNT);
		return tmpArray;
	}
	
	/** Iterates through the lexicon, until it has reached the given number of pointers
	  * @param PointersToProcess Number of pointers to stop reading the lexicon after
	  * @param lexiconStream the lexicon input stream to read 
	  * @param codesHashMap
	  * @param tmpStorageStorage
	  * @return IntLongTuple number of terms, number of pointers
	  */
	protected IntLongTuple scanLexiconForPointers(
		final long PointersToProcess, 
		final Iterator<Map.Entry<String,LexiconEntry>> lexiconStream, 
		final TIntIntHashMap codesHashMap,
		final ArrayList<TIntArrayList[]> tmpStorageStorage)
		throws IOException
	{
		int _processTerms = 0;	
		long numberOfPointersThisIteration = 0;
		int j=0; //counter of loop iterations
		while(numberOfPointersThisIteration < PointersToProcess) {
		
			if (! lexiconStream.hasNext())
				break;
			
			Map.Entry<String,LexiconEntry> lee = lexiconStream.next();
			LexiconEntry le = lee.getValue();
			
			_processTerms++;			
			numberOfPointersThisIteration += le.getDocumentFrequency();		
			tmpStorageStorage.add(createPointerForTerm(le));
			
			//the class TIntIntHashMap return zero when you look up for a
			//the value of a key that does not exist in the hash map.
			//For this reason, the values that will be inserted in the 
			//hash map are increased by one. 
			codesHashMap.put(le.getTermId(), j + 1);
			
			//increment counter
			j++;
		}
		if (logger.isDebugEnabled())
			logger.debug(
					numberOfPointersThisIteration + " pointers == "+
					_processTerms +" terms");
		return new IntLongTuple(_processTerms, numberOfPointersThisIteration);
	}
	
	
	/** Iterates through the lexicon, until it has reached the given number of terms
	  * @param _processTerms Number of terms to stop reading the lexicon after
	  * @param lexiconStream the lexicon input stream to read 
	  * @param codesHashMap mapping of termids to which offset in the storage array for terms to be processed this iteration
	  * @param tmpStorage place to put postings for this iteration
	  * @return IntLongTuple number of terms, number of pointers
	  */
	protected IntLongTuple scanLexiconForTerms(
		final int _processTerms, 
		final Iterator<Map.Entry<String,LexiconEntry>> lexiconStream, 
		final TIntIntHashMap codesHashMap,
		TIntArrayList[][] tmpStorage)
		throws IOException
	{
		int j = 0; 
		
		long numberOfPointersThisIteration = 0;
		for (; j < _processTerms; j++) {
		
			if (! lexiconStream.hasNext())
				break;
		
			Map.Entry<String,LexiconEntry> lee = lexiconStream.next();
			LexiconEntry le = lee.getValue();
		
			TIntArrayList[] tmpArray = new TIntArrayList[2 + fieldCount];
			final int tmpNT = le.getDocumentFrequency();
			for (int i=0;i<2+fieldCount;i++)
			{
				tmpArray[i] = new TIntArrayList(tmpNT);
			}
			
			numberOfPointersThisIteration += tmpNT;
			
			tmpStorage[j] = tmpArray;
			
			
			//the class TIntIntHashMap return zero when you look up for a
			//the value of a key that does not exist in the hash map.
			//For this reason, the values that will be inserted in the 
			//hash map are increased by one. 
			codesHashMap.put(le.getTermId(), j + 1);
		}
		if (logger.isDebugEnabled())
			logger.debug(
				numberOfPointersThisIteration + " pointers == "+
				j +" terms");
		return new IntLongTuple(j, numberOfPointersThisIteration);
	}

	/**
	 * Traverses the direct index and creates the inverted index entries 
	 * for the terms specified in the codesHashMap and tmpStorage.
	 * @param tmpStorage TIntArrayList[][] an array of the inverted index entries to store
	 * @param codesHashMap a mapping from the term identifiers to the index 
	 *		in the tmpStorage matrix. 
	 * @throws IOException if there is a problem while traversing the direct index.
	 */
	 
	protected void traverseDirectFile(TIntIntHashMap codesHashMap, TIntArrayList[][] tmpStorage) 
		throws IOException 
	{
		//scan the direct file
		PostingIndexInputStream directInputStream = (PostingIndexInputStream)index.getIndexStructureInputStream("direct");
		int docid = 0; //a document counter;
		final boolean _useFieldInformation = this.useFieldInformation;
		
		IterablePosting ip = null;
		while((ip = directInputStream.getNextPostings()) != null)
		{
			docid += directInputStream.getEntriesSkipped();
			
			assert docid < this.index.getCollectionStatistics().getNumberOfDocuments();
			
			int termid;
			FieldPosting fp = _useFieldInformation ? (FieldPosting) ip : null;
			
			//System.err.println("docid="+docid + " " + directInputStream.getCurrentPointer().toString());
			
			while( (termid = ip.next()) != IterablePosting.EOL)
			{
				//System.err.println("termid="+termid);
				assert termid < this.index.getCollectionStatistics().getNumberOfUniqueTerms();
				
				//codePairIndex is where this termid is stored in the tmpStorage array
				//if it is 0, then its not a term we care about on this pass of the direct file
				
				int codePairIndex = codesHashMap.get(termid);
				if (codePairIndex > 0) {
					/* need to decrease codePairIndex because it has been already 
					 * increased while storing in codesHashMap */
					codePairIndex--;
					TIntArrayList[] tmpMatrix = tmpStorage[codePairIndex];
					tmpMatrix[0].add(docid);
					tmpMatrix[1].add(ip.getFrequency());
					if (_useFieldInformation)
					{
						int[] tff = fp.getFieldFrequencies();
						for(int fi = 0; fi < fieldCount; fi++)
							tmpMatrix[2+fi].add(tff[fi]);
					}
				}
			}
			docid++;
		}
		directInputStream.close();
	}
	
	/** Writes the section of the inverted file 
	 * @param dos a temporary data structure that contains the offsets in the inverted
	 *  index for each term.
	 * @param tmpStorage Occurrences information, as described in traverseDirectFile().
	 *  This data is consumed by this method - once this method has been called, all
	 *  the data in tmpStorage will be destroyed.
	 * @param _processTerms The number of terms being processed in this iteration.
	 * @return the number of tokens processed in this iteration */
	protected long writeInvertedFilePart(
			final DataOutputStream dos, 
			TIntArrayList[][] tmpStorage, 
			final int _processTerms)
			throws IOException
		{
			BitIndexPointer p = new SimpleBitIndexPointer();
			//write to the inverted file. We should note that the lexicon 
			//should be updated with the start bit and byte offset for this
			//set of postings.
			int frequency; long numTokens = 0;
			
			//InMemoryIterablePosting mip = new InMemoryIterablePosting();
			
			for (int j = 0; j < _processTerms; j++) {
				
				frequency = 0; //the term frequency
				
				final int[] ids = tmpStorage[j][0].toNativeArray();
				final int[] tf = tmpStorage[j][1].toNativeArray();
				
				final int[][] tmpFields = new int[fieldCount][]; 
				for(int k=0;k<fieldCount;k++)
				{
					tmpFields[k] = tmpStorage[j][k+2].toNativeArray();
				}
				tmpStorage[j] = null;
								
				p.setOffset(file.getOffset());
				p.setNumberOfEntries(ids.length);
				p.write(dos);
				
				IterablePosting ip = null;
				if (fieldCount > 0)
				{
					ip = new ArrayOfFieldIterablePosting(ids, tf, null, tmpFields, null, true);					
				}
				else
				{
					ip = new ArrayOfBasicIterablePosting(ids, tf, null);
				}
				file.writePostings(ip);

				numTokens += frequency;				
			}
			return numTokens;
		}
	
	
//	protected long writeInvertedFilePart(
//		final DataOutputStream dos, 
//		TIntArrayList[][] tmpStorage, 
//		final int _processTerms)
//		throws IOException
//	{
//		BitIndexPointer p = new SimpleBitIndexPointer();
//		//write to the inverted file. We should note that the lexicon 
//		//should be updated with the start bit and byte offset for this
//		//set of postings.
//		int frequency; long numTokens = 0;
//		for (int j = 0; j < _processTerms; j++) {
//
//			
//			frequency = 0; //the term frequency
//			
//			final int[][] tmpMatrix = new int[2+fieldCount][]; 
//			for(int k=0;k<2+fieldCount;k++)
//			{
//				tmpMatrix[k] = tmpStorage[j][k].toNativeArray();
//			}
//			tmpStorage[j] = null;
//			
//			final int[] tmpMatrix0 = tmpMatrix[0];
//			final int[] tmpMatrix1 = tmpMatrix[1];
//			
//			p.setOffset(file.getByteOffset(), file.getBitOffset());
//			p.setNumberOfEntries(tmpMatrix0.length);
//			p.write(dos);
//
//			//THIS IS ALWAYS AN ERROR
//			/*
//			if (tmpMatrix[0].length == 0)
//			{
//				logger.error("Term had no postings - is this right?");
//				//This term has no postings
//				continue;
//			}*/
//			
//			//write the first entry
//			int docid;
//			file.writeGamma((docid = tmpMatrix0[0]) + 1);
//			int termfreq = tmpMatrix1[0];
//			frequency += termfreq;
//			file.writeUnary(termfreq);
//			
//			if (useFieldInformation)
//			{
//				for(int fi = 0; fi < fieldCount;fi++)
//				{
//					file.writeUnary(tmpMatrix[2+fi][0]+1);
//				}
//				for (int k = 1; k < tmpMatrix0.length; k++) {
//					file.writeGamma(tmpMatrix0[k] - docid);
//					docid = tmpMatrix0[k];
//					termfreq	 = tmpMatrix1[k];
//					frequency += termfreq;
//					file.writeUnary(termfreq);
//					for(int fi = 0; fi < fieldCount;fi++)
//					{
//						file.writeUnary(tmpMatrix[2+fi][k]+1);
//					}
//				}
//			}
//			else
//			{
//				for (int k = 1; k < tmpMatrix0.length; k++) {
//					file.writeGamma(tmpMatrix0[k] - docid);
//					docid = tmpMatrix0[k];
//					termfreq = tmpMatrix1[k];
//					frequency += termfreq;
//					file.writeUnary(termfreq);
//				}
//			}
//			
//			//long endOffset = file.getByteOffset();
//			//byte endBitOffset = file.getBitOffset();
//			//endBitOffset--;
//			//if (endBitOffset < 0 && endOffset > 0) {
//			//	endBitOffset = 7;
//			//	endOffset--;
//			//}
//			numTokens += frequency;
//			//dos.writeInt(frequency);
//			
//		}
//		return numTokens;
//	}
	
	/**
	 * The number of terms for which the inverted file 
	 * is built each time. The corresponding property
	 * is <tt>invertedfile.processterms</tt> and the 
	 * default value is <tt>75000</tt>. The higher the
	 * value, the greater the requirements for memory are, 
	 * but the less time it takes to invert the direct 
	 * file. 
	 */
	protected int processTerms = Integer.parseInt(ApplicationSetup.getProperty("invertedfile.processterms", "75000"));
	/**
	 * display memory usage
	 * @param r
	 */
	public static void displayMemoryUsage(Runtime r)
	{
		if (logger.isDebugEnabled())
			logger.debug("free: "+ (r.freeMemory() /1024) + "kb; total: "+(r.totalMemory()/1024)
					+"kb; max: "+(r.maxMemory()/1024)+"kb; "+
					Rounding.toString((100.0d*r.freeMemory() / r.totalMemory()),1)+"% free; "+
					Rounding.toString((100.0d*r.totalMemory() / r.maxMemory()),1)+"% allocated; "
		);
	}


	/** 
	 * get LexiconOutputStream
	 * @param _structureName
	 * @return LexiconOutputStream<String>
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	protected LexiconOutputStream<String> getLexOutputStream(String _structureName) throws IOException
	{
		return new FSOMapFileLexiconOutputStream(
				index.getPath(), index.getPrefix(), 
				_structureName, 
				(FixedSizeWriteableFactory<Text>)index.getIndexStructure("lexicon-keyfactory"));
	}

}


