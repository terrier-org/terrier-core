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
 * The Original Code is CandidateResult.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Nicola Tonellotto (original author)
 *   
 */
package org.terrier.matching.daat;

/** A class used to when maintaining a top-k candidate documents ResultSet.
 * 
 * @author Nicola Tonnelotto
 * @since 3.5
 * @see CandidateResultSet
 */
public class CandidateResult implements Comparable<CandidateResult> 
{
	private int docid;
	private double score;
	private short occurrence;

	/** Make a new CandidateResult for a ResultSet based on the
	 * specified docid.
	 * @param docid of the document
	 */
	public CandidateResult(int docid)
	{
		this.docid = docid;
		score = 0.0;
		occurrence = 0;
	}
	
	/** {@inheritDoc}. Enforces a sort by <i>ascending</i> score. */
	@Override
	public int compareTo(final CandidateResult o) 
	{
		if (this.score < o.score)
			return -1;
		else if (this.score > o.score)
			return 1;
		else
			return 0;
	}	
	
	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof CandidateResult))
			return false;
		return Double.compare(((CandidateResult)obj).score, this.score) == 0;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return getDocId();
    }

	/** Returns the docid of this result */
	public int    getDocId() 	  { return docid;      }
	
	/** Returns the score of this result */
	public double getScore() 	  { return score; 	   }
	
	/** Returns the occurrence value of this result */
	public short  getOccurrence() { return occurrence; }
	
	/** Increase the score by the specified amount.
	 * @param update Amount to increase document score by.
	 */
	public void updateScore(double update) 	   { this.score += update;      }
	/** Update the occurrence value of this result.
	 * @param update Mask to OR with current occurrence
	 */
	public void updateOccurrence(short update) { this.occurrence |= update; }
}
