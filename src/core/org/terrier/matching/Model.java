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
 * The Original Code is Model.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.matching;
/** This interface encapsulates all the common functionality between
  * standard IR models (including DFR models, TF/IDF, BM25 etc), and
  * Language Modelling models (Terrier includes PonteCroft Language
  * Modelling model).
  * @see org.terrier.matching.models.WeightingModel
  * @author Craig Macdonald
    */
public interface Model
{
	/** Returns a model dependant string, such that runs can be identified
	  * from TREC results. Example <tt>PL2c2.1</tt> would imply PL2 model, with the
	  * c parameter set to 2.1. 
	  * @return The string description of the current instantiation of the weighting
	  * model. */
	String getInfo();
	
	/** Many models require 1 parameter to be set - this is often dependant on the
	  * corpus and the model, so should be set for each Terrier instance. 
	  * @param param double the parameter value. */
	void setParameter(double param);
	
	/** Returns the current value of the parameter set using setParameter() method. */
	double getParameter();
}
