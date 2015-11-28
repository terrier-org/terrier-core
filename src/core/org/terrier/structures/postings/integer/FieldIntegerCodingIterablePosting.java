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
 * The Original Code is FieldIntegerCodingIterablePosting.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 */

package org.terrier.structures.postings.integer;

import java.io.IOException;

import org.terrier.compression.integer.ByteIn;
import org.terrier.compression.integer.codec.IntegerCodec;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.FieldDocumentIndex;
import org.terrier.structures.FieldDocumentIndexEntry;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.FieldPostingImpl;
import org.terrier.structures.postings.WritablePosting;
import org.terrier.utility.ArrayUtils;

/**
 * An IterablePosting implementation, which can optionally support Fields
 * 
 * @author Matteo Catena
 * @since 4.0
 */
public class FieldIntegerCodingIterablePosting extends
		BasicIntegerCodingIterablePosting implements FieldPosting {

	protected int[] fields;//current fields
	protected final int[][] fieldsMatrix;//all the ffs in the current chunk (uncompressed)
	protected final IntegerCodec fieldsCodec;
	
	protected final int fieldsCount;
	protected FieldDocumentIndex asFieldDocumentIndex;
	
	public FieldIntegerCodingIterablePosting(ByteIn input, int numberOfEntries,
			DocumentIndex documentIndex, int chunkSize, int fieldCount,
			IntegerCodec idsCodec, IntegerCodec tfsCodec, IntegerCodec fieldsCodec)
			throws IOException {
		super(input, numberOfEntries, documentIndex, chunkSize, idsCodec, tfsCodec);
		
		this.fieldsCount = fieldCount;
		this.fieldsCodec = fieldsCodec;
		fieldsMatrix = new int[fieldsCount][chunkSize];
		fields = new int[fieldsCount];
		asFieldDocumentIndex = (documentIndex instanceof FieldDocumentIndex) ? (FieldDocumentIndex) documentIndex : null;
	}

	
	
	@Override
	public WritablePosting asWritablePosting() {
		return new FieldPostingImpl(id, tf, fields);
	}



	@Override
	public int[] getFieldFrequencies() {

		if (fieldsCount <= 0)
			throw new UnsupportedOperationException();
		
		return fields;
	}

	@Override
	protected void decompress() throws IOException {
		super.decompress();
		for (int j = 0; j < fieldsCount; j++) {
			fieldsCodec.decompress(input, fieldsMatrix[j], chunkSize);
		}
	}

	@Override
	protected void skip() throws IOException {
		super.skip();
		for (int j = 0; j < fieldsCount; j++) {
			fieldsCodec.skip(input);
		}
	}

	@Override
	protected void get(int pos) throws IOException {
		super.get(pos);
		for (int j = 0; j < fieldsCount; j++) fields[j] = fieldsMatrix[j][pos] - 1; //-1, to deal with gamma and unary codec
	}

	@Override
	public int[] getFieldLengths() {
		
		if (fieldsCount <= 0)
			throw new UnsupportedOperationException();
		
		try {
			
			if (asFieldDocumentIndex != null) {
				
				return asFieldDocumentIndex.getFieldLengths(id);
			}
			else
			{
				FieldDocumentIndexEntry fdie = 
						((FieldDocumentIndexEntry)documentIndex.getDocumentEntry(id));
				return fdie.getFieldLengths();
				
			}	
			
		} catch (IOException ioe) {

			logger.error("Problem looking for doclength for document "+ id, ioe);			
			return new int[0];
		}
		
	}

	@Override
	public void setFieldLengths(int[] newLengths) {
		
		//TODO: what should we do here?
		throw new UnsupportedOperationException();
	}
	
	/** Makes a human readable form of this posting */
	@Override
	public String toString()
	{
		String F = (fieldsCount > 0) ? ",F[" + ArrayUtils.join(fields, ",") + "]" : "";
		//String B = (hasBlocks > 0) ? ",B[" + ArrayUtils.join(blocks, ",") + "]" : "";
		
		return "(" + id + "," + tf + F + ")";
	}

}
