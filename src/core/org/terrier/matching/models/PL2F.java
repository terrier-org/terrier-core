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
 * The Original Code is PL2F.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.matching.models;

import org.terrier.matching.models.basicmodel.PL;
import org.terrier.matching.models.normalisation.Normalisation2;

/** A convenience subclass of PerFieldNormWeightingModel setup to
 * do specifically PL2F. If you use this model, please cite:
 * C. Macdonald, V. Plachouras, B. He, C. Lioma, and I. Ounis. University
 * of Glasgow at WebCLEF-2005: Experiments in per-field normalisation
 * and language specific stemming. In Proc. of CLEF 2005.
 * @author Craig Macdonald
 * @since 3.0
 */
public class PL2F extends PerFieldNormWeightingModel {
	private static final long serialVersionUID = 1L;

	/** 
	 * Constructs an instance of PL2F.
	 * @throws Exception
	 */
	public PL2F() throws Exception {
		super(PL.class, Normalisation2.class);
	}

	/** 
	 * {@inheritDoc} 
	 */
	@Override
	public String getInfo() {
		return "PL2F";
	}
}
