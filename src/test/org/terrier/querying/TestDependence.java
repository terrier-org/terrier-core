package org.terrier.querying;

import static org.junit.Assert.*;

import org.junit.Test;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.indriql.SingleQueryTerm;
import org.terrier.querying.parser.Query.QTPBuilder;

public class TestDependence {

	@Test public void testOne()
	{ 
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.add(QTPBuilder.of(new SingleQueryTerm("a")).build());
		new DependenceModelPreProcess().process(mqt, "pBiL");
		System.out.println(mqt.toString());
		assertEquals(1, mqt.size());
	}
	
	@Test public void testTwo()
	{ 
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.add(QTPBuilder.of(new SingleQueryTerm("a")).build());
		mqt.add(QTPBuilder.of(new SingleQueryTerm("b")).build());
		new DependenceModelPreProcess().process(mqt, "pBiL");
		System.out.println(mqt.toString());
		assertEquals(5, mqt.size());
		
	}
	
	@Test public void testThree()
	{ 
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.add(QTPBuilder.of(new SingleQueryTerm("a")).build());
		mqt.add(QTPBuilder.of(new SingleQueryTerm("b")).build());
		mqt.add(QTPBuilder.of(new SingleQueryTerm("c")).build());
		new DependenceModelPreProcess().process(mqt, "pBiL");
		System.out.println(mqt.toString());
		assertEquals(8, mqt.size());
	}
	
}
