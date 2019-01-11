/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.ac.gla.uk
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
 * The Original Code is TRECIndexingSinglePass.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.applications;

import org.terrier.indexing.Collection;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.singlepass.BasicSinglePassIndexer;
import org.terrier.structures.indexing.singlepass.BlockSinglePassIndexer;

public class TRECIndexingSinglePass extends TRECIndexing {

	public TRECIndexingSinglePass() {
		super();
	}

	public TRECIndexingSinglePass(String _path, String _prefix, Collection c) {
		super(_path, _prefix, c);
	}

	public TRECIndexingSinglePass(String _path, String _prefix,
			String collectionSpec) {
		super(_path, _prefix, collectionSpec);
	}

	public TRECIndexingSinglePass(String _path, String _prefix) {
		super(_path, _prefix);
	}

	@Override
	protected Indexer loadIndexer(String pa, String pr) {
		BasicSinglePassIndexer _indexer;
		if (blocks)
			_indexer = new BlockSinglePassIndexer(pa, pr);
		else
			_indexer = new BasicSinglePassIndexer(pa, pr);
		return _indexer;
	}

}
