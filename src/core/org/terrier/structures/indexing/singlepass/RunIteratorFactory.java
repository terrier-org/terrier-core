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
 * The Original Code is RunIteratorFactory.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */

package org.terrier.structures.indexing.singlepass;

/** Base class for Factories of RunIterators.
  * @author Craig Macdonald
    * @since 2.2
  */
public abstract class RunIteratorFactory {
	protected int numberOfFields;
	
	protected RunIteratorFactory(int numF)
	{
		numberOfFields = numF;
	}
	
	/** Open the RunIterator for the specified run number */
	public abstract RunIterator createRunIterator(int runNumber) throws Exception;
}
