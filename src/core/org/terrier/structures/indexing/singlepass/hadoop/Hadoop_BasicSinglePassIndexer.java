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
 * The Original Code is SortAscendingTripleVectors.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk
 *  Richard McCreadie <richardm{a.}dcs.gla.ac.uk
 */

package org.terrier.structures.indexing.singlepass.hadoop;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TaskAttemptID;

import org.terrier.compression.bit.BitIn;
import org.terrier.compression.bit.BitOutputStream;
import org.terrier.indexing.Document;
import org.terrier.structures.BasicLexiconEntry;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.FSOMapFileLexiconOutputStream;
import org.terrier.structures.FieldDocumentIndexEntry;
import org.terrier.structures.FieldLexiconEntry;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.LexiconOutputStream;
import org.terrier.structures.SimpleDocumentIndexEntry;
import org.terrier.structures.indexing.CompressingMetaIndexBuilder;
import org.terrier.structures.indexing.DocumentIndexBuilder;
import org.terrier.structures.indexing.MetaIndexBuilder;
import org.terrier.structures.indexing.singlepass.BasicSinglePassIndexer;
import org.terrier.structures.indexing.singlepass.FieldPostingInRun;
import org.terrier.structures.indexing.singlepass.RunsMerger;
import org.terrier.structures.indexing.singlepass.SimplePostingInRun;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.FieldScore;
import org.terrier.utility.Files;
import org.terrier.utility.TerrierTimer;
import org.terrier.utility.io.HadoopPlugin;
import org.terrier.utility.io.HadoopUtility;
import org.terrier.utility.io.WrappedIOException;
import org.terrier.utility.io.HadoopPlugin.JobFactory;

/**
 * Single Pass MapReduce indexer. 
 * <p><h3>Map phase processing</h3>
 * Indexes as a Map task, taking in a series of documents, emitting posting lists for terms as
 * memory becomes exhausted. Two side-files are created for each map task: the first (run files) takes note of how many documents were indexed
 * for each flush and for each map; the second contains the statistics for each document in a minature document index
 * </p>
 * <p><h3>Reduce phase processing</h3>
 * All posting lists for each term are read in, one term at a time. Using the run files, the posting lists are output into the final inverted
 * file, with all document ids corrected. Lastly, when all terms have been processed, the document indexes are merged into the final document
 * index, and the lexicon hash and lexid created.
 * </p>
 * <p><h3>Partitioned Reduce processing</h3>
 * Normally, the MapReduce indexer is used with a single reducer. However, if the partitioner is used, multiple reduces can run concurrently,
 * building several final indices. In doing so, a large collection can be indexed into several output indices, which may be useful for distributed
 * retrieval.
 * </p>
 * @author Richard McCreadie and Craig Macdonald
 * @since 2.2
  */
