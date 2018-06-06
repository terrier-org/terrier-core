package org.terrier.querying;

import java.util.List;


public interface ScoredDocList extends List<ScoredDoc> {

	public default double getMaxScore(){
		return this.size() > 0 ? this.get(0).score : 0d;
	}
	
	public String[] getMetaKeys();
	
}
