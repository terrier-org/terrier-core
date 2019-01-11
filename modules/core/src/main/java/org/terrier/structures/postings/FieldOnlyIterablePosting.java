/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.ac.gla.uk
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
 * The Original Code is FieldOnlyIterablePosting.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures.postings;

import java.io.IOException;

/** This class takes an IterablePosting list that has fields, 
 * and makes it appear as a single basic posting list.
 * @author Craig Macdonald
 * @since 4.2
 */
public class FieldOnlyIterablePosting extends IterablePostingImpl {

	FieldPosting fieldParent;
	IterablePosting iterableParent;
	int fieldId;
	int currentId;
	int frequency = 0;
	
	public FieldOnlyIterablePosting(IterablePosting _ip, int _fieldId) 
	{
		this.fieldId = _fieldId;
		this.iterableParent = _ip;
		this.fieldParent = (FieldPosting) _ip;
		
	}
	
	@Override
	public int next() throws IOException {

		while( (currentId = iterableParent.next()) != EOL)
		{
			final int fieldFreq = fieldParent.getFieldFrequencies()[fieldId];
			if (fieldFreq > 0)
			{
				frequency = fieldFreq;
				return currentId;
			}
		}
		return EOL;
	}

	@Override
	public boolean endOfPostings() {
		return currentId != EOL;
	}

	@Override
	public int getId() {
		return currentId;
	}

	@Override
	public int getFrequency() {
		return frequency;
	}

	@Override
	public int getDocumentLength() {
		return fieldParent.getFieldLengths()[fieldId];
	}

	@Override
	public void setId(int id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public WritablePosting asWritablePosting() {
		return new BasicPostingImpl(this.currentId, this.frequency);
	}

	@Override
	public void close() throws IOException {
		iterableParent.close();
	}

}
