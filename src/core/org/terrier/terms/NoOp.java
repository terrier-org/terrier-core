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
 * The Original Code is NoOp.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.terms;

/** A do-nothing term pipeline object. 
 *  Simply passes the term onto the next component of the pipeline. 
 *  @author Craig Macdonald
 */
public class NoOp implements TermPipeline
{

	/** The implementation of a term pipeline.*/
    protected final TermPipeline next;

    /**
     * Constructs an instance of the class, given the next
     * component in the pipeline.
     * @param _next TermPipeline the next component in
     *      the term pipeline.
     */
    public NoOp(TermPipeline _next)
    {
        this.next = _next;
    }

	/** Pass the term onto the next term pipeline object,
	 *  without making any changes to it.
	 * @param t The term
	 */
	public final void processTerm(final String t)
    {
        if (t == null)
            return;
        next.processTerm(t);
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
