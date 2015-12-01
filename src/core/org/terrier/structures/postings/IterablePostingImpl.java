/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is IterablePostingImpl.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.postings;

import java.io.IOException;
/** A base implementation of an IterablePosting, that provides
 * a base implementation of next(int) method.
 * @since 3.0
 * @author Craig Macdonald
 */
public abstract class IterablePostingImpl implements IterablePosting {

	/**
	 * This implementation of next(int) which uses next() 
	 */
	public int next(int target) throws IOException {
		do
		{
			if (this.next() == EOL)
				return EOL;
		} while(this.getId() < target);
		return this.getId();
	}
	
	
}
