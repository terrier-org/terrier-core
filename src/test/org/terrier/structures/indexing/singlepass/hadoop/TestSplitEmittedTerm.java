/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.uk
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
 * The Original Code is TestSplitEmittedTerm.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richardm{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures.indexing.singlepass.hadoop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import junit.framework.TestCase;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;

import org.terrier.structures.indexing.singlepass.hadoop.SplitEmittedTerm.SETPartitioner;
import org.terrier.structures.indexing.singlepass.hadoop.SplitEmittedTerm.SETPartitionerLowercaseAlphaTerm;
import org.terrier.structures.indexing.singlepass.hadoop.SplitEmittedTerm.SETRawComparatorTerm;
import org.terrier.structures.indexing.singlepass.hadoop.SplitEmittedTerm.SETRawComparatorTermSplitFlush;

/** Tests for SplitEmittedTerm, including the Comparators and Partitioners */
@SuppressWarnings("deprecation")
public class TestSplitEmittedTerm extends TestCase {
	
	
	public void testMethods() throws Exception
	{
		SplitEmittedTerm t1 = new SplitEmittedTerm("t1", 10, 34);
		assertEquals("t1", t1.getTerm());
		assertEquals(10, t1.getSplitno());
		assertEquals(34, t1.getFlushno());
		
		t1.setFlushno(11);
		assertEquals(10, t1.getSplitno());
		assertEquals(11, t1.getFlushno());
		
		t1.setSplitno(5);
		assertEquals(5, t1.getSplitno());
		assertEquals(11, t1.getFlushno());
		
		t1.setTerm("t2");
		assertEquals("t2", t1.getTerm());
	}
	
	private void checkWritable(final String t, final int split, final int flush) throws Exception
	{
		SplitEmittedTerm t1 = new SplitEmittedTerm(t, split, flush);
		byte[] b = toBytes(t1);
		
		SplitEmittedTerm t2 = new SplitEmittedTerm();
		t2.readFields(new DataInputStream(new ByteArrayInputStream(b)));
		assertTrue(t1.equals(t2));
		assertTrue(t2.equals(t1));
		assertEquals(t, t2.getTerm());
		assertEquals(split, t2.getSplitno());
		assertEquals(flush, t2.getFlushno());
	}
	
	public void testWritable() throws Exception
	{
		checkWritable("t1", 10, 34);
		checkWritable("t1", Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
	
	private byte[] toBytes(Writable w) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		w.write(dos);
		return baos.toByteArray();
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
			value="DM_STRING_CTOR",
			justification="Check String.equals is used, not ==")
	private void checkEqualityTerm(String t, int split, int flush) throws Exception
	{
		SplitEmittedTerm t1 = new SplitEmittedTerm(t, split, flush);
		SETRawComparatorTerm compare = new SETRawComparatorTerm();
		SETRawComparatorTermSplitFlush compare2 = new SETRawComparatorTermSplitFlush();
		assertEquals(0, t1.compareTo(t1));
		assertTrue(t1.equals(t1));
		assertEquals(0, compare.compare(t1, t1));
		assertEquals(0, compare2.compare(t1, t1));
		byte[] t1w = toBytes(t1);
		assertEquals(0, compare.compare(t1w, 0, t1w.length, t1w, 0, t1w.length));
		assertEquals(0, compare2.compare(t1w, 0, t1w.length, t1w, 0, t1w.length));
		
		SplitEmittedTerm t1a = new SplitEmittedTerm(new String(t), split, flush);
		assertEquals(0, t1.compareTo(t1a));
		assertEquals(0, t1a.compareTo(t1));
		assertTrue(t1.equals(t1a));
		assertTrue(t1a.equals(t1));
		assertEquals(0, compare.compare(t1, t1a));
		assertEquals(0, compare.compare(t1a, t1));
		assertEquals(0, compare2.compare(t1, t1a));
		assertEquals(0, compare2.compare(t1a, t1));
	}
	
	public void testEqualityTerm() throws Exception
	{
		checkEqualityTerm("t1", 0, 0);
		checkEqualityTerm("t1", Integer.MAX_VALUE, Integer.MAX_VALUE);		
	}
	
	@edu.umd.cs.findbugs.annotations.SuppressWarnings(
				value="DM_STRING_CTOR",
				justification="Check String.equals is used, not ==")
	private void checkEqualityTermSplit(String t, int split1, int split2, int flush) throws Exception
	{
		SplitEmittedTerm t1 = new SplitEmittedTerm(t, split1, flush);
		SplitEmittedTerm t2 = new SplitEmittedTerm(new String(t), split2, flush);
		SETRawComparatorTerm compare = new SETRawComparatorTerm();
		SETRawComparatorTermSplitFlush compare2 = new SETRawComparatorTermSplitFlush();
		
		assertEquals(0, t1.compareTo(t1));
					
		assertFalse(t1.equals(t2));
		assertEquals(0, compare.compare(t1, t2));
		assertTrue(compare2.compare(t1, t2) < 0);
		
		byte[] t1w = toBytes(t1);
		byte[] t2w = toBytes(t2);
		assertEquals(0, compare.compare(t1w, 0, t1w.length, t2w, 0, t2w.length));
		assertTrue("Comparing t1 to t2 as bytes", compare2.compare(t1w, 0, t1w.length, t2w, 0, t2w.length)< 0);
	}
	
	public void testEqualityTermSplit() throws Exception
	{
		checkEqualityTermSplit("t1", 0, 1, 0);
		checkEqualityTermSplit("t1", Integer.MAX_VALUE -1, Integer.MAX_VALUE, Integer.MAX_VALUE);		
	}
	
	private void compareTerm(SplitEmittedTerm t1, SplitEmittedTerm t2) throws Exception
	{
		SETRawComparatorTerm compare = new SETRawComparatorTerm();
		//check for inequality of each pair
		assertFalse(t1.equals(t2));
		assertFalse(t2.equals(t1));
					
		assertTrue(t1.compareTo(t2) < 0);
		assertTrue(t2.compareTo(t1) > 0);
		assertTrue(compare.compare(t1, t2) < 0);
		assertTrue(compare.compare(t2, t1) > 0);
		
		SETRawComparatorTermSplitFlush compare2 = new SETRawComparatorTermSplitFlush();
		assertTrue(compare2.compare(t1, t2) < 0);
		assertTrue(compare2.compare(t2, t1) > 0);
		byte[] t1w = toBytes(t1);
		byte[] t2w = toBytes(t2);
		assertTrue(compare.compare(t1w, 0, t1w.length, t2w, 0, t2w.length)< 0);
		assertTrue("Comparing t1 to t2 as bytes", compare2.compare(t1w, 0, t1w.length, t2w, 0, t2w.length) < 0);
	}

	public void testCompareTerm() throws Exception
	{		
		SplitEmittedTerm t1 = new SplitEmittedTerm("t1", 0, 0);
		SplitEmittedTerm t2 = new SplitEmittedTerm("t2", 0, 0);
		compareTerm(t1, t2);
	
		t1 = new SplitEmittedTerm("t1", Integer.MAX_VALUE, Integer.MAX_VALUE);
		t2 = new SplitEmittedTerm("t2", Integer.MAX_VALUE, Integer.MAX_VALUE);
		compareTerm(t1, t2);
	}
	
	private void compareTermSplit(SplitEmittedTerm t1, SplitEmittedTerm t2) throws Exception
	{
		SETRawComparatorTerm compare = new SETRawComparatorTerm();
		//check for inequality of each pair			
		assertFalse(t1.equals(t2));
		assertFalse(t2.equals(t1));
					
		assertTrue(t1.compareTo(t2) < 0);
		assertTrue(t2.compareTo(t1) > 0);
		assertEquals(0, compare.compare(t1, t2));
		assertEquals(0, compare.compare(t2, t1));
		
		SETRawComparatorTermSplitFlush compare2 = new SETRawComparatorTermSplitFlush();
		assertTrue(compare2.compare(t1, t2) < 0);
		assertTrue(compare2.compare(t2, t1) > 0);
		byte[] t1w = toBytes(t1);
		byte[] t2w = toBytes(t2);
		assertEquals(0, compare.compare(t1w, 0, t1w.length, t2w, 0, t2w.length));
		assertEquals(0, compare.compare(t2w, 0, t2w.length, t1w, 0, t1w.length));
		assertTrue(compare2.compare(t1w, 0, t1w.length, t2w, 0, t2w.length) < 0);
		assertTrue(compare2.compare(t2w, 0, t2w.length, t1w, 0, t1w.length) > 0);
	}
	
	public void testCompareTermSplit() throws Exception
	{
		
		SplitEmittedTerm t1 = new SplitEmittedTerm("t1", 0, 0);
		SplitEmittedTerm t2 = new SplitEmittedTerm("t1", 1, 0);
		compareTermSplit(t1, t2);
		
		t1 = new SplitEmittedTerm("t1", Integer.MAX_VALUE-1, 0);
		t2 = new SplitEmittedTerm("t1", Integer.MAX_VALUE, 0);
		compareTermSplit(t1, t2);
		
	}
	
	static final int sign(int a)
	{
		if (a < 0)
			return -1;
		if (a > 0)
			return 1;
		return 0;
	}
	
	private void compareTermFlush(SplitEmittedTerm t1, SplitEmittedTerm t2) throws Exception
	{
		SETRawComparatorTerm compare = new SETRawComparatorTerm();
		//check for inequality of each pair			
		assertFalse(t1.equals(t2));
		assertFalse(t2.equals(t1));
					
		assertTrue(t1.compareTo(t2) < 0);
		assertTrue(t2.compareTo(t1) > 0);
		assertEquals(sign(t1.getTerm().compareTo(t2.getTerm())), sign(compare.compare(t1, t2)));
		assertEquals(sign(t2.getTerm().compareTo(t1.getTerm())), sign(compare.compare(t2, t1)));
		
		SETRawComparatorTermSplitFlush compare2 = new SETRawComparatorTermSplitFlush();
		assertTrue(compare2.compare(t1, t2) < 0);
		assertTrue(compare2.compare(t2, t1) > 0);
		byte[] t1w = toBytes(t1);
		byte[] t2w = toBytes(t2);
		assertEquals(sign(t1.getTerm().compareTo(t2.getTerm())), sign(compare.compare(t1w, 0, t1w.length, t2w, 0, t2w.length)));
		assertEquals(sign(t2.getTerm().compareTo(t1.getTerm())), sign(compare.compare(t2w, 0, t2w.length, t1w, 0, t1w.length)));
		assertTrue(compare2.compare(t1w, 0, t1w.length, t2w, 0, t2w.length) < 0);
		assertTrue(compare2.compare(t2w, 0, t2w.length, t1w, 0, t1w.length) > 0);
	}
	
	public void testCompareTermFlush() throws Exception
	{		
		SplitEmittedTerm t1,t2;
		t1 = new SplitEmittedTerm("t1", 0, 0);
		t2 = new SplitEmittedTerm("t1", 0, 1);
		compareTermFlush(t1, t2);
		
		t1 = new SplitEmittedTerm(".", 0, 0);
		t2 = new SplitEmittedTerm("0", 0, 0);
		compareTermFlush(t1, t2);
		
		t1 = new SplitEmittedTerm("0", 0, 0);
		t2 = new SplitEmittedTerm("\\", 0, 0);		
		compareTermFlush(t1, t2);
		
		t1 = new SplitEmittedTerm("t1", 0, Integer.MAX_VALUE -1);
		t2 = new SplitEmittedTerm("t1", 0, Integer.MAX_VALUE );
		compareTermFlush(t1, t2);
	}
	
	/* Test cases for SETPartitionerLowercaseAlphaTerm */
	public void testSETPLAT() throws Exception
	{
		final SETPartitionerLowercaseAlphaTerm p = new SETPartitionerLowercaseAlphaTerm();
		//single partition
		assertEquals(0, p.calculatePartition('0', 1));
		assertEquals(0, p.calculatePartition('9', 1));
		assertEquals(0, p.calculatePartition('-', 1));
		assertEquals(0, p.calculatePartition('a', 1));
		assertEquals(0, p.calculatePartition('z', 1));
		assertEquals(0, p.calculatePartition('}', 1));
		//two partitions
		assertEquals(0, p.calculatePartition('(', 2));
		assertEquals(0, p.calculatePartition('.', 2));
		assertEquals(0, p.calculatePartition(')', 2));
		assertEquals(0, p.calculatePartition('\\', 2));
		assertEquals(0, p.calculatePartition('/', 2));
		
		assertEquals(0, p.calculatePartition('0', 2));
		assertEquals(0, p.calculatePartition('9', 2));
		assertEquals(0, p.calculatePartition('-', 2));
		assertEquals(0, p.calculatePartition('a', 2));
		assertEquals(0, p.calculatePartition('l', 2));
		assertEquals(0, p.calculatePartition('m', 2));
		assertEquals(1, p.calculatePartition('n', 2));
		assertEquals(1, p.calculatePartition('o', 2));
		assertEquals(1, p.calculatePartition('z', 2));
		assertEquals(1, p.calculatePartition('}', 2));
		//(all upper case goto partition 0)
		assertEquals(0, p.calculatePartition('M', 2));
		assertEquals(0, p.calculatePartition('N', 2));
		assertEquals(0, p.calculatePartition('O', 2));
		
		//three partitions
		assertEquals(0, p.calculatePartition('0', 3));
		assertEquals(0, p.calculatePartition('9', 3));
		assertEquals(0, p.calculatePartition('-', 3));
		assertEquals(0, p.calculatePartition('a', 3));
		assertEquals(0, p.calculatePartition('h', 3));
		assertEquals(0, p.calculatePartition('i', 3));
		assertEquals(1, p.calculatePartition('j', 3));
		assertEquals(1, p.calculatePartition('r', 3));
		assertEquals(2, p.calculatePartition('s', 3));
		assertEquals(2, p.calculatePartition('t', 3));
		assertEquals(2, p.calculatePartition('u', 3));
		assertEquals(2, p.calculatePartition('z', 3));
		assertEquals(2, p.calculatePartition('}', 3));
		
		//26 partitions
		assertEquals(0, p.calculatePartition('0', 26));
		assertEquals(0, p.calculatePartition('9', 26));
		assertEquals(0, p.calculatePartition('-', 26));
		assertEquals(0, p.calculatePartition('a', 26));
		assertEquals(1, p.calculatePartition('b', 26));
		assertEquals(2, p.calculatePartition('c', 26));
		assertEquals(3, p.calculatePartition('d', 26));
		assertEquals(4, p.calculatePartition('e', 26));
		assertEquals(5, p.calculatePartition('f', 26));
		assertEquals(6, p.calculatePartition('g', 26));
		assertEquals(7, p.calculatePartition('h', 26));
		assertEquals(8, p.calculatePartition('i', 26));
		assertEquals(9, p.calculatePartition('j', 26));
		assertEquals(10, p.calculatePartition('k', 26));
		assertEquals(11, p.calculatePartition('l', 26));
		assertEquals(12, p.calculatePartition('m', 26));
		assertEquals(13, p.calculatePartition('n', 26));
		assertEquals(14, p.calculatePartition('o', 26));
		assertEquals(15, p.calculatePartition('p', 26));
		assertEquals(16, p.calculatePartition('q', 26));
		assertEquals(17, p.calculatePartition('r', 26));
		assertEquals(18, p.calculatePartition('s', 26));
		assertEquals(19, p.calculatePartition('t', 26));
		assertEquals(20, p.calculatePartition('u', 26));
		assertEquals(21, p.calculatePartition('v', 26));
		assertEquals(22, p.calculatePartition('w', 26));
		assertEquals(23, p.calculatePartition('x', 26));
		assertEquals(24, p.calculatePartition('y', 26));
		assertEquals(25, p.calculatePartition('z', 26));
	}
	
	/* Test cases for SETPartitioner */
	
	/** single map, single reducer */
	public void testSMSRCalculatePartition() throws Exception
	{
		final JobConf j = new JobConf();
		j.setNumMapTasks(1);
		final SETPartitioner p = new SETPartitioner();
		p.configure(j);
		assertEquals(0, p.calculatePartition(0, 1));
	}
	
	/** multiple map, single reducer */
	public void testMMSRCalculatePartition() throws Exception
	{
		final JobConf j = new JobConf();
		final int maptasks = 20;
		j.setNumMapTasks(maptasks);
		final SETPartitioner p = new SETPartitioner();
		p.configure(j);
		assertEquals(0, p.calculatePartition(0, 1));
		assertEquals(0, p.calculatePartition(19, 1));
		assertEquals(0, p.calculatePartition(10, 1));
		
		
	}
	
	/** multiple map, multiple reducer */
	public void testMMMRCalculatePartition() throws Exception
	{
		final JobConf j = new JobConf();
		final int maptasks = 20;
		j.setNumMapTasks(maptasks);
		final SETPartitioner p = new SETPartitioner();
		p.configure(j);
		
		assertEquals(0, p.calculatePartition(0, 2));
		assertEquals(0, p.calculatePartition(1, 2));
		assertEquals(0, p.calculatePartition(9, 2));
		assertEquals(1, p.calculatePartition(10, 2));
		assertEquals(1, p.calculatePartition(19, 2));
	}
}
