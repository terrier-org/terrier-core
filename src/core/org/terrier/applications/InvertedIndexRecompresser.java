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
 * The Original Code is InvertedIndexRecompresser.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 *  Matteo Catena
 *   
 */

package org.terrier.applications;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.compression.integer.codec.IntegerCodec;
import org.terrier.structures.AbstractPostingOutputStream;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.FSOMapFileLexiconOutputStream;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.LexiconOutputStream;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.indexing.CompressionFactory;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;
import org.terrier.structures.indexing.LexiconBuilder;
import org.terrier.structures.integer.IntegerCodecCompressionConfiguration;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.Files;
import org.terrier.utility.TerrierTimer;

/**
 * NOTE: BE CAREFUL, THIS APPLICATION OVERWRITES THE INDEX!!
 * 
 * This application recompress an inverted index using the specified compression configuration.
 * 
 * Usage:
 * InvertedIndexRecompresser
 * 
 * The terrier.properties must contain the following properties:
 * 
 * indexing.tmp-inverted.compression.configuration=IntegerCodecCompressionConfiguration
 * e.g.
 * indexing.tmp-inverted.compression.configuration={@link IntegerCodecCompressionConfiguration}
 * 
 * If you are targetting IntegerCodecCompressionConfiguration, you probably need the following properties:
 * 
 * compression.integer.chunk.size=N
 * compression.tmp-inverted.integer.ids.codec=the {@link IntegerCodec} to use to compress ids
 * compression.tmp-inverted.integer.ids.codec=the {@link IntegerCodec} to use to compress tfs
 * compression.tmp-inverted.integer.ids.codec=the {@link IntegerCodec} to use to compress fields (if any)
 * compression.tmp-inverted.integer.ids.codec=the {@link IntegerCodec} to use to compress blocks (if any) 
 * 
 * For example:
 * 
 * compression.tmp-inverted.integer.ids.codec=LemireFastPFORVBCodec
 * compression.tmp-inverted.integer.tfs.codec=LemireFastPFORVBCodec
 * compression.tmp-inverted.integer.fields.codec=LemireFastPFORVBCodec
 * compression.tmp-inverted.integer.blocks.codec=LemireFastPFORVBCodec
 * indexing.tmp-inverted.compression.configuration=IntegerCodecCompressionConfiguration
 * compression.integer.chunk.size=1024
 * 
 * 
 * <b>NOTE</b>: BE CAREFUL, THIS APPLICATION OVERWRITES THE INDEX!!
 * 
 * @author Matteo Catena, Craig Macdonald
 * @since 4.0
 *
 */
public class InvertedIndexRecompresser {
	
	static Logger logger = LoggerFactory.getLogger(InvertedIndexRecompresser.class);
	
	@SuppressWarnings("unchecked")
	static void compressInverted(IndexOnDisk index, AbstractPostingOutputStream icpw, Set<String> queryTerms) throws IOException {
			
		if (queryTerms == null)
			logger.info("Saving all terms...");
		
		Iterator<Entry<String, LexiconEntry>> iterator = (Iterator<Entry<String, LexiconEntry>>) 
				index.getIndexStructureInputStream("lexicon");
				
		PostingIndexInputStream iiis = (PostingIndexInputStream) index.getIndexStructureInputStream("inverted");
		
		FixedSizeWriteableFactory<LexiconEntry> valueFactory = (FixedSizeWriteableFactory<LexiconEntry>)index.getIndexStructure("lexicon-valuefactory");
		Class<? extends FixedSizeWriteableFactory<LexiconEntry>> valueFactoryClass = (Class<? extends FixedSizeWriteableFactory<LexiconEntry>>) valueFactory.getClass();
		
		LexiconOutputStream<String> los = new FSOMapFileLexiconOutputStream(
				index, "newlex", 
				valueFactoryClass);
		
		TerrierTimer tt = new TerrierTimer("Recompressing inverted index", index.getCollectionStatistics().getNumberOfPointers());
		tt.start();
		try{
		//int cnt = 0;
		while(iterator.hasNext()) {
			
			assert iterator.hasNext();
			assert iiis.hasNext();
			Entry<String,LexiconEntry> lee = iterator.next();
			IterablePosting postingList = iiis.next();
			
			if (queryTerms == null || queryTerms.contains(lee.getKey())) {
			
				BitIndexPointer bitPointer = icpw.writePostings(postingList);
						
				lee.getValue().setPointer(bitPointer);
				//lee.getValue().setTermId(cnt++);
				los.writeNextEntry(lee.getKey(), lee.getValue());	
				
				if (queryTerms != null) logger.debug("Saving "+lee.getKey()+"...");
			}
			tt.increment();
		}
		} finally {
			tt.finished();
			icpw.close();
			IndexUtil.close(iterator);
			IndexUtil.close(iiis);
			los.close();			
		}
	}
	
