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
 * The Original Code is MultiMeta.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.multi;

import java.io.IOException;

import org.terrier.structures.MetaIndex;

/** A MetaIndex for use with a MultiIndex. It wraps around multiple individual
 * meta indices to make them look like a single structure.
 *  
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 * */
public class MultiMeta implements MetaIndex {

	private MetaIndex[] metas;
	private int[] offsets;

	/**
	 * Constructor.
	 */
	public MultiMeta(MetaIndex[] metas, int[] offsets) {
		this.metas = metas;
		this.offsets = offsets;
	}

	/** {@inheritDoc} */
	public String[] getKeys() {
		return metas[0].getKeys();
	}

	/** {@inheritDoc} */
	public String getItem(String key, int docid) throws IOException {
		int offset = 0, i = 0;
		for (MetaIndex meta : metas) {
			if (docid < (offsets[i] + offset))
				return meta.getItem(key, docid - offset);
			offset += offsets[i++];
		}
		return null;
	}

	/** {@inheritDoc} */
	public String[] getAllItems(int docid) throws IOException {
		int offset = 0, i = 0;
		for (MetaIndex meta : metas) {
			if (docid < (offsets[i] + offset))
				return meta.getAllItems(docid - offset);
			offset += offsets[i++];
		}
		return null;
	}

	/** {@inheritDoc} */
	public String[] getItems(String[] keys, int docid) throws IOException {
		int offset = 0, i = 0;
		for (MetaIndex meta : metas) {
			if (docid < (offsets[i] + offset))
				return meta.getItems(keys, docid - offset);
			offset += offsets[i++];
		}
		return null;
	}

	/** {@inheritDoc} */
	public String[] getItems(String key, int[] docids) throws IOException {
		String[] metadata = new String[docids.length];
		int soffset = 0, eoffset = 0;
		for (int i = 0; i < metas.length; i++) {
			soffset = eoffset;
			eoffset += offsets[i];
			for (int j = 0; j < docids.length; j++)
				if ((docids[j] < eoffset) && (docids[j] >= soffset))
					metadata[j] = metas[i].getItem(key, docids[j] - soffset);
		}
		return metadata;
	}

	/** {@inheritDoc} */
	public String[][] getItems(String[] keys, int[] docids) throws IOException {
		String[][] metadata = new String[docids.length][];
		int soffset = 0, eoffset = 0;
		for (int i = 0; i < metas.length; i++) {
			soffset = eoffset;
			eoffset += offsets[i];
			for (int j = 0; j < docids.length; j++)
				if ((docids[j] < eoffset) && (docids[j] >= soffset))
					metadata[j] = metas[i].getItems(keys, docids[j] - soffset);
		}
		return metadata;
	}

	/** Not implemented. */
	public int getDocument(String key, String value) throws IOException {
		return -1;
	}

	/** Not implemented. */
	public void close() throws IOException {
	}

}
