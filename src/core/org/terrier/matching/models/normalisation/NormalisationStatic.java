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
 * The Original Code is NormalisationStatic.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
/*
 * Contributor(s):
 *   Craig Macdonald
 */
package org.terrier.matching.models.normalisation;

/**
 * This class implements a Normalisation method that forces all
 * term frequencies to the value of the parameter. If field retrieval
 * is enabled, then the parameter is multiplied by the field weight.
 * @author Craig Macdonald
  */
public class NormalisationStatic extends Normalisation{
	
	
	private static final long serialVersionUID = 1L;
	/** The name of the normalisation method .*/
	protected final String methodName = "Static";

	/**
	 * Get the name of the normalisation method.
	 * @return Return the name of the normalisation method.
	 */
	public String getInfo(){
		String info = this.methodName+"_"+parameter;
		return info;
	}

	/**
	 * Returns a static term frequency. 
	 * i.e. tf = (tf &gt; 0) ? parameter : 0
	 * @param tf The frequency of the query term in the document.
	 * @param docLength The number of tokens in the document.
	 * @param termFrequency The frequency of the query term in the collection.
	 * @return The normalised term frequency.
	 */
	public double normalise(double tf, double docLength, double termFrequency){
		if (docLength == 0)
			return tf;
		if (tf == 0)
			return 0;
		return parameter;
	}
}
