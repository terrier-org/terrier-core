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
 * The Original Code is MRF.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.matching.models.dependence;

import static org.terrier.matching.models.WeightingModelLibrary.log;

import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.NgramEntryStatistics;

public class MRF extends WeightingModel {

	private static final long serialVersionUID = 1L;
	int ngramLength;
	double defaultDf;
	double defaultCf;
	
	public MRF(){}

	public MRF(int _ngramLength) {
		this.ngramLength = _ngramLength;
	}
	
	@Override
	public void prepare() {
		super.prepare();
		//these statistics are as used by Ivory system, of which Don Metzler was one of the authors
		defaultDf = ((double) cs.getNumberOfDocuments())  / 100.0d;
		defaultCf = defaultDf * 2;
	}

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName() + "_mu" + this.c;
	}
	
	@Override
	public void setEntryStatistics(EntryStatistics _es) {
		super.setEntryStatistics(_es);
		ngramLength = ((NgramEntryStatistics)_es).getWindowSize();
	}

	@Override
	public double score(double matchingNGrams, double _docLength) {
		final double mu = this.c;
		double docLength = (double)_docLength;
		double tf = (double)matchingNGrams;
		return (log(1 + (tf/(mu * (defaultCf / super.numberOfTokens)))) + log(mu/(docLength+mu)));
	}

}
