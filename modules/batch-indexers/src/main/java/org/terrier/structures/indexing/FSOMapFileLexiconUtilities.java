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
 * The Original Code is FSOMapFileLexiconUtilities.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures.indexing;

import gnu.trove.TIntObjectHashMap;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.FSOMapFileLexicon;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.collections.FSOrderedMapFile;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.Files;
import org.terrier.utility.io.WrappedIOException;

public class FSOMapFileLexiconUtilities {

	public static final Logger logger = LoggerFactory.getLogger(FSOMapFileLexiconUtilities.class);
	
	
	/** 
	 * optimise
	 * @param structureName
	 * @param index
	 * @param statsCounter
	 * @param numEntries
	 * @throws IOException
	 */
	@SuppressWarnings({"unchecked", "resource"})
	public static void optimise(
			String structureName, 
			IndexOnDisk index,
			LexiconBuilder.CollectionStatisticsCounter statsCounter,
			int numEntries) 
		throws IOException
	{
	
		final String mapFileFilename = FSOMapFileLexicon.constructFilename(structureName, index.getPath(), index.getPrefix(), FSOMapFileLexicon.MAPFILE_EXT);
		final FixedSizeWriteableFactory<Text> keyFactory = 
			(FixedSizeWriteableFactory<Text>)index.getIndexStructure(structureName+"-keyfactory");
		final FixedSizeWriteableFactory<LexiconEntry> valueFactory = 
			(FixedSizeWriteableFactory<LexiconEntry>)index.getIndexStructure(structureName+"-valuefactory");
		logger.info("Optimising lexicon with "+ numEntries + " entries");
		//term id lookups
		boolean termIdsAligned = true;
		int[] termid2index = new int[numEntries];
		Arrays.fill(termid2index, -1);
		int counter= 0; int lastTermId = -1;
		
		//bsearch reduction
		int previousFirstChar = -1;
		int firstChar = 0;
		final TIntObjectHashMap<int[]> map = new TIntObjectHashMap<int[]>();
		
		
		Iterator<Map.Entry<Text,LexiconEntry>> iterator = 
			new FSOrderedMapFile.EntryIterator<Text, LexiconEntry>(mapFileFilename, keyFactory, valueFactory);
		Map.Entry<Text,LexiconEntry> lee = null;
		int termId = Integer.MIN_VALUE;
		try {
			while(iterator.hasNext())
			{
				lee = iterator.next();
				//System.err.println(lee.toString());
				//System.err.println(lee.toString() +" "+lee.getValue().getTermId()+" "+lee.getValue().getFrequency());
				
				//term id
				termId = lee.getValue().getTermId();
				if (! (termId == lastTermId+1))
					termIdsAligned = false;
				if (termid2index[termId] != -1)
				{
					throw new WrappedIOException(new IllegalArgumentException("Termid " + termId + " is not unique - used at entries " +termid2index[termId]+ " and" + counter));
				}
				termid2index[termId] = counter;
				lastTermId = termId;
				
				//bsearch reduction optimisaion
				firstChar = lee.getKey().charAt(0);
				if (firstChar!=previousFirstChar) {
					int[] boundaries = new int[] {counter, 0};
					map.put(firstChar, boundaries);
					previousFirstChar = firstChar;
				}
				
				//increments
				statsCounter.count(lee.getValue());
				counter++;
			}
		} catch (ArrayIndexOutOfBoundsException ae) {
			logger.error("Termid " + termId + " is  too large (expected only "
					+termid2index.length +" entries). Bad lexicon entry is: " 
					+ lee.getKey().toString() + " -> " + lee.getValue().toString() );
			throw ae;
		}
		
		if (counter != numEntries)
			termIdsAligned = false;
		IndexUtil.close(iterator);
		
		//deal with termids
		if (termIdsAligned)
		{
			index.setIndexProperty("index."+structureName+".termids", "aligned");
			logger.info("All ids for structure "+structureName+ " are aligned, skipping "
				+FSOMapFileLexicon.ID_EXT+ " file");
		}
		else
		{
			DataOutputStream dos = new DataOutputStream(Files.writeFileStream(
					FSOMapFileLexicon.constructFilename(structureName, ((IndexOnDisk) index).getPath(), ((IndexOnDisk) index).getPrefix(), FSOMapFileLexicon.ID_EXT)));
			for(int indexof : termid2index)
				dos.writeInt(indexof);
			dos.close();
			index.setIndexProperty("index."+structureName+".termids", (numEntries > 15000000) ? "file" : "fileinmem");
		}
		
		
		int[] mapKeys = map.keys();
		Arrays.sort(mapKeys);
		final int mapKeysSize = mapKeys.length;
		for (int i=0; i<mapKeysSize-1; i++) {
			int nextLowerBoundary = (map.get(mapKeys[i+1]))[0];
			int[] currentBoundaries = map.get(mapKeys[i]);
			currentBoundaries[1] = nextLowerBoundary;
			map.put(mapKeys[i], currentBoundaries);
		}
		//do something about the last entry
		int nextLowerBoundary = counter;
		int[] currentBoundaries = (int[])map.get(mapKeys[mapKeysSize-1]);
		currentBoundaries[1] = nextLowerBoundary;
		map.put(mapKeys[mapKeysSize-1], currentBoundaries);
		
		final ObjectOutputStream oos = new ObjectOutputStream(Files.writeFileStream(
				FSOMapFileLexicon.constructFilename(structureName, ((IndexOnDisk) index).getPath(), ((IndexOnDisk) index).getPrefix(), FSOMapFileLexicon.HASH_EXT)));
		oos.writeObject(map);
		oos.close();
		index.setIndexProperty("index."+structureName+".bsearchshortcut", "charmap");
		index.flush();
	}


	/** Does two things to a FSOMapFileLexicon: adds the termid lookup file (if required),
	 * and also creates the lexicon has file.
	 * @param structureName - name of the index structure that this FSOMapFileLexicon represents
	 * @param index - the index that the index belongs
	 * @throws IOException if an IO problem occurs
	 */
	@SuppressWarnings("unchecked")
	public static void optimise(
			String structureName, 
			IndexOnDisk index, 
			LexiconBuilder.CollectionStatisticsCounter statsCounter) 
		throws IOException
	{
		final String mapFileFilename = FSOMapFileLexicon.constructFilename(structureName, index.getPath(), index.getPrefix(), FSOMapFileLexicon.MAPFILE_EXT);
		final FixedSizeWriteableFactory<Text> keyFactory = 
			(FixedSizeWriteableFactory<Text>)index.getIndexStructure(structureName+"-keyfactory");
		final FixedSizeWriteableFactory<LexiconEntry> valueFactory = 
			(FixedSizeWriteableFactory<LexiconEntry>)index.getIndexStructure(structureName+"-valuefactory");
		final int numEntries = FSOrderedMapFile.numberOfEntries(mapFileFilename, keyFactory, valueFactory);
		optimise(structureName, index, statsCounter, numEntries);
	}

}
