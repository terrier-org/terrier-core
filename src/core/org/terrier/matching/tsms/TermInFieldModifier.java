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
 * The Original Code is TermInFieldModifier.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.matching.tsms;
import org.terrier.matching.models.WeightingModel;
import org.terrier.structures.Index;
import org.terrier.structures.IndexConfigurable;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.Posting;
import org.terrier.utility.FieldScore;
/**
 * Resets the scores of documents according to whether a term appears in 
 * a given set of fields. This class implements the TermScoreModifier interface.
 * @author Vassilis Plachouras
  */
public class TermInFieldModifier 
	extends WeightingModel 
	implements IndexConfigurable
{
	private static final long serialVersionUID = 1L;
	protected transient Index index = null;
	/**
	 * The fields that a query term should appear in.
	 */
	protected String field = null;
	
	/** 
	 * The requirement. By default it is true. 
	 */
	protected boolean requirement = true;
	
	/**
	 * Constructs an instance of a TermInFieldModifier given a
	 * field that the corresponding query term should appear in.
	 * @param _field String a field
	 */
	public TermInFieldModifier(String _field) {
		this.field = _field;
	}
	
	/**
	 * Constructs an instance of a TermInFieldModifier given a
	 * field that the corresponding query term should appear in.
	 * @param _field String a field
	 * @param req boolean the requirement for this field. If req is
	 *        true, then the term is required to appear in the field
	 *        (this is the default behaviour), otherwise the term
	 *        should not appear in the field.
	 */
	public TermInFieldModifier(String _field, boolean req) {
		this.field = _field;
		requirement = req;
	}
	
	/** 
	 * Resets the scores of documents for a particular term, based on 
	 * the fields a term appears in documents.
	 * @param scores double[] the scores of the documents.
	 * @param pointers int[][] the pointers read from the inverted file 
	 *        for a particular query term.
	 * @return the number of documents for which the scores were modified. 
	 */
	public int modifyScores(double[] scores, int[][] pointers) {
		int numOfModifiedDocs=0;
		//check that there field scores have been retrieved
		if (pointers.length < 3 || pointers[2] == null)
			return numOfModifiedDocs;
		
		int[] fieldscores = pointers[2];
		final int numOfPointers = fieldscores.length;
		FieldScore fScore = new FieldScore();
		fScore.insertField(field);
		int fieldScore = fScore.getFieldScore();
		if (fieldScore == 0) 
			return numOfModifiedDocs;
		
		//for each document that contains the query term, the score is computed.
		//int docFieldScore;
		if (requirement) { //the term should appear in the field
			for (int j = 0; j < numOfPointers; j++) {
				//filter out results that do not have the corresponding query 
				//term in the given field.
				if((fieldscores[j] & fieldScore) == 0) {
					if (scores[j]!=Double.NEGATIVE_INFINITY)
						numOfModifiedDocs++;
					scores[j] = Double.NEGATIVE_INFINITY;
					
				}
			}
		} else { //the term should not appear in the field
			for (int j = 0; j < numOfPointers; j++) {
				//filter out results that have the corresponding query 
				//term in the given field.
				if((fieldscores[j] & fieldScore) > 0) {
					if (scores[j]!=Double.NEGATIVE_INFINITY)
						numOfModifiedDocs++;
					scores[j] = Double.NEGATIVE_INFINITY;
				}
			}
		}
		return numOfModifiedDocs;
	}
	
	
	int fieldIndex = -1;
	/** 
	 * {@inheritDoc} 
	 */
	public void prepare()
	{
		String[] indexFieldNames = index.getIndexProperty("index.inverted.fields.names", "").split("\\s*,\\s*");
		int i=0;
		for(String f : indexFieldNames)
		{
			if (f.equals(this.field))
			{
				fieldIndex = i;
				break;
			}
			i++;
		}
		if (fieldIndex == -1)
		{
			throw new IllegalArgumentException("Field '" + this.field + "' is not a valid field; possibilities were " +  index.getIndexProperty("index.inverted.fields.names", ""));
		}
		//else
		//{
		//	System.err.println("Field '" + "' is the "+fieldIndex+"th field (0 based)");
		//}
	}
	
	//implementation assumes scores are additive
	/** 
	 * {@inheritDoc} 
	 */
	public double score(Posting _p)
	{
		FieldPosting p = (FieldPosting)_p;
		if (requirement)
		{
			if (p.getFieldFrequencies()[fieldIndex] == 0)
			{
				return Double.NEGATIVE_INFINITY;
			}
		}
		else
		{
			if (p.getFieldFrequencies()[fieldIndex] > 0)
				return Double.NEGATIVE_INFINITY;
		}
		return 0;
	}
	
	/** 
	 * Returns the name of the class and whether the query term is required.
	 */
	public String getName() {
		return "TermInFieldModifier("+field+","+requirement+")";
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double score(double tf, double docLength) {
		// TODO Auto-generated method stub
		return 0;
	}

	/** 
	 * {@inheritDoc} 
	 */
	public void setIndex(Index i) {
		this.index = i;
	}
}
