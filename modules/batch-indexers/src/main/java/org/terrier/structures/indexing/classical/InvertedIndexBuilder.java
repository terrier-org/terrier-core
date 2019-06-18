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
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
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
import org.terrier.utility.TerrierTimer;
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
 * The three strategies are:
 * <ul>
 * <li>Read until an assumed amount of memory is consumed. The amount to consume is upto 80% of the free memory at startup.</li>
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
	
	protected int fieldCount = 0;

	/** Indicates whether field information is used. */
	protected boolean useFieldInformation;
	
	protected IndexOnDisk index = null;
	
	protected String structureName = null;
	
	/** The number of pointers to be processed in an interation. This directly corresponds to the
	  * property <tt>invertedfile.processpointers</tt>. If this property is set and &gt; 0, then each
	  * iteration of the inverted index creation will be done to a set number of pointers, not a set
	  * number of terms, overriding <tt>invertedfile.processterms</tt>. Default is 20000000. */
	protected long numberOfPointersPerIteration = Long.parseLong(
		ApplicationSetup.getProperty("invertedfile.processpointers", "20000000"));
	
	
	/**
	 * The underlying bit file.
	 */
	protected AbstractPostingOutputStream file;
	protected CompressionConfiguration compressionConfig;
	
	protected String lexScanClassName;

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

	protected LexiconScanner getLexScanner(Iterator<Map.Entry<String,LexiconEntry>> lexStream) throws Exception
	{
		lexScanClassName = ApplicationSetup.getProperty("invertedfile.lexiconscanner", "pointers");
		switch(lexScanClassName) {
		case "pointers": return new PointerThresholdLexiconScanner(lexStream); 
		case "terms": return new TermCountLexiconScanner(lexStream);
		case "mem" : return new BasicMemSizeLexiconScanner(lexStream);
		default: 
			if (! lexScanClassName.contains(".")) lexScanClassName = "org.terrier.structures.indexing.classical."+lexScanClassName;
			return ApplicationSetup.getClass(lexScanClassName).asSubclass(LexiconScanner.class).getConstructor(Iterator.class).newInstance(lexStream);
		}
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
		
		long assumedNumberOfPointers = Long.parseLong(index.getIndexProperty("num.Pointers", "0"));
		TerrierTimer tt = new TerrierTimer("Inverting the direct index", assumedNumberOfPointers);
		Runtime r = Runtime.getRuntime();
		logger.debug("creating inverted index");
		final String LexiconFilename = index.getPath() + "/" + index.getPrefix() + ".lexicon";
		
		
		long _numberOfTokens = 0;
		long _numberOfPointers = 0;
		int _numberOfUniqueTerms = index.getCollectionStatistics().getNumberOfUniqueTerms();
		
		fieldCount = index.getIntIndexProperty("index.direct.fields.count", 0);
		this.useFieldInformation = fieldCount > 0;
		try {
			Iterator<Map.Entry<String,LexiconEntry>> lexiconStream = 
				(Iterator<Map.Entry<String,LexiconEntry>>)index.getIndexStructureInputStream("lexicon");
		
			LexiconScanner lexScanner = getLexScanner(lexiconStream);
			
			//A temporary file for storing the updated lexicon file, after
			// creating the inverted file
			DataOutputStream dos = new DataOutputStream(Files.writeFileStream(LexiconFilename.concat(".tmp2")));

			//if the set number of terms to process is higher than the
			// available,

			long startProcessingLexicon = 0;
			long startTraversingDirectFile = 0;
			long startWritingInvertedFile = 0;
			long numberOfPointersThisIteration = 0;
			
			int i=0; int iterationCounter = 0;
			// generate a message guessing iteration counts


			if (numberOfPointersPerIteration == 0)
			{
				logger.warn("Using old-fashioned number of terms strategy. Please consider setting invertedfile.processpointers for forward compatible use");
			}
		
			while(i<_numberOfUniqueTerms)
			{
				iterationCounter++;
				
				logger.info("Iteration "+iterationCounter);
				
				//traverse the lexicon looking to determine terms to process in this iteration				
				startProcessingLexicon = System.currentTimeMillis();
				
				final LexiconScanResult scanResult = lexScanner.scanLexicon();
				numberOfPointersThisIteration = scanResult.pointers;
				
				_numberOfPointers += scanResult.pointers;//no of pointers to process on this iteration
				logger.debug("Selected " + scanResult.terms + " terms, " + scanResult.pointers + " pointers for this iteration");
				
				if (scanResult.terms == 0)
				{
					logger.warn("No terms found this iteration - presuming end of iteration cycle (perhaps some lexicon terms are empty)");
					break;
				}
				i += scanResult.terms;
				
				if (logger.isDebugEnabled())
					logger.debug("time to process part of lexicon: " + ((System.currentTimeMillis()- startProcessingLexicon) / 1000D));
				
				
				displayMemoryUsage(r);	
				
				//Scan the direct file looking for those terms
				startTraversingDirectFile = System.currentTimeMillis();
				traverseDirectFile(scanResult.codesHashMap, scanResult.tmpStorage);
				if (logger.isDebugEnabled())
					logger.debug("time to traverse direct file: " + ((System.currentTimeMillis() - startTraversingDirectFile) / 1000D));
				
				displayMemoryUsage(r);			
	
				//write the inverted file for this part of the lexicon, ie processTerms number of terms
				startWritingInvertedFile = System.currentTimeMillis();
				long[] rtr = writeInvertedFilePart(dos, scanResult.tmpStorage, scanResult.terms);
				_numberOfTokens += rtr[0];
				if (logger.isDebugEnabled())
				{
					logger.debug("time to write inverted file: "
					 + ((System.currentTimeMillis()- startWritingInvertedFile) / 1000D));
					logger.debug("temporary memory used: "
							 + FileUtils.byteCountToDisplaySize(rtr[1]));
				}
				
							
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
				
				scanResult.codesHashMap.clear();
				scanResult.tmpStorage = null;
				tt.setDone(_numberOfPointers);
			}
			
			
			
			file.close();
			file = null;
			IndexUtil.close(lexiconStream);
			dos.close();
			
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
			tt.finished();
			System.gc();
			
		} catch (Exception ioe) {
			logger.error("Exception occured during creating the inverted file. Stack trace follows.", ioe);
		} finally {
			tt.finished();
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
	
	
	class LexiconScanResult 
	{
		int terms;
		long pointers;
		TIntIntHashMap codesHashMap;
		TIntArrayList[][] tmpStorage;
		
		LexiconScanResult(int terms, long pointers,
				TIntIntHashMap codesHashMap, TIntArrayList[][] tmpStorage) {
			super();
			this.terms = terms;
			this.pointers = pointers;
			this.codesHashMap = codesHashMap;
			this.tmpStorage = tmpStorage;
		}
		
		LexiconScanResult(int terms, long pointers,
				TIntIntHashMap codesHashMap, List<TIntArrayList[]> tmpStorageStorage) {
			this(terms, pointers, codesHashMap, (TIntArrayList[][]) tmpStorageStorage.toArray(
					new TIntArrayList[0][0]));
		}
		
		@Override
		public String toString()
		{
			return terms + " terms" + " " + pointers;
		}

	}
	
	abstract class LexiconScanner
	{
		Iterator<Map.Entry<String,LexiconEntry>> lexiconStream;
		LexiconScanner(Iterator<Map.Entry<String,LexiconEntry>> lexIn){
			this.lexiconStream = lexIn;
		}
		
		abstract LexiconScanResult scanLexicon();
	}
	
	class BasicMemSizeLexiconScanner extends LexiconScanner
	{
		final Runtime runtime = Runtime.getRuntime();
		long memThreshold;
		
		public BasicMemSizeLexiconScanner(Iterator<Map.Entry<String,LexiconEntry>> lexiconStream)
		{
			super(lexiconStream);
			
			long free;
			if (runtime.maxMemory() == Long.MAX_VALUE)
			{
				free = runtime.freeMemory() - ApplicationSetup.MEMORY_THRESHOLD_SINGLEPASS;
			}
			else
			{
				long localAllocated =  runtime.totalMemory()-runtime.freeMemory();
				logger.debug("Memory: already allocated in use is " + FileUtils.byteCountToDisplaySize(localAllocated));
				free = runtime.maxMemory() - localAllocated - ApplicationSetup.MEMORY_THRESHOLD_SINGLEPASS;
			}
			//we need at least 5MB free
			assert free > 5 * 1024*1024;
			memThreshold = (long) (0.8f * free);
			logger.info("Memory threshold is " + FileUtils.byteCountToDisplaySize(memThreshold));
		}
		
		
		
		@Override
		LexiconScanResult scanLexicon() {
			
			long projectedPointerCount = (int) (memThreshold / ((long) (2l + fieldCount) * Integer.BYTES));
			logger.debug("Scanning lexicon for "+ memThreshold + " memory -- upto " + projectedPointerCount + " pointers");
			long numberOfPointersThisIteration = 0;
			TIntIntHashMap codesHashMap = new TIntIntHashMap();
			List<TIntArrayList[]> tmpStorageStorage = new ArrayList<TIntArrayList[]>();
			long cumulativeSize = 0;
			int j=0;
			while (lexiconStream.hasNext())
			{
				Map.Entry<String,LexiconEntry> lee = lexiconStream.next();
				LexiconEntry le = lee.getValue();
				//the class TIntIntHashMap return zero when you look up for a
				//the value of a key that does not exist in the hash map.
				//For this reason, the values that will be inserted in the 
				//hash map are increased by one. 
				codesHashMap.put(le.getTermId(), j + 1);
				tmpStorageStorage.add(createPointerForTerm(le));
				numberOfPointersThisIteration += le.getDocumentFrequency();				
				j++;
				cumulativeSize += 
						le.getDocumentFrequency() * (2l + fieldCount) * Integer.BYTES;  //pointer storage
				
				if (numberOfPointersThisIteration > projectedPointerCount)
					break;
			}
			LexiconScanResult rtr = new LexiconScanResult(j, numberOfPointersThisIteration, codesHashMap, tmpStorageStorage);
			if (logger.isDebugEnabled())
				logger.debug(memThreshold + " " + FileUtils.byteCountToDisplaySize(memThreshold) 
						+ " reached with " + rtr + ", actually required " 
						+ FileUtils.byteCountToDisplaySize(cumulativeSize));
			return rtr;
		}
	}
	
	class PointerThresholdLexiconScanner extends LexiconScanner
	{

		public PointerThresholdLexiconScanner(
				Iterator<Entry<String, LexiconEntry>> lexIn) {
			super(lexIn);
		}
		
		long PointersToProcess = numberOfPointersPerIteration;

		@Override
		LexiconScanResult scanLexicon() {
			
			logger.debug("Scanning lexicon for "+ PointersToProcess + " pointers");
			TIntIntHashMap codesHashMap = new TIntIntHashMap();
			ArrayList<TIntArrayList[]> tmpStorageStorage = new ArrayList<TIntArrayList[]>();
			
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
			LexiconScanResult rtr = new LexiconScanResult(_processTerms, numberOfPointersThisIteration, codesHashMap, tmpStorageStorage);
			logger.debug(rtr.toString());					
			return rtr;
		}
		
	}
	
	class TermCountLexiconScanner extends LexiconScanner
	{
		public TermCountLexiconScanner(Iterator<Map.Entry<String,LexiconEntry>> lexiconStream)
		{
			super(lexiconStream);
		}
		
		int _processTerms = processTerms;
		
		@Override
		LexiconScanResult scanLexicon() {
			
			TIntIntHashMap codesHashMap = new TIntIntHashMap();
			TIntArrayList[][] tmpStorage = new TIntArrayList[_processTerms][];
			
			logger.debug("Scanning lexicon for "+ _processTerms + " terms");

			long numberOfPointersThisIteration = 0;
			int j=0;
			for (; j < _processTerms; j++) {
				
				if (! lexiconStream.hasNext())
					break;
			
				Map.Entry<String,LexiconEntry> lee = lexiconStream.next();
				LexiconEntry le = lee.getValue();				
				numberOfPointersThisIteration += le.getDocumentFrequency();
				
				tmpStorage[j] = createPointerForTerm(le);
				
				
				//the class TIntIntHashMap return zero when you look up for a
				//the value of a key that does not exist in the hash map.
				//For this reason, the values that will be inserted in the 
				//hash map are increased by one. 
				codesHashMap.put(le.getTermId(), j + 1);
			}
			LexiconScanResult rtr = new LexiconScanResult(j, numberOfPointersThisIteration, codesHashMap, tmpStorage);
			logger.debug(rtr.toString());
			return rtr;
			
		}
		
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
		
		assert codesHashMap.size() > 0;
		//int minTermId = StaTools.min(codesHashMap.keys()) +1;
		
		IterablePosting ip = null;
		
		while((ip = directInputStream.getNextPostings()) != null)
		{
			docid += directInputStream.getEntriesSkipped();
			
			assert docid < this.index.getCollectionStatistics().getNumberOfDocuments();
			
			int termid;
			FieldPosting fp = _useFieldInformation ? (FieldPosting) ip : null;
			
			//use skipping to possibly avoid DF decompression
//			termid = ip.next(minTermId-1);
//			if (termid == IterablePosting.EOL)
//				continue;
			
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
	 * @return the number of tokens processed in this iteration and the number of temporary bytes of RAM required */
	protected long[] writeInvertedFilePart(
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
			long size = 0;
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
				size += ids.length * (2 + fieldCount) * 4;
				
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
			return new long[]{numTokens, size};
		}
	
	
	
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


