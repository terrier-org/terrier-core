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
 * The Original Code is TermPipelineAccessor.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.terms;
/**
 * This interface allows code to access the TermPipeline without implementing
 * the end of the term pipeline.
 * 
 * @author Craig Macdonald
  */
public interface TermPipelineAccessor {
	/**
	 * Puts the given term through the pipeline.
	 * @param term String the term to process.
	 * @return String the processed term.
	 */
	String pipelineTerm(String term);
	
	
	/** 
	 * This method implements the specific rest option needed to implements
	 * query or doc oriented pipeline policy. Normally it should be expected to reset all the pipeline.
	 * @return results of the reset operation
	 */
	boolean resetPipeline();
}
