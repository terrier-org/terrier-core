/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.ac.gla.uk
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
 * The Original Code is ThreadedBatchIndexing.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.applications;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.indexing.Collection;
import org.terrier.indexing.CollectionFactory;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.merging.BlockStructureMerger;
import org.terrier.structures.merging.StructureMerger;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.TagSet;
/** An implementation of BatchIndexing that uses Java 8 parallel streams to
 * increase indexing speed on multi-core machines.
 * @author Craig Macdonald
 * @since 4.2
 */
public class ThreadedBatchIndexing extends BatchIndexing {
	
	/** The logger used */
	protected static Logger logger = LoggerFactory.getLogger(ThreadedBatchIndexing.class);
	
	final boolean singlePass;
	int maxThreads = -1;
	
	public ThreadedBatchIndexing(String _path, String _prefix, boolean _singlePass) {
		super(_path, _prefix);
		singlePass = _singlePass;
	}
	
	public ThreadedBatchIndexing(String _path, String _prefix, boolean _singlePass, int threads) {
		super(_path, _prefix);
		singlePass = _singlePass;
		this.maxThreads = threads;
	}
	
	protected static Collection loadCollection(List<String> files) {
		//load the appropriate collection
		final String collectionName = ApplicationSetup.getProperty("trec.collection.class", "TRECCollection");
		
		Class<?>[] constructerClasses = {List.class,String.class,String.class,String.class};
		Object[] constructorValues = {files,TagSet.TREC_DOC_TAGS,
			ApplicationSetup.makeAbsolute(
				ApplicationSetup.getProperty("trec.blacklist.docids", ""), 
				ApplicationSetup.TERRIER_ETC), 
		    ApplicationSetup.makeAbsolute(
			ApplicationSetup.getProperty("trec.collection.pointers", "docpointers.col"), 
				ApplicationSetup.TERRIER_INDEX_PATH)
		};
		Collection rtr = CollectionFactory.loadCollection(collectionName, constructerClasses, constructorValues);
		if (rtr == null)
		{
			throw new IllegalArgumentException("Collection class named "+ collectionName + " not loaded, aborting");
		}
		return rtr;
	}
	
	/** Define maximum number of threads in use. -1 for no limit. */
	public void setMaxThreads(int threads)
	{
		this.maxThreads = threads;
	}

	@Override
	public void index()
	{	
		try{
			final long starttime = System.currentTimeMillis();
			final AtomicInteger indexCounter = new AtomicInteger();
			final AtomicInteger mergeCounter = new AtomicInteger();			
			
			final int threadCount = ForkJoinPool.commonPool().getParallelism();
			logger.info("Started " + this.getClass().getSimpleName() + " with parallelism " + threadCount);
			if (singlePass)
			{
				int reservationFactor = Math.min(threadCount, 10);
				logger.warn("Multi-threaded singlepass indexing is experimental - caution advised due to threads competing for available memory! YMMV.");
				logger.info("Memory reserved was " + ApplicationSetup.MEMORY_THRESHOLD_SINGLEPASS);
				logger.info("Increasing reserved memory for singlepass by factor of "+ reservationFactor);
				ApplicationSetup.MEMORY_THRESHOLD_SINGLEPASS *= reservationFactor;
				logger.info("Memory reserved is now "+ApplicationSetup.MEMORY_THRESHOLD_SINGLEPASS);
			}
			
			List<List<String>> partitioned = CollectionFactory.splitCollectionSpecFileList(ApplicationSetup.COLLECTION_SPEC, threadCount);
			logger.info("Partitioned collection.spec into "+ partitioned.size() + " partitions");
			
			IndexOnDisk.setIndexLoadingProfileAsRetrieval(false);
			Function<List<String>,String> indexer = new Function<List<String>,String>()
			{
				@Override
				public String apply(List<String> files) {
					String thisPrefix = prefix + "_stream"+indexCounter.getAndIncrement();
					Collection c = loadCollection(files);
					BatchIndexing indexing = singlePass 
							? new TRECIndexingSinglePass(path, thisPrefix, c)
							: new TRECIndexing(path, thisPrefix, c);
					indexing.blocks = blocks;
					indexing.index();
					return thisPrefix;
				}	
			};
			BinaryOperator<String> merger = new BinaryOperator<String>()
			{
				@Override
				public String apply(String t, String u) {
					Index.setIndexLoadingProfileAsRetrieval(false);
					IndexOnDisk src1 = IndexOnDisk.createIndex(path, t);
					IndexOnDisk src2 = IndexOnDisk.createIndex(path, u);
					String thisPrefix = prefix + "_merge"+mergeCounter.getAndIncrement();
					IndexOnDisk newIndex = IndexOnDisk.createNewIndex(path, thisPrefix);
					if (blocks)
						new BlockStructureMerger(src1, src2, newIndex).mergeStructures();
					else
						new StructureMerger(src1, src2, newIndex).mergeStructures();
					
					try {
						src1.close();
						src2.close();
						newIndex.close();
						//TODO: could index deletion occur in parallel
						IndexUtil.deleteIndex(path, t);
						IndexUtil.deleteIndex(path, u);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return thisPrefix;
				}	
			};
			
			ForkJoinPool forkPool = this.maxThreads == -1 
					? ForkJoinPool.commonPool()
					: new ForkJoinPool(this.maxThreads);
			String tmpPrefix = forkPool.submit(() -> partitioned.parallelStream().map(indexer).reduce(merger).get()).get();
			
			IndexUtil.renameIndex(path, tmpPrefix, path, prefix);
			logger.info("Parallel indexing completed after " 
				+ (System.currentTimeMillis() - starttime)/1000 + " seconds, using " 
				+ threadCount + " threads");
			logger.info("Final index is at "+path+" " + prefix);
		} catch (Throwable e) {
			logger.error("Problem occurred during parallel indexing", e);
		}
	}

}