@SuppressWarnings("deprecation")
public class Hadoop_BasicSinglePassIndexer 
	extends BasicSinglePassIndexer 
	implements Mapper<Text, SplitAwareWrapper<Document>, SplitEmittedTerm, MapEmittedPostingList>,
	Reducer<SplitEmittedTerm, MapEmittedPostingList, Object, Object>
{

	/** TREC-388: disable per-flush compression of docids, as docid alignment problems
	  * can arise if map tasks are restarted. Be vary careful of changing this.
	  */
	static final boolean RESET_IDS_ON_FLUSH = false;
	
	/**
	 * main
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
    {
        if (args.length == 2 && args[0].equals("--finish"))
        {
            final JobFactory jf = HadoopPlugin.getJobFactory("HOD-TerrierIndexing");
            if (jf == null)
                throw new Exception("Could not get JobFactory from HadoopPlugin");
            try{
            	finish(ApplicationSetup.TERRIER_INDEX_PATH, Integer.parseInt(args[1]), jf);
            } catch (Exception e) {
            	logger.error("Couldn't finish index", e);
            } finally {
            	jf.close();
            }
        }
        else
        {
        	System.err.println("Usage: Hadoop_BasicSinglePassIndexer [--finish numberOfReduceTasks]");
        }
    }
	/**
	 * finish
	 * @param destinationIndexPath
	 * @param numberOfReduceTasks
	 * @param jf
	 * @throws Exception
	 */
	public static void finish(final String destinationIndexPath, int numberOfReduceTasks, final JobFactory jf) throws Exception
	{
		final String[] reverseMetaKeys = ApplicationSetup.getProperty("indexer.meta.reverse.keys", "").split("\\s*,\\s*");
		Index.setIndexLoadingProfileAsRetrieval(false);
		if (numberOfReduceTasks == 1)
		{			
			IndexOnDisk index = Index.createIndex(destinationIndexPath, ApplicationSetup.TERRIER_INDEX_PREFIX);
			if (index == null)
			{
				throw new IOException("No such index ["+destinationIndexPath+","+ApplicationSetup.TERRIER_INDEX_PREFIX+"]");
			}
			CompressingMetaIndexBuilder.reverseAsMapReduceJob(index, "meta", reverseMetaKeys, jf);
			index.close();
			return;
		}
		//make a list of MR jobs in separate threads
		List<Thread> threads = new ArrayList<Thread>(numberOfReduceTasks);
		for(int i=0;i<numberOfReduceTasks;i++)
		{
			final int id = i;
			threads.add(new Thread() {
					@Override
					public void run() {
						try{
							IndexOnDisk index = Index.createIndex(destinationIndexPath, ApplicationSetup.TERRIER_INDEX_PREFIX+"-"+id);
							CompressingMetaIndexBuilder.reverseAsMapReduceJob(index, "meta", reverseMetaKeys, jf);
							index.close();
						} catch (Exception e) {
							logger.error("Problem finishing meta", e);
							e.printStackTrace();
						}
					}				
				});			
		}
		//start the threads
		for(Thread t : threads)
			t.start();
		//wait for the threads to end
		for(Thread t : threads)
			t.join();
	}
	
	static enum Counters { 
		INDEXED_DOCUMENTS, INDEXED_EMPTY_DOCUMENTS, INDEXER_FLUSHES, INDEXED_TOKENS, INDEXED_POINTERS;
	};
	
	/** JobConf of the current running job */	
	protected JobConf jc;

	/** The split that these documents came form **/
	protected int splitnum;
	protected boolean start;
	
	/**
	 * Empty constructor
	 */
	public Hadoop_BasicSinglePassIndexer() {
		super(0,0,0);
		numberOfDocuments = currentId = numberOfDocsSinceCheck = numberOfDocsSinceFlush = numberOfUniqueTerms = 0;
		numberOfTokens = numberOfPointers = 0;
		flushNo=0;
		flushList = new LinkedList<Integer>();
	}
	
	/** Configure this indexer. Firstly, loads ApplicationSetup appropriately. 
	 * Actual configuration of indexer is then handled by configureMap() or configureReduce()
	 * depending on whether a Map or Reduce task is being configured.
	 * @param _jc The configuration for the job
	 */
	public void configure(JobConf _jc) 
	{
		this.jc = _jc;
		
		//1. configure application
		try{ 
			HadoopUtility.loadTerrierJob(_jc);
		} catch (Exception e) {
			throw new Error("Cannot load ApplicationSetup", e);
		}
		
		//2. configurure indexer
		try{
			if (HadoopUtility.isMap(_jc))
			{
				configureMap();
			} else {
				configureReduce();
			}
		} catch (Exception e) { 
			throw new Error("Cannot configure indexer", e);
		}
	}
	
	/** Called when the Map or Reduce task ends, to finish up the indexer. Actual cleanup is
	 * handled by closeMap() or closeReduce() depending on whether this is a Map or Reduce task.
	 */
	public void close() throws IOException
	{
		if (HadoopUtility.isMap(jc))
		{
			closeMap();
		} else {
			closeReduce();
		}
	}
	
	@Override
	/** Hadoop indexer does not have the consideration of boundary documents. */
	protected void load_builder_boundary_documents() { }
	

	/* ==============================================================
	 * Map implementation from here down
	 * ==============================================================
	 */
	
	/** output collector for the current map indexing process */
	protected OutputCollector<SplitEmittedTerm, MapEmittedPostingList> outputPostingListCollector;
	
	/** Current map number */
	protected String mapTaskID;
	/** How many flushes have we made */
	protected int flushNo;

	/** OutputStream for the the data on the runs (runNo, flushes etc) */
	protected DataOutputStream RunData;
	/** List of how many documents are in each flush we have made */
	protected LinkedList<Integer> flushList;
	
	protected void configureMap() throws Exception
	{	
		super.init();
		Path indexDestination = FileOutputFormat.getWorkOutputPath(jc);
		Files.mkdir(indexDestination.toString());
		mapTaskID = TaskAttemptID.forName(jc.get("mapred.task.id")).getTaskID().toString();
		currentIndex = Index.createNewIndex(indexDestination.toString(), mapTaskID);
		maxMemory = Long.parseLong(ApplicationSetup.getProperty("indexing.singlepass.max.postings.memory", "0"));
		//during reduce, we dont want to load indices into memory, as we only use
		//them as streams
		currentIndex.setIndexProperty("index.preloadIndices.disabled", "true");
		RunData = new DataOutputStream(
				Files.writeFileStream(
						new Path(indexDestination, mapTaskID+".runs").toString())
				);
		RunData.writeUTF(mapTaskID);
		start = true;
		createMemoryPostings();
		super.emptyDocIndexEntry = new SimpleDocumentIndexEntry();
		super.docIndexBuilder = new DocumentIndexBuilder(currentIndex, "document");
		super.metaBuilder = createMetaIndexBuilder();
		emptyDocIndexEntry = (FieldScore.FIELDS_COUNT > 0) ? new FieldDocumentIndexEntry(FieldScore.FIELDS_COUNT) : new SimpleDocumentIndexEntry();
	}
	
	
	
	protected MetaIndexBuilder createMetaIndexBuilder()
	{
		final String[] forwardMetaKeys = ApplicationSetup.getProperty("indexer.meta.forward.keys", "docno").split("\\s*,\\s*");
		final int[] metaKeyLengths = parseInts(ApplicationSetup.getProperty("indexer.meta.forward.keylens", "20").split("\\s*,\\s*"));
		//no reverse metadata during main indexing, pick up as separate job later
		return new CompressingMetaIndexBuilder(currentIndex, forwardMetaKeys, metaKeyLengths, new String[0]);
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
			value="DM_GC",
			justification="Forcing GC is an essential part of releasing" +
					"memory for further indexing")
	@Override
	/** causes the posting lists built up in memory to be flushed out */
	protected void forceFlush() throws IOException
	{
		logger.info("Map "+mapTaskID+", flush requested, containing "+numberOfDocsSinceFlush+" documents, flush "+flushNo);
		if (mp == null)
			throw new IOException("Map flushed before any documents were indexed");
		mp.finish(new HadoopRunWriter(outputPostingListCollector, mapTaskID, splitnum, flushNo));
		RunData.writeInt(currentId);
		if (currentReporter != null)
			currentReporter.incrCounter(Counters.INDEXER_FLUSHES, 1);
		System.gc();
		createMemoryPostings();
		memoryCheck.reset();
		numberOfDocsSinceFlush = 0;
		if (RESET_IDS_ON_FLUSH)
			currentId = 0;
		flushNo++;
	}
	
	/**
	 * Map processes a single document. Stores the terms in the document along with the posting list
	 * until memory is full or all documents in this map have been processed then writes then to
	 * the output collector.  
	 * @param key - Wrapper for Document Number
	 * @param value - Wrapper for Document Object
	 * @param _outputPostingListCollector Collector for emitting terms and postings lists
	 * @throws IOException
	 */
	public void map(
			Text key, SplitAwareWrapper<Document> value, 
			OutputCollector<SplitEmittedTerm, MapEmittedPostingList> _outputPostingListCollector, 
			Reporter reporter) 
		throws IOException 
	{
		final String docno = key.toString();
		currentReporter = reporter;
		reporter.setStatus("Currently indexing "+docno);
		final Document doc = value.getObject();
		
		if (start) {
			splitnum = value.getSplitIndex();
			System.out.println(splitnum);
			//RunData.writeInt(splitnum);
			start = false;
		}
		
		this.outputPostingListCollector = _outputPostingListCollector;
		
		/* setup for parsing */
		createDocumentPostings();
		String term;//term we're currently processing
		numOfTokensInDocument = 0;
		//numberOfDocuments++;
		//get each term in the document
		while (!doc.endOfDocument()) {
			reporter.progress();
			if ((term = doc.getNextTerm())!=null && !term.equals("")) {
				termFields = doc.getFields();
				/* pass term into TermPipeline (stop, stem etc) */
				pipeline_first.processTerm(term);

				/* the term pipeline will eventually add the term to this object. */
			}
			if (MAX_TOKENS_IN_DOCUMENT > 0 &&
					numOfTokensInDocument > MAX_TOKENS_IN_DOCUMENT)
				break;
		}
		
		//if we didn't index all tokens from document,
		//we need tocurrentId get to the end of the document.
		while (!doc.endOfDocument()){
			doc.getNextTerm();
		}
		/* we now have all terms in the DocumentTree, so we save the document tree */
		if (termsInDocument.getDocumentLength() == 0)
		{	/* this document is empty, add the minimum to the document index */
			// Nothing in the ifile
			indexEmpty(doc.getAllProperties());
		}
		else
		{	/* index this document */
			try{
				indexDocument(doc.getAllProperties(), termsInDocument);
				numberOfTokens += numOfTokensInDocument;
				reporter.incrCounter(Counters.INDEXED_TOKENS, numOfTokensInDocument);
				reporter.incrCounter(Counters.INDEXED_POINTERS, termsInDocument.getNumberOfPointers());
			} catch (IOException ioe) {
				throw ioe;				
			} catch (Exception e) {
				throw new WrappedIOException(e);
			}
		}
		termsInDocument.clear();
		reporter.incrCounter(Counters.INDEXED_DOCUMENTS, 1);
	}
	
	protected Reporter currentReporter;
	
	/**
	 * Write the empty document to the inverted index
	 */
	protected void indexEmpty(final Map<String,String> docProperties) throws IOException
	{
		/* add doc to documentindex, even though it's empty */
		if(IndexEmptyDocuments)
		{	
			logger.warn("Adding empty document "+docProperties.get("docno"));
			docIndexBuilder.addEntryToBuffer(emptyDocIndexEntry);
			metaBuilder.writeDocumentEntry(docProperties);
			currentId++;
			numberOfDocuments++;
			currentReporter.incrCounter(Counters.INDEXED_EMPTY_DOCUMENTS, 1);
		}
	}
	
	/** Finish up the map processing. Forces a flush, then writes out the final run data */
	protected void closeMap() throws IOException
	{
		forceFlush();
		docIndexBuilder.finishedCollections();
		currentIndex.setIndexProperty("index.inverted.fields.count", ""+FieldScore.FIELDS_COUNT);
		if (FieldScore.FIELDS_COUNT > 0)
		{
			currentIndex.addIndexStructure("document-factory", FieldDocumentIndexEntry.Factory.class.getName(), "java.lang.String", "${index.inverted.fields.count}");
		}
		else
		{
			currentIndex.addIndexStructure("document-factory", SimpleDocumentIndexEntry.Factory.class.getName(), "", "");
		}
		metaBuilder.close();
		currentIndex.flush();
		currentIndex.close();
		RunData.writeInt(-1);
		RunData.writeInt(numberOfDocuments);
		RunData.writeInt(splitnum);
		RunData.close();
		logger.info("Map "+mapTaskID+ " finishing, indexed "+numberOfDocuments+ " in "+(flushNo-1)+" flushes");
	}

	/* ==============================================================
	 * Reduce implementation from here down
	 * ==============================================================
	 */
	
	/** OutputStream for the Lexicon*/ 
	protected LexiconOutputStream<String> lexstream;
	/** runIterator factory being used to generate RunIterators */
	protected HadoopRunIteratorFactory runIteratorF = null;
	/** records whether the reduce() has been called for the first time */
	protected boolean reduceStarted = false;
	
	protected boolean mutipleIndices = true;
	protected int reduceId;
	protected String[] MapIndexPrefixes = null;
	protected Reporter lastReporter = null;
	
	protected void configureReduce() throws Exception
	{	
		super.init();
		start = true;
		//load in the current index
		final Path indexDestination = FileOutputFormat.getWorkOutputPath(jc);
		Files.mkdir(path = indexDestination.toString());
		final String indexDestinationPrefix = jc.get("indexing.hadoop.prefix", "data");
		reduceId = TaskAttemptID.forName(jc.get("mapred.task.id")).getTaskID().getId();
		indexDestination.toString();
		mutipleIndices = jc.getBoolean("indexing.hadoop.multiple.indices", true);
		if (jc.getNumReduceTasks() > 1)
		{
			//gets the reduce number and suffices this to the index prefix
			prefix = indexDestinationPrefix + "-"+reduceId;
		}
		else
		{
			prefix = indexDestinationPrefix;
		}
		
		currentIndex = Index.createNewIndex(path, prefix);
		
		super.merger = createtheRunMerger();
		reduceStarted = false;	
	}
	
	protected LinkedList<MapData> loadRunData() throws IOException 
	{
		// Load in Run Data
		ArrayList<String> mapTaskIDs = new ArrayList<String>();
		final LinkedList<MapData> runData = new LinkedList<MapData>();
		DataInputStream runDataIn;
	
		final String jobId = TaskAttemptID.forName(jc.get("mapred.task.id")).getJobID().toString().replaceAll("job", "task");
		
		final FileStatus[] files = FileSystem.get(jc).listStatus(
			FileOutputFormat.getOutputPath(jc), 
			new org.apache.hadoop.fs.PathFilter()
			{ 
				public boolean accept(Path path)
				{					
					final String name = path.getName();
					//1. is this a run file
					if (!(  name.startsWith( jobId )  && name.endsWith(".runs")))
						return false;
					return true;
				}
			}
		);

		if (files == null || files.length == 0)
		{
			throw new IOException("No run status files found in "+FileOutputFormat.getOutputPath(jc));
		}
		
		final int thisPartition = TaskAttemptID.forName(jc.get("mapred.task.id")).getTaskID().getId();
		final SplitEmittedTerm.SETPartitioner partitionChecker = new SplitEmittedTerm.SETPartitioner();
		partitionChecker.configure(jc);
		
		
		MapData tempHRD;
		for (FileStatus file : files) 
		{
			logger.info("Run data file "+ file.getPath().toString()+" has length "+Files.length(file.getPath().toString()));
			runDataIn = new DataInputStream(Files.openFileStream(file.getPath().toString()));
			tempHRD = new MapData(runDataIn);
			//check to see if this file contained our split information
			if (mutipleIndices && partitionChecker.calculatePartition(tempHRD.getSplitnum(), jc.getNumReduceTasks()) != thisPartition)
				continue;
			
			mapTaskIDs.add(tempHRD.getMap());
			runData.add(tempHRD);
			runDataIn.close();
		}
		// Sort by splitnum
		Collections.sort(runData);
		Collections.sort(mapTaskIDs, new IDComparator(runData));
		// A list of the index shards
		MapIndexPrefixes = mapTaskIDs.toArray(new String[0]);
		return runData;
	}
	
	/**
	 * Merge the postings for the current term, converts the document ID's in the
	 * postings to be relative to one another using the run number, number of documents
	 * covered in each run, the flush number for that run and the number of documents
	 * flushed.
	 * @param mapData - info about the runs(maps) and the flushes
	 */
	public void startReduce(LinkedList<MapData> mapData) throws IOException
	{
		logger.info("The number of Reduce Tasks being used : "+jc.getNumReduceTasks());
		((HadoopRunsMerger)(super.merger)).beginMerge(mapData);
		this.currentIndex.setIndexProperty("max.term.length", ApplicationSetup.getProperty("max.term.length", ""+20));
		lexstream = new FSOMapFileLexiconOutputStream(this.currentIndex, "lexicon", 
				(FieldScore.FIELDS_COUNT  > 0 ? FieldLexiconEntry.Factory.class : BasicLexiconEntry.Factory.class));
		// Tell the merger how many to Reducers to merge for
		((HadoopRunsMerger) merger).setNumReducers(
				mutipleIndices ? jc.getNumReduceTasks() : 1);
	}
	
	/** Main reduce algorithm step. Called for every term in the merged index, together with accessors
	 * to the posting list information that has been written.
	 * This reduce has no output.
	 * @param Term indexing term which we are reducing the posting lists into
	 * @param postingIterator Iterator over the temporary posting lists we have for this term
	 * @param output Unused output collector
	 * @param reporter Used to report progress
	 */
	public void reduce(
			SplitEmittedTerm Term, 
			Iterator<MapEmittedPostingList> postingIterator, 
			OutputCollector<Object, Object> output, 
			Reporter reporter)
		throws IOException
	{
		//if (logger.isDebugEnabled()) logger.debug("Reduce for term "+Term.getText());
		reporter.setStatus("Reducer is merging term " + Term.getTerm());
		if (! reduceStarted)
		{
			final LinkedList<MapData> runData = loadRunData();
        	startReduce(runData);
			reduceStarted = true;
		}
		String term = Term.getTerm().trim();
		if (term.length() == 0)
			return;
		runIteratorF.setRunPostingIterator(postingIterator);
		runIteratorF.setTerm(term);
		try{
			merger.mergeOne(lexstream);
		} catch (Exception e) {
			throw new WrappedIOException(e);
		}
		reporter.progress();
		this.lastReporter = reporter;
	}

	/** Merges the simple document indexes made for each map, instead creating the final document index */	
	@SuppressWarnings("unchecked")
	protected void mergeDocumentIndex(Index[] src, int numdocs) throws IOException
	{
		logger.info("Merging document and meta indices");
		final DocumentIndexBuilder docidOutput = new DocumentIndexBuilder(currentIndex, "document");
		final MetaIndexBuilder metaBuilder = this.createMetaIndexBuilder();
		int docCount =-1;
		TerrierTimer tt = new TerrierTimer("Merging document & meta indices", numdocs);
		tt.start();
		try{ 
			for (Index srcIndex: src)
			{
				final Iterator<DocumentIndexEntry> docidInput = (Iterator<DocumentIndexEntry>)srcIndex.getIndexStructureInputStream("document");
				final Iterator<String[]> metaInput1 = (Iterator<String[]>)srcIndex.getIndexStructureInputStream("meta");
			    while (docidInput.hasNext())
				{
					docCount++;
					docidOutput.addEntryToBuffer(docidInput.next());
			        metaBuilder.writeDocumentEntry(metaInput1.next());
			        this.lastReporter.progress();
			        tt.increment();
				}
			    IndexUtil.close(docidInput);
			    IndexUtil.close(metaInput1);
			}
		} finally {
			tt.finished();
		}
		metaBuilder.close();
		docidOutput.finishedCollections();
		if (FieldScore.FIELDS_COUNT > 0)
		{
			currentIndex.addIndexStructure("document-factory", FieldDocumentIndexEntry.Factory.class.getName(), "java.lang.String", "${index.inverted.fields.count}");
		}
		else
		{
			currentIndex.addIndexStructure("document-factory", SimpleDocumentIndexEntry.Factory.class.getName(), "", "");
		}
		
		//check document counts
		if (docCount != numdocs)
		{
			logger.warn("Mismatch between expected ("+numdocs+") and found document counts ("+docCount+")");
		}
		
		logger.info("Finished merging document indices from "+src.length+" map tasks: "+docCount +" documents found");
	}

	/** finishes the reduce step, by closing the lexicon and inverted file output,
 	  * building the lexicon hash and index, and merging the document indices created
	  * by the map tasks. The output index finalised */
	protected void closeReduce() throws IOException {
		
		if (! reduceStarted)
		{
			logger.warn("No terms were input, skipping reduce close");
			return;
		}
		//generate final index structures
		//1. any remaining lexicon terms
		merger.endMerge(lexstream);
		//2. the end of the inverted file
		merger.getBos().close();
		lexstream.close();
		
		
		//index updating is ONLY for 
		currentIndex.addIndexStructure(
				"inverted",
				invertedIndexClass,
				"org.terrier.structures.IndexOnDisk,java.lang.String,org.terrier.structures.DocumentIndex,java.lang.Class", 
				"index,structureName,document,"+ 
					(FieldScore.FIELDS_COUNT > 0
						? fieldInvertedIndexPostingIteratorClass
						: basicInvertedIndexPostingIteratorClass ));
		currentIndex.addIndexStructureInputStream(
                "inverted",
                invertedIndexInputStreamClass,
                "org.terrier.structures.IndexOnDisk,java.lang.String,java.util.Iterator,java.lang.Class",
                "index,structureName,lexicon-entry-inputstream,"+
                	(FieldScore.FIELDS_COUNT > 0
						? fieldInvertedIndexPostingIteratorClass
						: basicInvertedIndexPostingIteratorClass ));
		currentIndex.setIndexProperty("index.inverted.fields.count", ""+FieldScore.FIELDS_COUNT );
		currentIndex.setIndexProperty("index.inverted.fields.names", ArrayUtils.join(FieldScore.FIELD_NAMES, ","));
		
		
		//3. finalise the lexicon
		currentIndex.setIndexProperty("num.Terms",""+ lexstream.getNumberOfTermsWritten() );
		currentIndex.setIndexProperty("num.Tokens",""+lexstream.getNumberOfTokensWritten() );
		currentIndex.setIndexProperty("num.Pointers",""+lexstream.getNumberOfPointersWritten() );
		if (FieldScore.FIELDS_COUNT > 0)
			currentIndex.addIndexStructure("lexicon-valuefactory", FieldLexiconEntry.Factory.class.getName(), "java.lang.String", "${index.inverted.fields.count}");
		
		if (lexstream.getNumberOfTermsWritten() == 0)
		{
			logger.warn("Lexicon wrote no terms, but reduceStarted = "+ reduceStarted);
		}
		
		this.finishedInvertedIndexBuild();
			
		
		//the document indices are only merged if we are creating multiple indices
		//OR if this is the first reducer for a job creating a single index
		if (mutipleIndices || reduceId == 0)
		{
			//4. document index
			Index[] sourceIndices = new Index[MapIndexPrefixes.length];
			int numdocs = 0;
		 	for (int i= 0; i<MapIndexPrefixes.length;i++)
			{
				sourceIndices[i] = Index.createIndex(FileOutputFormat.getOutputPath(jc).toString(), MapIndexPrefixes[i]);
				if (sourceIndices[i] == null)
					throw new IOException("Could not load index from ("
						+FileOutputFormat.getOutputPath(jc).toString()+","+ MapIndexPrefixes[i] +") because "
						+Index.getLastIndexLoadError());
				numdocs += sourceIndices[i].getCollectionStatistics().getNumberOfDocuments();
			}
		 	this.mergeDocumentIndex(sourceIndices, numdocs);
		 	
		 	//5. close the map phase indices
			for(Index i : sourceIndices)
			{
				i.close();
			}
		}
		currentIndex.flush();
	}

	/** Creates the RunsMerger and the RunIteratorFactory */
	protected RunsMerger createtheRunMerger() {
		logger.info("creating run merged with fields="+useFieldInformation);
		runIteratorF = 
			new HadoopRunIteratorFactory(null, 
				(useFieldInformation 
					? FieldPostingInRun.class
					: SimplePostingInRun.class),
				super.numFields);
		HadoopRunsMerger tempRM = new HadoopRunsMerger(runIteratorF);
		try{
			tempRM.setBos(new BitOutputStream(
					currentIndex.getPath() + ApplicationSetup.FILE_SEPARATOR 
					+ currentIndex.getPrefix() + ".inverted" + BitIn.USUAL_EXTENSION));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return (RunsMerger)tempRM;
	}

}
