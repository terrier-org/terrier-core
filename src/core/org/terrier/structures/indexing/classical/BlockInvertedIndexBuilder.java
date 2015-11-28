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
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
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

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.BlockEntryStatistics;
import org.terrier.structures.FSOMapFileLexicon;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.LexiconOutputStream;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.SimpleBitIndexPointer;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;
import org.terrier.structures.postings.ArrayOfBlockFieldIterablePosting;
import org.terrier.structures.postings.ArrayOfBlockIterablePosting;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

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

	protected String finalLexiconClass = "org.terrier.structures.Lexicon";

	/**
	 * constructor
	 * @param index
	 * @param structureName
	 */
	public BlockInvertedIndexBuilder(IndexOnDisk index, String structureName, CompressionConfiguration compressionConfig) {
		super(index, structureName, compressionConfig);
		lexiconOutputStream = LexiconOutputStream.class;
	}

	/**
	 * This method creates the block html inverted index. The approach used is
	 * described briefly: for a group of M terms from the lexicon we build the
	 * inverted file and save it on disk. In this way, the number of times we
	 * need to read the direct file is related to the parameter M, and
	 * consequently to the size of the available memory.
	 */
	@SuppressWarnings("unchecked")
	public void createInvertedIndex() {
		numberOfPointersPerIteration = Integer.parseInt(ApplicationSetup.getProperty("invertedfile.processpointers", "2000000")); 
		processTerms = Integer.parseInt(ApplicationSetup.getProperty("invertedfile.processterms", "25000"));
		try {
			Runtime r = Runtime.getRuntime();
			logger.info("creating block inverted index");
			final String LexiconFilename = index.getPath() + "/" + index.getPrefix() + ".lexicon";
			//final int numberOfDocuments = index.getCollectionStatistics().getNumberOfDocuments();
			
			fieldCount = index.getIntIndexProperty("index.direct.fields.count", 0);
			this.useFieldInformation = fieldCount > 0;
			
			long assumedNumberOfPointers = Long.parseLong(index.getIndexProperty("num.Pointers", "0"));
			long numberOfTokens = 0;
			long numberOfPointers = 0;

			int numberOfUniqueTerms = index.getCollectionStatistics().getNumberOfUniqueTerms();
			Iterator<Map.Entry<String, LexiconEntry>> lexiconStream = (Iterator<Map.Entry<String, LexiconEntry>>)this.index.getIndexStructureInputStream("lexicon");

			// A temporary file for storing the updated
			// lexicon file, after creating the inverted file
			DataOutputStream dos = new DataOutputStream(Files.writeFileStream(LexiconFilename.concat(".tmp2")));

			// if the set number of terms to process is higher than the
			// available,
			if (processTerms > numberOfUniqueTerms)
				processTerms = (int) numberOfUniqueTerms;

			long startProcessingLexicon = 0;
			long startTraversingDirectFile = 0;
			long startWritingInvertedFile = 0;
			long numberOfPointersThisIteration = 0;

			int i = 0;
			int iterationCounter = 0;
			/* generate a message guessing iteration counts */
			String iteration_message_suffix = null;
			if (numberOfPointersPerIteration > 0)
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
					+ ((numberOfUniqueTerms % processTerms ==0 )
						? (numberOfUniqueTerms/processTerms)
						: 1+(numberOfUniqueTerms/processTerms))
					+ " iterations";
			}
			/* finish number of iteration calculation */
			if (numberOfPointersPerIteration == 0)
			{
				logger.warn("Using old-fashioned number of terms strategy. Please consider setting invertedfile.processpointers for forward compatible use");
			}

			while (i < numberOfUniqueTerms) {
				iterationCounter++;
				TIntIntHashMap codesHashMap = null;
				TIntArrayList[][] tmpStorage = null;
				IntLongTuple results = null;

				logger.info("Iteration " + iterationCounter+ iteration_message_suffix);

				// traverse the lexicon looking to determine the first N() terms
				// this can be done two ways: for the first X terms
				// OR for the first Y pointers

				startProcessingLexicon = System.currentTimeMillis();

				if (numberOfPointersPerIteration > 0) {// we've been configured
														// to run with a given
														// number of pointers
					logger.info("Scanning lexicon for "
							+ numberOfPointersPerIteration + " pointers");
					/*
					 * this is less speed efficient, as we have no way to guess
					 * how many terms it will take to fill the given number of
					 * pointers. The advantage is that memory consumption is
					 * more directly correlated to number of pointers than
					 * number of terms, so when indexing tricky collections, it
					 * is easier to find a number of pointers that can fit in
					 * memory
					 */

					codesHashMap = new TIntIntHashMap();
					ArrayList<TIntArrayList[]> tmpStorageStorage = new ArrayList<TIntArrayList[]>();
					results = scanLexiconForPointers(
							numberOfPointersPerIteration, lexiconStream,
							codesHashMap, tmpStorageStorage);
					tmpStorage = (TIntArrayList[][]) tmpStorageStorage
							.toArray(new TIntArrayList[0][0]);

				} else// we're running with a given number of terms
				{
					tmpStorage = new TIntArrayList[processTerms][];
					codesHashMap = new TIntIntHashMap(processTerms);
					results = scanLexiconForTerms(processTerms, lexiconStream,
							codesHashMap, tmpStorage);
				}

				processTerms = results.Terms;// no of terms to process on
												// this iteration
				numberOfPointersThisIteration = results.Pointers;
				numberOfPointers += results.Pointers;// no of pointers to
														// process on this
														// iteration
				i += processTerms;

				if (processTerms == 0)
					break;
				logger.info("time to process part of lexicon: "	+ ((System.currentTimeMillis() - startProcessingLexicon) / 1000D));

				InvertedIndexBuilder.displayMemoryUsage(r);

				// Scan the direct file looking for those terms
				startTraversingDirectFile = System.currentTimeMillis();
				traverseDirectFile(codesHashMap, tmpStorage);
				logger.info("time to traverse direct file: "+ ((System.currentTimeMillis() - startTraversingDirectFile) / 1000D));

				InvertedIndexBuilder.displayMemoryUsage(r);

				// write the inverted file for this part of the lexicon, ie
				// processTerms number of terms
				startWritingInvertedFile = System.currentTimeMillis();
				numberOfTokens += writeInvertedFilePart(dos, tmpStorage,
						processTerms);
				logger.info("time to write inverted file: "	+ ((System.currentTimeMillis() - startWritingInvertedFile) / 1000D));

				InvertedIndexBuilder.displayMemoryUsage(r);

				logger.info("time to perform one iteration: "+ ((System.currentTimeMillis() - startProcessingLexicon) / 1000D));
				logger.info("number of pointers processed: "+ numberOfPointersThisIteration);

				tmpStorage = null;
				codesHashMap.clear();
				codesHashMap = null;
			}

			logger.info("Finished generating inverted file, rewriting lexicon");
//			this.numberOfDocuments = numberOfDocuments;
//			this.numberOfUniqueTerms = numberOfUniqueTerms;
//			this.numberOfTokens = numberOfTokens;
//			this.numberOfPointers = numberOfPointers;
			file.close(); file = null;
			
			if (lexiconStream instanceof Closeable) {
				((Closeable)lexiconStream).close();
			}
			dos.close();
			// finalising the lexicon file with the updated information
			// on the frequencies and the offsets
//			finalising the lexicon file with the updated information
			//on the frequencies and the offsets
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

		} catch (IOException ioe) {
			logger.error("IOException occured during creating the inverted file. Stack trace follows.", ioe);
		}
	}

	protected TIntArrayList[] createPointerForTerm(LexiconEntry le)
	{
		TIntArrayList[] tmpArray = new TIntArrayList[4+fieldCount];
		final int tmpNT = le.getDocumentFrequency();
		for(int i = 0; i < fieldCount+3; i++)
			tmpArray[i] = new TIntArrayList(tmpNT);
		if (le instanceof BlockEntryStatistics)
		{
			tmpArray[fieldCount+3] = new TIntArrayList(((BlockEntryStatistics)le).getBlockCount());
		}
		else
		{
			tmpArray[fieldCount+3] = new TIntArrayList(le.getFrequency());
		}
		return tmpArray;
	}



	/**
	 * Traverses the direct fies recording all occurrences of terms noted in
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
		
		IterablePosting ip = null;
		while((ip = directInputStream.getNextPostings()) != null)
		{
			docid += directInputStream.getEntriesSkipped();
			int termid;
			FieldPosting fp = _useFieldInformation ? (FieldPosting) ip : null;
			BlockPosting bp = (BlockPosting) ip;
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
//					blockfreq = blockfreqs[k];
//					tmpMatrix[fieldCount+2].add(blockfreq);
//					blockidstart = 0;
//					if (k > 0) {
//						for (int l = 0; l < k; l++)
//							blockidstart += blockfreqs[l];
//					}
//					blockidend = blockidstart + blockfreq;
//
//					for (int l = blockidstart; l < blockidend; l++) {
//						tmpMatrix[fieldCount+3].add(blockids[l]);
//					}
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
	 * @return the number of tokens processed in this iteration
	 */
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
				
				final int[] tmpMatrix_blockFreq = tmpStorage[j][2+fieldCount].toNativeArray();
				final int[] tmpMatrix_blockIds = tmpStorage[j][3+fieldCount].toNativeArray();	
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
					ip = new ArrayOfBlockFieldIterablePosting(ids, tf, null, tmpFields, null, true, tmpMatrix_blockFreq, tmpMatrix_blockIds);					
				}
				else
				{
					ip = new ArrayOfBlockIterablePosting(ids, tf, tmpMatrix_blockFreq, tmpMatrix_blockIds);
				}
				file.writePostings(ip);

				numTokens += frequency;				
			}
			return numTokens;
		}
	
	
