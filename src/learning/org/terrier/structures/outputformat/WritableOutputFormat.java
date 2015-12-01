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
 * The Original Code is WritableOutputFormat.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.structures.outputformat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.hadoop.io.Writable;
import org.terrier.matching.FatResultSet;
import org.terrier.matching.FatResultsMatching;
import org.terrier.matching.ResultSet;
import org.terrier.querying.SearchRequest;
import org.terrier.structures.Index;

/** 
 * This class can be used for writing {@link FatResultSet}s to disk
 * for later use by {@link FatResultsMatching}
 * 
 * @author Craig Macdonald
 * @since 4.0
 */
public class WritableOutputFormat implements RawOutputFormat {

	/** Created a new NullOuputFormat */
	public WritableOutputFormat(Index i){} 
	
	@Override
	public void printResults(PrintWriter pw, SearchRequest q,
			String method, String iteration, int numberOfResults)
			throws IOException 
	{
		throw new UnsupportedOperationException();		
	}

	@Override
	public void writeResults(OutputStream os, SearchRequest q,
			String method, String iteration, int numberOfResults)
			throws IOException 
	{		
		final ResultSet rs = q.getResultSet();
		DataOutputStream dos = new DataOutputStream(os);
		dos.writeUTF(q.getQueryID());
		((Writable)rs).write(dos);
		dos.flush();
		dos = null;
		System.err.println("Wrote " + rs.getResultSize() + " results for query " + q.getQueryID());
	}
	
}