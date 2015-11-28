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
 * The Original is in 'TestCollectionStatistics.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.utility;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.Test;
import org.terrier.structures.CollectionStatistics;


public class TestCollectionStatistics {

	
	@Test public void testWritable() throws Exception
	{
		CollectionStatistics cs1 = new CollectionStatistics(5, 6, 7, 8, new long[]{2});
		
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		cs1.write(dos);
		dos.flush();
		final byte[] bytes = baos.toByteArray();
		assertTrue(bytes.length > 0);
		CollectionStatistics cs2 = new CollectionStatistics();
		cs2.readFields(new DataInputStream(new ByteArrayInputStream(bytes)));
		
		assertEquals(cs1.getNumberOfDocuments(), cs2.getNumberOfDocuments());
		assertEquals(cs1.getNumberOfUniqueTerms(), cs2.getNumberOfUniqueTerms());
		
		assertEquals(cs1.getNumberOfPointers(), cs2.getNumberOfPointers());
		assertEquals(cs1.getNumberOfTokens(), cs2.getNumberOfTokens());
		assertEquals(cs1.getAverageDocumentLength(), cs2.getAverageDocumentLength(), 0.0d);
		//TODO: test fields
		
	}
	
}
