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
 * The Original Code is SkipTermPipeline.java.
 *
 * The Original Code is Copyright (C) 2008-2010 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Rodrygo Santos <rodrygo{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   
 */
package org.terrier.terms;

import gnu.trove.THashSet;
import org.terrier.utility.ApplicationSetup;

/** Class that identified tokens which should not be passed down the entire term pipeline, and instead
  * passed onto a specified stage instead. Tokens are autmatically lowercased if <tt>lowercase</tt> 
  * is set (as it is by default). If no tokens are specified, then no tokens are omitted from the term
  * pipeline.
  * <p><b>Properties</b>
  * <ul><li><tt>termpipelines.skip</tt> - list of tokens to skip</li>
  * <li><tt>lowercase</tt> - whether tokens should be lowercased</li>
  * </ul>
  * @author Rodrygo Santos and Craig Macdonald
    * @since 2.2
  */
public class SkipTermPipeline implements TermPipeline {
	
	final TermPipeline next, last;
	final THashSet<String> skipTerms = new THashSet<String>();

	
	/** Instantiate this object, using properties to define tokens. Skip tokens are
	  * specified as a comma delimited list, using the <tt>termpipelines.skip</tt>
	  * property. Terms are lowercased if <tt>lowercase</tt> is set (as it is by default).
	  */
	public SkipTermPipeline(TermPipeline _next, TermPipeline _last) 
	{
		this.next = _next;
		this.last = _last;
		String tokens = ApplicationSetup.getProperty("termpipelines.skip", null);
		if (tokens == null || (tokens = tokens.trim()).length() == 0)
		{
			return;
		}
		if (Boolean.parseBoolean(ApplicationSetup.getProperty("lowercase", "true")))
			tokens = tokens.toLowerCase();
		for (String st : tokens.split("\\s*,\\s*"))
        {
            skipTerms.add(st);
        }
	}
	

	/** Instantiate this object. Terms in skipTokens will be passed to the last term pipeline
	  * object instead of the next.
	  */
	public SkipTermPipeline(TermPipeline _next, TermPipeline _last, String[] _skipTokens)
	{
		this.next = _next;
		this.last = _last;
		for (String st : _skipTokens)
		{
			skipTerms.add(st);
		}
	}

	/** Processes this token. If is a specified token, then passes it to the last
	  * stage in the pipgeline, instead of onto the next one.
	  * @param term
	  */
	public void processTerm(String term) {
		// if term should be skiped
		if (skipTerms.contains(term))
		{
			// jump to last termpipeline
			last.processTerm(term);
		}
		else
		{
			// proceed to next termpipeline
			next.processTerm(term);
		}
	}
	
	/**
	 * Implements the  default operation for all TermPipeline subclasses;
	 * By default do nothing.
	 * This method should be overrided by any TermPipeline that want to implements doc/query
	 * oriented lifecycle.
	 * @return return how the reset has gone
	 */
	public boolean reset() {
		return next!=null ? next.reset() : true;
	}
}
