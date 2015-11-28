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
 * The Original Code is Indexer.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (Original author) 
 */
package org.terrier.structures.indexing;
import gnu.trove.TObjectIntHashMap;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.indexing.Collection;
import org.terrier.indexing.Document;
import org.terrier.structures.AbstractPostingOutputStream;
import org.terrier.structures.BasicDocumentIndexEntry;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.indexing.classical.InvertedIndexBuilder;
import org.terrier.structures.merging.BlockStructureMerger;
import org.terrier.structures.merging.StructureMerger;
import org.terrier.terms.PorterStemmer;
import org.terrier.terms.SkipTermPipeline;
import org.terrier.terms.Stopwords;
import org.terrier.terms.TermPipeline;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.FieldScore;
/**
 * <B>Properties:</b>
 * <ul>
 * <li><tt>termpipelines</tt> - the sequence of {@link TermPipeline} stages (e.g. {@link Stopwords} removal and {@link PorterStemmer}).
 * <li><tt>termpipelines.skip</tt> - a list of tokens which should not be skipped from the term pipeline. If not set or empty, then none will be skipped.</li>
 * <li><tt>indexing.max.tokens</tt> - The maximum number of tokens the indexer will attempt to index in a document.
 * If 0, then all tokens will be indexed (default).</li>
 * <li><tt>ignore.empty.documents</tt> - Assign empty documents with docids. Default true</li>
 * <li><tt>indexing.max.docs.per.builder</tt> - Maximum number of documents in an index before a new index is created, and merged later.
 * <li><tt>indexing.builder.boundary.docnos</tt> - Docnos of documents that force the index being created to be completed, and a new index to be commenced. An alternative to <tt>indexing.max.docs.per.builder</tt>
 * <li><tt>indexer.meta.forward.keys</tt> - comma delimited list of {@link Document} properties to index as document metadata in the {@link MetaIndex}. Defaults to "docno", which permits docid->docno lookups.. Examples are "docno,url" or "docno,url,content"</li>
 * <li><tt>indexer.meta.forward.keylens</tt> - comma delimited list of the length of the values to record in the {@link MetaIndex}. Defaults to 20.</li>
 * <li><tt>indexer.meta.reverse.keys</tt> - comma delimited list of {@link Document} properties to permit lookups for (i.e. docno->docid). Defaults to empty (none are enabled).</li>
 * </ul>
 * @author Craig Macdonald
  */
public abstract class Indexer
{
	/** the logger for this class */
	protected static final Logger logger = LoggerFactory.getLogger(Indexer.class);


	/**
	 * The number of documents indexed with a set
	 * of builders. If a collection consists of 
	 * more documents, then we need to create
	 * new builders and later merge the data
	 * structures. The corresponding property is
	 * <tt>indexing.max.docs.per.builder</tt> and the
	 * default value is <tt>18000000</tt> (18 million documents). If the property
	 * is set equal to zero, then there is no limit.
	 */
	protected int MAX_DOCS_PER_BUILDER = 0;
	
	/** 
	 * The maximum number of tokens in a document. 
	 * If it is set to zero, then there is no limit 
	 * in the number of tokens indexed for a document. Set by property <tt>indexing.max.tokens</tt>.
	 */
	protected int MAX_TOKENS_IN_DOCUMENT = 0;
	
	/** The DOCNO of documents to force builder boundaries */
	protected final HashSet<String> BUILDER_BOUNDARY_DOCUMENTS = new HashSet<String>();
	
	/** 
	 * Indicates whether field information should be saved in the 
	 * created data structures.
	 */
	protected boolean useFieldInformation;
	
	/**
	 * The default namespace for the term pipeline classes.
	 */
	private final static String PIPELINE_NAMESPACE = "org.terrier.terms.";
	/**
	 * The first component of the term pipeline.
	 */
	protected TermPipeline pipeline_first;
	/**
	 * Indicates whether an entry for empty documents is stored in the 
	 * document index, or empty documents should be ignored.
	 */
	protected boolean IndexEmptyDocuments;
	
	/**
	 * The builder that creates the direct index.
	 */
	protected AbstractPostingOutputStream directIndexBuilder;
	
	/**
	 * The builder that creates the document index.
	 */
	protected DocumentIndexBuilder docIndexBuilder;
	
	/**
	 * The builder that creates the inverted index.
	 */
	protected InvertedIndexBuilder invertedIndexBuilder;
	
	/**
	 * The builder that creates the lexicon.
	 */
	protected LexiconBuilder lexiconBuilder;
	
	protected MetaIndexBuilder metaBuilder;
	
	/**
	 * The common prefix of the data structures filenames. 
	 */
	protected String fileNameNoExtension;
	
