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
 * The Original Code is TestResultSets.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.matching;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.terrier.matching.daat.CandidateResult;
import org.terrier.matching.daat.CandidateResultSet;

public class TestResultSets {

	@Test public void testCandidateResultComparable()
	{
		CandidateResult r11 = new CandidateResult(10);
		r11.updateScore(5d);
		CandidateResult r12 = new CandidateResult(9);
		r12.updateScore(10d);

		CandidateResult r10 = new CandidateResult(8);
		r10.updateScore(-5d);
		CandidateResult r01 = new CandidateResult(7);
		r01.updateScore(-10d);
		
		CandidateResult r00 = new CandidateResult(7);
		r00.updateScore(Double.NEGATIVE_INFINITY);
		
		//check equals
		assertEquals(0, r11.compareTo(r11));
		assertEquals(0, r12.compareTo(r12));
		assertTrue(r11.equals(r11));
		assertTrue(r12.equals(r12));
		
	
		//check positive signs
		assertEquals(-1, r11.compareTo(r12));
		assertEquals(1, r12.compareTo(r11));
		
		//check negative signs
		assertEquals(-1, r01.compareTo(r10));
		assertEquals(1, r10.compareTo(r01));
		
		//check neg inf
		assertEquals(-1, r00.compareTo(r01));
		assertEquals(-1, r00.compareTo(r12));
		assertEquals(1, r01.compareTo(r00));
		assertEquals(1, r12.compareTo(r00));
	}
	
	
	
	@Test public void testNormalResultSetExactSize()
	{
		ResultSet r = new QueryResultSet(10);
		r.setExactResultSize(1000);
		ResultSet r2 = r.getResultSet(0, 5);
		assertEquals(1000, r2.getExactResultSize());
	}
	
	@Test public void testAccumulatorResultSetExactSize()
	{
		AccumulatorResultSet r = new AccumulatorResultSet(10);
		r.scoresMap.adjustOrPutValue(5, 1.0d, 1.0d);
		r.occurrencesMap.adjustOrPutValue(5, (short)1, (short)1);
		r.initialise();
		r.setExactResultSize(1000);
		ResultSet r2 = r.getResultSet(0, 5);
		assertEquals(1000, r2.getExactResultSize());
	}
	
	@Test public void testSorting() 
	{
		ResultSet r1 = new CollectionResultSet(2);
		r1.initialise();
		ResultSet r2 = new QueryResultSet(2);
		r2.initialise();
		
		for (ResultSet r : new ResultSet[]{r1,r2})
		{
			r.getDocids()[0]	= 10;
			r.getScores()[0] = 5d;
			r.getDocids()[1]	= 9;
			r.getScores()[1] = 10d;
			r.sort();
			assertEquals(9, r.getDocids()[0]);
			assertEquals(10, r.getDocids()[1]);
	
			assertEquals(10d, r.getScores()[0], 0.0d);
			assertEquals(5d, r.getScores()[1], 0.0d);
		}
	}
	
	@Test public void testCandidateResultSet() 
	{
		CandidateResult r11 = new CandidateResult(10);
		r11.updateScore(5d);
		CandidateResult r12 = new CandidateResult(9);
		r12.updateScore(10d);
		
		
		CandidateResultSet c = new CandidateResultSet(Arrays.asList(r11, r12));
		c.sort();
		ResultSet r1 = c.getResultSet(0, 2);
		assertEquals(9, r1.getDocids()[0]);
		assertEquals(10, r1.getDocids()[1]);
		
		ResultSet r2 = r1.getResultSet(0, 2);
		
		for (ResultSet r : new ResultSet[]{r1,r2})
		{
			r.sort();
			assertEquals(9, r.getDocids()[0]);
			assertEquals(10, r.getDocids()[1]);
	
			assertEquals(10d, r.getScores()[0], 0.0d);
			assertEquals(5d, r.getScores()[1], 0.0d);
		}
	}
	
	@Test public void testCandidateResultSetNegative() 
	{
		CandidateResult r11 = new CandidateResult(10);
		r11.updateScore(-15d);
		CandidateResult r12 = new CandidateResult(9);
		r12.updateScore(-10d);
		
		
		CandidateResultSet c = new CandidateResultSet(Arrays.asList(r11, r12));
		c.sort();
		ResultSet r1 = c.getResultSet(0, 2);
		ResultSet r2 = r1.getResultSet(0, 2);
		
		
		for (ResultSet r : new ResultSet[]{r1,r2})
		{
			r.sort();
			assertEquals(9, r.getDocids()[0]);
			assertEquals(10, r.getDocids()[1]);
	
			assertEquals(-10d, r.getScores()[0], 0.0d);
			assertEquals(-15d, r.getScores()[1], 0.0d);
		}
	}
	
	@Test public void testSortingNegative() 
	{
		ResultSet r1 = new CollectionResultSet(2);
		r1.initialise();
		ResultSet r2 = new QueryResultSet(2);
		r2.initialise();
		
		for (ResultSet r : new ResultSet[]{r1,r2})
		{
			r.getDocids()[0]	= 10;
			r.getScores()[0] = -5d;
			r.getDocids()[1]	= 9;
			r.getScores()[1] = -1d;
			r.sort();
			assertEquals(9, r.getDocids()[0]);
			assertEquals(10, r.getDocids()[1]);
	
			assertEquals(-1d, r.getScores()[0], 0.0d);
			assertEquals(-5d, r.getScores()[1], 0.0d);
		}
	}
	
	
	@Test public void testCollectionResultSetExactSize()
	{
		CollectionResultSet r = new CollectionResultSet(10);
		r.initialise();
		r.setExactResultSize(1000);
		ResultSet r2 = r.getResultSet(0, 5);
		assertEquals(1000, r2.getExactResultSize());
	}
	
}
