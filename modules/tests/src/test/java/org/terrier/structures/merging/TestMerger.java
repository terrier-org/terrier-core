package org.terrier.structures.merging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;
import org.terrier.indexing.IndexTestUtils;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup;

public class TestMerger extends ApplicationSetupBasedTest {

	@Test public void test111() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		IndexOnDisk index1 = (IndexOnDisk) IndexTestUtils.makeIndex(new String[]{"doc1"}, new String[]{"this is a sentence"});
		IndexOnDisk index2 = (IndexOnDisk) IndexTestUtils.makeIndex(new String[]{"doc2"}, new String[]{"this is also a sentence"});
		IndexOnDisk index3 = (IndexOnDisk) IndexTestUtils.makeIndex(new String[]{"doc3"}, new String[]{"a third sentence"});
		
		IndexOnDisk merged1 = Index.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH, ""+ new Random().nextInt(100) );
		new StructureMerger(index1, index2, merged1).mergeStructures();
		
		IndexOnDisk merged2 = Index.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH, ""+ new Random().nextInt(100) );
		new StructureMerger(merged1, index3, merged2).mergeStructures();
		
		assertEquals(3, merged2.getCollectionStatistics().getNumberOfDocuments());
		assertTrue(merged2.hasIndexStructure("inverted"));
		assertTrue(merged2.hasIndexStructure("direct"));		
	}
	
	@Test public void test111_blocks() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		IndexOnDisk index1 = (IndexOnDisk) IndexTestUtils.makeIndexBlocks(new String[]{"doc1"}, new String[]{"this is a sentence"});
		IndexOnDisk index2 = (IndexOnDisk) IndexTestUtils.makeIndexBlocks(new String[]{"doc2"}, new String[]{"this is also a sentence"});
		IndexOnDisk index3 = (IndexOnDisk) IndexTestUtils.makeIndexBlocks(new String[]{"doc2"}, new String[]{"a third sentence"});
		
		checkTerm(index1, "sentence", 1, true, false);
		checkTerm(index2, "sentence", 1, true, false);
		checkTerm(index3, "sentence", 1, true, false);
		
		IndexOnDisk merged1 = Index.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH, ""+ new Random().nextInt(100) );
		new BlockStructureMerger(index1, index2, merged1).mergeStructures();
		
		checkTerm(merged1, "sentence", 2, true, false);
		
		IndexOnDisk merged2 = Index.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH, ""+ new Random().nextInt(100) );
		new BlockStructureMerger(merged1, index3, merged2).mergeStructures();
		
		assertEquals(3, merged2.getCollectionStatistics().getNumberOfDocuments());
		
		checkTerm(index2, "sentence", 1, true, false);
	}
	
	void checkTerm(Index i, String term, int df, boolean blocks, boolean fields) throws Exception
	{
		LexiconEntry le = i.getLexicon().getLexiconEntry("sentence");
		assertEquals(df, le.getDocumentFrequency());
		IterablePosting ip = i.getInvertedIndex().getPostings(le);
		assertEquals(blocks, ip instanceof BlockPosting);
		assertEquals(fields, ip instanceof FieldPosting);
		
	}
	
	@Test public void test11() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		IndexOnDisk index1 = (IndexOnDisk) IndexTestUtils.makeIndex(new String[]{"doc1"}, new String[]{"this is a sentence"});
		IndexOnDisk index2 = (IndexOnDisk) IndexTestUtils.makeIndex(new String[]{"doc2"}, new String[]{"this is also a sentence"});
		IndexOnDisk merged = Index.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH, ""+ new Random().nextInt(100) );
		new StructureMerger(index1, index2, merged).mergeStructures();
		assertEquals(2, merged.getCollectionStatistics().getNumberOfDocuments());
		assertTrue(merged.hasIndexStructure("inverted"));
		assertTrue(merged.hasIndexStructure("direct"));		
	}
	
	@Test public void test21_blocks() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		ApplicationSetup.setProperty("indexing.max.docs.per.builder", "1");
		IndexOnDisk index1 = (IndexOnDisk) IndexTestUtils.makeIndexBlocks(new String[]{"doc1", "doc1a"}, new String[]{"this is a sentence", "this is also a sentence"});
		checkTerm(index1, "sentence", 2, true, false);
		
		IndexOnDisk index2 = (IndexOnDisk) IndexTestUtils.makeIndexBlocks(new String[]{"doc2"}, new String[]{"a third sentence"});
		checkTerm(index2, "sentence", 1, true, false);
		
		IndexOnDisk merged = Index.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH, ""+ new Random().nextInt(100) );
		new BlockStructureMerger(index1, index2, merged).mergeStructures();

		assertEquals(3, merged.getCollectionStatistics().getNumberOfDocuments());
		checkTerm(merged, "sentence", 3, true, false);
		
	
	}
	
	@Test(expected=IllegalArgumentException.class) public void test10() throws Exception
	{
		ApplicationSetup.setProperty("termpipelines", "");
		IndexOnDisk index1 = (IndexOnDisk) IndexTestUtils.makeIndex(new String[]{"doc1"}, new String[]{"this is a sentence"});
		IndexOnDisk empty = Index.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH, ""+ new Random().nextInt(100) );		
		IndexOnDisk merged = Index.createNewIndex(ApplicationSetup.TERRIER_INDEX_PATH, ""+ new Random().nextInt(100) );
		new StructureMerger(index1, empty, merged).mergeStructures();
		assertEquals(1, merged.getCollectionStatistics().getNumberOfDocuments());
		assertTrue(merged.hasIndexStructure("inverted"));
		assertTrue(merged.hasIndexStructure("direct"));		
	}
	
}
