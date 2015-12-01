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
 * The Original Code is TestTermPipelineAccessor.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.terms;

import static org.junit.Assert.*;
import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;

/** Test that a TermPipelineAccessor behaves as expected
 * @since 3.0
 * @author Craig Macdonald
 */
public class TestTermPipelineAccessor extends ApplicationSetupBasedTest {
	
	@Test public void testStemming()
	{
		TermPipelineAccessor tpa = new BaseTermPipelineAccessor(TRv2PorterStemmer.class.getName());
		assertEquals("archaeolog", tpa.pipelineTerm("archaeology"));
		assertEquals("meet", tpa.pipelineTerm("meeting"));
	}
	
	@Test public void testStopwords()
	{
		TermPipelineAccessor tpa = new BaseTermPipelineAccessor(Stopwords.class.getName());
		assertNull(tpa.pipelineTerm("i"));
		assertNull(tpa.pipelineTerm("the"));
	}
}
