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
 * The Original Code is TestCollections.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.indexing;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.TagSet;


@SuppressWarnings("unchecked")
public class TestCollections extends ApplicationSetupBasedTest {

	static Class<? extends Collection>[] STREAM_COLLECTIONS = (Class<? extends Collection>[]) new Class<?>[]{
		TRECCollection.class,
		//TRECUTFCollection.class,
		WARC018Collection.class,
		WARC09Collection.class,
		WARC10Collection.class
	};
	
	static Class<? extends Collection>[] ALL_COLLECTIONS = (Class<? extends Collection>[]) new Class<?>[]{
		TRECCollection.class,
		//TRECUTFCollection.class,
		TRECWebCollection.class,
		WARC018Collection.class,
		WARC09Collection.class,
		WARC10Collection.class,
		SimpleFileCollection.class,
		SimpleXMLCollection.class,
		SimpleMedlineXMLCollection.class,
		TwitterJSONCollection.class
		
	};
	
	@Test public void testStreamConstructor() throws Exception
	{
		for(Class<? extends Collection> c : STREAM_COLLECTIONS)
		{
			c.getConstructor(InputStream.class);
		}
	}
	
	@Test public void testFactoryConstructorDefault() throws Exception
	{
		File f = super.tmpfolder.newFile("collection.spec");
		f.createNewFile();
		ApplicationSetup.COLLECTION_SPEC = f.toString();
		for(Class<? extends Collection> c : ALL_COLLECTIONS)
		{
			assertNotNull(CollectionFactory.loadCollection(c.getName()));
		}
	}
		
	@Test public void testFactoryConstructorTREClike() throws Exception
	{
		List<String> files = new ArrayList<>();
		Class<?>[] constructerClasses = {List.class,String.class,String.class,String.class};
		Object[] constructorValues = {files, TagSet.TREC_DOC_TAGS, null, null};
		for(Class<? extends Collection> c : ALL_COLLECTIONS)
		{
			assertNotNull(
					CollectionFactory.loadCollection(c.getName(),constructerClasses, constructorValues)
							);
		}
	}
	
	@Test public void testFactoryConstructorTRECThreadedlike() throws Exception
	{
		File f = super.tmpfolder.newFile("collection.spec");
		f.createNewFile();
		Class<?>[] constructerClasses = {String.class,String.class,String.class,String.class};
		Object[] constructorValues = {f.toString(), TagSet.TREC_DOC_TAGS, null, null};
		for(Class<? extends Collection> c : ALL_COLLECTIONS)
		{
			assertNotNull(
					CollectionFactory.loadCollection(c.getName(),constructerClasses, constructorValues)
							);
		}
	}
	
}
