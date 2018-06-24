package org.terrier.structures.bit;

import org.terrier.compression.bit.BitFileBuffered;
import org.terrier.compression.bit.BitInSeekable;
import org.terrier.compression.bit.ConcurrentBitFileBuffered;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.bit.BitPostingIndex;

public class ConcurrentBitPostingIndexUtilities {

	public static void makeConcurrent(BitPostingIndex bpi, DocumentIndex newDoi)
	{
		for(int i=0;i<bpi.file.length;i++)
		{
			BitInSeekable bis = bpi.file[i];
			if (bis instanceof BitFileBuffered && !( bis instanceof ConcurrentBitFileBuffered))
			{
				BitFileBuffered theFile = (BitFileBuffered)bis;
				ConcurrentBitFileBuffered newFile = ConcurrentBitFileBuffered.of(theFile);
				bpi.file[i] = newFile;
			}
		}
		bpi.doi = newDoi;
	}
	
}
