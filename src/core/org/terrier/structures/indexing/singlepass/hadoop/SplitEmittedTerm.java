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
 * The Original Code is SplitEmittedTerm.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richardm{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures.indexing.singlepass.hadoop;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.CharacterCodingException;

import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobConfigurable;
import org.apache.hadoop.mapred.Partitioner;

/**
 * Represents a Term key used during MapReduce Indexing. Term keys are emitted from
 * each map task, and are used for sorting and partitioning the output. Paritioning
 * is done by splitno. Two options for sorting (a) term only, (b) term, split, flush
 * @author Richard McCreadie
 * @since 3.0
 */
@SuppressWarnings("deprecation")
public class SplitEmittedTerm implements WritableComparable<SplitEmittedTerm>{

	/** Should we use the hadoop readText method or java.io.readUTF? */
	private static final boolean USE_HADOOP_TEXT = false;
	
	/**
	 * Factory method for creating a new Term key object
	 * @param term
	 * @param splitno
	 * @param flushno
	 * @return a new split emitted term.
	 */
	public static SplitEmittedTerm createNewTerm(String term, int splitno, int flushno) {
		return new SplitEmittedTerm(term, splitno, flushno);
	}
	
	/** The term */
	private String term;
	/** The split that this instance of the term has been processed by */ 
	private int splitno;
	/** The flush within the split that this instance of the term was emitted by */
	private int flushno;
	
	/**
	 * Empty Constructor
	 */
	public SplitEmittedTerm() {}
	
	/**
	 * Constructor for a Term key. Is used for sorting map output and partitioning
	 * posting lists between reducers. Each term is only unique in conjunction with
	 * the split and flush that it was emitted from.
	 * @param _term 
	 * @param _splitno
	 * @param _flushno
	 */
	public SplitEmittedTerm(String _term, int _splitno, int _flushno) {
		this.term = _term;
		this.splitno = _splitno;
		this.flushno = _flushno;
	}
	
	
	
	@Override
	public int hashCode() {
		return term.hashCode() + splitno + flushno;
	}

	@Override
	public boolean equals(Object _o)
	{
		if (! (_o instanceof SplitEmittedTerm))
			return false;
		SplitEmittedTerm o = (SplitEmittedTerm)_o;
		return this.term.equals(o.term) && this.splitno == o.splitno && this.flushno == o.flushno;		
	}
	
	@Override
	public String toString() {
		return term + ":" + splitno + ":" + flushno;
	}

	/**
	 * Read in a Term key object from the input stream 'in'
	 */
	public void readFields(DataInput in) throws IOException {
		if (USE_HADOOP_TEXT)
			term = Text.readString(in); 
		else 
			term = in.readUTF();
		splitno = WritableUtils.readVInt(in);
		flushno = WritableUtils.readVInt(in);
	}

	/**
	 * Write out this Term key to output stream 'out'
	 */
	public void write(DataOutput out) throws IOException {
		if (USE_HADOOP_TEXT)
			Text.writeString(out, term);
		else
			out.writeUTF(term);
		WritableUtils.writeVInt(out, splitno);
		WritableUtils.writeVInt(out, flushno);		
	}

	/**
	 * Compares this Term key to another term key. Note that terms are
	 * unique only in conjunction with their associated split and flush.  
	 */
	public int compareTo(SplitEmittedTerm term2) {
		int result;
		if ((result = term.compareTo(term2.getTerm()))!=0) return result;
		if ((result = splitno - term2.getSplitno())!=0) return result;
		return flushno - term2.getFlushno();
	}	
	
	

	/**
	 * @return the term
	 */
	public String getTerm() {
		return term;
	}

	/**
	 * @param _term the term to set
	 */
	public void setTerm(String _term) {
		this.term = _term;
	}

	/**
	 * @return the splitno
	 */
	public int getSplitno() {
		return splitno;
	}

	/**
	 * @param _splitno the splitno to set
	 */
	public void setSplitno(int _splitno) {
		this.splitno = _splitno;
	}

	/**
	 * @return the flushno
	 */
	public int getFlushno() {
		return flushno;
	}

	/**
	 * @param _flushno the flushno to set
	 */
	public void setFlushno(int _flushno) {
		this.flushno = _flushno;
	}
	
	/** Sorter by term only */
	public static class SETRawComparatorTerm implements RawComparator<SplitEmittedTerm>, Serializable
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Compares raw Term key 1 to raw Term key 2. Note that only terms are considered.  
		 */
//		public int compare(byte[] bterm1, int offset1, int length1, byte[] bterm2, int offset2,
//				int length2)
//		{
//			return Text.Comparator.compareBytes(bterm1, offset1, length1, bterm2, offset2, length2);
//		}
//		
		
		public int compare(byte[] bterm1, int offset1, int length1, byte[] bterm2, int offset2,
				int length2)
		{
			if (USE_HADOOP_TEXT)
			{
				try {
					return Text.decode(bterm1, offset1, length1).trim().compareTo(Text.decode(bterm2, offset2, length2).trim());
				} catch (CharacterCodingException e) {
					return 0;
				}
			}
			else
			{
				try {
					DataInputStream b1S = new DataInputStream(new ByteArrayInputStream(bterm1, offset1, length1));
					DataInputStream b2S = new DataInputStream(new ByteArrayInputStream(bterm2, offset2, length2));
					String term1 = b1S.readUTF();
					String term2 = b2S.readUTF();
					return term1.trim().compareTo(term2.trim());
				} catch (IOException e) {
					System.err.println("ERROR during raw comparision of term objects, unable to read input streams.");
					e.printStackTrace();
				}
				return 0;
			}
		}
		/** 
		 * {@inheritDoc} 
		 */
		public int compare(SplitEmittedTerm o1, SplitEmittedTerm o2) {
			return o1.getTerm().compareTo(o2.getTerm());
		}
	}
	/** 
	 * A comparator for comparing different split emitted terms. Note that this is a RAW
	 * comparator, i.e. the compare method that gets called is the byte[] one.
	 */
	public static class SETRawComparatorTermSplitFlush implements RawComparator<SplitEmittedTerm>, Serializable
	{
		private static final long serialVersionUID = 1L;
	
		/**
		 * Compares raw Term key 1 to raw Term key 2. Note that terms are
		 * unique only in conjunction with their associated split and flush.  
		 */		
		public int compare(byte[] bterm1, int offset1, int length1, byte[] bterm2, int offset2, int length2)
		{
			//this implementation doesnt create SplitEmittedTerm objects, saving a bit on gc
			DataInputStream b1S = new DataInputStream(new ByteArrayInputStream(bterm1, offset1, length1));
			DataInputStream b2S = new DataInputStream(new ByteArrayInputStream(bterm2, offset2, length2));
			try {
				String t1;
				String t2;
				if (USE_HADOOP_TEXT)
				{
					t1 = Text.readString(b1S);
					t2 = Text.readString(b2S);
				} else {
					t1 = b1S.readUTF();
					t2 = b2S.readUTF();
				}
				int result = t1.compareTo(t2);
				if (result != 0)
					return result;
				int i1 = WritableUtils.readVInt(b1S);
				int i2 = WritableUtils.readVInt(b2S);
				if (i1 != i2)
					return i1 - i2;
				i1 = WritableUtils.readVInt(b1S);
				i2 = WritableUtils.readVInt(b2S);
				return i1 - i2;
			} catch (IOException e) {
				System.err.println("ERROR during raw comparision of term objects, unable to read input streams.");
				e.printStackTrace();
				return 0;
			}			
		}

		/**
		 * Compares Term key 1 to Term key 2. Note that terms are
		 * unique only in conjunction with their associated split and flush.  
		 */
		public int compare(SplitEmittedTerm term1, SplitEmittedTerm term2) {
			return term1.compareTo(term2);
		}
	}
	
	/** Partitions SplitEmittedTerms by split that they came from.
	 */
	public static class SETPartitioner implements Partitioner<SplitEmittedTerm, MapEmittedPostingList>, JobConfigurable
	{
		/** The number of chunks the collection was split into */
		private int numSplits;
		
		/**
		 * Configure the partitioner functionality, i.e. calculate the
		 * number of splits there were.
		 */
		public void configure(JobConf conf) {
			// there is one split per map task
			numSplits = conf.getNumMapTasks();
		}
	
		/** Retuns the partition for the specified term and posting list, given the specified
		 * number of partitions.
		 */
		public int getPartition(SplitEmittedTerm term, MapEmittedPostingList posting,
				int numPartitions)
		{
			//System.err.println("set="+term.toString() + " partition="+ calculatePartition(term.getSplitno(), numPartitions));
			return calculatePartition(term.getSplitno(), numPartitions);
		}
		
		/** Calculates the partitions for a given split number.
		 * @param splitno - which split index, starting at 0
		 * @param numPartitions - number of partitions (reducers) configured
		 * @return the reduce partition number to allocate the split to. */
		public int calculatePartition(int splitno, int numPartitions) {
			final int partitionSize = (int) (Math.ceil((double)numSplits / (double) numPartitions ));
			return splitno / partitionSize;
		}
	}
	
	/** Partitions SplitEmittedTerms by term. This version assumes that most initial characters are in lowercase a-z.
	 * 0-9 will goto the first partition, all character higher than 'z' will go to the last partition.
	 */
	public static class SETPartitionerLowercaseAlphaTerm implements Partitioner<SplitEmittedTerm, MapEmittedPostingList>
	{
		/** Retuns the partition for the specified term and posting list, given the specified
		 * number of partitions.
		 */
		public int getPartition(SplitEmittedTerm term, MapEmittedPostingList posting,
				int numPartitions)
		{
			//System.err.println("set="+term.toString() + " partition="+ calculatePartition(term.getSplitno(), numPartitions));
			return calculatePartition(term.getTerm().charAt(0),
					numPartitions);
		}
		
		/** Calculates the partitions for a given split number.
		 * @param _initialChar - what's the first character in the term
		 * @param numPartitions - number of partitions (reducers) configured
		 * @return the reduce partition number to allocate the split to. */
		public int calculatePartition(char _initialChar, int numPartitions) {
			final int partitionSize = (int) (Math.ceil( 26.0d / (double) numPartitions ));
			int initialChar = (int)_initialChar;
			if (initialChar < 'a')
				return 0;
			if (initialChar > 'z')
				return numPartitions -1;
			return (initialChar - 97) / partitionSize;
		}
		/** 
		 * {@inheritDoc} 
		 */
		public void configure(JobConf jc) {}
	}

	
	
	
}
