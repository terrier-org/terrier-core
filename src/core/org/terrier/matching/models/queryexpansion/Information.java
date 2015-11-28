/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is Information.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
  *   Ben He <ben{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching.models.queryexpansion;

import static org.terrier.matching.models.WeightingModelLibrary.log;

/** 
 * This class implements the Kullback-Leibler divergence for
 * query expansion. See G. Amati's PhD Thesis.
 * @author Gianni Amati, Ben He
  */
public class Information extends QueryExpansionModel {
    /** A default constructor.*/
    public Information() {
		super();
    }
    
    /**
     * Returns the name of the model.
     * @return the name of the model
     */
    public final String getInfo() {
		return "Information";
    }
    /**
     * This method computes the normaliser of parameter-free query expansion.
     * @return The normaliser.
     */
    public final double parameterFreeNormaliser(){	
    	return (maxTermFrequency) * Math.log(collectionLength/totalDocumentLength)/
			(Math.log(2d)*totalDocumentLength);
		//return  maxTermFrequency * log(collectionLength/totalDocumentLength)/ log(totalDocumentLength);
	}
	
    /**
     * This method computes the normaliser of parameter-free query expansion.
     * @param maxTermFrequency The maximum of the term frequency of the query terms.
     * @param collectionLength The number of tokens in the collections.
     * @param totalDocumentLength The sum of the length of the top-ranked documents.
     * @return The normaliser.
     */
	public final double parameterFreeNormaliser(double maxTermFrequency, double collectionLength, double totalDocumentLength){
		return (maxTermFrequency) * Math.log(collectionLength/totalDocumentLength)/
		(Math.log(2d)*totalDocumentLength);
	}
    
    /** This method implements the query expansion model.
     *  @param withinDocumentFrequency double The term frequency in the X top-retrieved documents.
     *  @param termFrequency double The term frequency in the collection.
     *  @return double The query expansion weight using he complete 
     *          Kullback-Leibler divergence.
     */
    public final double score(double withinDocumentFrequency, double termFrequency) {
		return - log(withinDocumentFrequency / this.totalDocumentLength );
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
		return - log(withinDocumentFrequency / this.totalDocumentLength);
    }
}
