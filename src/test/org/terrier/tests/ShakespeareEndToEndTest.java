/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is ShakespeareEndToEndTest.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.terrier.structures.BasicLexiconEntry;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.CompressingMetaIndex;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.FieldDocumentIndexEntry;
import org.terrier.structures.FieldLexiconEntry;
import org.terrier.structures.Index;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.PostingIndexInputStream;
import org.terrier.structures.CompressingMetaIndex.InputStream;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.PostingUtil;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.FieldScore;
import org.terrier.utility.StaTools;

public abstract class ShakespeareEndToEndTest extends BatchEndToEndTest 
{
	private static final int NUMBER_UNIQUE_TERMS = 2357;
	private static final int NUMBER_POINTERS = 5510;
	protected static final String[] CHECK_TERMS_NOT_EXIST = new String[]{
		"0", "1", "macdonald", "docno"
	};
	protected static final String[] CHECK_TERMS = new String[]{
		//these words are unaffected by Porter stemmer
		"shylock",
		"duke"
	};
	protected static final int[] CHECK_TERMS_DFS = new int[]{
		9,
		6
	};
	
	protected static final int[] DOCUMENT_UNIQUE_TERMS = new int[]{
		51,
		416,
		327,
		363,
		163,
		350,
		73,
		103,
		149,
		176,
		211,
		139,
		251,
		0,
		279,
		635,
		105,
		218,
		185,
		722,
		64,
		530
	};
	
	protected static final int[] DOCUMENT_LENGTHS = new int[]{
		87,  //0
		686, //1
		492, //2
		668, //3
		195, //4
		867, //5
		92,  //6
		166, //7
		223, //8
		278, //9
		313, //10
		205, //11
		389, //12
		0,   //13
		472, //14
		1180,//15
		157, //16
		315, //17
		331, //18
		1739,//19
		96,  //20
		1178 //21
	};
	
		
	public static final String[] DOCUMENT_NAMES = new String[]{
		"SHK-MOV-0-0",    //0
		"SHK-MOV-I-I",    //1
		"SHK-MOV-I-II",   //2
		"SHK-MOV-I-III",  //3
		"SHK-MOV-II-I",   //4
		"SHK-MOV-II-II",  //5
		"SHK-MOV-II-III", //6
		"SHK-MOV-II-IV",  //7
		"SHK-MOV-II-V",   //8
		"SHK-MOV-II-VI",  //9
		"SHK-MOV-II-VII", //10
		"SHK-MOV-II-VIII",//11
		"SHK-MOV-II-IX",  //12
		"SHK-MOV-interval", //13
		"SHK-MOV-III-I",  //14
		"SHK-MOV-III-II", //15
		"SHK-MOV-III-III",//16
		"SHK-MOV-III-IV", //17
		"SHK-MOV-III-V",  //18
		"SHK-MOV-IV-I",   //19
		"SHK-MOV-IV-II",  //20
		"SHK-MOV-V-I"	  //21
	};
	
	
	static TIntObjectHashMap<TObjectIntHashMap<String>> doc2term2freqs = new TIntObjectHashMap<TObjectIntHashMap<String>>();
	static
	{
		TObjectIntHashMap<String> termsInDoc0 = new TObjectIntHashMap<String>();
		termsInDoc0.put("nerissa", 1);
		termsInDoc0.put("balthasar", 1);
		termsInDoc0.put("stephano", 1);
		termsInDoc0.put("clown", 1);
			
		doc2term2freqs.put(0, termsInDoc0);		
	}
	
	
	@SuppressWarnings({ "unchecked", "resource" })
	public void checkMetaIndex(Index index, String[] docnos) throws Exception {
		int docid = -1;
		//check as a stream
		Iterator<String[]> iMi = (Iterator<String[]>) index.getIndexStructureInputStream("meta");
		//not a close problem, because its the same object
		CompressingMetaIndex.InputStream cmiis = (InputStream) iMi;
		assertNotNull("Failed to get a meta input stream", iMi);
		while(iMi.hasNext())
		{
			docid++;
			final String[] names = iMi.next();
			assertEquals("Docnos for document "+ docid + " dont match", docnos[docid], names[0]);
			assertEquals("Docid was not correct", docid, cmiis.getIndex());
		}
		//check docid is as large as expected
		assertEquals("Metaindex as stream didnt have expected number of entries", docnos.length -1, docid);
		IndexUtil.close(iMi);
		
		//check random access
		MetaIndex mi = index.getMetaIndex();
		assertNotNull("Failed to get a metaindex", mi);
		final int numberOfDocuments = index.getCollectionStatistics().getNumberOfDocuments();
		for(docid = 0; docid < numberOfDocuments; docid++)
		{
			assertEquals("Normal lookup: Document name for document "+ docid + " was not correct", docnos[docid], mi.getItem("docno", docid));
			assertEquals("Reverse lookup: Document id not correct for docno "+ docnos[docid], docid, mi.getDocument("docno", docnos[docid]));
		}
		
		
		//check methods that take more than one docid at once
		int[] docids = new int[docnos.length];
		for(int i=0;i< docids.length;i++)
			docids[i] = docids.length -i -1;
		
		final String[] retrdocnos = mi.getItems("docno", docids);
		assertEquals(docids.length -1, docids[0]);
		assertEquals(retrdocnos.length, docnos.length);
		for(int i=0; i< docids.length; i++)
			assertEquals(docnos[docids[i]], retrdocnos[i]);
		
		docids = new int[docnos.length];
		for(int i=0;i< docids.length;i++)
			docids[i] = docids.length -i -1;
		
		final String[][] retrdocnosA = mi.getItems(new String[]{"docno"}, docids);
		assertEquals(retrdocnosA.length, docnos.length);
		assertEquals(docids.length -1, docids[0]);
		for(int i=0; i< docids.length; i++)
			assertEquals(docnos[docids[i]], retrdocnosA[i][0]);
		
		
		//finished with meta index
		mi = null;
	}
	
