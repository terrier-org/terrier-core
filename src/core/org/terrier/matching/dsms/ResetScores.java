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
 * The Original Code is ResetScores.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.matching.dsms;

import java.util.Arrays;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.ResultSet;
import org.terrier.structures.Index;
import org.terrier.utility.ApplicationSetup;
/** Resets the scores in the resultset to 0.00001d
 * @since 3.5
 * @author Craig Macdonald
 */
public class ResetScores implements DocumentScoreModifier {

	protected final double DEFAULT = Double.parseDouble(ApplicationSetup.getProperty("reset.scores.default", "0.00001d"));
	
	@Override
	public String getName() {
		return "ResetScores";
	}

	@Override
	public boolean modifyScores(Index index, MatchingQueryTerms queryTerms,
			ResultSet resultSet) 
	{
		Arrays.fill(resultSet.getScores(), DEFAULT);
		return true;
	}
	/** {@inheritDoc}*/ @Override
	public Object clone() {
		return this;
	}

}
