package org.terrier.utility;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.terrier.matching.dsms.DocumentScoreModifier;

public class TestClassNameParser {

	List<DocumentScoreModifier> get(String names) {
		List<DocumentScoreModifier> rtr = null;
		try{
			rtr = new ClassNameParser<DocumentScoreModifier>(
					names, DocumentScoreModifier.class.getPackage().getName(), DocumentScoreModifier.class)
					.parseAll();
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
		return rtr;
	}
	
	@Test
	public void test() {
		assertEquals(1, get("DFRDependenceScoreModifier").size());
		assertEquals(2, get("MRFDependenceScoreModifier,DFRDependenceScoreModifier").size());		
	}

}