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
 * The Original Code is StaticFeature.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.matching.models;

import gnu.trove.TIntDoubleHashMap;

import org.terrier.structures.postings.Posting;

/** Class for query independent features loaded from file.
 * See {@link StaticScoreModifierWeightingModel} for supported
 * input file formats. 
 * @since 4.0 
 * @author Craig Macdonald
 */
public class StaticFeature extends StaticScoreModifierWeightingModel {
	private static final long serialVersionUID = 1L;

	public StaticFeature(String[] params) {
		super(params);
	}

	public StaticFeature(double[] scores) {
		super(scores);
	}

	public StaticFeature(TIntDoubleHashMap scores) {
		super(scores);
	}

	@Override
	public double score(Posting p) {
		return asFloat
			? getScoreF(p.getId())
			: getScoreD(p.getId());
	}
	
}
