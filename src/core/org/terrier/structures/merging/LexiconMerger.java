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
 * The Original Code is BlockDirectIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Ben He <ben{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.structures.merging;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.FSOMapFileLexiconOutputStream;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.LexiconOutputStream;
import org.terrier.structures.Pointer;
import org.terrier.structures.SimpleBitIndexPointer;
import org.terrier.structures.indexing.LexiconBuilder;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;

/**
 * Merges many lexicons, termids and offsets are not kept.
 * @author Vassilis Plachouras
 */
public class LexiconMerger {

	/** The logger used */
	private static Logger logger = LoggerFactory.getLogger(LexiconMerger.class);

	protected IndexOnDisk srcIndex1;
	protected IndexOnDisk srcIndex2;
	protected IndexOnDisk destIndex;

	
	/**
	 * A constructor that sets the filenames of the lexicon
	 * files to merge
	 * @param src1 Source index 1
	 * @param src2 Source index 2
	 * @param dest Destination index
	 */
	public LexiconMerger(IndexOnDisk src1, IndexOnDisk src2, IndexOnDisk dest)
	{
		srcIndex1 = src1;
		srcIndex2 = src2;
		destIndex = dest;
	}
	
	/**
	 * Merges the two lexicons into one. After this stage, the offsets in the
	 * lexicon are not correct. They will be updated only after creating the 
	 * inverted file.
	 */
	@SuppressWarnings("unchecked")
	public void mergeLexicons() {
		try {
			
			//setting the input streams
			Iterator<Map.Entry<String,LexiconEntry>> lexInStream1 = 
				(Iterator<Map.Entry<String,LexiconEntry>>)srcIndex1.getIndexStructureInputStream("lexicon");
			Iterator<Map.Entry<String,LexiconEntry>> lexInStream2 = 
				(Iterator<Map.Entry<String,LexiconEntry>>)srcIndex2.getIndexStructureInputStream("lexicon");
			
			for(String structure : new String[]{"lexicon-keyfactory", "lexicon-valuefactory"})
			{
				IndexUtil.copyStructure(srcIndex1, destIndex, structure, structure);
			}
			for(String property : new String[] {"max.term.length", "index.inverted.fields.count"} )
			{
				destIndex.setIndexProperty(property, srcIndex1.getIndexProperty(property, null));
			}
			
			//setting the output stream
			LexiconOutputStream<String> lexOutStream = new FSOMapFileLexiconOutputStream(
					destIndex, 
					"lexicon",
					 (Class <FixedSizeWriteableFactory<LexiconEntry>>)destIndex.getIndexStructure("lexicon-valuefactory").getClass()
					);
			
			boolean hasMore1 = false;
			boolean hasMore2 = false;
			String term1;
			String term2;

			int termId = 0;
			
			Pointer p = new SimpleBitIndexPointer();
		
			hasMore1 = lexInStream1.hasNext(); 
			hasMore2 = lexInStream2.hasNext(); 
			Map.Entry<String,LexiconEntry> lee1 = lexInStream1.next();
			Map.Entry<String,LexiconEntry> lee2 = lexInStream2.next();
			while (hasMore1 && hasMore2) {
				
				
				
				term1 = lee1.getKey();
				term2 = lee2.getKey();
				int lexicographicalCompare = term1.compareTo(term2);
				if (lexicographicalCompare < 0) {
					lee1.getValue().setTermId(termId);
					lee1.getValue().setPointer(p);
					lexOutStream.writeNextEntry(term1, lee1.getValue());
					termId++;
					if (hasMore1 = lexInStream1.hasNext()) lee1 = lexInStream1.next();
				
				} else if (lexicographicalCompare > 0) {
					lee2.getValue().setTermId(termId);
					lee2.getValue().setPointer(p);
					lexOutStream.writeNextEntry(term2, lee2.getValue());
					termId++;
					if (hasMore2 = lexInStream2.hasNext()) lee2 = lexInStream2.next();
				} else {
					lee1.getValue().setTermId(termId);
					lee1.getValue().setPointer(p);
					lee1.getValue().add(lee2.getValue());
					lexOutStream.writeNextEntry(term1, lee1.getValue());
					if (hasMore1 = lexInStream1.hasNext()) lee1 = lexInStream1.next();
					if (hasMore2 = lexInStream2.hasNext()) lee2 = lexInStream2.next();
					termId++;
				}
			}
			
			if (hasMore1) {
				while (hasMore1) {
					lee1.getValue().setTermId(termId);
					lee1.getValue().setPointer(p);
					lexOutStream.writeNextEntry(lee1.getKey(), lee1.getValue());
					if (hasMore1 = lexInStream1.hasNext()) lee1 = lexInStream1.next();
					termId++;
				}
			} else if (hasMore2) {
				while (hasMore2) {
					lee1.getValue().setTermId(termId);
					lee1.getValue().setPointer(p);
					lexOutStream.writeNextEntry(lee2.getKey(), lee2.getValue());
					if (hasMore2 = lexInStream2.hasNext()) lee2 = lexInStream2.next();
					termId++;
				}		
			}
			IndexUtil.close(lexInStream1);
			IndexUtil.close(lexInStream2);
			lexOutStream.close();
			//recopy the value factory to ensure the field settings are correct
			for(String structure : new String[]{"lexicon-valuefactory"})
			{
				IndexUtil.copyStructure(srcIndex1, destIndex, structure, structure);
			}
			
			LexiconBuilder.optimise(destIndex, "lexicon");
			destIndex.flush();
		} catch(IOException ioe) {
			logger.error("IOException while merging lexicons.", ioe);
		}
	}
	/**
	 * main
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		if (args.length != 6)
		{
			logger.error("usage: java org.terrier.structures.merging.LexiconMerger srcPath1 srcPrefix1 srcPath2 srcPrefix2 destPath1 destPrefix1 ");
			return;
		}
		Index.setIndexLoadingProfileAsRetrieval(false);
		
		IndexOnDisk indexSrc1 = Index.createIndex(args[0], args[1]);
		IndexOnDisk indexSrc2 = Index.createIndex(args[2], args[3]);
		IndexOnDisk indexDest = Index.createNewIndex(args[4], args[5]);

		LexiconMerger lMerger = new LexiconMerger(indexSrc1, indexSrc2, indexDest);
		long start = System.currentTimeMillis();
		if(logger.isInfoEnabled()){
			logger.info("started at " + (new Date()));
		}
		lMerger.mergeLexicons();
		indexSrc1.close();
		indexSrc2.close();
		indexDest.close();

		if(logger.isInfoEnabled()){
			logger.info("finished at " + (new Date()));
			long end = System.currentTimeMillis();
			logger.info("time elapsed: " + ((end-start)*1.0d/1000.0d) + " sec.");
		}
	}

	
}