	public void checkInvertedIndexStream(Index index, int[] documentLengths) throws Exception
	{
		final int numDocs = index.getCollectionStatistics().getNumberOfDocuments();
		TIntIntHashMap calculatedDocLengths = new TIntIntHashMap();
		PostingIndexInputStream iiis = (PostingIndexInputStream) index.getIndexStructureInputStream("inverted");
		assertNotNull(iiis);
		int ithTerm = -1;
		while(iiis.hasNext())
		{
			ithTerm++;
			final IterablePosting ip = iiis.getNextPostings();
			int count = 0;
			final int expected = iiis.getNumberOfCurrentPostings();
			while(ip.next() != IterablePosting.EOL)
			{
				//System.err.println("Got id " + ip.getId());
				assertTrue("Got too big a docid ("+ip.getId()+") from inverted index input stream for term at index " + ithTerm, ip.getId() < numDocs);
				assertEquals(documentLengths[ip.getId()], ip.getDocumentLength());
				count++;
				calculatedDocLengths.adjustOrPutValue(ip.getId(), ip.getFrequency(), ip.getFrequency());
			}
			assertEquals(expected, count);
		}
		iiis.close();
		assertEquals("Number of documents is unexpected,", documentLengths.length - countZero(documentLengths), calculatedDocLengths.size());
		long tokens = 0;
		for(int docid : calculatedDocLengths.keys())
		{
			assertEquals("Document length for docid "+docid+" is unexpected,", documentLengths[docid], calculatedDocLengths.get(docid));
			tokens += calculatedDocLengths.get(docid);
		}
		assertEquals("Number of tokens is unexpected,", StaTools.sum(documentLengths), tokens);
	}
	
	private static int countZero(int[] in)
	{
		int count = 0;
		for(int i : in)
			if (i == 0)
				count++;
		return count;
	}
	
