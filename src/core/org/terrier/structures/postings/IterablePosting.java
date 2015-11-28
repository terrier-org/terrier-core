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
 * The Original Code is IterablePosting.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 *   Nicola Tonellotto
 */
package org.terrier.structures.postings;

import java.io.Closeable;
import java.io.IOException;
/** An interface that allows a list of postings to be iterated over 
 * @since 3.0
 * @author Craig Macdonald, Nicola Tonnellotto
 */
public interface IterablePosting extends Posting, Closeable {
	
	/** Value which denotes that the end of the posting list has been reached */
	int EOL = -1;
	
	/** Move this iterator to the next posting.
     * @return id of next posting, or EOL if end of posting list 
     */
	int next() throws IOException;
	
	/** Move this iterator to the posting with specified id, or next posting after that.
	 * This is usually implemented internally by next(), but more
	 * efficient implementations can override this behaviour.
	 * @param targetId id of the posting to find in this postingn list.
	 * @return id of the posting found, or EOL if end of posting list.
	 */
	int next(int targetId) throws IOException;
	
	/** Status method to see if this posting list iterator has been finished
	 * @return true if next() or next(int) would return EOL or have returned EOL.
	 */
	boolean endOfPostings();
}
