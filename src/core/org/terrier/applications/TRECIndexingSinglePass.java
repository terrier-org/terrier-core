package org.terrier.applications;

import org.terrier.indexing.Collection;
import org.terrier.structures.indexing.Indexer;
import org.terrier.structures.indexing.singlepass.BasicSinglePassIndexer;
import org.terrier.structures.indexing.singlepass.BlockSinglePassIndexer;
import org.terrier.utility.ApplicationSetup;

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
		if (ApplicationSetup.BLOCK_INDEXING)
			_indexer = new BlockSinglePassIndexer(pa, pr);
		else
			_indexer = new BasicSinglePassIndexer(pa, pr);
		return _indexer;
	}

}