	@SuppressWarnings("unchecked")
	public void checkLexicon(Index index) throws Exception
	{
		//check as random access
		Lexicon<String> lex = index.getLexicon();
		assertNotNull(lex);
		for(String notFoundTerm : CHECK_TERMS_NOT_EXIST)
		{
			assertNull("Found entry for term " + notFoundTerm + " that we shouldn't have!", lex.getLexiconEntry(notFoundTerm));
		}
		int i=-1;
		
		TObjectIntHashMap<String> checkFreqs = new TObjectIntHashMap<String>(CHECK_TERMS.length);
		for(String foundTerm: CHECK_TERMS)
		{
			i++;
			LexiconEntry le = lex.getLexiconEntry(foundTerm);
			assertNotNull("Didnt find lexicon entry for term " + foundTerm, le);
			assertEquals("Dcoument frequency incorrect for term " + foundTerm, CHECK_TERMS_DFS[i], le.getDocumentFrequency());
			
			//check lookup by termid 
			Map.Entry<String, LexiconEntry> lee2 = lex.getLexiconEntry(le.getTermId());
			assertNotNull(lee2);
			assertEquals(foundTerm, lee2.getKey());
			assertEquals(CHECK_TERMS_DFS[i], lee2.getValue().getDocumentFrequency());
			
			//make a note of this term for the stream checking
			checkFreqs.put(foundTerm, CHECK_TERMS_DFS[i]);
		}
		
		//check as stream
		TIntHashSet termIds = new TIntHashSet();
		Iterator<Map.Entry<String, LexiconEntry>> lexIn = (Iterator<Entry<String, LexiconEntry>>) index.getIndexStructureInputStream("lexicon");
		int count = 0;
		while(lexIn.hasNext())
		{
			Map.Entry<String, LexiconEntry> lee = lexIn.next();
			assertNotNull(lee);
			assertNotNull(lee.getKey());
			assertTrue(lee.getKey().length() > 1);
			assertNotNull(lee.getValue());
			if (checkFreqs.containsKey(lee.getKey()))
			{
				assertEquals(checkFreqs.get(lee.getKey()), lee.getValue().getDocumentFrequency());
				checkFreqs.remove(lee.getKey());
			}
			termIds.add(lee.getValue().getTermId());
			count++;
		}
		assertEquals(NUMBER_UNIQUE_TERMS, count);
		assertEquals(NUMBER_UNIQUE_TERMS, termIds.size());
		assertEquals(0, StaTools.min(termIds.toArray()));
		assertEquals(NUMBER_UNIQUE_TERMS-1, StaTools.max(termIds.toArray()));
		assertTrue("Not all terms found in lexicon as stream", checkFreqs.size() == 0);
		IndexUtil.close(lexIn);
	}
	
	
	public void checkDirectIndex(Index index, int maxTermId, int numberOfTerms, int documentLengths[], int[] documentPointers) throws Exception {
		checkDirectIndex(index, maxTermId, numberOfTerms, documentLengths,
				documentPointers, true);
	}

