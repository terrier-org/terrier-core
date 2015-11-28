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
 * The Original Code is MapEmittedPostingList.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richardm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.structures.indexing.singlepass.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


/**
 * Sub-Class of WritableByteArray, i.e. the posting list,
 * with additional information about which Map and Flush 
 * it came from.
 * @author Richard McCreadie
  * @since 2.2
 */
public class MapEmittedPostingList extends WritableByteArray{
	
	/** The Map Number */
	protected String Map;
	/** The Flush Number */
	protected int flushNo;
	/** The Split Number */
	protected int splitNo;
	
	/**
	 * Constructor
	 * @param map - Map task id
	 * @param flush - Flush Number
	 * @param split - Split Number
	 * @param c - Document Frequency
	 * @param c2 - Term Frequency
	 */
	public MapEmittedPostingList (String map, int flush, int split, int c, int c2) {
		super(c,c2);
		Map = map;
		flushNo =flush;
		splitNo = split;
	}
	
	/**
	 * Super Constructor
	 * @param c - Document Frequency
	 * @param c2 - Term Frequency
	 */
	public MapEmittedPostingList (int c, int c2) {
		super(c,c2);
	}
	
	/**
	 * Empty Constructor
	 */
	public MapEmittedPostingList () {
		super();
	}
	
	/**
	 * Factory Method
	 * @param mapTaskID - Map Number
	 * @param flushNo - Flush Number
	 * @param splitNo - Split Number
	 * @param postingList - Posting List
	 * @param DocumentFreq - Document Frequency
	 * @param TermFreq - Term Frequency
	 * @return a newly created Indexing_WritableRunPostingData
	 */
	public static MapEmittedPostingList create_Hadoop_WritableRunPostingData (String mapTaskID, int flushNo, int splitNo, byte[] postingList, int DocumentFreq, int TermFreq) {
		MapEmittedPostingList w = new MapEmittedPostingList(mapTaskID, flushNo, splitNo, DocumentFreq, TermFreq);
		w.setArray(postingList);
		return w;
	}
	/**
	 * Super Factory Method
	 * @param postingList - Posting List
	 * @param DocumentFreq - Document Frequency
	 * @param TermFreq - Term Frequency
	 * @return a newly created Indexing_WritableRunPostingData
	 */
	public static MapEmittedPostingList create_Hadoop_WritableRunPostingData (byte[] postingList, int DocumentFreq, int TermFreq) {
		MapEmittedPostingList w = new MapEmittedPostingList(DocumentFreq, TermFreq);
		w.setArray(postingList);
		return w;
		
	}
	
	/**
	 * Returns the Map & Flush Number
	 */
	public String toString() {
		return "MapNo="+Map+ ",FlushNo="+flushNo;
	}

	/**
	 * get Map
	 * @return map
	 */
	public String getMap() {
		return Map;
	}

	/**
	 * set map
	 * @param map
	 */
	public void setMap(String map) {
		Map = map;
	}
	
	/**
	 * get flush no
	 * @return flush number
	 */
	public int getFlushNo() {
		return flushNo;
	}

	/**
	 * set flush no
	 * @param flush
	 */
	public void setFlushNo(int flush) {
		flushNo = flush;
	}

	/**
	 * @return the splitNo
	 */
	public int getSplitNo() {
		return splitNo;
	}

	/**
	 * @param _splitNo the splitNo to set
	 */
	public void setSplitNo(int _splitNo) {
		this.splitNo = _splitNo;
	}

	/**
	 * Reads this object from the input stream 'in' 
	 */
	public void readFields(DataInput in) throws IOException {
		arraylength = in.readInt();
		Map = in.readUTF();
		flushNo = in.readInt();
		splitNo = in.readInt();
		DocumentFreq = in.readInt();
		TermFreq = in.readInt();
		array = new byte[arraylength];
		in.readFully(array);
		//System.err.println("DEBUG: Finished Read, ArrayL:"+arraylength+" RunNo:"+Run+" DocF:"+DocumentFreq+" TermF:"+TermFreq+" Buffer:"+array.toString());
		
	}
	
	/**
	 * Reads this object from the input stream 'in' apart from the
	 * array. 
	 * @param in
	 * @throws IOException
	 */
	public void readFieldsMinusArray(DataInput in) throws IOException {
		arraylength = in.readInt();
		Map = in.readUTF();
		flushNo = in.readInt();
		splitNo = in.readInt();
		DocumentFreq = in.readInt();
		TermFreq = in.readInt();
		array = new byte[1];
		//System.err.println("DEBUG: Finished Read, ArrayL:"+arraylength+" RunNo:"+Run+" DocF:"+DocumentFreq+" TermF:"+TermFreq+" Buffer:"+array.toString());
		
	}

	/** Write this object to the output stream 'out' */
	public void write(DataOutput out) throws IOException {
		out.writeInt(array.length);
		out.writeUTF(Map);
		out.writeInt(flushNo);
		out.writeInt(splitNo);
		out.writeInt(DocumentFreq);
		out.writeInt(TermFreq);
		out.write(array);
		//System.err.println("DEBUG: Finished Write, ArrayL:"+array.length+" RunNo:"+Run+" DocF:"+DocumentFreq+" TermF:"+TermFreq+" Buffer:"+array.toString());
	}
	/**
	 * print array
	 */
	public void printArray() {
		System.err.print("DEBUG: Posting Buffer Contents: ");
		for (int i = 0; i<array.length; i=i+1) {
			System.err.print(array[i]);
		}
		System.err.println();
	}

}
