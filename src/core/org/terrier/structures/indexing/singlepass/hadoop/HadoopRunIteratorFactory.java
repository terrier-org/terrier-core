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
 * The Original Code is HadoopRunIteratorFactory.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.indexing.singlepass.hadoop;

import java.util.Iterator;

import org.terrier.structures.indexing.singlepass.PostingInRun;
import org.terrier.structures.indexing.singlepass.RunIterator;
import org.terrier.structures.indexing.singlepass.RunIteratorFactory;

/** Creates a new Iterator over runs which can be used within the Hadoop framework.
 * @since 2.2
 * @author Craig Macdonald
  */
public class HadoopRunIteratorFactory extends RunIteratorFactory {

	Class <? extends PostingInRun> postingClass;
	Iterator<MapEmittedPostingList> postingIterator;
	String currentTerm;
	/**
	 * constructor
	 * @param _postingIterator
	 * @param _postingClass
	 * @param numberOfFields
	 */
	public HadoopRunIteratorFactory(
			Iterator<MapEmittedPostingList> _postingIterator,
			Class <? extends PostingInRun> _postingClass,
			int numberOfFields) {
		super(numberOfFields);
		this.postingClass = _postingClass;
		this.postingIterator = _postingIterator;
	}

	@Override
	public RunIterator createRunIterator(int runNumber) throws Exception {
		return new HadoopRunPostingIterator(postingClass, runNumber, postingIterator, currentTerm, super.numberOfFields);
	}
	
	/** Update the posting iterator currently being used */
	public void setRunPostingIterator(Iterator<MapEmittedPostingList> _postingIterator)
	{
		this.postingIterator = _postingIterator;
	}
	
	/** Update the term which is currently being processed */
	public void setTerm(String _term)
	{
		currentTerm = _term;
	}

}
