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
 * The Original Code is PerFieldNormWeightingModel.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.matching.models;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.matching.models.basicmodel.BasicModel;
import org.terrier.matching.models.normalisation.Normalisation;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.FieldLexiconEntry;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.Posting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.StaTools;

/** 
 * A class for generating arbitrary per-field normalisation models.
 * Per-field normalisation models are field-based models - i.e.
 * they take into account the frequency of a term in each individual
 * field of the document. In particular, they apply length normalisation
 * on the frequency from each field, before using a linear combination
 * of term frequencies to garner the final frequency.
 * <p><b>Properties:</b></p>
 * <ul>
 * <li>w.fieldId (starting at 0)</li>
 * <li>c.fieldId (starting at 0)</li>
 * </ul>
 * @author Craig Macdonald
 * @since 3.0
 */
public class PerFieldNormWeightingModel extends WeightingModel {

	private static final long serialVersionUID = 1L;
	protected static final Logger logger = LoggerFactory.getLogger(PerFieldNormWeightingModel.class);

	protected String[] params;
	protected BasicModel basicModel;
	protected Normalisation[] fieldNormalisations;
	
	protected double[] fieldGlobalFrequencies;
	protected double[] fieldWeights;
	protected int fieldCount;
	
	Class<? extends Normalisation> normClass;
	/** 
	 * Constructs an instance of PerFieldNormWeightingModel
	 * @param _basicModel
	 * @param _normalisationModel
	 * @throws Exception
	 */
	public PerFieldNormWeightingModel(
			Class<? extends BasicModel> _basicModel,
			Class<? extends Normalisation> _normalisationModel) throws Exception
	{
		this.params = new String[]{_basicModel.getSimpleName(), _normalisationModel.getSimpleName()};
		this.basicModel = _basicModel.newInstance();
		this.normClass = _normalisationModel;
	}
	/** 
	 * Constructs an instance of PerFieldNormWeightingModel
	 * @param parameters
	 * @throws Exception
	 */
	public PerFieldNormWeightingModel(String[] parameters) throws Exception
	{
		this.params = parameters;
		this.basicModel = ApplicationSetup.getClass(parameters[0]).asSubclass(BasicModel.class).newInstance();
		this.normClass = ApplicationSetup.getClass(parameters[1]).asSubclass(Normalisation.class);
	}
	
	@Override
	public PerFieldNormWeightingModel clone() {
		PerFieldNormWeightingModel rtr = (PerFieldNormWeightingModel) super.clone();
		rtr.basicModel = (BasicModel) this.basicModel.clone();
		rtr.params = Arrays.copyOf(this.params,  this.params.length);
		if (fieldGlobalFrequencies != null)
			rtr.fieldGlobalFrequencies = Arrays.copyOf(fieldGlobalFrequencies, this.fieldCount);
		if (fieldWeights != null)
		rtr.fieldWeights = Arrays.copyOf(fieldWeights, this.fieldCount);
		if (fieldNormalisations != null)
			rtr.fieldNormalisations = new Normalisation[this.fieldCount];
		for(int fi=0;fi<fieldCount;fi++)
			rtr.fieldNormalisations[fi] = fieldNormalisations[fi].clone();
			
		return rtr;
	}
	
	@Override
	public void prepare() {
		super.prepare();
		for(int fi=0;fi<fieldCount;fi++)
		{
			String weightControlName = "w." + fi;
			if (rq.hasControl(weightControlName)) {
				fieldWeights[fi] = Double.parseDouble(rq.getControl(weightControlName)); 
			}
			String paramControlName = "c." + fi;
			if (rq.hasControl(paramControlName)) {
				this.fieldNormalisations[fi].setParameter(Double.parseDouble(rq.getControl(paramControlName)) );
			}
		}
	}

	@Override
	public double score(Posting _p) {
		FieldPosting p = (FieldPosting)_p;
		final int[] tff = p.getFieldFrequencies();
		final int[] fieldLengths = p.getFieldLengths();
		final double[] normFieldFreqs = new double[fieldCount];
		for(int i=0;i<fieldCount;i++)
		{
			if (tff[i] != 0.0d)
				normFieldFreqs[i] = fieldWeights[i] * fieldNormalisations[i].normalise(tff[i], fieldLengths[i], fieldGlobalFrequencies[i]);
		}
		final double tf = StaTools.sum(normFieldFreqs);
		//System.err.println("tf=" + tf);
		if (tf == 0.0d)
			return 0.0d;
		return basicModel.score(tf, super.documentFrequency, super.termFrequency, super.keyFrequency, p.getDocumentLength());
	}

	@Override
	public void setCollectionStatistics(CollectionStatistics _cs) 
	{
		super.setCollectionStatistics(_cs);
		fieldCount = _cs.getNumberOfFields();
		WeightingModelLibrary.checkForFields(_cs);
		if (fieldCount == 1) {
			logger.warn("Weighting model " + this.getClass().getSimpleName() + " is invoked on an index with only 1 field - this is probably incorrect.");
		}

		basicModel.setNumberOfDocuments(_cs.getNumberOfDocuments());
		basicModel.setNumberOfTokens(_cs.getNumberOfTokens());
		
		fieldNormalisations = new Normalisation[fieldCount];
		fieldGlobalFrequencies  = new double[fieldCount];
		fieldWeights = new double[fieldCount];
		try {			
			for(int fi=0;fi<fieldCount;fi++)
			{
				fieldWeights[fi] = Double.parseDouble(ApplicationSetup.getProperty("w."+ fi, ""+1.0));
				Normalisation nf = this.fieldNormalisations[fi] = normClass.newInstance();
				final double param = Double.parseDouble(ApplicationSetup.getProperty("c."+ fi, ""+1.0));
				nf.setParameter(param);
				nf.setNumberOfDocuments(_cs.getNumberOfDocuments());
				final long tokensf = _cs.getFieldTokens()[fi];
				nf.setNumberOfTokens(tokensf);
				nf.setAverageDocumentLength(_cs.getAverageFieldLengths()[fi]);				
			}	
		} catch (Exception e) {
			throw new IllegalStateException(e);			
		}
	}

	/** 
	 * {@inheritDoc} 
	 */
	@Override
	public void setEntryStatistics(EntryStatistics _es) {
		super.setEntryStatistics(_es);
		if (! (_es instanceof FieldLexiconEntry))
			return;
		FieldLexiconEntry fes = (FieldLexiconEntry)_es;
		int[] reportedfieldGlobalFrequencies = fes.getFieldFrequencies();
		
		for(int i=0;i<fieldCount;i++)
		{
			fieldGlobalFrequencies[i] = getOverflowed(reportedfieldGlobalFrequencies[i]);
			fieldNormalisations[i].setDocumentFrequency(getOverflowed(es.getDocumentFrequency()));
		}
	}

	/** 
	 * {@inheritDoc} 
	 */	
	@Override
	public String getInfo() {
		StringBuilder s = new StringBuilder();
		s.append(this.getClass().getSimpleName());
		s.append('(');
		s.append(basicModel.getInfo());
		for(Normalisation n : fieldNormalisations)
		{
			s.append("Norm");
			s.append(n.getInfo());
			s.append(',');
		}
		s.append("w=");
		s.append(ArrayUtils.join(fieldWeights, ","));
		return s.toString();
	}


	@Override
	public double score(double tf, double docLength) {
		return 0;
	}


}