	/**
	 * The path in which the data structures are stored.
	 */
	protected String path;
	/** The prefix of the data structures, ie the first part of the filename */
	protected String prefix;
	/** The index being worked on, denoted by path and prefix */
	protected IndexOnDisk currentIndex = null;

	
	/** Creates an indexer at the location ApplicationSetup.TERRIER_INDEX_PATH and
	 * ApplicationSetup.TERRIER_INDEX_PREFIX
	 */
	public Indexer()
	{
		this(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
	}
	
	/**
	 * Creates an instance of the class. The generated data structures
	 * will be saved in the given path. The of the data is given by the prefix
	 * parameter.
	 * @param _path String the path where the generated data structures will be saved.
	 * @param _prefix String the filename that the data structures will have.
	 */ 
	public Indexer(String _path, String _prefix) {
		this.fileNameNoExtension = ApplicationSetup.makeAbsolute(_prefix, _path);
		this.prefix = _prefix;
		this.path = _path;
	}
	
	/** Protected do-nothing constructor for use by child classes */
	protected Indexer(long a, long b, long c) {
	}

	/** This method must be called by anything which directly extends Indexer.
      * See: http://benpryor.com/blog/2008/01/02/dont-call-subclass-methods-from-a-superclass-constructor/
	  */
	protected void init()
	{
		FieldScore.init();
		//init fields before constructing pipeline
		this.load_field_ids();
		//construct pipeline using list specified in terrier.properties
        //this object should be the last item in the pipeline
		this.load_indexer_properties();
        this.load_pipeline();
        //load the docnos of any documents that should force builder boundaries
        this.load_builder_boundary_documents();
        
	}

	/**
	 * An abstract method for creating the direct index, the document index
	 * and the lexicon for the given collections.
	 * @param collections Collection[] An array of collections to index
	 */
	public abstract void createDirectIndex(Collection[] collections);
	/**
	 * An abstract method for creating the inverted index, given that the
	 * the direct index, the document index and the lexicon have
	 * already been created.
	 */
	public abstract void createInvertedIndex();
	
	/**
	 * An abstract method that returns the last component 
	 * of the term pipeline.
	 * @return TermPipeline the end of the term pipeline.
	 */
	protected abstract TermPipeline getEndOfPipeline();

	/** mapping: field name -> field id, returns 0 for no mapping */	
	protected TObjectIntHashMap<String> fieldNames = new TObjectIntHashMap<String>(0);
	/** the number of fields */
	protected int numFields = 0;
	
	
	protected MetaIndexBuilder createMetaIndexBuilder()
	{
		final String[] forwardMetaKeys = ApplicationSetup.getProperty("indexer.meta.forward.keys", "docno").split("\\s*,\\s*");
		final int[] metaKeyLengths = parseInts(ApplicationSetup.getProperty("indexer.meta.forward.keylens", "20").split("\\s*,\\s*"));
		final String[] reverseMetaKeys = ApplicationSetup.getProperty("indexer.meta.reverse.keys", "").split("\\s*,\\s*");
		return new CompressingMetaIndexBuilder(currentIndex, forwardMetaKeys, metaKeyLengths, reverseMetaKeys);
	}
	
	protected static final int[] parseInts(String[] in)
	{
		final int l = in.length;
		final int[] rtr = new int[l];
		for(int i=0;i<l;i++)
			rtr[i] = Integer.parseInt(in[i]);
		return rtr;
	}
	
	protected void load_indexer_properties()
	{
		IndexEmptyDocuments = !ApplicationSetup.IGNORE_EMPTY_DOCUMENTS;
		MAX_TOKENS_IN_DOCUMENT = Integer.parseInt(ApplicationSetup.getProperty("indexing.max.tokens", "0"));
		MAX_DOCS_PER_BUILDER = Integer.parseInt(ApplicationSetup.getProperty("indexing.max.docs.per.builder", "18000000"));
	}

	/** loads a mapping of field name -> field id */
	protected void load_field_ids()
	{
		FieldScore.init();
		useFieldInformation = FieldScore.USE_FIELD_INFORMATION;
		if (! FieldScore.USE_FIELD_INFORMATION || FieldScore.FIELDS_COUNT == 0)
			return;
		numFields = FieldScore.FIELDS_COUNT;
		logger.info("Indexer using " + numFields + " fields");
		int i=0;
		for (String f: FieldScore.FIELD_NAMES)
		{
			i++;
			String[] tagNames = ArrayUtils.parseDelimitedString(f, "|");
			for (String tag : tagNames) {
				fieldNames.put(tag, i);
			}
		}
	}

	/** 
	 * Creates the term pipeline, as specified by the
	 * property <tt>termpipelines</tt> in the properties
	 * file. The default value of the property <tt>termpipelines</tt>
	 * is <tt>Stopwords,PorterStemmer</tt>. This means that we first
	 * remove stopwords and then apply Porter's stemming algorithm.
	 */
	//@SuppressWarnings("unchecked")
	protected void load_pipeline()
	{
		String[] pipes = ApplicationSetup.getProperty(
				"termpipelines", "Stopwords,PorterStemmer").trim()
				.split("\\s*,\\s*");
		
		TermPipeline next = getEndOfPipeline();
		final TermPipeline last = next;
		TermPipeline tmp;
		for(int i=pipes.length-1; i>=0; i--)
		{
			try{
				String className = pipes[i];
				if (className.length() == 0)
					continue;
				if (className.indexOf(".") < 0 )
					className = PIPELINE_NAMESPACE + className;
				else if (className.startsWith("uk.ac.gla.terrier"))
					className = className.replaceAll("uk.ac.gla.terrier", "org.terrier");
				Class<? extends TermPipeline> pipeClass = Class.forName(className, false, this.getClass().getClassLoader()).asSubclass(TermPipeline.class);
				tmp = pipeClass.getConstructor(new Class[]{TermPipeline.class}).newInstance(new Object[] {next});
				next = tmp;
			}catch (Exception e){
				logger.warn("TermPipeline object "+PIPELINE_NAMESPACE+pipes[i]+" not found: "+e);
				e.printStackTrace();
			}
		}
		String skipTerms = null;
		//add SkipTermPipeline as the first pipeline step to allow for special terms to skip the pipeline processing sequence
		if ((skipTerms = ApplicationSetup.getProperty("termpipelines.skip", null)) != null && skipTerms.trim().length() > 0)
			pipeline_first = new SkipTermPipeline(next, last);
		else
			pipeline_first = next;
	}


	/** Loads the builder boundary documents from the property <tt>indexing.builder.boundary.docnos</tt>, comma delimited. */
	protected void load_builder_boundary_documents()
	{
		final String[] docnos = ApplicationSetup.getProperty("indexing.builder.boundary.docnos", "").split("\\s*,\\s*");
		for(int i=0;i<docnos.length;i++)
		{
			docnos[i] = docnos[i].trim();
			if (docnos[i].length() > 0)
				BUILDER_BOUNDARY_DOCUMENTS.add(docnos[i]);
		}
		if (BUILDER_BOUNDARY_DOCUMENTS.size() > 0)
			logger.info("Watching for "+BUILDER_BOUNDARY_DOCUMENTS.size()+ " documents that force index builder boundaries.");
	}
	
	/**
	 * Creates the data structures for a set of collections. 
	 * It creates a set of data structures for every 
	 * <tt>indexing.max.docs.per.builder</tt>, if the value of 
	 * this property is greater than zero, and then it mertges
	 * the generated data structures.
	 * @param collections The document collection objects to index.
	 */
	public void index(Collection[] collections) {
		//the number of collections to index
		final int numOfCollections = collections.length;
		int counter = 0;
		final String oldIndexPrefix = prefix;
		
		//while (collections[numOfCollections-1].hasNext()) {
		while (! collections[numOfCollections-1].endOfCollection()) {	
			counter++;
			
			prefix = oldIndexPrefix + "_" + counter;
			fileNameNoExtension = path + ApplicationSetup.FILE_SEPARATOR + prefix;
			//ApplicationSetup.setupFilenames();
			logger.info("creating the data structures " + prefix);
			this.createDirectIndex(collections);
			this.createInvertedIndex();
		}
		
		//merge the data structures
		if (counter > 1) { 
			logger.info("merging data structures");
			merge(path, oldIndexPrefix, 1, counter);	
		}
		else
		{
			try{
				IndexUtil.renameIndex(path, prefix, path, oldIndexPrefix);
			} catch (IOException ioe ) {
				logger.error("Could not rename index", ioe);
			}
		}
		//restore the prefix
		prefix = oldIndexPrefix;
		//ApplicationSetup.TERRIER_INDEX_PREFIX=oldIndexPrefix;
		//ApplicationSetup.setupFilenames();
		fileNameNoExtension = path + ApplicationSetup.FILE_SEPARATOR + prefix;
	}

	/** Merge a series of numbered indices in the same path/prefix area. New merged index
	  * will be stored at mpath/mprefix_highest+1.
	  * @param mpath Path of all indices
	  * @param mprefix Common prefix of all indices
	  * @param lowest lowest subfix of prefix
	  * @param highest highest subfix of prefix 
	  */
	public static void merge(String mpath, String mprefix, int lowest, int highest)
	{
		//we define the counterMerged in order to
		//ensure that the merged data structures will
		//have different names

		LinkedList<String[]> llist = new LinkedList<String[]>();
		for (int i=lowest; i<=highest; i++) {
				llist.add(new String[]{mpath,mprefix+ "_" + i});
		}
		merge(mpath, mprefix, llist, highest+1);
	}

	/** Merge two indices.
	  * @param index1 Path/Prefix of source index 1
	  * @param index2 Path/Prefix of source index 2
	  * @param outputIndex Path/Prefix of destination index 
	  */
	protected static void mergeTwoIndices(String[] index1, String[] index2, String[] outputIndex){
		StructureMerger sMerger = null;
		IndexOnDisk src1 = Index.createIndex(index1[0], index1[1]);
		IndexOnDisk src2 = Index.createIndex(index2[0], index2[1]);
		IndexOnDisk dst = Index.createNewIndex(outputIndex[0], outputIndex[1]);
		logger.info("Merging "+ src1+ " & "+ src2 +" to " + dst);
		if (ApplicationSetup.BLOCK_INDEXING) 
			sMerger = new BlockStructureMerger(src1, src2, dst);
		else 
			sMerger = new StructureMerger(src1, src2, dst);
										  
		//sMerger.setNumberOfBits(FieldScore.FIELDS_COUNT);
		sMerger.mergeStructures();
		try{
			src1.close(); src2.close(); dst.close();
		} catch (IOException ioe) {
			logger.error("Problem flushing index dst", ioe);
		}
		//delete old indices  
		try{
			IndexUtil.deleteIndex(index1[0], index1[1]);
			IndexUtil.deleteIndex(index2[0], index2[1]);
		} catch (IOException ioe) {
			logger.warn("Could not delete merge input indices ", ioe);
        }
	}

	/** Merge a series of indices, in pair-wise fashion
	  * @param mpath Common path of all indices
	  * @param mprefix Prefix of target index
	  * @param counterMerged - number of indices to merge
	  */
	public static void merge(String mpath, String mprefix, LinkedList<String[]> llist, int counterMerged)
	{
		while (llist.size() > 1) {
			LinkedList<String[]> tmpList = new LinkedList<String[]>();
			// merge every two indices stored in the linked list
			for (int i=0; i<llist.size(); i++){
				String[] filename1 = llist.get(i++);
				// if the first index is the end of the linked list (which means the size of the linked list
				// is odd), merge with the previous merged index.
				String[] filename2 = (i==llist.size())?(tmpList.removeLast()):llist.get(i);
				String[] outputFilename = new String[]{mpath,mprefix  + "_" + (counterMerged++)};
				//logger.info("Merging "+ filename1 + " and " + filename2 + " to " + outputFilename);
				mergeTwoIndices(filename1, filename2, outputFilename);
				tmpList.add(outputFilename);
			}
			llist = tmpList; tmpList = null;
		}
		logger.info("Done merging");
		
		//rename the generated structures 
		try{
			IndexUtil.renameIndex(mpath, mprefix+"_"+ (counterMerged-1), mpath, mprefix);
		} catch (IOException ioe) {
			logger.error("Could not rename merged index", ioe);
		}
	}

	/** event method to be overridden by child classes */
	protected void finishedDirectIndexBuild() {}
	/** event method to be overridden by child classes */
	protected void finishedInvertedIndexBuild() {}
	
	/** Returns the is the index will record fields */
	public boolean useFieldInformation() {
		return useFieldInformation;
	}

	protected DocumentIndexEntry emptyDocIndexEntry = new BasicDocumentIndexEntry();
		
	
	/** Adds an entry to document index for empty document @param docid, only if
		IndexEmptyDocuments is set to true.
	*/
	protected void indexEmpty(Map<String,String> docProperties) throws IOException
	{
		if (! IndexEmptyDocuments)
			return;
		/* add doc to documentindex, even though it's empty */	
		logger.warn("Adding empty document "+docProperties.get("docno"));
		docIndexBuilder.addEntryToBuffer(emptyDocIndexEntry);
		metaBuilder.writeDocumentEntry(docProperties);	
	}

	/** Utility method for merging indices */
	public static void main(String args[]) throws Exception
	{
		if (args[0].equals("--merge") && args.length == 3)
		{
			merge(
				ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX,
				Integer.parseInt(args[1]), Integer.parseInt(args[2])
			);
			return;
		}
		logger.error("Usage: org.terrier.indexing.Indexer --merge [lowid] [highid]");
	}
	
}
