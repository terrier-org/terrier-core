/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org 
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * Information Retrieval Group
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
 * The Original Code is PL.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Ben He <ben{a.}dcs.gla.ac.uk> 
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching.models.basicmodel;

import static org.terrier.matching.models.WeightingModelLibrary.*;

/**
 * This class implements the PL weighting model.
 * @author Gianni Amati, Ben He, Vassilis Plachouras
 * @since 3.0
 */
public class PL extends P {
	private static final long serialVersionUID = 1L;

	@Override
	public double score(
			double tf,
			double documentFrequency,
			double F_t,
			double keyFrequency,
			double documentLength) {
		double NORM = 1.0D / (tf + 1d);
		double f = F_t / numberOfDocuments;

		return NORM
			* keyFrequency
			* (tf * log(1d / f)
				+ f * LOG_2_OF_E
				+ 0.5d * log(2 * Math.PI * tf)
				+ tf * (log(tf) - LOG_2_OF_E));
	}

	@Override
	public String getInfo() {
		return "PL";
	}
	
}