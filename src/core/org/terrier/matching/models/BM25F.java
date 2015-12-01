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
 * The Original Code is BM25F.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.matching.models;

import org.terrier.matching.models.basicmodel.BM;
import org.terrier.matching.models.normalisation.NormalisationB;

/** A convenience subclass of PerFieldNormWeightingModel setup to
 * do specifically BM25F, as described by [Zaragoza TREC-2004]. 
 * [Robertson CIKM-2004] describe a different model, which does
 * not normalise frequencies in a per-field manner.
 * <p><b>References:</b>
 * <ul>
 * <li>[Zaragoza TREC-2004] H. Zaragoza, N. Craswell, M. Taylor, S. Saria, S. Robertson: Microsoft Cambridge at TREC 13: Web and Hard Tracks. In Proc. of TREC 2004. </li>
 * <li>[Robertson CIKM-2004] S. Robertson, H. Zaragoza, M. Taylor: Simple BM25 Extension to Multiple Weighted Fields. In Proc. of CIKM 2004.</li>
 * </ul>
 * 
 * @author Craig Macdonald
 * @since 3.0
 */
public class BM25F extends PerFieldNormWeightingModel {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an instance of the BM25F
	 * @throws Exception
	 */
	public BM25F() throws Exception {
		super(new String[] {
			BM.class.getName(),
			NormalisationB.class.getName()
		});		
	}

	@Override
	public String getInfo() {
		return "BM25F";
	}
}
