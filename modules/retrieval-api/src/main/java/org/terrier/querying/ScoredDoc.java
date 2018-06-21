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
 * The Original Code is ScoredDoc.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.querying;

import java.util.Map;

public class ScoredDoc {

	Map<String,Integer> metaKeyOffset;
	int docid;
	double score;
	short occurrences;
	String[] metadata;
	
	public ScoredDoc(int docid, double score,
			short occurrences, String[] metadata, Map<String, Integer> metaKeyOffset) {
		super();
		this.metaKeyOffset = metaKeyOffset;
		this.docid = docid;
		this.score = score;
		this.occurrences = occurrences;
		this.metadata = metadata;
	}
	public int getDocid() {
		return docid;
	}
	public double getScore() {
		return score;
	}
	public short getOccurrences() {
		return occurrences;
	}
	
	public String getMetadata(String key) {
		return metaKeyOffset.containsKey(key) ? metadata[metaKeyOffset.get(key)] : null;
	}
	
	public String[] getAllMetadata() {
		return metadata;
	}
	
}
