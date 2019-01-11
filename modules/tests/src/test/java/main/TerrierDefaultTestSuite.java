package main;

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
 * The Original Code is TerrierDefaultTestSuite.java
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.terrier.applications.TestCLITool;
import org.terrier.compression.bit.TestCompressedBitFiles;
import org.terrier.compression.bit.TestCompressedBitFilesDelta;
import org.terrier.compression.bit.TestCompressedBitFilesGolomb;
import org.terrier.evaluation.TestAdhocEvaluation;
import org.terrier.evaluation.TestTRECQrelsInMemory;
import org.terrier.fat.TestFatCandidateResultSet;
import org.terrier.fat.TestFatFeaturedScoringMatching;
import org.terrier.fat.TestFatFullMatching;
import org.terrier.fat.TestFatScoringMatching;
import org.terrier.fat.TestLinearModelMatching;
import org.terrier.indexing.TestCollectionFactory;
import org.terrier.indexing.TestCollections;
import org.terrier.indexing.TestCompressionConfig;
import org.terrier.indexing.TestCrawlDate;
import org.terrier.indexing.TestIndexers;
import org.terrier.indexing.TestSimpleFileCollection;
import org.terrier.indexing.TestSimpleXMLCollection;
import org.terrier.indexing.TestTRECCollection;
import org.terrier.indexing.TestTRECWebCollection;
import org.terrier.indexing.TestTaggedDocument;
import org.terrier.indexing.TestTweetJSONCollection;
import org.terrier.indexing.TestWARC10Collection;
import org.terrier.indexing.tokenisation.TestEnglishTokeniser;
import org.terrier.indexing.tokenisation.TestUTFTokeniser;
import org.terrier.matching.TestMatching.TestDAATFullMatching;
import org.terrier.matching.TestMatching.TestTAATFullMatching;
import org.terrier.matching.TestMatchingQueryTerms;
import org.terrier.matching.TestResultSets;
import org.terrier.matching.TestTRECResultsMatching;
import org.terrier.matching.matchops.TestMatchOpQLParser;
import org.terrier.matching.matchops.TestTRECQueryingMatchOpQL;
import org.terrier.matching.models.TestWeightingModelFactory;
import org.terrier.querying.TestDecorate;
import org.terrier.querying.TestManager;
import org.terrier.querying.TestSimpleDecorate;
import org.terrier.querying.parser.TestQueryParser;
import org.terrier.querying.summarisation.TestDefaultSummariser;
import org.terrier.rest.TestClientAndServer;
import org.terrier.statistics.TestGammaFunction.TestWikipediaLanczosGammaFunction;
import org.terrier.structures.TestBasicLexiconEntry;
import org.terrier.structures.TestBitIndexPointer;
import org.terrier.structures.TestCompressingMetaIndex;
import org.terrier.structures.TestIndexOnDisk;
import org.terrier.structures.TestIndexUtil;
import org.terrier.structures.TestTRECQuery;
import org.terrier.structures.bit.TestBitPostingIndex;
import org.terrier.structures.bit.TestBitPostingIndexInputStream;
import org.terrier.structures.bit.TestPostingStructures;
import org.terrier.structures.collections.TestFSArrayFile;
import org.terrier.structures.collections.TestFSOrderedMapFile;
import org.terrier.structures.indexing.TestIndexing;
import org.terrier.structures.indexing.TestIndexingFatalErrors;
import org.terrier.structures.indexing.singlepass.TestInverted2DirectIndexBuilder;
import org.terrier.structures.postings.TestFieldORIterablePosting;
import org.terrier.structures.postings.TestFieldOnlyIterablePosting;
import org.terrier.structures.postings.TestORIterablePosting;
import org.terrier.structures.postings.TestPhraseIterablePosting;
import org.terrier.structures.postings.TestProximityIterablePosting;
import org.terrier.structures.serialization.TestFixedSizeTextFactory;
import org.terrier.terms.TestPorterStemmer;
import org.terrier.terms.TestRemoveDiacritics;
import org.terrier.terms.TestSnowball;
import org.terrier.terms.TestTermPipelineAccessor;
import org.terrier.tests.ShakespeareEndToEndTestSuite;
import org.terrier.utility.TestArrayUtils;
import org.terrier.utility.TestCollectionStatistics;
import org.terrier.utility.TestDistance;
import org.terrier.utility.TestHeapSort;
import org.terrier.utility.TestMavenResolution;
import org.terrier.utility.TestRounding;
import org.terrier.utility.TestStaTools;
import org.terrier.utility.TestStringTools;
import org.terrier.utility.TestTagSet;
import org.terrier.utility.TestTermCodes;
import org.terrier.utility.TestUnitUtils;
import org.terrier.utility.TestVersion;
import org.terrier.utility.io.TestCountingInputStream;
import org.terrier.utility.io.TestRandomDataInputMemory;


