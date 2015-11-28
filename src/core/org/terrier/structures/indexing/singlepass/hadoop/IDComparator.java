/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is IDComparator.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 *   Richard McCreadie <richardm{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures.indexing.singlepass.hadoop;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Compares String objects. It is used to sort the Map ID's for the MapData files loaded
 * during the reduce step. It makes sure that the Map ID's are in order of the documents
 * that they processed. 
 * @author Richard McCreadie
 * @since 3.0
 */
public class IDComparator implements Comparator<String> {

	/** Mapping between map task ids and the data contained within the map */
	Map<String, MapData> id2splitData = new HashMap<String,MapData>();
	/** Array containing the data about each map task */
	MapData[] splitData;
	/** 
	 * constructor
	 * @param mapData
	 */
	public IDComparator(LinkedList<MapData> mapData) {
		splitData = mapData.toArray(new MapData[mapData.size()]);
		for(MapData m : splitData)
		{
			id2splitData.put(m.getMap(), m);
		}
	}
	/** 
	 * {@inheritDoc} 
	 */
	public final int compare(String id1, String id2) {
		final MapData md1 = id2splitData.get(id1);
		final MapData md2 = id2splitData.get(id2);
		if (md1 == null || md2 == null) return -1;
		return md1.compareTo(md2);
	}
	
}
