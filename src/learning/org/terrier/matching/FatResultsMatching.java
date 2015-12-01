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
 * The Original Code is FatResultsMatching.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.matching;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.daat.FatCandidateResultSet;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.Index;
import org.terrier.structures.outputformat.WritableOutputFormat;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

/** 
 * Reads a {@link FatResultSet} serialized to disk by {@link WritableOutputFormat}.
 * <b>Properties:</b>
 * <ul>
 * <li><tt>fat.results.matching.file</tt> - filename of the FatResultSet to read. Mandatory.</li>
 * <li><tt>fat.results.matching.max.results</tt> - maximum results to return for a given query.</li>
 * </ul>  
 * @author Craig Macdonald
 * @since 4.0
 */
public class FatResultsMatching implements Matching {

	DataInputStream dis;
	String filename;
	static Logger logger = LoggerFactory.getLogger(FatResultsMatching.class);
	int maxResults = 0;
	
	public FatResultsMatching(String _filename, int _maxResults) throws IOException
	{
		filename = _filename;
		maxResults = _maxResults;
		if (filename == null)
            throw new IllegalArgumentException("fat.results.matching.file needs to be specified");
		logger.info("Reading fat resultsets from " + filename);
		dis = new DataInputStream(Files.openFileStream(filename));
	}
	
	public FatResultsMatching (Index i) throws IOException {
         filename = ApplicationSetup.getProperty("fat.results.matching.file", null);
         maxResults = Integer.parseInt(ApplicationSetup.getProperty("fat.results.matching.max.results", "0"));
         if (filename == null)
                 throw new IllegalArgumentException("fat.results.matching.file needs to be specified");
         logger.info("Reading fat resultsets from " + filename);
         dis = new DataInputStream(Files.openFileStream(filename));
	}
	
	@Override
	public String getInfo() {
		return this.getClass().getSimpleName();
	}

	@Override
    public ResultSet match(String queryNumber, MatchingQueryTerms queryTerms)
		throws IOException
	{
        //return the FatResultSet for queryNumber queryNumber
        FatCandidateResultSet fatResultSet = new FatCandidateResultSet();
        boolean wrap = false;
        while (true) {
        	try{
                String qid = dis.readUTF();
                fatResultSet.readFields(dis);
                if (queryNumber.equals(qid))
                {
                	logger.info("Found fat resultset for " + qid + " with " + fatResultSet.getResultSize() + " results");
                	if (maxResults == 0 || fatResultSet.getResultSize() < maxResults)
                		return fatResultSet;
                	logger.info("Cropping fat results to " + maxResults + " results");
                	return fatResultSet.getResultSet(0, maxResults);
                }
        	}catch (EOFException eof) {
        		if (wrap)
        			return null;
        		logger.info("Reopening: Reading fat resultsets from " + filename);
        		dis = new DataInputStream(Files.openFileStream(filename));
        		wrap = true;
        		continue;
        	}
        }
    }

	@Override
	public void setCollectionStatistics(CollectionStatistics cs) {}

}