//	@Override
//	protected long writeInvertedFilePart(final DataOutputStream dos,
//			TIntArrayList[][] tmpStorage, final int processTerms)
//			throws IOException
//	{
//		// write to the inverted file. We should note that the lexicon
//		// file should be updated as well with the term frequency and
//		// the startOffset pointer
//		
//		int frequency;
//		long numTokens = 0;
//		BitIndexPointer p = new SimpleBitIndexPointer();
//		
//		for (int j = 0; j < processTerms; j++) {
//			frequency = 0; // the term frequency
//			
//			final int[][] tmpMatrix = new int[4+fieldCount][];
//			for(int k=0;k<4+fieldCount;k++)
//			{
//				tmpMatrix[k] = tmpStorage[j][k].toNativeArray();
//			}
//			tmpStorage[j] = null;
//			final int[] tmpMatrix_docids = tmpMatrix[0];
//			final int[] tmpMatrix_freqs = tmpMatrix[1];
//			final int[] tmpMatrix_blockFreq = tmpMatrix[2+fieldCount];
//			final int[] tmpMatrix_blockIds = tmpMatrix[3+fieldCount];
//			
//			p.setOffset(file.getByteOffset(), file.getBitOffset());
//			p.setNumberOfEntries(tmpMatrix_docids.length);
//			p.write(dos);
//
//			// write the first entry
//			int docid = tmpMatrix_docids[0];
//			file.writeGamma(docid + 1);
//			int termfreq = tmpMatrix_freqs[0];
//			frequency += termfreq;
//			file.writeUnary(termfreq);
//			
//			if (fieldCount > 0)
//			{	
//				for(int fi = 0; fi < fieldCount;fi++)
//				{
//					file.writeUnary(tmpMatrix[2+fi][0]+1);
//				}
//			}
//			
//			int blockfreq = tmpMatrix_blockFreq[0];
//			file.writeUnary(blockfreq + 1);
//			int blockid;
//			if (blockfreq != 0)
//			{
//				blockid = tmpMatrix_blockIds[0];
//				file.writeGamma(blockid + 1);
//				for (int l = 1; l < blockfreq; l++) {
//					file.writeGamma(tmpMatrix_blockIds[l] - blockid);
//					blockid = tmpMatrix_blockIds[l];
//				}
//			}
//			int blockindex = blockfreq;
//			for (int k = 1; k < tmpMatrix_docids.length; k++) {
//				file.writeGamma(tmpMatrix_docids[k] - docid);
//				docid = tmpMatrix_docids[k];
//				termfreq = tmpMatrix_freqs[k];
//				frequency += termfreq;
//				file.writeUnary(termfreq);
//				if (fieldCount > 0)
//				{
//					for(int fi = 0; fi < fieldCount;fi++)
//					{
//						file.writeUnary(tmpMatrix[2+fi][k]+1);
//					}
//				}
//				blockfreq = tmpMatrix_blockFreq[k];
//				file.writeUnary(blockfreq + 1);
//				if (blockfreq == 0)
//					continue;
//				blockid = tmpMatrix_blockIds[blockindex];
//				file.writeGamma(blockid + 1);
//				blockindex++;
//				for (int l = 1; l < blockfreq; l++) {
//					file.writeGamma(tmpMatrix_blockIds[blockindex] - blockid);
//					blockid = tmpMatrix_blockIds[blockindex];
//					blockindex++;
//				}
//			}
//			numTokens += frequency;
//			
//		}
//		return numTokens;
//	}

}
