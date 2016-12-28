package org.terrier.structures;

public interface NgramEntryStatistics extends EntryStatistics {

	public int getWindowSize();
	public void setWindowSize(int ws);
	
}
