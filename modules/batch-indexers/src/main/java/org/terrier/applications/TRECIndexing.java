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
 * The Original Code is TRECIndexing.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 *   Ben He <ben{a.}dcs.gla.ac.uk>
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> 
 */
package org.terrier.applications;
import org.terrier.indexing.Collection;
import org.terrier.indexing.CollectionFactory;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.structures.indexing.classical.BlockIndexer;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.TagSet;
/**
 * This class creates the indices for a test collection.
 * <p>
 * <b>Properties:</b>
 * <ul>
 * <li><tt>trec.indexer.class</tt> - name of the class to use as the indexer. This only applies to the Index method.</li>
 * <li><tt>trec.collection.class</tt> - name of the class to use as the Collection.</li>
 * </ul>
 * @author Gianni Amati, Vassilis Plachouras, Ben He, Craig Macdonald
 */
public class TRECIndexing extends BatchIndexing {
	/** The indexer object.*/
	//Indexer indexer;
	/** The collection to index. */
	protected Collection collectionTREC;
	
	
	public TRECIndexing(String _path, String _prefix) {
		super(_path, _prefix);
	}
	
	/**
	 * A constructor that initialised the data structures
	 * to use for indexing. 
	 * @param _path Absolute path to where the index should be created
	 * @param _prefix Prefix of the index files, usually "data"
	 */
	public TRECIndexing(String _path, String _prefix, String collectionSpec)
	{
		super(_path, _prefix);
		super.collectionSpec = collectionSpec;
		//indexer = loadIndexer(path, prefix);
	}
	
	/**
	 * A constructor that initialised the data structures
	 * to use for indexing. 
	 * @param _path Absolute path to where the index should be created
	 * @param _prefix Prefix of the index files, usually "data"
	 */
	public TRECIndexing(String _path, String _prefix, Collection c)
	{
		super(_path, _prefix);
		collectionTREC = c;
		//indexer = loadIndexer(path, prefix);
	}

	protected Indexer loadIndexer(String pa, String pr) {
		//load the appropriate indexer
		String indexerName = ApplicationSetup.getProperty(
			"trec.indexer.class",
			blocks
				? BlockIndexer.class.getName()
				: BasicIndexer.class.getName());
		if (indexerName.indexOf('.') == -1)
			indexerName = "org.terrier.structures.indexing.classical."+indexerName;
		Indexer _indexer = null;
		try{
			_indexer = ApplicationSetup.getClass(indexerName).asSubclass(Indexer.class)
				.getConstructor(String.class, String.class)
				.newInstance(pa, pr);
		} catch (ClassNotFoundException e) {
			logger.error("Indexer class named "+ indexerName + " not found", e);
		} catch (InstantiationException ie) {
			logger.error("Error while instantiating Indexer class named "+ indexerName + " : " + ie.getCause(), ie);
		} catch (Exception e){
			logger.error("Indexer class named "+ indexerName + "problem", e);
		}
		_indexer.setExternalParalllism(this.externalParalllism);
		return _indexer;
	}

	/**
	 * A default constructor that initialised the data structures
	 * to use for indexing.
	 */
	public TRECIndexing() {
		this(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
	}
	
	/**
	 * Calls the method index(Collection[]) of the
	 * class Indexer in order to build the data
	 * structures for a set of collections. This 
	 * particular method of the Indexer uses a 
	 * set of builders for a subset of the collection
	 * and builds separate data structures, which are 
	 * later merged.
	 */
	@Override
	public void index() {
		if (IndexOnDisk.existsIndex(path, prefix))
		{
			logger.error("Cannot index while an index exists at "+path + ","+ prefix);
			return;
		}

		if (collectionTREC == null){
			if (collectionFiles.size() > 0)
			{
				collectionTREC = loadCollection(collectionFiles);
			} else {
				collectionTREC = loadCollection(collectionSpec);
			}
		}

		loadIndexer(path, prefix).index(collectionTREC);
		try{
			collectionTREC.close();
		} catch (Exception e) {
			logger.warn("problem closing collection", e);
		}
	}
	
	/** 
	 * Used for testing purposes.
	 * @param args the command line arguments.
	 */
	public static void main(String[] args)
	{
		long startTime = System.currentTimeMillis();
		TRECIndexing t = new TRECIndexing();		
		t.index();
		long endTime = System.currentTimeMillis();
		if(logger.isInfoEnabled())
			logger.info("Elapsed time="+((endTime-startTime)/1000.0D));
	}
	
	
}
