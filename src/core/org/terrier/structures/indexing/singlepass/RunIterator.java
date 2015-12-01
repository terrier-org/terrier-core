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
 * The Original Code is RunIterator.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */

package org.terrier.structures.indexing.singlepass;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

/** An abstract base class which allows PostingInRuns to be iterated over.
  * Implementations are typically more generic versions of RunReader from Terrier 2.0 and 2.1.
  * @author Craig Macdonald
  * @since 2.2
    */
public abstract class RunIterator implements Iterator<PostingInRun>, Closeable {

	/** class that new postings are derived from */
	protected Class<? extends PostingInRun> postingClass;
	/** current posting */
	protected PostingInRun posting;
	/** Run/Flush that the current posting came from */
	protected int flushNo;
	
	protected int numberOfFields;

	/** create a new instance of this class.
	  * @param _postingClass Class of the PostingInRun type that postings in this run have
	  * @param _runNo The run number currently being processed */
	protected RunIterator(Class<? extends PostingInRun> _postingClass, int _runNo, int _numberOfFields)
	{
		this.postingClass = _postingClass;
		this.flushNo = _runNo;
		this.numberOfFields = _numberOfFields;
	}

	/** Create a new posting */	
	protected void createPosting() throws Exception {
		if (numberOfFields > 0)
			posting = postingClass.getConstructor(Integer.TYPE).newInstance(numberOfFields);
		else
			posting = postingClass.newInstance();
	}

	/** Get the run/flush number that the current posting came from */	
	public int getRunNo()
	{
		return flushNo;
	}

	/** iterator implementation */	
	public abstract boolean hasNext();

	/** iterator implementation */
	public abstract PostingInRun next();

	/** returns the current posting */	
	public PostingInRun current()
	{
		return posting;
	}
	
	/** unsupported iterator method */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/** close this RunIterator */	
	public void close() throws IOException {}

}
