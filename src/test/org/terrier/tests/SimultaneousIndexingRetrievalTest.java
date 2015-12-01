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
 * The Original Code is TestUtils.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import org.terrier.indexing.FileDocument;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.matching.ResultSet;
import org.terrier.querying.Manager;
import org.terrier.querying.SearchRequest;
import org.terrier.realtime.UpdatableIndex;
import org.terrier.realtime.incremental.IncrementalIndex;
import org.terrier.realtime.memory.MemoryIndex;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.MetaIndex;
import org.terrier.utility.ApplicationSetup;

public class SimultaneousIndexingRetrievalTest extends ApplicationSetupBasedTest {
	
	@Test
	public void multiThreadIndexingTest() throws Exception {
		
		UncaughtIRException threadsStatusException = new  UncaughtIRException();
		Thread.setDefaultUncaughtExceptionHandler(threadsStatusException);
		
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "docno");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "140");
		
		//-------------------------------------
		// First Check the Memory only index 
		//-------------------------------------
		
		MemoryIndex memIndex = new MemoryIndex();
		
		// generates 4 threads that generate documents to index
		Thread indexing1 = new Thread(new DocumentGenerator(1000, memIndex));
		Thread indexing2 = new Thread(new DocumentGenerator(1000, memIndex));
		Thread indexing3 = new Thread(new DocumentGenerator(1000, memIndex));
		Thread indexing4 = new Thread(new DocumentGenerator(1000, memIndex));
		indexing1.start();
		indexing2.start();
		indexing3.start();
		indexing4.start();
		
		indexing1.join();
		indexing2.join();
		indexing3.join();
		indexing4.join();
		
		assertTrue(!threadsStatusException.isThrown());
		
		//-------------------------------------
		// Second check the Incremental index 
		//-------------------------------------
		
		// flush partial indexes to disk every 1000 documents
		ApplicationSetup.setProperty("incremental.flushdocs", "1000");
		ApplicationSetup.setProperty("incremental.flush", "flushdocs");
		
		IncrementalIndex inc = IncrementalIndex.get(terrier_etc, "test");
		
		// generates 4 threads that generate documents to index
		Thread indexing5 = new Thread(new DocumentGenerator(2000, inc));
		Thread indexing6 = new Thread(new DocumentGenerator(2000, inc));
		Thread indexing7 = new Thread(new DocumentGenerator(2000, inc));
		Thread indexing8 = new Thread(new DocumentGenerator(2000, inc));
		indexing5.start();
		indexing6.start();
		indexing7.start();
		indexing8.start();
				
		indexing5.join();
		indexing6.join();
		indexing7.join();
		indexing8.join();
		
		assertTrue(!threadsStatusException.isThrown());
		
	}
	
	@Test
	public void multiThreadRetrievalTest() throws Exception {
		
		UncaughtIRException threadsStatusException = new  UncaughtIRException();
		Thread.setDefaultUncaughtExceptionHandler(threadsStatusException);
		
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "docno");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "140");
		
		//-------------------------------------
		// First Check the Memory only index 
		//-------------------------------------
		
		// add some random documents to the index
		MemoryIndex memIndex = new MemoryIndex();
		Thread indexing1 = new Thread(new DocumentGenerator(5000, memIndex));
		indexing1.start();
		indexing1.join();
		
		Thread querying1 = new Thread(new QueryGenerator(100, memIndex));
		Thread querying2 = new Thread(new QueryGenerator(100, memIndex));
		Thread querying3 = new Thread(new QueryGenerator(100, memIndex));
		Thread querying4 = new Thread(new QueryGenerator(100, memIndex));
		querying1.start();
		querying2.start();
		querying3.start();
		querying4.start();
		
		querying1.join();
		querying2.join();
		querying3.join();
		querying4.join();
		
		assertTrue(!threadsStatusException.isThrown());
		
		//-------------------------------------
		// Second check the Incremental index 
		//-------------------------------------
				
		// flush partial indexes to disk every 1000 documents
		ApplicationSetup.setProperty("incremental.flushdocs", "1000");
		ApplicationSetup.setProperty("incremental.flush", "flushdocs");
				
		IncrementalIndex inc = IncrementalIndex.get(terrier_etc, "test");
		Thread indexing2 = new Thread(new DocumentGenerator(5000, inc));
		indexing2.start();
		indexing2.join();
		
		Thread querying5 = new Thread(new QueryGenerator(100, inc));
		Thread querying6 = new Thread(new QueryGenerator(100, inc));
		Thread querying7 = new Thread(new QueryGenerator(100, inc));
		Thread querying8 = new Thread(new QueryGenerator(100, inc));
		querying5.start();
		querying6.start();
		querying7.start();
		querying8.start();
		
		querying5.join();
		querying6.join();
		querying7.join();
		querying8.join();
		
		assertTrue(!threadsStatusException.isThrown());
		
	}
	
	@Test
	public void multiThreadBothTest() throws Exception {
		
		UncaughtIRException threadsStatusException = new  UncaughtIRException();
		Thread.setDefaultUncaughtExceptionHandler(threadsStatusException);
		
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "docno");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "140");
		
		//-------------------------------------
		// First Check the Memory only index 
		//-------------------------------------
		
		MemoryIndex memIndex = new MemoryIndex();
		Thread indexingS = new Thread(new DocumentGenerator(1, memIndex));
		indexingS.start();
		indexingS.join();
		
		Thread indexing1 = new Thread(new DocumentGenerator(2000, memIndex));
		Thread indexing2 = new Thread(new DocumentGenerator(2000, memIndex));
		Thread querying1 = new Thread(new QueryGenerator(100, memIndex));
		Thread querying2 = new Thread(new QueryGenerator(100, memIndex));
		
		indexing1.start();
		indexing2.start();
		querying1.start();
		querying2.start();
		
		indexing1.join();
		indexing2.join();
		querying1.join();
		querying2.join();
		
		assertTrue(!threadsStatusException.isThrown());
		
		//-------------------------------------
		// Second check the Incremental index 
		//-------------------------------------
						
		// flush partial indexes to disk every 1000 documents
		ApplicationSetup.setProperty("incremental.flushdocs", "1000");
		ApplicationSetup.setProperty("incremental.flush", "flushdocs");
						
		IncrementalIndex inc = IncrementalIndex.get(terrier_etc, "test");
		Thread indexingS2 = new Thread(new DocumentGenerator(1500, inc));
		indexingS2.start();
		indexingS2.join();
		
		Thread indexing3 = new Thread(new DocumentGenerator(1000, inc));
		Thread indexing4 = new Thread(new DocumentGenerator(1000, inc));
		Thread querying3 = new Thread(new QueryGenerator(300, inc));
		Thread querying4 = new Thread(new QueryGenerator(300, inc));
		
		indexing3.start();
		indexing4.start();
		querying3.start();
		querying4.start();
		
		indexing3.join();
		indexing4.join();
		querying3.join();
		querying4.join();
		
		assertTrue(!threadsStatusException.isThrown());
	}
	
	
	public class UncaughtIRException implements java.lang.Thread.UncaughtExceptionHandler {

		boolean thrown = false;
		
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			System.err.println("Failed on information retrieval exception - likely that an non-thread-safe method has been called concurrently with indexing.");
			e.printStackTrace();
			thrown=true;
			((HaltableThread)t).haltASAP();
			
		}

		public boolean isThrown() {
			return thrown;
		}
		
	}
	
	
	public interface HaltableThread {
		
		/**
		 * Try and halt this thread as soon as possible
		 */
		public void haltASAP();
		
	}
	
	public class QueryGenerator implements Runnable, HaltableThread {

		int numQueries;
		UpdatableIndex index;
		boolean halt = false;
		
		
		/**
		 * Query generator constructor
		 * @param numQueries - how many queries to run
		 * @param index - the index to retrieve from
		 */
		public QueryGenerator(int numQueries, UpdatableIndex index) {
			super();
			this.numQueries = numQueries;
			this.index = index;
		}

		@Override
		public void run() {
			Random generator=new Random();
			
			for (int qid =0; qid<numQueries; qid++) {
				
				StringBuilder query = new StringBuilder();
				
				int queryLength = generator.nextInt(4)+1;
				
				for (int qt=0; qt<queryLength; qt++) {
					// either generate a random term or select one from the lexicon
					boolean randomTerm = generator.nextBoolean();
					if (randomTerm) {
						// generate a random term
						int wordLength = generator.nextInt(10)+1;
						
						for (int i=0;i<wordLength;i++) {
					        int randomNum=((char) (generator.nextInt(26) + 'a'));
					        query.append((char)randomNum);

					    }
						query.append(" ");
					} else {
						if (halt) return;
						// select a term from the lexicon
						int numTerms = ((Index)index).getCollectionStatistics().getNumberOfUniqueTerms();
						Lexicon<String> lex = ((Index)index).getLexicon();
						
						// do a MetaIndex get here as well to check that
						MetaIndex meta = ((Index)index).getMetaIndex();
						try {
							meta.getItem("docno", 0);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						// get a lexicon entry to check that
						String term = lex.getIthLexiconEntry(generator.nextInt(numTerms)).getKey();
						query.append(term+" ");	
					}
					
					
				}
				if (halt) return;
				retrieve(query.toString());
				
				
			}
			
		}
		
		public ResultSet retrieve(String query) {
			StringBuffer sb = new StringBuffer();
			
			sb.append(query);
			
			Manager queryingManager = new Manager((Index)index);

			SearchRequest srq = queryingManager.newSearchRequest("query", sb.toString());
			srq.addMatchingModel("Matching","DirichletLM");
			srq.setOriginalQuery(sb.toString());
			
			queryingManager.runPreProcessing(srq);
			queryingManager.runMatching(srq);
			queryingManager.runPostProcessing(srq);
			queryingManager.runPostFilters(srq);
			return srq.getResultSet();
		}

		@Override
		public void haltASAP() {
			halt=true;
			
		}
		
	}
	
	
	/**
	 * A thread instance that generates a series or random documents
	 * @author Richard McCreadie
	 *
	 */
	public class DocumentGenerator implements Runnable, HaltableThread {

		int numDocuments;
		UpdatableIndex index;
		boolean halt = false;
		Tokeniser tokeniser;
		
		/**
		 * Create a new document generator
		 * @param numDocuments - how many documents to create
		 * @param index - how many documents to add to the index
		 */
		public DocumentGenerator(int numDocuments, UpdatableIndex index) {
			super();
			this.numDocuments = numDocuments;
			this.index =index;
			tokeniser = Tokeniser.getTokeniser();
		}

		@Override
		public void run() {
			Random generator=new Random();
			
			for (int docid =0; docid<numDocuments; docid++) {
				if (halt) return;
				StringBuilder document = new StringBuilder();
				
				int documentLength = generator.nextInt(1000)+1;
				
				for (int wordid =0; wordid<documentLength; wordid++) {
					int wordLength = generator.nextInt(10)+1;
					
					for (int i=0;i<wordLength;i++) {
				        int randomNum=((char) (generator.nextInt(26) + 'a'));
				        document.append((char)randomNum);

				    }
					document.append(" ");
				}
				Map<String,String> props = new HashMap<String,String>();
				props.put("docno", this.getClass().toString()+"-doc"+docid);
				Reader documentReader = new StringReader(document.toString().trim());
				FileDocument terrierDocument = new FileDocument(documentReader, props, tokeniser);
				try {
					if (halt) return;
					index.indexDocument(terrierDocument);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}

		@Override
		public void haltASAP() {
			halt=true;
			
		}
		
		
		
		
	}
	
}