	@SuppressWarnings("unchecked")
	public void checkDirectIndex(Index index, int maxTermId, int numberOfTerms, int documentLengths[], int[] documentPointers, boolean checkContents) throws Exception {
		boolean D_DEBUG = false;
		
		TIntHashSet termIds = new TIntHashSet();
		
		long tokens = 0;
		long pointers = 0;
		int docid = 0;
		
		/*for (Object o : index.getProperties().keySet()) {
			System.err.println(((String)o+"="+index.getProperties().getProperty((String)o)));
		}*/
		
		final PostingIndexInputStream piis = (PostingIndexInputStream) index.getIndexStructureInputStream("direct");
		assertNotNull("No direct index input stream found", piis);
		while(piis.hasNext())
		{
			IterablePosting ip = piis.next();
			int doclen = 0;	int docpointers = 0;		
			docid += piis.getEntriesSkipped();
			if (D_DEBUG) System.err.println("getEntriesSkipped=" + piis.getEntriesSkipped());
			if (D_DEBUG) System.err.println("docid=" + docid);
			//we shouldnt be in the postings for an empty document
			assert documentLengths[docid] > 0;
			while(ip.next() != IterablePosting.EOL)
			{
				if (D_DEBUG) System.err.println("termid" +ip.getId() + " f=" + ip.getFrequency()+" dlength="+ip.getDocumentLength()+" docid="+docid);
				termIds.add(ip.getId());
				tokens += ip.getFrequency();
				doclen += ip.getFrequency();
				pointers++; docpointers++;
				assertEquals("Document length for docid = "+docid+" on termid "+ip.getId()+" is wrong",documentLengths[docid], ip.getDocumentLength());
				if (numberOfTerms > 0)
					assertTrue("Got too big a termid ("+ip.getId()+") from direct index input stream, numTerms=" + numberOfTerms, ip.getId() < maxTermId);
				
			}
			if (documentPointers.length > 0)
				assertEquals("Numebr of pointers for docid " + docid + " is incorrect", documentPointers[docid], docpointers);
			assertEquals("Document length for docid "+docid+" is incorrect", documentLengths[docid], doclen);
			docid++;
		}
		piis.close();
		CollectionStatistics cs = index.getCollectionStatistics();
		assertEquals("Number of documents is incorrect", cs.getNumberOfDocuments(), docid);
		assertEquals("Number of pointers is incorrect", cs.getNumberOfPointers(), pointers);
		assertEquals("Number of tokens is incorrect", cs.getNumberOfTokens(), tokens);
		if (numberOfTerms > 0)
		{
			assertEquals("Not all termIds found in direct index", termIds.size(), numberOfTerms);
		}
		
		
		//now check the direct index for specific terms we know it should contain
		if (checkContents)
		{
			Lexicon<String> lex = index.getLexicon();
			PostingIndex<Pointer> direct = (PostingIndex<Pointer>) index.getDirectIndex();
			DocumentIndex doi = index.getDocumentIndex();
			for(int docid2 : doc2term2freqs.keys())
			{
				Pointer p = doi.getDocumentEntry(docid2);
				TObjectIntHashMap<String> terms = doc2term2freqs.get(docid2);
				
				assertTrue(p.getNumberOfEntries() > 0);
				IterablePosting ip = direct.getPostings(p);
				
				int[][] postings = PostingUtil.getAllPostings(ip);
				
				for(Object o : terms.keys())
				{
					String term = (String) o;
					LexiconEntry le = lex.getLexiconEntry(term);
					assertNotNull("LexiconEntry for term " + term + " not found", le);
					int termid = le.getTermId();
					int find = Arrays.binarySearch(postings[0], termid);
					assertTrue("term " + term + " not found in document "+ docid2, find >= 0);
					assertEquals("term " + term + " had incorrect frequency in document "+ docid2, terms.get(term), postings[1][find]);
				}
				
				ip = direct.getPostings(p);
				while(ip.next() != IterablePosting.EOL)
				{
					if (D_DEBUG) System.err.println("termid" +ip.getId() + " f=" + ip.getFrequency());
					termIds.add(ip.getId());
					assertEquals(DOCUMENT_LENGTHS[docid2], ip.getDocumentLength());
					if (numberOfTerms > 0)
						assertTrue("Got too big a termid ("+ip.getId()+") from direct index input stream, numTerms=" + numberOfTerms, ip.getId() < maxTermId);
				}
			}
			
		}
			
		
	}
	
	@SuppressWarnings("unchecked")
	public void checkDocumentLengths(Index index, int[] lengths, int[] document_unique_terms) throws Exception {
		int docid = -1;
		//check index as stream
		Iterator<DocumentIndexEntry> iDie = (Iterator<DocumentIndexEntry>) index.getIndexStructureInputStream("document");
		assertNotNull("Failed to get a document inputstream", iDie);
		while(iDie.hasNext())
		{
			DocumentIndexEntry die = iDie.next();
			docid++;
			//System.out.println(die.getDocumentLength());
			assertEquals("Document lengths for docid "+ docid + " dont match", lengths[docid], die.getDocumentLength());
		}
		//check docid is as large as expected
		assertEquals("Metaindex as stream didnt have expected number of entries", lengths.length -1, docid);
		IndexUtil.close(iDie);
		
		//check index in random access
		DocumentIndex di = index.getDocumentIndex();
		assertNotNull("Failed to get a document index", di);
		final int numberOfDocuments = index.getCollectionStatistics().getNumberOfDocuments();
		for(docid =0; docid < numberOfDocuments; docid++)
		{
			assertEquals("Document lengths for docid "+ docid + " dont match", lengths[docid], di.getDocumentLength(docid));
			assertEquals("Document lengths for docid "+ docid + " dont match", lengths[docid], di.getDocumentEntry(docid).getDocumentLength());
			if (document_unique_terms.length > 0)
				assertEquals("Number of pointers for docid " + docid + " dont match", document_unique_terms[docid], di.getDocumentEntry(docid).getNumberOfEntries());
		}
		
		di = null;
	}
	
