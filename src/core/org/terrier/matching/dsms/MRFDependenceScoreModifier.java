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
 * The Original Code is MRFDependenceScoreModifier.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.matching.dsms;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.ResultSet;
import static org.terrier.matching.models.WeightingModelLibrary.log;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.Index;
import org.terrier.utility.ApplicationSetup;

/** Implements Markov Random Fields. See Metzler & Croft, SIGIR 2005.
 * Note that this implementation does not utilise the frequency of a
 * tuple in the collection - instead, this is assumed to be a constant,
 * as per the implementation in the Ivory retrieval system.
 * <b>Properties:</b>
 * <ul>
 * <li><i>See properties for DependenceScoreModifier</i></li>
 * <li><tt>mrf.mu</tt> - Mu of MRF model, in the Dirichlet model.</li>
 * </ul>
 * <b>References:</b>
 * Metzler, D. and Croft, W.B., "A Markov Random Field Model for Term Dependencies," Proceedings of the 28th annual international ACM SIGIR conference on Research and development in information retrieval (SIGIR 2005), 472-479, 2005
 * 
 * @author Craig Macdonald
 * @since 3.0
 */
public class MRFDependenceScoreModifier extends DependenceScoreModifier {

	protected double MU = Double.parseDouble(ApplicationSetup.getProperty("mrf.mu", "4000d"));
		//ApplicationSetup.getProperty("proximity.norm2.c", "4000d")));
	double defaultDf;
	double defaultCf;
	
	/** 
	 * {@inheritDoc} 
	 */
	@Override	public boolean modifyScores(Index index, MatchingQueryTerms terms,
			ResultSet set)
	{
		MU = Double.parseDouble(ApplicationSetup.getProperty("mrf.mu", ApplicationSetup.getProperty("proximity.norm2.c", "4000d")));
		return super.modifyScores(index, terms, set);
	}

	/** 
	 * {@inheritDoc} 
	 */
	@Override	protected double scoreFDSD(int matchingNGrams, int _docLength) {		
		final double mu = MU;
		double docLength = (double)_docLength;
		double tf = (double)matchingNGrams;
		return w_o * (log(1 + (tf/(mu * (defaultCf / super.numTokens)))) + log(mu/(docLength+mu)));
	}

	/** 
	 * {@inheritDoc} 
	 */
	@Override
	public void setCollectionStatistics(CollectionStatistics cs, Index _index) {
		super.setCollectionStatistics(cs, _index);
		w_o = Double.parseDouble(ApplicationSetup.getProperty("proximity."+super.ngramLength+".w_o", 
				ApplicationSetup.getProperty("proximity.w_o", "1.0d")));
		//these statistics are as used by Ivory system
		defaultDf = ((double) cs.getNumberOfDocuments())  / 100.0d;
		defaultCf = defaultDf * 2;
	}

}
