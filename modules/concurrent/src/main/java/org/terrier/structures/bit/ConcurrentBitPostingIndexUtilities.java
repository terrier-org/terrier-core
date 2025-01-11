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
 * The Original Code is Copyright (C) 2017-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures.bit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.compression.bit.*;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.bit.BitPostingIndex;

public class ConcurrentBitPostingIndexUtilities {

	static Logger logger = LoggerFactory.getLogger(ConcurrentBitPostingIndexUtilities.class);
	private static final boolean USE_CHANNEL = true;

	public static boolean isConcurrent(BitPostingIndex bpi) {
		BitInSeekable bis = bpi.file[0];
		if (bis instanceof BitFileChannel)
			return true;
		if (bis instanceof ConcurrentBitFileBuffered)
			return true;
		if (bis instanceof BitFileInMemoryLarge)
			return true;
		if (bis instanceof BitFileInMemory)
			return true;
		return false;
	}

	public static void makeConcurrent(BitPostingIndex bpi, DocumentIndex newDoi)
	{
		for(int i=0;i<bpi.file.length;i++)
		{
			BitInSeekable bis = bpi.file[i];
			if (USE_CHANNEL)
			{
				if (bis instanceof BitFileBuffered && !( bis instanceof BitFileChannel))
				{
					BitFileBuffered theFile = (BitFileBuffered)bis;
					BitFileChannel newFile = BitFileChannel.of(theFile);
					bpi.file[i] = newFile;
					logger.debug("Changing BitFileBuffered to BitFileChannel");
				} else if (bis instanceof BitFileInMemoryLarge) {
					logger.debug("BitFileInMemoryLarge already re-entrant");
				}
			}
			else
			{
				if (bis instanceof BitFileBuffered && !( bis instanceof ConcurrentBitFileBuffered))
				{
					BitFileBuffered theFile = (BitFileBuffered)bis;
					ConcurrentBitFileBuffered newFile = ConcurrentBitFileBuffered.of(theFile);
					bpi.file[i] = newFile;
					logger.debug("Changing BitFileBuffered to ConcurrentBitFileBuffered");
				} else if (bis instanceof BitFileInMemoryLarge) {
					logger.debug("BitFileInMemoryLarge already re-entrant");
				}
			}
		}
		bpi.doi = newDoi;
	}
	
}
