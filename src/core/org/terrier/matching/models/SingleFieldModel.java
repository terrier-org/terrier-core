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
 * The Original Code is SingleFieldModel.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */


package org.terrier.matching.models;

import java.util.Arrays;

import org.terrier.structures.BasicLexiconEntry;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.EntryStatistics;
import org.terrier.structures.FieldEntryStatistics;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.Posting;
import org.terrier.utility.ArrayUtils;

/** Use a normal weighting model on a pre-determine subset of the field. 
 * Assumes that IDF can be calculated from all fields, 
 * but that TFc comes from the specified fields.
 * Usage: <tt>trec.model=SingleFieldModel(PL2,2)</tt>
 * 
 * @author Craig Macdonald
 * @since 4.0
 */
public class SingleFieldModel extends WeightingModel {
	private static final long serialVersionUID = 1L;
	String[] params;
	WeightingModel basicModel;	
	int[] activeFieldIds;
	
	public SingleFieldModel(String[] parameters) throws Exception
	{
		this.params = parameters;
		this.basicModel = WeightingModelFactory.newInstance(parameters[0]);
		activeFieldIds = new int[parameters.length -1];
		for(int i=1;i<parameters.length;i++)
		{
			activeFieldIds[i-1] = Integer.parseInt(parameters[i]);
		}
	}
	
	@Override
	public SingleFieldModel clone() {
		SingleFieldModel rtr = (SingleFieldModel) super.clone();
		rtr.basicModel = this.basicModel.clone();
		rtr.params = Arrays.copyOf(params, params.length);
		rtr.activeFieldIds = Arrays.copyOf(activeFieldIds, activeFieldIds.length);
		return rtr;
	}
	
	@Override
	public void prepare() {
		super.prepare();
		basicModel.setKeyFrequency(super.keyFrequency);
		basicModel.prepare();
	}

	@Override
	public double score(Posting _p) {
		FieldPosting p = (FieldPosting)_p;
		final int[] tff = p.getFieldFrequencies();
		final int[] lf = p.getFieldLengths();
		//System.err.println("tff=" + ArrayUtils.join(tff, ","));
		//System.err.println("lf=" + ArrayUtils.join(lf, ","));
		assert lf != null : "No fields lengths from posting ";
		assert tff.length == lf.length : "Mismatch between lengths of field length and frequencies";
		int tf = 0, l = 0;
		for(int fieldId : activeFieldIds)
		{
			tf += tff[fieldId];
			l += lf[fieldId];
		}
		if (tf == 0)
			return 0;
		assert l > 0 : "Frequency but no length for docid " + p.getId();
		//System.err.println("tf=" + tf + " l="+l);
		final double rtr = basicModel.score(tf, l);
		if (Double.isNaN(rtr)) System.err.println("BPosting " + p.getId() + " had NaN : tf=" + tf + " l="+l+ " tf=" + org.terrier.utility.ArrayUtils.join(tff, ",") + " lf=" + org.terrier.utility.ArrayUtils.join(lf, ","));

		return rtr;
	}

	@Override
	public void setCollectionStatistics(CollectionStatistics _cs) 
	{
		super.setCollectionStatistics(_cs);
		int fieldCount = _cs.getNumberOfFields();
		if (fieldCount < 1)
			throw new IllegalStateException("Fields must be 1 or more");
		long tokens = 0;
		final long[] tokensf = _cs.getFieldTokens();
		for(int fieldId : activeFieldIds)
		{
			tokens += tokensf[fieldId];
		}
		
		super.numberOfTokens = tokens;
		super.averageDocumentLength = (double)tokens / (double)_cs.getNumberOfDocuments();
		
		basicModel.setCollectionStatistics(
				new CollectionStatistics(_cs.getNumberOfDocuments(), _cs.getNumberOfUniqueTerms(), tokens, _cs.getNumberOfPointers(), new long[0]));
	}

	@Override
	public void setEntryStatistics(EntryStatistics _es) {
		super.setEntryStatistics(_es);
		FieldEntryStatistics fes = (FieldEntryStatistics)_es;
		long TF = 0;
		int [] TFf = fes.getFieldFrequencies();
		for(int fieldId : activeFieldIds)
		{
			TF += getOverflowed(TFf[fieldId]);
		}
		super.termFrequency = TF;
		super.documentFrequency = fes.getDocumentFrequency();
		BasicLexiconEntry les = new BasicLexiconEntry();
		les.setStatistics(fes.getDocumentFrequency(), (int)TF);
		basicModel.setEntryStatistics(les);
	}
	
	@Override
	public String getInfo() {
		return "SingleFieldModel("+ArrayUtils.join(params, ",")+")";
	}


	@Override
	public double score(double tf, double docLength) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void setParameter(double c) {
		super.setParameter(c);
		basicModel.setParameter(c);
	}

}
