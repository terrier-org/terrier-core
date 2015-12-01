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
 * The Original Code is BlockStructureMerger.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author) 
 */
package org.terrier.structures.merging;

import org.terrier.utility.ApplicationSetup;
import java.util.Date;

import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.indexing.CompressionFactory;
import org.terrier.utility.ArrayUtils;


/**
 * This class merges two sets of data structures (ie direct index, 
 * inverted index, document index, lexicon and statistics), created by 
 * Terrier with position information (blocks) and possibly field 
 * information, into one set of data structures. 
 *
 * 
 * @author Vassilis Plachouras and Craig Macdonald
  * @see org.terrier.structures.merging.StructureMerger
 */
public class BlockStructureMerger extends StructureMerger {
	
	
	/**
	 * constructor
	 * @param _srcIndex1
	 * @param _srcIndex2
	 * @param _destIndex
	 */
	public BlockStructureMerger(IndexOnDisk _srcIndex1, IndexOnDisk _srcIndex2, IndexOnDisk _destIndex)
	{
		super(_srcIndex1, _srcIndex2, _destIndex);
		String[] fieldNames = ArrayUtils.parseCommaDelimitedString(srcIndex1.getIndexProperty("index.inverted.fields.names", ""));
		int blocks = srcIndex1.getIntIndexProperty("index.inverted.blocks", 1);
		int maxblocks = srcIndex1.getIntIndexProperty("index.inverted.blocks.max", ApplicationSetup.MAX_BLOCKS);
		compressionDirectConfig = CompressionFactory.getCompressionConfiguration("direct", fieldNames, blocks, maxblocks);
		compressionInvertedConfig = CompressionFactory.getCompressionConfiguration("inverted", fieldNames, blocks, maxblocks);
	}

	


	/** usage: java org.terrier.structures.merging.BlockStructureMerger [binary bits] [inverted file 1] [inverted file 2] [output inverted file]
     */
	public static void main(String[] args) {
		if (args.length != 6)
		{
			logger.error("usage: java org.terrier.structures.merging.BlockStructureMerger srcPath1 srcPrefix1 srcPath2 srcPrefix2 destPath1 destPrefix1 ");
			logger.error("Exiting ...");
			return;
		}
		Index.setIndexLoadingProfileAsRetrieval(false);
		IndexOnDisk indexSrc1 = Index.createIndex(args[0], args[1]);
		IndexOnDisk indexSrc2 = Index.createIndex(args[2], args[3]);
		IndexOnDisk indexDest = Index.createNewIndex(args[4], args[5]);
		
		StructureMerger sMerger = new BlockStructureMerger(indexSrc1, indexSrc2, indexDest);
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
		
		logger.info("finished at " + (new Date()));
		long end = System.currentTimeMillis();
		logger.info("time elapsed: " + ((end-start)*1.0d/1000.0d) + " sec.");
	}
	
}

