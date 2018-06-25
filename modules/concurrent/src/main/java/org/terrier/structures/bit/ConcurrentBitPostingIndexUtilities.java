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
 * The Original Code is ConcurrentBitPostingIndexUtilities.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
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
