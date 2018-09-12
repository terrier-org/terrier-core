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
 * The Original Code is TestDependence.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.querying;

import static org.junit.Assert.*;

import org.junit.Test;
import org.terrier.matching.BaseMatching;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;
import org.terrier.matching.matchops.Operator;
import org.terrier.matching.matchops.SingleTermOp;
import org.terrier.matching.matchops.SynonymOp;
import org.terrier.matching.matchops.UnorderedWindowOp;
import org.terrier.querying.parser.Query.QTPBuilder;

public class TestDependence {

	@Test public void testOne()
	{ 
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.add(QTPBuilder.of(new SingleTermOp("a")).build());
		new DependenceModelPreProcess().process(mqt, "pBiL");
		System.out.println(mqt.toString());
		assertEquals(1, mqt.size());
	}
	
	@Test public void testTwo()
	{ 
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.add(QTPBuilder.of(new SingleTermOp("a")).build());
		mqt.add(QTPBuilder.of(new SingleTermOp("b")).build());
		new DependenceModelPreProcess().process(mqt, "pBiL");
		System.out.println(mqt.toString());
		assertEquals(5, mqt.size());
		
	}
	
	@Test public void testThree()
	{ 
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.add(QTPBuilder.of(new SingleTermOp("a")).build());
		mqt.add(QTPBuilder.of(new SingleTermOp("b")).build());
		mqt.add(QTPBuilder.of(new SingleTermOp("c")).build());
		new DependenceModelPreProcess().process(mqt, "pBiL");
		System.out.println(mqt.toString());
		assertEquals(8, mqt.size());
	}
	
	@Test public void test13()
	{ 
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		for(int i=0;i<13;i++)
		{
			mqt.add(QTPBuilder.of(new SingleTermOp(String.valueOf(i))).build());
		}
		new DependenceModelPreProcess().process(mqt, "pBiL");
		System.out.println(mqt.toString());
		int size = mqt.size();
		Operator qt = mqt.get(size -1).getKey();
		assertTrue(qt instanceof UnorderedWindowOp);
		UnorderedWindowOp uwt = (UnorderedWindowOp) qt;
		assertFalse( uwt.getConstituents().length > uwt.getDistance() );
	}
	

	@Test public void testSimple() {
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setTermProperty("a", 1d);
		mqt.setTermProperty("b", 1d);
		assertEquals(2, mqt.size());
		
		Request r = new Request();
		r.setMatchingQueryTerms(mqt);
		new DependenceModelPreProcess().process(null, r);
		
		assertEquals(5, mqt.size());
		
	}
	
	@Test public void testWithSyn() {
		MatchingQueryTerms mqt = new MatchingQueryTerms();
		mqt.setTermProperty("a", 1d);
		mqt.add(
				QTPBuilder.of(new SynonymOp(new String[]{"b", "c"}))
				.setTag(BaseMatching.BASE_MATCHING_TAG)
				.build()
			);
		
		assertEquals(2, mqt.size());
		
		Request r = new Request();
		r.setMatchingQueryTerms(mqt);
		new DependenceModelPreProcess().process(null, r);
		
		assertEquals(5, mqt.size());
		for(MatchingTerm mt : mqt)
		{
			System.err.println(mt.getKey().toString() + " " + mt.getValue().toString());
		}
	}
	
}
