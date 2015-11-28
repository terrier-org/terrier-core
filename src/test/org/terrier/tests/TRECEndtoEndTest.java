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
 * The Original Code is TRECEndtoEndTest.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.tests;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;
import org.terrier.applications.FileFind;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.indexing.singlepass.Inverted2DirectIndexBuilder;
import org.terrier.utility.Files;

public abstract class TRECEndtoEndTest extends BatchEndToEndTest
{
	protected static final String testData = System.getProperty("user.dir") + "/share/tests/trec/";
	protected String corpusFolder;
	protected String topicsFile;
	protected String qrelsFile;
	protected String corpusName;
	
	public TRECEndtoEndTest(String corpusName)
	{
		this(
			corpusName,
			System.getProperty(corpusName + ".corpus", null), 
			System.getProperty(corpusName + ".topics", null), 
			System.getProperty(corpusName + ".qrels", null));
	}
	
	public TRECEndtoEndTest(final String corpus, String corpusLocation, String topics, String qrels)
	{
		super();
		this.corpusName = corpus;
		this.corpusFolder = corpusLocation;
		this.topicsFile = topics;
		this.qrelsFile = qrels;
		if (corpus == null)
			throw new IllegalArgumentException("Corpus name not set");
		if (corpusLocation == null)
			throw new IllegalArgumentException("Corpus folder not set");
		if (topics == null)
			throw new IllegalArgumentException("Corpus topics not set");
		if (qrels == null)
			throw new IllegalArgumentException("Qrels folder not set");
		if (! Files.exists(testData+this.corpusName+".runs"))
			throw new IllegalArgumentException("No corpus runs file found for a corpus named " + corpus + " at " +  testData+this.corpusName+".runs");
		
		super.indexingOptions.add("-Dindexer.meta.reverse.keys=");
		
		//also check that the index has the expected properties
		super.testHooks.add(new BatchEndToEndTestEventHooks() {
			@Override
			public void checkIndex(BatchEndToEndTest test, Index index)
					throws Exception {
				String line = null;
				BufferedReader br = Files.openFileReader(testData+corpus+".indexproperties");
				while((line = br.readLine()) != null)
				{
					if (line.startsWith("#"))
						continue;
					String[] parts = line.split("\\t");
					assertEquals(parts[1], index.getIndexProperty(parts[0], ""));
				}
			}
			
		});
	}
	
	protected void runsAndEvaluate() throws Exception
	{
		BufferedReader br = Files.openFileReader(testData+this.corpusName+".runs");
		String line = null;
		while((line = br.readLine()) != null)
		{
			if (line.startsWith("#"))
				continue;
			String[] parts = line.split("\\t");
			doTrecTerrierRunAndEvaluate(
					new String[]{this.topicsFile}, parts[0].split(" "), this.qrelsFile, Float.parseFloat(parts[1]));
		}
		br.close();
	}
	
	protected void doTest(List<String> indexingOptions) throws Exception {
		indexingOptions.add(0, "-i");
		doTrecTerrierIndexing(indexingOptions.toArray(new String[0]));
		runsAndEvaluate();
	}

	@Test public void testBasicClassical() throws Exception {		
		doTest(new ArrayList<String>());
	}
	
	@Test public void testBasicClassicalFields() throws Exception {
		List<String> indexingOptions = new ArrayList<String>();
		indexingOptions.add("-DFieldTags.process=TITLE,ELSE");
		doTest(indexingOptions);
	}
	
	@Test public void testBasicSP() throws Exception {
		List<String> indexingOptions = new ArrayList<String>();
		indexingOptions.add("-j");
		doTest(indexingOptions);
	}
	
	@Test public void testBasicSPFields() throws Exception {
		List<String> indexingOptions = new ArrayList<String>();
		indexingOptions.add("-DFieldTags.process=TITLE,ELSE");
		indexingOptions.add("-j");
		doTest(indexingOptions);
	}
	
	@Test public void testBlockClassical() throws Exception {
		List<String> indexingOptions = new ArrayList<String>();
		indexingOptions.add("-Dblock.indexing=true");
		doTest(indexingOptions);
	}	
	
	@Test public void testBlockClassicalFields() throws Exception {
		List<String> indexingOptions = new ArrayList<String>();
		indexingOptions.add("-DFieldTags.process=TITLE,ELSE");
		indexingOptions.add("-Dblock.indexing=true");
		doTest(indexingOptions);
	}
	
	@Test public void testBlockSP() throws Exception {
		List<String> indexingOptions = new ArrayList<String>();
		indexingOptions.add("-j");
		indexingOptions.add("-Dblock.indexing=true");
		doTest(indexingOptions);
	}
	
	@Test public void testBlockSPFields() throws Exception {
		List<String> indexingOptions = new ArrayList<String>();
		indexingOptions.add("-DFieldTags.process=TITLE,ELSE");
		indexingOptions.add("-j");
		indexingOptions.add("-Dblock.indexing=true");
		doTest(indexingOptions);
	}
	
	
	@Override
	protected void addDirectStructure(IndexOnDisk index) throws Exception {
		if (! index.hasIndexStructure("direct"))
		{
			new Inverted2DirectIndexBuilder(index).createDirectIndex();
		}
	}

	@Override
	protected int countNumberOfTopics(String filename) throws Exception {
		BufferedReader br = Files.openFileReader(filename);
		String line;
		int count = 0;
		while((line = br.readLine()) != null)
		{
			if (line.toLowerCase().contains("<top>"))
				count++;
		}
		br.close();
		return count;
	}

	@Override
	protected void makeCollectionSpec(PrintWriter p) throws Exception {
		LinkedList<String> corpusDir = new LinkedList<String>();
		corpusDir.add(corpusFolder);
		List<String> files = new ArrayList<String>();
		FileFind.findFiles(files, corpusDir);
		Collections.sort(files);
		assertTrue("No files found for corpus", files.size() > 0);
		boolean matched = false;
		for(String file : files)
		{
			if (file.contains("md5sums") || file.contains("inlinks"))
				continue;
			if (file.matches(".+(\\.gz|\\.GZ)$"))
			{
				matched = true;
				p.println(file);
			}
		}
		assertTrue("No files found for corpus", matched);
		p.close();
	}
	
}