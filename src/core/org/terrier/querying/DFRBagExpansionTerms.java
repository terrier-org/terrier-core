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
 * The Original Code is DFRBagExpansionTerms.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.querying;
import gnu.trove.TIntObjectHashMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.models.queryexpansion.QueryExpansionModel;
import org.terrier.querying.parser.SingleTermQuery;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Rounding;

/**
 * This class implements a data structure of terms in the top-retrieved documents. 
 * In particular, this implementation treats the entire feedback set as a bag of words,
 * and weights term occurrences in this bag.
 * <P><b>Properties</b>:<ul>
 * <li><tt>expansion.mindocuments</tt> - the minimum number of documents a term must exist in 
 * before it can be considered to be informative. Defaults to 2. For more information, see
 * 	Giambattista Amati: Information Theoretic Approach to Information Extraction. FQAS 2006: 519-529 <a href="http://dx.doi.org/10.1007/11766254_44">DOI 10.1007/11766254_44</a></li></ul>
 * @author Gianni Amati, Ben He, Vassilis Plachouras, Craig Macdonald
  */
public class DFRBagExpansionTerms extends ExpansionTerms {
	/** The logger used */
	protected static Logger logger = LoggerFactory.getLogger(DFRBagExpansionTerms.class);
	/** The terms in the top-retrieval documents. */
	protected TIntObjectHashMap<ExpansionTerm> terms;
	/** The lexicon used for retrieval. */
	protected Lexicon<String> lexicon;
	protected PostingIndex<?> directIndex;
	protected DocumentIndex documentIndex;
	/** The number of documents in the collection. */
	protected int numberOfDocuments;
	/** The number of tokens in the collection. */
	protected long numberOfTokens;
	/** The average document length in the collection. */
	protected double averageDocumentLength;
	/** The number of tokens in the X top ranked documents. */
	protected double totalDocumentLength;
	/**
	 * The parameter-free term weight normaliser.
	 */
	public double normaliser = 1d;

	protected int feedbackDocumentCount = 0;
	
	/** The minimum number of documents a term must occur in to be considered for expanded terms. This is not considered a parameter of query expansion, as the default value of 2 works extremely well. Set using the property <tt>expansion.mindocuments</tt> */
	int EXPANSION_MIN_DOCUMENTS = Integer.parseInt(ApplicationSetup.getProperty("expansion.mindocuments","2"));
	
	
	/**
 	* Constructs an instance of ExpansionTerms.
	* @param collStats Statistics of the used corpora
	* @param _lexicon Lexicon The lexicon used for retrieval.
	* @param _directIndex DirectIndex to use for finding terms for documents
	* @param _documentIndex DocumentIndex to use for finding statistics about documents
 	*/
	public DFRBagExpansionTerms(CollectionStatistics collStats, Lexicon<String> _lexicon, PostingIndex<?> _directIndex, DocumentIndex _documentIndex) {
		this.numberOfDocuments = collStats.getNumberOfDocuments();
		this.numberOfTokens = collStats.getNumberOfTokens();
		this.averageDocumentLength = collStats.getAverageDocumentLength();
        this.terms = new TIntObjectHashMap<ExpansionTerm>();
		this.totalDocumentLength = 0;
		this.lexicon = _lexicon;
		this.documentIndex = _documentIndex;
		this.directIndex = _directIndex;
	}

	/** Allows the totalDocumentLength to be set after the fact */
	public void setTotalDocumentLength(double totalLength)
	{
		 this.totalDocumentLength = totalLength;
	}

	/** Returns the termids of all terms found in the top-ranked documents */
	public int[] getTermIds()
	{
		return terms.keys();
	}

	/** Returns the unique number of terms found in all the top-ranked documents */
	public int getNumberOfUniqueTerms()
	{
		return terms.size();
	}

	/** Returns expanded terms
	 * 
	 * @return terms
	 */
	public TIntObjectHashMap<ExpansionTerm> getExpansionTerms()
	{
		return terms;	
	}

	/**
 	* This method implements the functionality of assigning expansion weights to
	* the terms in the top-retrieved documents, and returns the most informative
	* terms among them. Conservative Query Expansion (ConservativeQE) is used if
	* the number of expanded terms is set to 0. In this case, no new query terms
	* are added to the query, only the existing ones reweighted.
	* @param numberOfExpandedTerms int The number of terms to extract from the
	*		top-retrieved documents. ConservativeQE is set if this parameter is set to 0.
	* 	* @return TermTreeNode[] The expanded terms.
 	*/
	public SingleTermQuery[] getExpandedTerms(int numberOfExpandedTerms) {
		return getExpandedTerms(numberOfExpandedTerms, model);
	}

