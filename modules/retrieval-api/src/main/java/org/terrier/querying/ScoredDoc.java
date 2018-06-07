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
