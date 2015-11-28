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
 * The Original Code is TestWeightingModelFactory.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Rodrygo Santos <rodrygo{a.}dcs.gla.ac.uk> (original author)
 *   
 */

package org.terrier.matching.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.terrier.matching.models.BM25;
import org.terrier.matching.models.DFRWeightingModel;
import org.terrier.matching.models.PL2;
import org.terrier.matching.models.WeightingModel;
import org.terrier.matching.models.WeightingModelFactory;

public class TestWeightingModelFactory {

	@Test public void testGetBasicModels()
	{
		WeightingModel bm25 = WeightingModelFactory.newInstance("BM25");
		assertTrue(bm25 instanceof BM25);
		WeightingModel pl2 = WeightingModelFactory.newInstance("PL2");
		assertTrue(pl2 instanceof PL2);
	}
	
	@Test public void testGetBasicModelsWrongNamespace()
	{
		WeightingModel bm25 = WeightingModelFactory.newInstance("uk.ac.gla.terrier.matching.models.BM25");
		assertTrue(bm25 instanceof BM25);
		WeightingModel pl2 = WeightingModelFactory.newInstance("uk.ac.gla.terrier.matching.models.PL2");
		assertTrue(pl2 instanceof PL2);
	}
	
	@Test public void testGetAdvancedModels()
	{
		WeightingModel m = WeightingModelFactory.newInstance("DFRWeightingModel(P,L,2)");
		assertNotNull(m);
		assertTrue(m instanceof DFRWeightingModel);
		assertEquals("PL2c1.0", m.getInfo());
	}
	
	@Test public void testCacheWorks()
	{
		WeightingModel m1 = WeightingModelFactory.newInstance("BM25");
		WeightingModel m2 = WeightingModelFactory.newInstance("BM25");
		assertTrue(m1 == m2);
	}
}
