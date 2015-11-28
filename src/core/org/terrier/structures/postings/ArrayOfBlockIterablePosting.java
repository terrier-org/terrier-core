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
 * The Original Code is ArrayOfBlockIterablePosting.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */

package org.terrier.structures.postings;

import java.io.IOException;

/**
 * An array structure that golds BlockIterablePostings
 * 
 * @author Craig Macdonald
 * @since 4.0
 *
 */
public class ArrayOfBlockIterablePosting extends ArrayOfBasicIterablePosting implements BlockPosting {

	final int[] posCount;
	final int[] allpos;
	int positionIndice = 0;
	
	public ArrayOfBlockIterablePosting(int[] _ids, int[] _freqs, int[] posCount, int[] allpos) {
		super(_ids, _freqs);
		this.posCount = posCount;
		this.allpos = allpos;
	}

	@Override
	public int[] getPositions() {
		final int numberOfPositions = posCount[this.indice];
		final int[] rtr = new int[numberOfPositions];
		System.arraycopy(allpos, positionIndice, rtr, 0, numberOfPositions);
		return rtr;

	}

	@Override
	public int next() throws IOException {
		int rtr = super.next();
		if (indice > 0) 
			positionIndice += posCount[indice-1];
		return rtr;
	}

}
