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
 * The Original Code is KLCorrect.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching.models.queryexpansion;

import static org.terrier.matching.models.WeightingModelLibrary.log;

/** 
 * This class implements the correct Kullback-Leibler divergence for
 * query expansion, which takes the contribution into consideration. 
 * See the equation before (8.13), page 149.
 * @author Gianni Amati, Ben He, Vassilis Plachouras
  */
public class KLCorrect extends QueryExpansionModel {
    /** A default constructor.*/
    public KLCorrect() {
		super();
    }
    
    /**
     * Returns the name of the model.
     * @return the name of the model
     */
    public final String getInfo() {
        return "KLCorrect";
    }
	/** 
	 * {@inheritDoc} 
	 */
    public final double parameterFreeNormaliser(){
		return 1d;
	}
	/** 
	 * {@inheritDoc} 
	 */
	public final double parameterFreeNormaliser(double maxTermFrequency, double collectionLength, double totalDocumentLength){
		return 1d;
	}
    
    /** This method implements the query expansion model.
     *  @param withinDocumentFrequency double The term frequency in the X top-retrieved documents.
     *  @param termFrequency double The term frequency in the collection.
     *  @return double The query expansion weight using he complete 
     *  Kullback-Leibler divergence.
     */
    public final double score(double withinDocumentFrequency, double termFrequency) {
        if (withinDocumentFrequency / this.totalDocumentLength < termFrequency / this.collectionLength)
            return 0;
        double f = withinDocumentFrequency / this.totalDocumentLength;
        double p = termFrequency / this.collectionLength;
        double D = f * log(f, p) + f * log(1 - f, 1 - p);
        return D;
    } 
    
    /**
	 * This method implements the query expansion model.
	 * @param withinDocumentFrequency double The term frequency in the X top-retrieved documents.
     * @param termFrequency double The term frequency in the collection.
     * @param totalDocumentLength double The sum of length of the X top-retrieved documents.
     * @param collectionLength double The number of tokens in the whole collection.
     * @param averageDocumentLength double The average document length in the collection.
	 * @return double The score returned by the implemented model.
	 */
	public final double score(
        double withinDocumentFrequency, 
        double termFrequency,
        double totalDocumentLength, 
        double collectionLength, 
        double averageDocumentLength
    ){
        if (withinDocumentFrequency / totalDocumentLength < termFrequency / collectionLength)
            return 0;
        double f = withinDocumentFrequency / totalDocumentLength;
        double p = termFrequency / collectionLength;
        double D = f * log(f, p) + f * log(1 - f, 1 - p);
        return D;    
    }
}
