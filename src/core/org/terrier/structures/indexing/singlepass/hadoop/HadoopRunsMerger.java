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
 * The Original Code is HadoopRunsMerger.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richardm{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   
 */
package org.terrier.structures.indexing.singlepass.hadoop;

import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;

import org.terrier.structures.BasicLexiconEntry;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.LexiconOutputStream;
import org.terrier.structures.indexing.singlepass.PostingInRun;
import org.terrier.structures.indexing.singlepass.RunIterator;
import org.terrier.structures.indexing.singlepass.RunIteratorFactory;
import org.terrier.structures.indexing.singlepass.RunsMerger;

/**
 * This is the main merger class for Hadoop runs. It provides functionality for
 * the merging of lexicons and inverted index shards from the map task indexers.
 * @since 2.2
 * @author Richard McCreadie and Craig Macdonald
  */
public class HadoopRunsMerger extends RunsMerger {

	/** The data loaded from side-effect files about each map task */ 
	protected LinkedList<MapData> mapData = null;
	
	/** Number of Reducers Used*/
	protected int numReducers = 0;
	/**
	 * Constructs an instance of HadoopRunsMerger.
	 * @param _runsSource
	 */
	public HadoopRunsMerger(RunIteratorFactory _runsSource) {
		super(_runsSource);
	}

	/**
	 * Alternate Merge operation for merging a linked list of runs of the form
	 * Hadoop_MapData. This routine merges the multiple runs
	 * created during the map process of hadoop indexing as such it corrects for
	 * Document id 'shift' caused by random splitting of runs due to flushing and
	 * map splitting.
	 * @param _mapData - information about the number of documents per map and run. One element for every map.
	 * @throws IOException
	 */
	public void beginMerge(LinkedList<MapData> _mapData)
	{
		mapData = _mapData;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public void endMerge(LexiconOutputStream<String> lexStream) {}
	/** 
	 * {@inheritDoc} 
	 */
	public void mergeOne(LexiconOutputStream<String> lexStream) throws Exception
	{	
		int maxDF = 0;
		RunIterator run = runsSource.createRunIterator(-1);
		HadoopRunPostingIterator _run = (HadoopRunPostingIterator)run;
		
		lastTermWritten = null;
		lastFreq = 0;
		lastDocFreq= 0;
		lastDocument = -1;
		long startOffset = this.getByteOffset();
		byte startBitOffset = this.getBitOffset();
		LexiconEntry le = null;
		// for each run in the list 
		//for one term: for each set of postings for that term
		while (run.hasNext()) {
			PostingInRun posting = run.next();
			lastTermWritten = posting.getTerm();
			
			if (posting.getDf() > maxDF) 
				maxDF = posting.getDf();
			
			//final int _runMapID = TaskID.forName(_run.getMapNo()).getId();
			//final int runNumber = run.getRunNo();
			final int docOffset = getDocumentOffset(_run.getSplitNo(), _run.getRunNo());
			lastDocument = posting.append(bos, lastDocument, docOffset);
			if (le == null)
				le = posting.getLexiconEntry();
			else
				posting.addToLexiconEntry(le);
			lastFreq += posting.getTF();
			lastDocFreq += posting.getDf();
		}
		le.setTermId(currentTerm++);
		((BasicLexiconEntry)le).setOffset(startOffset, startBitOffset);
		lexStream.writeNextEntry(lastTermWritten, le);
		numberOfPointers += lastDocFreq;
	}
	
	/** Gets the number of Reducers to Merge for:
	 * 1 for single Reducer,
	 * &gt;1 for multi-Reducers
	 * @return how many reducers are in use.
	 */
	public int getNumReducers() {
		return numReducers;
	}

	/** Sets the number of Reducers to Merge for:
	 * 1 for single Reducer,
	 * &gt;1 for multi-Reducers
	 */
	public void setNumReducers(int _numReducers) {
		this.numReducers = _numReducers;
	}
	/** 
	 * Get the offset for the document based on a split and flush.
	 */
	public int getDocumentOffset(int splitNo, int flushNumber) throws IOException {
		int NumPreDocs = 0;
		MapData correctHRD = null;
		for (MapData tempHRD : mapData)
		{
			if (splitNo == tempHRD.getSplitnum() ) {
				//System.out.println("Reducer number : "+reduceNumber+", Splitnum"+tempSplitnum+", Run Map Number : "+_run.getMapNo());
				correctHRD = tempHRD;
				break;
			}
			NumPreDocs += tempHRD.getMapDocs();
		}
		if (correctHRD == null)
			throw new IOException("Did not find map data for split "+ splitNo);
		
		int currentFlushDocs=0;
		// Add the FlushShift, if configured to do so 
		if (Hadoop_BasicSinglePassIndexer.RESET_IDS_ON_FLUSH)
		{		
			
			ListIterator<Integer> LI = correctHRD.getFlushDocSizes().listIterator(0);
			//System.out.println("Runs Flush number : "+run.getRunNo()+", Size of HRD :"+correctHRD.getFlushDocSizes().size());
			int currentFlush =0;
			while (currentFlush<flushNumber) {
				//System.out.println("Runs Flush number : "+run.getRunNo()+", FlushCheck : "+currentFlush+", Size of HRD :"+correctHRD.getFlushDocSizes().size());
				currentFlushDocs += LI.next(); 
				currentFlush++;
			}		
		}
		return NumPreDocs+currentFlushDocs;
		
	}

}
