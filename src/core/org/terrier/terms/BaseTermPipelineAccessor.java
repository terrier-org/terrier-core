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
 * The Original Code is BaseTermPipelineAccessor.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.terms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.utility.ApplicationSetup;
/** A base implementation for TermPipelineAccessor
 * @since 3.0
 * @author Craig Macondald
 */
public class BaseTermPipelineAccessor 
implements TermPipeline, TermPipelineAccessor 
{
	protected static final Logger logger = LoggerFactory.getLogger(BaseTermPipelineAccessor.class);
	private static Class<?>[] constructor_array_termpipeline = new Class[]{TermPipeline.class};
	
	/** The default namespace for TermPipeline modules to be loaded from */
	public final static String NAMESPACE_PIPELINE = "org.terrier.terms.";
	
	TermPipeline pipeline_first;
	
	/** This class is used as a TermPipelineAccessor, and this variable stores
	  * the result of the TermPipeline run for that term. */
	protected String pipelineOutput = null;
	
	
	/** Construct a term pipeline using the specified class names */
	public BaseTermPipelineAccessor(String... pipes)
	{
		TermPipeline next = this;
		final TermPipeline last = next;
		TermPipeline tmp;
		for(int i=pipes.length-1; i>=0; i--)
		{
			try{
				String className = pipes[i];
				if (className.length() == 0)
					continue;
				if (className.indexOf(".") < 0 )
					className = NAMESPACE_PIPELINE + className;
				else if (className.startsWith("uk.ac.gla.terrier"))
					className = className.replaceAll("uk.ac.gla.terrier", "org.terrier");
				Class<? extends TermPipeline> pipeClass = Class.forName(className, false, this.getClass().getClassLoader()).asSubclass(TermPipeline.class);
				tmp = (TermPipeline) (pipeClass.getConstructor(
						constructor_array_termpipeline)
						.newInstance(new Object[] {next}));
				next = tmp;
			}catch (Exception e){
				logger.error("TermPipeline object "+NAMESPACE_PIPELINE+pipes[i]+" not found",e);
			}
		}
		String skipTerms = null;
		//add SkipTermPipeline as the first pipeline step to allow for special terms to skip the pipeline processing sequence
		if ((skipTerms = ApplicationSetup.getProperty("termpipelines.skip", null)) != null && skipTerms.trim().length() > 0)
			pipeline_first = new SkipTermPipeline(next, last);
		else
			pipeline_first = next;
	}

	/* -------------------term pipeline implementation --------------------*/
	/**
	 * Make this object a term pipeline implementor.
	 * @see org.terrier.terms.TermPipeline
	 */
	public void processTerm(String t)
	{
		pipelineOutput = t;
	}
	
	/** {@inheritDoc} */
	public boolean reset() {
		return true;
	}
	
	/*-------------- term pipeline accessor implementation ----------------*/
	/** A term pipeline accessor */
	public String pipelineTerm(String t)
	{
		pipelineOutput = null;
		pipeline_first.processTerm(t);
		return pipelineOutput;
	}
	
	/**
	* Reset all the pipeline.
	* @return return how the reset operation has gone over all the stage
	*/
	public boolean resetPipeline() {
		return pipeline_first.reset();
	}
}