	/* @param QEModel QueryExpansionModel the model used for query expansion */
	protected SingleTermQuery[] getExpandedTerms(int numberOfExpandedTerms, QueryExpansionModel QEModel) {
		assignWeights(QEModel);
				
		SingleTermQuery[] results = null;
		if (numberOfExpandedTerms != 0)
		{
			ExpansionTerm[] termEntries = terms.getValues(new ExpansionTerm[0]);
			//sort by descending score
			Arrays.sort(termEntries, EXPANSIONTERM_DESC_SCORE_SORTER);
			
			numberOfExpandedTerms = Math.min(termEntries.length, numberOfExpandedTerms);
			results = new SingleTermQuery[numberOfExpandedTerms];
			logger.debug("First weight = "+termEntries[0].getWeightExpansion() + " last weight="+termEntries[termEntries.length-1].getWeightExpansion());
			for (int i = 0; i < numberOfExpandedTerms; i++)
			{
				Map.Entry<String,LexiconEntry> lee = lexicon.getLexiconEntry(termEntries[i].getTermID());
				results[i] = new SingleTermQuery(lee.getKey());
				results[i].setWeight(termEntries[i].getWeightExpansion());
			}		
		} else { //numberOfExpandedTerms=0, Conservative"QE"
		
			results = new SingleTermQuery[originalTermids.size()];
			int i=0;
			for(int termId : originalTermids.keys())
			{
				results[i] = new SingleTermQuery(originalTermids.get(termId));
				results[i].setWeight(terms.get(termId).getWeightExpansion());
				//if (!QEModel.PARAMETER_FREE)
				//	results[i].setWeight(results[i].getWeight()*QEModel.ROCCHIO_BETA);
				i++;
			}
		}
		return results;
	}

	/** Remove the records for a given term */
	public void deleteTerm(int termid)
	{
		terms.remove(termid);
	}

	/**
	 * Returns the weight of a given term, computed by the 
	 * specified query expansion model.
	 * @param term String the term to set the weight for.
	 * @param model QueryExpansionModel the used query expansion model.
	 * @return double the weight of the specified term.
	 */
	public double getExpansionWeight(String term, QueryExpansionModel model)
	{
		return this.getExpansionWeight(lexicon.getLexiconEntry(term).getTermId(), model);
	}
	
	/**
	 * Returns the weight of a given term.
	 * @param term String the term to get the weight for.
	 * @return double the weight of the specified term.
	 */
	public double getExpansionWeight(String term)
	{
		return this.getExpansionWeight(lexicon.getLexiconEntry(term).getTermId(), model);
	}
	/**
	 * Returns the un-normalised weight of a given term.
	 * @param term String the given term.
	 * @return The un-normalised term weight.
	 */
	public double getOriginalExpansionWeight(String term){
		return getExpansionWeight(term)*normaliser;
	}
	
	/**
	 * Returns the frequency of a given term in the top-ranked documents.
	 * @param term String the term to get the frequency for.
	 * @return double the frequency of the specified term in the top-ranked documents.
	 */
	public double getFrequency(String term){
		return this.getFrequency(lexicon.getLexiconEntry(term).getTermId());
	}
	
	/**
	 * Returns the frequency of a given term in the top-ranked documents.
	 * @param termId int the id of the term to get the frequency for.
	 * @return double the frequency of the specified term in the top-ranked documents.
	 */
	public double getFrequency(int termId){
		ExpansionTerm o = terms.get(termId);
		if (o == null)
			return 0;
		return o.getWithinDocumentFrequency();
	}

	/**
	 * Returns the number of the top-ranked documents a given term occurs in.
	 * @param termId int the id of the term to get the frequency for.
	 * @return double the document frequency of the specified term in the top-ranked documents.
	 */
	public double getDocumentFrequency(int termId){
		ExpansionTerm o = terms.get(termId);
		if (o == null)
			return 0;
		return o.getDocumentFrequency();
	}
	
