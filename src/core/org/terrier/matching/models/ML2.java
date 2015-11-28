/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - Department of Computing Science
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
 * The Original Code is ML2.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>  (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> 
 */
package org.terrier.matching.models;

import org.terrier.matching.models.normalisation.Normalisation;
import org.terrier.matching.models.normalisation.Normalisation2;
import org.terrier.statistics.GammaFunction;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.FieldEntryStatistics;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.Posting;
import org.terrier.utility.ApplicationSetup;

/** This class implements the ML2 field-based weighting model. 
 * <p><b>Properties</b></p>:
 * <ul>
 * <li><tt>c.0</tt>, <tt>c.1</tt>, etc. Normalisation parameter for each field.
 * <li><tt>p.0</tt>, <tt>p.1</tt>, etc. Prior weight adjustments for each field. In the original
 * paper, all of these were left at the default value of 1.0d.
 * </ul>
 * <p>
 * If you use this model, please cite V. Plachouras and I. Ounis,
 * Multinomial Randomness Models for Retrieval with Document Fields, In Proc. ECIR 2007.
 * @author Vassilis Plachouras and Craig Macdonald
 * @since 3.0
 */
public class ML2 extends WeightingModel {
	static final double LOG2 = Math.log(2.0d);
	
	Class<? extends Normalisation> normClass;
	Normalisation[] fieldNormalisations;
	private static final long serialVersionUID = 1L;

	protected int fieldCount;
	protected double[] fieldWeights;
	protected double[] p;
	protected int[] fieldTermFrequencies;
	
	final GammaFunction gF = GammaFunction.getGammaFunction();
	double initialScore = 0.0d;
	
	/** 
	 * Constructs an instance of ML2
	 * @param parameters
	 * @throws Exception
	 */
	public ML2(String[] parameters) throws Exception
	{
		this.normClass = Class.forName(parameters[0]).asSubclass(Normalisation.class);
	}
	
	public ML2() 
	{
		this.normClass = Normalisation2.class;
	}
	
	@Override
	public String getInfo() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public void prepare() {
		super.prepare();
		initialScore = -gF.compute_log(super.termFrequency + 1.0d)/LOG2;
		
	}

	@Override
	public void setCollectionStatistics(CollectionStatistics _cs) {
		super.setCollectionStatistics(_cs);
		fieldCount = _cs.getNumberOfFields();
		p = new double[fieldCount];
		fieldWeights = new double[fieldCount];
		this.fieldNormalisations = new Normalisation[fieldCount];
		try{
			for(int fi=0;fi<fieldCount;fi++)
			{
				fieldWeights[fi] = Double.parseDouble(ApplicationSetup.getProperty("w."+ fi, ""+1.0));
				final Normalisation nf = normClass.newInstance();
				this.fieldNormalisations[fi] = nf;
				final double param = Double.parseDouble(ApplicationSetup.getProperty("c."+ fi, ""+1.0));
				nf.setParameter(param);
				nf.setNumberOfDocuments(_cs.getNumberOfDocuments());
				final long tokensf = _cs.getFieldTokens()[fi];
				nf.setNumberOfTokens(tokensf);
				nf.setAverageDocumentLength(_cs.getAverageFieldLengths()[fi]);
				p[fi] = 1.0d / ((double)fieldCount * (double)_cs.getNumberOfDocuments());
				//System.err.println("p["+fi+"]="+ p[fi]);
				p[fi] = p[fi] / Double.parseDouble( ApplicationSetup.getProperty("p." + fi, "1.0d"));
				//System.err.println("p["+fi+"]="+ p[fi]);
			}
		
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	/** 
	 * {@inheritDoc} 
	 */
	@Override
	public void setEntryStatistics(EntryStatistics _es) {
		super.setEntryStatistics(_es);
		fieldTermFrequencies = ((FieldEntryStatistics)_es).getFieldFrequencies();
	}

	/** 
	 * {@inheritDoc} 
	 */
	@Override
	public double score(Posting _p) {
		FieldPosting fp = (FieldPosting)_p;
		double q = 1.0d;
		double score = initialScore;
		
		double tf_q = super.termFrequency;
		double denom = 0.0d;
		final int[] tff = fp.getFieldFrequencies();
		final int[] fieldLengths = fp.getFieldLengths();
		//System.err.println("initial score=" + score);
		for(int fi = 0; fi < fieldCount; fi++)
		{
			if (tff[fi] == 0)
				continue;
			final double tfn_i = this.fieldNormalisations[fi].normalise(tff[fi], fieldLengths[fi], fieldTermFrequencies[fi]);
			score += (gF.compute_log(tfn_i+1) - tfn_i * Math.log(p[fi]) )/LOG2;
			//System.err.println("field " + fi + " lhs="+gF.compute_log(tfn_i+1) + " rhs=" + tfn_i * Math.log(p[fi]));
			denom += tfn_i;
			tf_q -= tfn_i;
			q -= p[fi];
			//System.err.println("field " + fi + " score=" + score);
		}
		score += (gF.compute_log(tf_q + 1.0d) - tf_q * Math.log(q))/ LOG2;
		//System.err.println("After score=" + score + " denom=" + denom);
		score = score / (denom + 1.0d);
		return keyFrequency * score;
	}
	
	@Override
	public double score(double tf, double docLength) {
		return 0;
	}


}
