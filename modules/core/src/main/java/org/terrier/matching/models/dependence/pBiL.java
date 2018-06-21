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
 * The Original Code is pBiL.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.matching.models.dependence;

import org.terrier.matching.models.WeightingModel;
import org.terrier.statistics.GammaFunction;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.NgramEntryStatistics;

public class pBiL extends WeightingModel {

	private static final long serialVersionUID = 1L;
	protected static final double REC_LOG_2 = 1.0d / Math.log(2.0d);
	protected static final GammaFunction gf = GammaFunction.getGammaFunction();
	
	boolean norm2 = false;
	int ngramLength;
	
	public pBiL() {}
	
	
	public pBiL(int _ngramLength) {
		this.ngramLength = _ngramLength;
	}
	
	@Override
	public String getInfo() {
		return this.getClass().getSimpleName();
	}

	@Override
	public void setEntryStatistics(EntryStatistics _es) {
		super.setEntryStatistics(_es);
		ngramLength = ((NgramEntryStatistics)_es).getWindowSize();
	}


	@Override
	public double score(double matchingNGrams, double docLength) {
		
		if (matchingNGrams == 0)
			return 0.0d;
		
		
		final double numberOfNGrams = (docLength > 0 && docLength < ngramLength) ? 1
				: docLength - ngramLength + 1.0d;
		
		if (matchingNGrams >= numberOfNGrams)
			matchingNGrams = numberOfNGrams - 0.1d;
		
		double score = 0.0d;
		
		// apply Norm2 to pf?
		//System.err.println("C="+ super.c + " windows="+ numberOfNGrams + " avgDocLen="+ super.averageDocumentLength + " gf="+gf.getClass().getSimpleName());
		final double matchingNGramsNormalised = norm2 
				? ((double)matchingNGrams) * Math.log(1.0d + super.c * averageDocumentLength / numberOfNGrams) * REC_LOG_2
				: matchingNGrams;
		
		double background = norm2 ? averageDocumentLength : numberOfNGrams;
		if (background == 1d)
			background++;
		final double p = 1.0D / background;
		final double q = 1.0d - p;
		//System.err.println("background="+background + " p="+p + " q="+q);
		score = 
			- gf.compute_log(background + 1.0d) * REC_LOG_2
			+ gf.compute_log(matchingNGramsNormalised + 1.0d) * REC_LOG_2
			+ gf.compute_log(background - matchingNGramsNormalised+ 1.0d)* REC_LOG_2
			- matchingNGramsNormalised * Math.log(p) * REC_LOG_2
			- (background - matchingNGramsNormalised) * Math.log(q) * REC_LOG_2;
		score = score / (1.0d + matchingNGramsNormalised);
		assert !( Double.isInfinite(score) || Double.isNaN(score)) : "Score was "+score+", with:"
			+" matchingNGrams="+matchingNGrams
			+" docLength="+docLength
			+" background="+background
			+" matchingNGramsNormalised="+matchingNGramsNormalised;
		return score * this.keyFrequency;
	}

}
