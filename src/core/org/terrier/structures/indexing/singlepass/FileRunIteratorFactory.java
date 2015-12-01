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
 * The Original Code is FileRunIteratorFactory.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.structures.indexing.singlepass;

/** Creates FileRunIterators, using the specificed filenames as the run data files, and
  * the specified class as the type of the postings in the run files */
public class FileRunIteratorFactory extends RunIteratorFactory {
	/** type of the postings in the run data files */
	Class <? extends PostingInRun> postingClass;
	/** all the run filesnames */
	String[][] files;
	/**
	 * constructor
	 * @param _files
	 * @param _postingClass
	 * @param numFields
	 */
	public FileRunIteratorFactory(String[][] _files, Class <? extends PostingInRun> _postingClass, int numFields)
	{
		super(numFields);
		files = _files;
		postingClass = _postingClass;
	}
	
	/** Return a RunIterator for the specified runNumber */
	public RunIterator createRunIterator(int runNumber) throws Exception
	{
		return new FileRunIterator<PostingInRun>(files[runNumber][0], files[runNumber][1], runNumber, postingClass, super.numberOfFields);
	}

}