	public static void recompressInverted(IndexOnDisk index) throws Exception 
	{
		assert ! IndexUtil.isStructureOpen(index, "inverted");
		assert ! IndexUtil.isStructureOpen(index, "lexicon");
		
		
		CompressionConfiguration compressionConfig = CompressionFactory.getCompressionConfiguration("tmp-inverted", 
				ArrayUtils.parseCommaDelimitedString(index.getIndexProperty("index.inverted.fields.names", "")), 
				index.getIntIndexProperty("index.inverted.blocks", 0), 
				index.getIntIndexProperty("index.inverted.blocks.max", 0));
		logger.info("Recompressing inverted structure using " + compressionConfig.toString());
		
		AbstractPostingOutputStream icpw = compressionConfig.getPostingOutputStream(
				index.getPath() + ApplicationSetup.FILE_SEPARATOR + index.getPrefix() + "." + "tmp-inverted" + compressionConfig.getStructureFileExtension());
				
		//recompress
		compressInverted(index, icpw, null);
		
		//if (ApplicationSetup.getProperty("trec.topics", "").isEmpty()) {
		//	compressInverted(index, icpw, null);
		//} else {
		//	TRECQuerying2 tq2 = new TRECQuerying2();
		//	compressInverted(index, icpw, tq2.getAllQueryTerms());
		//}	
		
		compressionConfig.writeIndexProperties(index, "lexicon-entry-inputstream");
		logger.info("New inverted file size: "+ Files.length(index.getPath() + ApplicationSetup.FILE_SEPARATOR + index.getPrefix() + "." + "tmp-inverted" + compressionConfig.getStructureFileExtension()));
		
		
		//2. rename lexicon and inverted structures, old and new				
		IndexUtil.renameIndexStructure(index, "inverted", "invertedbak");
		IndexUtil.renameIndexStructure(index, "tmp-inverted", "inverted");
		IndexUtil.deleteStructure(index, "invertedbak");
		IndexUtil.deleteStructure(index, "invertedbak-inputstream");
		IndexUtil.renameIndexStructure(index, "lexicon", "lexiconbak");
		IndexUtil.renameIndexStructure(index, "newlex", "lexicon");		
		IndexUtil.deleteStructure(index, "lexiconbak");
		IndexUtil.deleteStructure(index, "lexiconbak-inputstream");
		IndexUtil.deleteStructure(index, "newlex-keyfactory");
		IndexUtil.deleteStructure(index, "newlex-valuefactory");
		IndexUtil.deleteStructure(index, "newlex-entry-inputstream");		

		//3. reoptimise lexicon	
		LexiconBuilder.optimise(index, "lexicon");
		index.flush();
	}

	public static void main(String[] args) throws Exception {
//		
//		String chooice = args[1];
//		String dataSource = null;
//		if ("FILE".equalsIgnoreCase(chooice)) {
//			dataSource = "file";
//		}
//		else if ("MEMORY".equalsIgnoreCase(chooice)){
//			dataSource = "fileinmem";
//		} else {
//			System.err.println("Second parameter must be FILE or MEMORY");
//			System.exit(-1);
//		}		

		IndexOnDisk.setIndexLoadingProfileAsRetrieval(false);
		IndexOnDisk index = Index.createIndex();
		recompressInverted(index);	
		index.close();
	}

}
