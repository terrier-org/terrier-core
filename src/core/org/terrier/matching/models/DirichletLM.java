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
 * The Original Code is DirichletLM.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s): Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching.models;


/** Bayesian smoothing with Dirichlet Prior. This has one parameter, mu &gt; 0. <i>"The optimal value of
 * mu also tends to be larger for long queries than for title queries. The optimal ... seems to vary from
 * collection to collection, though in most cases, it is around 2,000. The tail of the curves is generally
 * flat."</i> This class sets mu to 2500 by default. As a default, this gives higher performance than 
 * BM25 (b=0.75) on TREC Terabyte track 2004.
 * <p>
 * The retrieval performance of this weighting model has been empirically verified to be similar to that reported
 * below. This model is formulated such that all scores are &gt; 0. 
 * <p>A Study of Smoothing Methods for Language Models Applied to Information Retrieval. 
 * Zhai & Lafferty, ACM Transactions on Information Systems, Vol. 22, No. 2, April 2004, Pages 179--214.
 * <p>
 * @author Craig Macdonald
 * @since 3.0
 */
public class DirichletLM extends WeightingModel {

	private static final long serialVersionUID = 1L;

	/** 
	 * Constructs an instance of DirichletLM
	 */
	public DirichletLM() {
		super();
		c = 2500;
	}

	@Override
	public double score(double tf, double docLength) {
		return WeightingModelLibrary.log(1 + (tf/(c * (super.termFrequency / numberOfTokens))) ) + WeightingModelLibrary.log(c/(docLength+c));
	}

	@Override
	public String getInfo() {
		return "DirichletLM";
	}

}
