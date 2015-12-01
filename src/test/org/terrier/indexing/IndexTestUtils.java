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
 * The Original Code is IndexTestUtils.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.terrier.indexing.tokenisation.EnglishTokeniser;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.structures.indexing.classical.BlockIndexer;
import org.terrier.structures.indexing.singlepass.BasicSinglePassIndexer;
import org.terrier.utility.ApplicationSetup;

public class IndexTestUtils {

	static int count = 0;
	
	public static Index makeIndex(String[] docnos, String[] documents) throws Exception
	{
		count++;
		return makeIndex(docnos, documents, new BasicIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX + '-'+ count), ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX + '-'+ count);
	}
	
	public static Index makeIndexSinglePass(String[] docnos, String[] documents) throws Exception
	{
		count++;
		return makeIndex(docnos, documents, new BasicSinglePassIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX + '-'+ count), ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX + '-'+ count);
	}
	
	public static Index makeIndexFields(String[] docnos, String[] documents) throws Exception
	{
		count++;
		return makeIndexFields(docnos, documents, new BasicIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX + '-'+ count), ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX + '-'+ count);
	}
	
	public static Index makeIndexBlocks(String[] docnos, String[] documents) throws Exception
	{
		count++;
		return makeIndex(docnos, documents, new BlockIndexer(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX + '-'+ count), ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX + '-'+ count);
	}
	
	public static Index makeIndex(String[] docnos, String[] documents, Class<? extends Indexer> indexClz) throws Exception
	{
		count++;
		return makeIndex(docnos, documents, indexClz.getConstructor(String.class, String.class).newInstance(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX + '-'+ count), ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX + '-'+ count);
	}
	
	public static Document makeDocumentFromText(String contents, Map<String,String> docProperties) {
		return makeDocumentFromText(contents, docProperties, new EnglishTokeniser());
	}
	
	public static Document makeDocumentFromText(String contents, Map<String,String> docProperties, Tokeniser t) {
		return new FileDocument(new ByteArrayInputStream(contents.getBytes()), docProperties, new EnglishTokeniser());
	}
	
	
	public static Collection makeCollection(String[] docnos, String[] documents) throws Exception
	{
		assertEquals(docnos.length, documents.length);
		Document[] sourceDocs = new Document[docnos.length];
		for(int i=0;i<docnos.length;i++)
		{
			Map<String,String> docProperties = new HashMap<String,String>();
			docProperties.put("filename", docnos[i]);
			docProperties.put("docno", docnos[i]);
			sourceDocs[i] = new FileDocument(new ByteArrayInputStream(documents[i].getBytes()), docProperties, new EnglishTokeniser());
		}
		Collection col = new CollectionDocumentList(sourceDocs, "filename");
		return col;
	}
	
	public static Index makeIndex(String[] docnos, String[] documents, Indexer indexer, String path, String prefix) throws Exception
	{
		assertFalse("Index at "+ path + "," +  prefix + " already exists!", 
				IndexOnDisk.existsIndex(path, prefix));
		assertEquals(docnos.length, documents.length);
		Document[] sourceDocs = new Document[docnos.length];
		for(int i=0;i<docnos.length;i++)
		{
			Map<String,String> docProperties = new HashMap<String,String>();
			docProperties.put("filename", docnos[i]);
			docProperties.put("docno", docnos[i]);
			sourceDocs[i] = makeDocumentFromText(documents[i], docProperties);
		}
		Collection col = makeCollection(docnos, documents);
		indexer.index(new Collection[]{col});		
		Index index = Index.createIndex(path, prefix);
		assertNotNull(index);
		assertEquals(sourceDocs.length, index.getCollectionStatistics().getNumberOfDocuments());
		return index;
	}
	
	public static Index makeIndexFields(String[] docnos, String[] documents, Indexer indexer, String path, String prefix) throws Exception
	{
		assertFalse("Index at "+ path + "," +  prefix + " already exists!", 
			IndexOnDisk.existsIndex(path, prefix));
		assertEquals(docnos.length, documents.length);
		Document[] sourceDocs = new Document[docnos.length];
		for(int i=0;i<docnos.length;i++)
		{
			Map<String,String> docProperties = new HashMap<String,String>();
			docProperties.put("filename", docnos[i]);
			docProperties.put("docno", docnos[i]);
			sourceDocs[i] = new TaggedDocument(new ByteArrayInputStream(documents[i].getBytes()), docProperties, new EnglishTokeniser());
		}
		Collection col = new CollectionDocumentList(sourceDocs, "filename");
		indexer.index(new Collection[]{col});		
		Index index = Index.createIndex(path, prefix);
		assertNotNull(index);
		assertEquals(sourceDocs.length, index.getCollectionStatistics().getNumberOfDocuments());
		return index;
	}
	
}
