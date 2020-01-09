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
 * The Original Code is BlockInvertedIndexBuilder.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Douglas Johnson <johnsoda{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> 
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures.indexing.classical;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;

import objectexplorer.ObjectGraphMeasurer;
import objectexplorer.ObjectGraphMeasurer.Footprint;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.FSOMapFileLexicon;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.LexiconOutputStream;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.SimpleBitIndexPointer;
import org.terrier.structures.indexing.CompressionFactory;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;
import org.terrier.structures.postings.ArrayOfBlockFieldIterablePosting;
import org.terrier.structures.postings.ArrayOfBlockIterablePosting;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.FieldScore;
import org.terrier.utility.Files;
import org.terrier.utility.TerrierTimer;

import com.jakewharton.byteunits.BinaryByteUnit;

/**
 * Builds an inverted index saving term-block information. It optionally saves
 * term-field information as well.
 * <p>
 * <b>Algorithm:</b>
 * <ol>
 * <li>While there are terms left:
 * <ol>
 * <li>Read M term ids from lexicon, in lexicographical order</li>
 * <li>Read the occurrences of these M terms into memory from the direct file</li>
 * <li>Write the occurrences of these M terms to the inverted file</li>
 * </ol>
 * <li>Rewrite the lexicon, removing block frequencies, and adding inverted
 * file offsets</li>
 * <li>Write the collection statistics</li>
 * </ol>
 * <p>
 * <b>Lexicon term selection:</b> There are two strategies of selecting the
 * number of terms to read from the lexicon. The trade-off here is to read a
 * small enough number of terms into memory such that the occurrences of all
 * those terms from the direct file can fit in memory. On the other hand, the
 * less terms that are read implies more iterations, which is I/O expensive, as
 * the entire direct file has to be read for every iteration.<br>
 * The two strategies are:
 * <ul>
 * <li>Read a fixed number of terms on each iterations - this corresponds to
 * the property <tt>invertedfile.processterms</tt></li>
 * <li>Read a fixed number of occurrences (pointers) on each iteration. The
 * number of pointers can be determined using the sum of frequencies of each
 * term from the lexicon. This corresponds to the property
 * <tt>invertedfile.processpointers</tt>. </li></ul>
 *  By default, the 2nd 
 * strategy is chosen, unless the <tt>invertedfile.processpointers</tt> has a
 * zero value specified.
 * <p>
 * <b>Properties:</b>
 * <ul>
 * <li><tt>invertedfile.processterms</tt> - the number of terms to process in
 * each iteration. Defaults to 25,000</li>
 * <li><tt>invertedfile.processpointers</tt> - the number of pointers to
 * process in each iteration. Defaults to 2,000,000, which specifies that
 * invertedfile.processterms should be read from the lexicon, regardless of the
 * number of pointers.</li>
 * </ul>
 * 
 * @author Douglas Johnson &amp; Vassilis Plachouras &amp; Craig Macdonald
  */
public class BlockInvertedIndexBuilder extends InvertedIndexBuilder {
	
	/**
	 * constructor
	 * @param index
	 * @param structureName
	 */
	public BlockInvertedIndexBuilder(IndexOnDisk index, String structureName, CompressionConfiguration compressionConfig) {
		super(index, structureName, compressionConfig);
		lexiconOutputStream = LexiconOutputStream.class;
	}
	
	@Override
	protected LexiconScanner getLexScanner(Iterator<Map.Entry<String,LexiconEntry>> lexStream, CollectionStatistics stats) throws Exception
	{
		lexScanClassName = ApplicationSetup.getProperty("invertedfile.lexiconscanner", DEFAULT_LEX_SCANNER_PROP_VALUE);
		switch(lexScanClassName) {
		case "pointers": return new PointerThresholdLexiconScanner(lexStream, stats); 
		case "terms": return new TermCountLexiconScanner(lexStream, stats);
		case "mem" : return new BlockMemSizeLexiconScanner(lexStream, stats);
		default: 
			if (! lexScanClassName.contains(".")) lexScanClassName = "org.terrier.structures.indexing.classical."+lexScanClassName;
			return ApplicationSetup.getClass(lexScanClassName).asSubclass(LexiconScanner.class).getConstructor(Iterator.class, CollectionStatistics.class).newInstance(lexStream, stats);
		}
	}

	/**
	 * This method creates the block inverted index. The approach used is
	 * described briefly: for a group of M terms from the lexicon we build the
	 * inverted file and save it on disk. In this way, the number of times we
	 * need to read the direct file is related to the parameter M, and
	 * consequently to the size of the available memory.
	 */
	@SuppressWarnings("unchecked")
	public void createInvertedIndex() {
		
		//these defaults are lower for the block indexer
		numberOfPointersPerIteration = Integer.parseInt(ApplicationSetup.getProperty("invertedfile.processpointers", "2000000")); 
		processTerms = Integer.parseInt(ApplicationSetup.getProperty("invertedfile.processterms", "25000"));
		
		long assumedNumberOfPointers = Long.parseLong(index.getIndexProperty("num.Pointers", "0"));
		TerrierTimer tt = new TerrierTimer("Inverting the direct index", assumedNumberOfPointers);
		tt.start();
		try {
			Runtime r = Runtime.getRuntime();
			logger.info("creating block inverted index");
			final String LexiconFilename = index.getPath() + "/" + index.getPrefix() + ".lexicon";
			
			fieldCount = index.getIntIndexProperty("index.direct.fields.count", 0);
			this.useFieldInformation = fieldCount > 0;
			
			long numberOfTokens = 0;
			long numberOfPointers = 0;

			int numberOfUniqueTerms = index.getCollectionStatistics().getNumberOfUniqueTerms();
			Iterator<Map.Entry<String, LexiconEntry>> lexiconStream = (Iterator<Map.Entry<String, LexiconEntry>>)this.index.getIndexStructureInputStream("lexicon");

			LexiconScanner lexScanner = getLexScanner(lexiconStream, index.getCollectionStatistics());
			logger.info(lexScanner.toString());
			String iterationcount = "of " + lexScanner.estimatedIterations();

			
			// A temporary file for storing the updated
			// lexicon file, after creating the inverted file
			DataOutputStream dos = new DataOutputStream(Files.writeFileStream(LexiconFilename.concat(".tmp2")));

			long startProcessingLexicon = 0;
			long startTraversingDirectFile = 0;
			long startWritingInvertedFile = 0;
			long numberOfPointersThisIteration = 0;

			int i = 0;
			int iterationCounter = 0;

			while (i < numberOfUniqueTerms) {
				iterationCounter++;

				logger.info("Iteration " + iterationCounter + " " +iterationcount); //+ iteration_message_suffix);

				// traverse the lexicon looking to determine the number of terms to process
				startProcessingLexicon = System.currentTimeMillis();
				final LexiconScanResult scanResult = lexScanner.scanLexicon();
				
				
				numberOfPointersThisIteration = scanResult.pointers;
				numberOfPointers += scanResult.pointers;// no of pointers to
														// process on this
														// iteration
				i += scanResult.terms;

				if (scanResult.terms == 0)
					break;
				logger.info("time to process part of lexicon: "	+ ((System.currentTimeMillis() - startProcessingLexicon) / 1000D));

				InvertedIndexBuilder.displayMemoryUsage(r);

				// Scan the direct file looking for those terms
				startTraversingDirectFile = System.currentTimeMillis();
				traverseDirectFile(scanResult.codesHashMap, scanResult.tmpStorage);
				logger.info("time to traverse direct file: "+ ((System.currentTimeMillis() - startTraversingDirectFile) / 1000D));

				InvertedIndexBuilder.displayMemoryUsage(r);

				// write the inverted file for this part of the lexicon, ie
				// processTerms number of terms
				startWritingInvertedFile = System.currentTimeMillis();
				
				long[] rtr = writeInvertedFilePart(dos, scanResult.tmpStorage,
						scanResult.terms);
				numberOfTokens += rtr[0];
				logger.info("time to write inverted file: "
					 + ((System.currentTimeMillis()- startWritingInvertedFile) / 1000D));
				logger.info("temporary memory used: "
						 + BinaryByteUnit.format(rtr[1]));
				
				InvertedIndexBuilder.displayMemoryUsage(r);

				logger.info("time to perform one iteration: "+ ((System.currentTimeMillis() - startProcessingLexicon) / 1000D));
				logger.info("number of pointers processed: "+ numberOfPointersThisIteration);

				scanResult.tmpStorage = null;
				scanResult.codesHashMap.clear();
				scanResult.codesHashMap = null;
				tt.setDone(numberOfPointers);
			}
			tt.finished();
			logger.info("Finished generating inverted file, rewriting lexicon");
			file.close(); file = null;
			
			if (lexiconStream instanceof Closeable) {
				((Closeable)lexiconStream).close();
			}
			dos.close();
			// finalising the lexicon file with the updated information
			// on the frequencies and the offsets
			// reading the original lexicon
			lexiconStream = (Iterator<Map.Entry<String,LexiconEntry>>)index.getIndexStructureInputStream("lexicon");
			
			
			// the updated lexicon
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
			los.close();
			dis.close();
			Files.delete(LexiconFilename.concat(".tmp2"));
			FSOMapFileLexicon.deleteMapFileLexicon("lexicon", index.getPath(), index.getPrefix());
			FSOMapFileLexicon.renameMapFileLexicon("tmplexicon", index.getPath(), index.getPrefix(), "lexicon", index.getPath(), index.getPrefix());
			
			compressionConfig.writeIndexProperties(index, "lexicon-entry-inputstream");
			
			//these should be already set, but in case their not
			index.setIndexProperty("num.Terms", ""+numberOfUniqueTerms);
			index.setIndexProperty("num.Tokens", ""+numberOfTokens);
			index.setIndexProperty("num.Pointers", ""+numberOfPointers);
			System.gc();

		} catch (Exception ioe) {
			logger.error("IOException occured during creating the inverted file. Stack trace follows.", ioe);
		} finally {
			tt.finished();
		}
	}

	protected TIntArrayList[] createPointerForTerm(LexiconEntry le)
	{
		TIntArrayList[] tmpArray = new TIntArrayList[4+fieldCount];
		final int tmpNT = le.getDocumentFrequency();
		for(int i = 0; i < fieldCount+3; i++)
			tmpArray[i] = new TIntArrayList(tmpNT+1);
		tmpArray[fieldCount+3] = new TIntArrayList(le.getFrequency()+1);
		return tmpArray;
	}

	class BlockMemSizeLexiconScanner extends BasicMemSizeLexiconScanner {

		BlockMemSizeLexiconScanner(
				Iterator<Entry<String, LexiconEntry>> lexiconStream, CollectionStatistics stats) {
			super(lexiconStream, stats);
		}
		
		@Override
		public String toString() {
			return this.getClass().getSimpleName() + ": lexicon scanning until approx " + BinaryByteUnit.format(memThreshold) +" of memory, including positions, is consumed";
		}
		
		@Override
		String estimatedIterations() {
			final long pointers = this.collStats.getNumberOfPointers();
			final long tokens = this.collStats.getNumberOfTokens();
			long projected_size = pointers * (3+ fieldCount) * Integer.BYTES //pointers
					+ tokens * Integer.BYTES;
			projected_size = (long) (projected_size * tintlist_overhead); 
			return (int)Math.ceil((double)projected_size / (double)memThreshold) + " (estimated) iterations";
		}
		
		@Override
		LexiconScanResult scanLexicon() {
			
			TIntIntHashMap codesHashMap = new TIntIntHashMap();
			ArrayList<TIntArrayList[]> tmpStorageStorage = new ArrayList<TIntArrayList[]>();
			long numberOfPointersThisIteration = 0;
			long cumulativePointersSize = 0;
			long cumulativeTermsSize = 0;			
			long numberOfBlocksThisIteration = 0;
			
			int j=0;
			while (lexiconStream.hasNext())
			{
				Map.Entry<String,LexiconEntry> lee = lexiconStream.next();
				LexiconEntry le = lee.getValue();
				
				tmpStorageStorage.add(createPointerForTerm(le));
			
				numberOfPointersThisIteration += le.getDocumentFrequency();
				cumulativePointersSize += 
						(16 + Integer.BYTES + 16 + 2* Integer.BYTES)* (3 + fieldCount)  //array and tintarraylist overheads
						+ le.getDocumentFrequency() * (3 + fieldCount) * Integer.BYTES  //pointer storage: 3 is docid, tf, blockfreq
						+ le.getFrequency() * Integer.BYTES; //block storage
				cumulativeTermsSize += 
						(2 * Integer.BYTES + 1); //codesHashMap: two ints and one byte for every entry
						numberOfBlocksThisIteration += le.getFrequency();
 
				//the class TIntIntHashMap return zero when you look up for a
				//the value of a key that does not exist in the hash map.
				//For this reason, the values that will be inserted in the 
				//hash map are increased by one. 
				codesHashMap.put(le.getTermId(), j + 1);
				j++;

				//we account for 5times overhead on the tintinthashmap, and
				//12% overhead on the tintlist_overhead.
				if ((tintlist_overhead * cumulativePointersSize + tintint_overhead * cumulativeTermsSize) > memThreshold)
					break;
			}
			LexiconScanResult rtr = new LexiconScanResult(j, numberOfPointersThisIteration, codesHashMap, tmpStorageStorage);
			
			logger.debug(
					BinaryByteUnit.format(memThreshold) + " reached at "
					+ BinaryByteUnit.format(cumulativePointersSize) +" * "+tintlist_overhead+" for tmpStorageStorage, "
					+ BinaryByteUnit.format(cumulativeTermsSize) +" * "+ tintint_overhead +" for codesHashMap "
					+ " given "
					+ rtr.toString() + " and " + numberOfBlocksThisIteration + " blocks");
			if (logger.isTraceEnabled())
			{
				Footprint footprint1 = ObjectGraphMeasurer.measure(tmpStorageStorage);
				logger.trace("tmpStorageStorage type usage is " + footprint1.toString());
				Footprint footprint2 = ObjectGraphMeasurer.measure(codesHashMap);
				logger.trace("codesHashMap type usage is " + footprint2.toString());
			}

			return rtr;
		}
		
	}


	/**
	 * Traverses the direct files recording all occurrences of terms noted in
	 * codesHashMap into tmpStorage.
	 * 
	 * @param codesHashMap
	 *			contains the term ids that are being processed in this
	 *			iteration, as keys. Values are the corresponding index in
	 *			tmpStorage that information about this terms should be placed.
	 * @param tmpStorage
	 *			Records the occurrences information. First dimension is for
	 *			each term, as of the index given by codesHashMap; Second
	 *			dimension contains fieldCount+4 TIntArrayLists : (document id, term
	 *			frequency, field0, ... fieldCount-1 , block frequencies, block ids).
	 */
	@Override
	protected void traverseDirectFile(TIntIntHashMap codesHashMap,
			TIntArrayList[][] tmpStorage) throws IOException {
		
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
			int termid;
			FieldPosting fp = _useFieldInformation ? (FieldPosting) ip : null;
			BlockPosting bp = (BlockPosting) ip;
			
			//use skipping to possibly avoid DF decompression
//			termid = ip.next(minTermId-1);
//			if (termid == IterablePosting.EOL)
//				continue;
			
			while( (termid = ip.next()) != IterablePosting.EOL)
			{
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
					int[] positions = bp.getPositions();
					tmpMatrix[fieldCount+2].add(positions.length);
					for(int pos : positions)
						tmpMatrix[fieldCount+3].add(pos);
					
					
					
				}
			}
			docid++;
		}
		directInputStream.close();
	}

	/**
	 * Writes the section of the inverted file
	 * 
	 * @param dos
	 *			a temporary data structure that contains the offsets in the
	 *			inverted index for each term.
	 * @param tmpStorage
	 *			Occurrences information, as described in traverseDirectFile().
	 *			This data is consumed by this method - once this method has
	 *			been called, all the data in tmpStorage will be destroyed.
	 * @param _processTerms
	 *			The number of terms being processed in this iteration.
	 * @return the number of tokens processed in this iteration and the number of bytes of temporary mem that were used
	 */
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
				
				final int[] tmpMatrix_blockFreq = tmpStorage[j][2+fieldCount].toNativeArray();
				final int[] tmpMatrix_blockIds = tmpStorage[j][3+fieldCount].toNativeArray();	
				final int[][] tmpFields = new int[fieldCount][]; 
				for(int k=0;k<fieldCount;k++)
				{
					tmpFields[k] = tmpStorage[j][k+2].toNativeArray();
				}
				tmpStorage[j] = null;
				size += ids.length * (3 + fieldCount) * Integer.BYTES + tmpMatrix_blockIds.length * Integer.BYTES;
								
				p.setOffset(file.getOffset());
				p.setNumberOfEntries(ids.length);
				p.write(dos);
				
				IterablePosting ip = null;
				if (fieldCount > 0)
				{
					ip = new ArrayOfBlockFieldIterablePosting(ids, tf, null, tmpFields, null, true, tmpMatrix_blockFreq, tmpMatrix_blockIds);					
				}
				else
				{
					ip = new ArrayOfBlockIterablePosting(ids, tf, tmpMatrix_blockFreq, tmpMatrix_blockIds);
				}
				file.writePostings(ip);

				numTokens += frequency;				
			}
			return new long[]{numTokens,size};
		}
	
	/** Use this main method to recover the creation of an inverted index, should it fail */
	public static void main(String[] args) throws Exception {
		IndexOnDisk indx = Index.createIndex();
		String structureName = "inverted";
		new BlockInvertedIndexBuilder(
				indx, 
				structureName, 
				CompressionFactory.getCompressionConfiguration(
						structureName, FieldScore.FIELD_NAMES, ApplicationSetup.BLOCK_SIZE, ApplicationSetup.MAX_BLOCKS)
			).createInvertedIndex();
		indx.flush();
		indx.close();
	}

}