	/**
	 * Assign weight to terms that are stored in ExpansionTerm[] terms.
	 * @param QEModel QueryExpansionModel the used query expansion model.
	 */
	public void assignWeights(QueryExpansionModel QEModel){
		// Set required statistics to the query expansion model
		QEModel.setTotalDocumentLength(this.totalDocumentLength);
		QEModel.setCollectionLength(this.numberOfTokens);
		QEModel.setAverageDocumentLength(this.averageDocumentLength);
		QEModel.setNumberOfDocuments(this.numberOfDocuments);
		
		// weight the terms
		int posMaxWeight = 0;
		
		ExpansionTerm[] allTerms = terms.getValues(new ExpansionTerm[0]);
		final int minDF = feedbackDocumentCount < EXPANSION_MIN_DOCUMENTS ? 0 : EXPANSION_MIN_DOCUMENTS;
		final int len = allTerms.length;
		for (int i=0; i<len; i++)
		{	
			if (minDF > 0 && allTerms[i].getDocumentFrequency() < minDF &&	
					!originalTermids.contains(allTerms[i].getTermID())) 
			{
				allTerms[i].setWeightExpansion(0);
				continue;
			}
			
			double TF = 0;
			//double Nt = 0;
			Map.Entry<String, LexiconEntry> lee = lexicon.getLexiconEntry(allTerms[i].getTermID());
			if (lee == null)
			{
				logger.error("Termid " + allTerms[i].getTermID() +" was not found in the lexicon");
				continue;
			}

			TF = lee.getValue().getFrequency();
			//Nt = lee.getValue().getDocumentFrequency();
			allTerms[i].setWeightExpansion(QEModel.score(
				allTerms[i].getWithinDocumentFrequency(),
				TF
				)
			);
			if (allTerms[i].getWeightExpansion() > allTerms[posMaxWeight].getWeightExpansion())
				posMaxWeight = i;
		}
		
		// get the normaliser
		normaliser = allTerms[posMaxWeight].getWeightExpansion();
		if (QEModel.PARAMETER_FREE){
			QEModel.setMaxTermFrequency(allTerms[posMaxWeight].getWithinDocumentFrequency());
			normaliser = QEModel.parameterFreeNormaliser();
			if(logger.isDebugEnabled()){
				logger.info("parameter free query expansion.");
			}
		}
		if(logger.isDebugEnabled()){
			String term = lexicon.getLexiconEntry(allTerms[posMaxWeight].termID).getKey();
			logger.debug("term with the maximum weight: " + term +
				", normaliser: " + Rounding.toString(normaliser, 4));
		}
		for (int i = 0; i < len; i++){
			allTerms[i].setWeightExpansion(allTerms[i].getWeightExpansion()/normaliser);
			//expandedTerms[i].normalisedFrequency = 
			//terms[i].getWeightExpansion()/normaliser;
			if (!QEModel.PARAMETER_FREE)
				allTerms[i].setWeightExpansion(allTerms[i].getWeightExpansion()*QEModel.ROCCHIO_BETA);
				//normalisedFrequency *= QEModel.ROCCHIO_BETA;		   
		}
	}
	
	/**
	 * Returns the weight of a term with the given
	 * term identifier, computed by the specified 
	 * query expansion model.
	 * @param termId int the term identifier to set the weight for.
	 * @param model QueryExpansionModel the used query expansion model.
	 * @return double the weight of the specified term.
	 */
	public double getExpansionWeight(int termId, QueryExpansionModel model){
		double score = 0;
		ExpansionTerm o = terms.get(termId);
		if (o != null)
		{
			double TF = 0;
			//double Nt = 0;
			Map.Entry<String, LexiconEntry> lee = lexicon.getLexiconEntry(termId);
			TF = lee.getValue().getFrequency();
			//Nt = lee.getValue().getDocumentFrequency();
			score = model.score(o.getWithinDocumentFrequency(),
					TF,
					this.totalDocumentLength,
					this.numberOfTokens,
					this.averageDocumentLength
					);
		}
		return score;
	}
	
	/**
	 * Returns the weight of a term with the given
	 * term identifier.
	 * @param termId int the term identifier to set the weight for.
	 * @return double the weight of the specified term.
	 */
	public double getExpansionWeight(int termId){
		ExpansionTerm o = terms.get(termId);
		if (o == null)
			return -1;
		return o.getWeightExpansion();
	}

	/** Returns the probability of a given termid occurring
	  * in the expansion documents. Returns the quotient
	  * document frequency in the expansion documents, divided
	  * by the total length of all the expansion documents.
	  * @param termId int the term identifier to obtain the probability
	  * @return double the probability of the term */
	public double getExpansionProbability(int termId) {
		ExpansionTerm o = terms.get(termId);
		if (o == null)
			return -1;
		return o.getDocumentFrequency() / totalDocumentLength;
	}
	/** 
	 * Adds the feedback document to the feedback set. 
	 */
	public void insertDocument(FeedbackDocument doc) throws IOException
	{
		insertDocument(doc.docid, doc.rank, doc.score);
	}
	/** 
	 * Adds the feedback document from the index given a docid 
	 */
	public void insertDocument(int docid, int rank, double score) throws IOException
	{
		totalDocumentLength += documentIndex.getDocumentLength(docid);
		final IterablePosting ip = directIndex.getPostings((BitIndexPointer)documentIndex.getDocumentEntry(docid));
		if (ip == null)
		{
			logger.warn("document id "+docid+" not found");
			return;
		}
		while(ip.next() != IterablePosting.EOL)
		{
			this.insertTerm(ip.getId(), ip.getFrequency());
		}
		feedbackDocumentCount++;
	}
	
	/**
 	* Add a term in the X top-retrieved documents as a candidate of the 
	* expanded terms.
 	* @param termID int the integer identifier of a term
 	* @param withinDocumentFrequency double the within document 
 	*		frequency of a term
 	*/
	protected void insertTerm(int termID, double withinDocumentFrequency) {
		final ExpansionTerm et = terms.get(termID);
		if (et == null)
			terms.put(termID, new ExpansionTerm(termID, withinDocumentFrequency));
		else
			et.insertRecord(withinDocumentFrequency);
	}
}
