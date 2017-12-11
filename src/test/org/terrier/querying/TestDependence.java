package org.terrier.querying;

import static org.junit.Assert.*;

import org.junit.Test;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.indriql.QueryTerm;
import org.terrier.matching.indriql.SingleQueryTerm;
import org.terrier.matching.indriql.UnorderedWindowTerm;
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
	
	@Test public void test13()
	{ 
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		for(int i=0;i<13;i++)
		{
			mqt.add(QTPBuilder.of(new SingleQueryTerm(String.valueOf(i))).build());
		}
		new DependenceModelPreProcess().process(mqt, "pBiL");
		System.out.println(mqt.toString());
		int size = mqt.size();
		QueryTerm qt = mqt.get(size -1).getKey();
		assertTrue(qt instanceof UnorderedWindowTerm);
		UnorderedWindowTerm uwt = (UnorderedWindowTerm) qt;
		assertFalse( uwt.getConstituents().length > uwt.getDistance() );
	}
	
}
