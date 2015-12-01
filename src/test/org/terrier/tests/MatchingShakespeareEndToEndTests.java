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
 * The Original Code is MatchingShakespeareEndToEndTests.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.tests;


public class MatchingShakespeareEndToEndTests {

	public static class TestDAATFullMatchingShakespeareEndToEndTest extends BasicShakespeareEndToEndTest
	{
		public TestDAATFullMatchingShakespeareEndToEndTest()
		{
			testHooks.add(new BatchEndToEndTestEventHooks(){
				@Override
				public String[] processRetrievalOptions(String[] retrievalOptions) {
					String[] options = super.processRetrievalOptions(retrievalOptions);
					String[] newoptions = new String[options.length+1];
					System.arraycopy(options, 0, newoptions, 0, options.length);
					newoptions[options.length] = "-Dtrec.matching=org.terrier.matching.daat.Full";
					return newoptions;
				}			
			});
		}
	}
	
	public static class TestDAATFullNoPLMMatchingShakespeareEndToEndTest extends BasicShakespeareEndToEndTest
	{
		public TestDAATFullNoPLMMatchingShakespeareEndToEndTest()
		{
			testHooks.add(new BatchEndToEndTestEventHooks(){
				@Override
				public String[] processRetrievalOptions(String[] retrievalOptions) {
					String[] options = super.processRetrievalOptions(retrievalOptions);
					String[] newoptions = new String[options.length+1];
					System.arraycopy(options, 0, newoptions, 0, options.length);
					newoptions[options.length] = "-Dtrec.matching=org.terrier.matching.daat.FullNoPLM";
					return newoptions;
				}			
			});
		}
	}
	
	public static class TestTAATFullNoPLMMatchingShakespeareEndToEndTest extends BasicShakespeareEndToEndTest
	{
		public TestTAATFullNoPLMMatchingShakespeareEndToEndTest()
		{
			testHooks.add(new BatchEndToEndTestEventHooks(){
				@Override
				public String[] processRetrievalOptions(String[] retrievalOptions) {
					String[] options = super.processRetrievalOptions(retrievalOptions);
					String[] newoptions = new String[options.length+1];
					System.arraycopy(options, 0, newoptions, 0, options.length);
					newoptions[options.length] = "-Dtrec.matching=org.terrier.matching.taat.FullNoPLM";
					return newoptions;
				}			
			});
		}
	}
}