/** This class defines the active JUnit test classes for Terrier
 * @since 3.0
 * @author Craig Macdonald */
@RunWith(Suite.class)
@SuiteClasses({
	//shakepseare based end-to-end tests are higher up, as otherwise they check if streams in other unit tests are open
	
	
	
	//.tests
	ShakespeareEndToEndTestSuite.class,
	
	//.applications
	TestCLITool.class,
	
	//.compression
	TestCompressedBitFiles.class,
	TestCompressedBitFilesDelta.class,
	TestCompressedBitFilesGolomb.class,
	
	
	//.evaluation
	TestAdhocEvaluation.class,
	TestTRECQrelsInMemory.class,
	
	//.fat
	TestFatCandidateResultSet.class,
	TestFatFeaturedScoringMatching.class,
	TestFatFullMatching.class,
	TestFatScoringMatching.class,
	TestLinearModelMatching.class,
	
	//.indexing
	TestCollections.class,
	TestCollectionFactory.class,
	TestCompressionConfig.class,
	TestCrawlDate.class,
	TestIndexers.class,
	TestSimpleFileCollection.class,
	TestTaggedDocument.class,
	TestSimpleXMLCollection.class,
	TestTRECCollection.class,
	TestTRECWebCollection.class,
	TestTweetJSONCollection.class,
	TestWARC10Collection.class,
	
	//.indexing.tokenisation
	TestEnglishTokeniser.class,
	TestUTFTokeniser.class,
	
	
	
	//.matching
	TestMatchingQueryTerms.class,
	TestDAATFullMatching.class,
	TestTAATFullMatching.class,
	TestTRECResultsMatching.class,
	TestResultSets.class,
	
	//matching.matchops
	TestTRECQueryingMatchOpQL.class,
	TestMatchOpQLParser.class,
	
	//matching.models
	TestWeightingModelFactory.class,
	
	//querying
	TestManager.class,
	TestSimpleDecorate.class,
	TestDecorate.class,
	
	//querying.parser
	TestQueryParser.class,
	
	//querying.summarisation
	TestDefaultSummariser.class,
	
	//rest
	TestClientAndServer.class,
	
	//.statistics
	TestWikipediaLanczosGammaFunction.class,
	
	//.structures
	TestBasicLexiconEntry.class,
	TestBitIndexPointer.class,
	TestBitPostingIndex.class,
	TestBitPostingIndexInputStream.class,
	TestCompressingMetaIndex.class,
	TestPostingStructures.class,
	TestIndexUtil.class,
	TestTRECQuery.class,
	TestIndexOnDisk.class,
	
	//.structures.collections
	TestFSOrderedMapFile.class,
	TestFSArrayFile.class,
	
	//.structures.indexing
	TestIndexing.class,
	TestIndexingFatalErrors.class,
	
	//.structures.indexing.sp.hadoop
	TestInverted2DirectIndexBuilder.class,
	
	//.structures.indexing.sp.hadoop
//	TestBitPostingIndexInputFormat.class,
//	TestSplitEmittedTerm.class,
//	TestPositingAwareSplit.class,
//	
	//structures.postings
	TestFieldOnlyIterablePosting.class,
	TestORIterablePosting.class,
	TestFieldORIterablePosting.class,
	TestPhraseIterablePosting.class,
	TestProximityIterablePosting.class,
	
	//.structures.serialization
	TestFixedSizeTextFactory.class,
	
	//.terms
	TestTermPipelineAccessor.class,
	TestPorterStemmer.class,
	TestSnowball.class,
        TestRemoveDiacritics.class,
	
	//.utility
	TestArrayUtils.class,
	TestCollectionStatistics.class,
	TestDistance.class,
	TestHeapSort.class,
	TestMavenResolution.class,
	TestRounding.class,
	TestTagSet.class,
	TestStaTools.class,
	TestStringTools.class,
	TestTermCodes.class,
	TestUnitUtils.class,
	TestVersion.class,
	//TestTimer.class,
	
	//utility.io
	TestRandomDataInputMemory.class,
	TestCountingInputStream.class,
	
	
})
public class TerrierDefaultTestSuite {}