	protected void checkCollectionStatistics(Index index)
	{
		final CollectionStatistics cs = index.getCollectionStatistics();
		assertEquals("Number of documents doesn't match", DOCUMENT_LENGTHS.length, cs.getNumberOfDocuments());
		assertEquals("Number of tokens doesn't match", StaTools.sum(DOCUMENT_LENGTHS), cs.getNumberOfTokens());
		assertEquals("Average document length doesn't match", StaTools.mean(DOCUMENT_LENGTHS), cs.getAverageDocumentLength(), 0.0d);
		assertEquals("Number of pointers doesnt match", NUMBER_POINTERS, cs.getNumberOfPointers());
		assertEquals("Number of unique terms doesn't match", NUMBER_UNIQUE_TERMS, cs.getNumberOfUniqueTerms());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void checkIndex() throws Exception
	{	
		Index index = Index.createIndex();
		assertNotNull("Failed to get an index", index);
		final String[] expectedStructures = new String[]{
			"inverted", "lexicon", "meta", "document", "document-factory", "lexicon-keyfactory", "lexicon-valuefactory", "direct"
		};
		final String[] expectedStructuresInputStream = new String[]{
				"inverted", "lexicon", "meta", "document", "direct"
		};
		        
		for (String structureName : expectedStructures )
			assertTrue("Index has no "+ structureName + " structure", index.hasIndexStructure(structureName));
		for (String structureName : expectedStructuresInputStream )
			assertTrue("Index has no "+ structureName + " inputstream structure", index.hasIndexStructure(structureName));
		
		checkDocumentLengths(index, DOCUMENT_LENGTHS, DOCUMENT_UNIQUE_TERMS);
		checkMetaIndex(index, DOCUMENT_NAMES);
		checkLexicon(index);
		checkInvertedIndexStream(index, DOCUMENT_LENGTHS);
		checkDirectIndex(index, 
				index.getCollectionStatistics().getNumberOfUniqueTerms(), 
				index.getCollectionStatistics().getNumberOfUniqueTerms(), 
				DOCUMENT_LENGTHS,
				DOCUMENT_UNIQUE_TERMS, true);
		checkCollectionStatistics(index);
		if (FieldScore.FIELDS_COUNT > 0)
		{
			assertTrue("LexiconEntry is not of type FieldLexiconEntry", ((FixedSizeWriteableFactory<LexiconEntry>)index.getIndexStructure("lexicon-valuefactory")).newInstance()
					instanceof FieldLexiconEntry);
			assertTrue("DocumentIndexEntry is not of type FieldDocumentIndexEntry", ((FixedSizeWriteableFactory<DocumentIndexEntry>)index.getIndexStructure("document-factory")).newInstance()
					instanceof FieldDocumentIndexEntry);
		}
		else
		{
			assertTrue("LexiconEntry is not of type BasicLexiconEntry", ((FixedSizeWriteableFactory<LexiconEntry>)index.getIndexStructure("lexicon-valuefactory")).newInstance()
					instanceof BasicLexiconEntry);
		}
		index.close();
		super.checkIndex();
	}
	
	

	@Override
	protected void makeCollectionSpec(PrintWriter p) throws Exception {
		p.println(System.getProperty("user.dir") + "/share/tests/shakespeare/shakespeare-merchant.trec.1");
		p.println(System.getProperty("user.dir") + "/share/tests/shakespeare/shakespeare-merchant.trec.2");
		p.close();
	}

	@Override
	protected void addGlobalTerrierProperties(Properties p) throws Exception {
		super.addGlobalTerrierProperties(p);
		p.setProperty("trec.topics.parser","SingleLineTRECQuery");
		p.setProperty("ignore.low.idf.terms","false");
	}
	
	@Override
	protected int countNumberOfTopics(String filename) throws Exception
	{
		//TODO: add a line count cache to reduce disk IO
		int count = 0;
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line = null;
		while( (line = br.readLine()) != null)
		{
			if (line.trim().length() > 0)
				count++;
		}
		br.close();
		return count;
	}
}
