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
	
	public ThreadedBatchIndexing(String _path, String _prefix, boolean _singlePass) {
		super(_path, _prefix);
		singlePass = _singlePass;
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
			logger.error("Collection class named "+ collectionName + " not found, aborting");
		}
		return rtr;
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
				logger.warn("Multi-threaded singlepass indexing is experimental - caution advised due to threads competing for available memory! YMMV.");
				logger.info("Increasing reserved memory for singlepass by factor of "+ threadCount);
				ApplicationSetup.MEMORY_THRESHOLD_SINGLEPASS *= threadCount;
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
					if (ApplicationSetup.BLOCK_INDEXING)
						new BlockStructureMerger(src1, src2, newIndex).mergeStructures();
					else
						new StructureMerger(src1, src2, newIndex).mergeStructures();
					
					try {
						src1.close();
						src2.close();
						newIndex.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return thisPrefix;
				}	
			};
			String tmpPrefix = partitioned.parallelStream().map(indexer).reduce(merger).get();
			IndexUtil.renameIndex(path, tmpPrefix, path, prefix);
			logger.info("Parallel indexing completed after " + (System.currentTimeMillis() - starttime)/1000 + " seconds");
		} catch (Throwable e) {
			logger.error("Problem occurred during parallel indexing", e);
		}
	}

}
