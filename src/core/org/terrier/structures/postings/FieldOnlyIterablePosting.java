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
	
	public FieldOnlyIterablePosting(IterablePosting _ip, int _fieldId) throws Exception
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
