/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - Department of Computing Science
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
 * The Original Code is FixedSizeTextFactory.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.seralization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Makes a {@link org.apache.hadoop.io.Text} object that has a fixed size when written using {@link org.apache.hadoop.io.Writable} methods.
 * 
 * @author Craig Macdonald
 * @since 3.0
 * @see org.apache.hadoop.io.Text
 * @see FixedSizeWriteableFactory
 * @see org.apache.hadoop.io.Writable
 */
public class FixedSizeTextFactory implements FixedSizeWriteableFactory<Text> {
	protected static final Logger logger = LoggerFactory.getLogger(FixedSizeTextFactory.class);
	class FixedSizeText extends Text {
		public FixedSizeText() {
			super();
		}

		public FixedSizeText(byte[] b) {
			super(b);
		}

		public FixedSizeText(String s) {
			super(s);
		}

		public FixedSizeText(Text t) {
			super(t);
		}

		@Override
		public void readFields(DataInput in) throws IOException {
			super.readFields(in);
			//System.err.println("Term "+this.toString() + " read in "+ (this.getLength()+WritableUtils.getVIntSize(this.getLength())) + " bytes");
			in.skipBytes(maxKeyWrittenSize - (this.getLength()+WritableUtils.getVIntSize(this.getLength())));
		}

		@Override
		public void write(DataOutput out) throws IOException {
			super.write(out);
			try{
				out.write(ZERO_BUFFER, 0, maxKeyWrittenSize - (this.getLength()+WritableUtils.getVIntSize(this.getLength())));
			}catch (ArrayIndexOutOfBoundsException aioobe) {
				logger.error("Term "+this.toString() + " written in " + (this.getLength()+WritableUtils.getVIntSize(this.getLength())) 
						+ " bytes. Max expected size was "+maxKeyWrittenSize 
						+ ". Increase relevant property: max.term.length for Lexicon, or indexer.meta.forward.keylens for metadata",
					aioobe);
			}
		}
	}
	
	final byte[] ZERO_BUFFER;
	final int termLength;
	final int maxKeyWrittenSize;
	
	
	/** Make a factory object with the specified length, decoded using Integer.parseInt() */
	public FixedSizeTextFactory(String _termLength)
	{
		this(Integer.parseInt(_termLength));
	}
	
	/** Make a factory object with the specified length */
	public FixedSizeTextFactory(int _termLength)
	{
		this.termLength = _termLength; //TODO : consider non-utf terms - need to derive maximum size
		this.maxKeyWrittenSize = getMaximumTextLength(_termLength);
		logger.debug("FixedSizeTextFactory: maxKeyWrittenSize="+maxKeyWrittenSize + " for term of size "+ termLength);
		ZERO_BUFFER = new byte[maxKeyWrittenSize];
	}
	
	/** Returns the size of instance of this class when written using Writable */
	public int getSize() 
	{		
		return maxKeyWrittenSize;
	}
	
	/** For the Hadoop Text class, given a String of charCount, how long
	 * is the maximum encoded bytes?
	 * @param charCount maximum length of the String
	 * @return maximum number of bytes
	 */
	public static int getMaximumTextLength(int charCount)
	{
		return WritableUtils.getVIntSize(charCount) + 3*charCount;
	}

	
	/** Returns a new instance of Text with desired properties */
	public Text newInstance() {
		return new FixedSizeText();
	}
}
