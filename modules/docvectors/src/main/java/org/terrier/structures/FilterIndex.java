package org.terrier.structures;

import java.io.IOException;
import org.terrier.querying.IndexRef;

public abstract class FilterIndex extends Index {

	Index parent;
	public FilterIndex(Index _parent) {
		this.parent = _parent;
	}

	@Override public IndexRef getIndexRef() { 
		return makeDirectIndexRef(this); 
	}
	
	@Override
	public void close() throws IOException {
		parent.close();
	}

	@Override
	public CollectionStatistics getCollectionStatistics() {
		return parent.getCollectionStatistics();
	}

	@Override
	public PostingIndex<?> getDirectIndex() {
		return parent.getDirectIndex();
	}

	@Override
	public DocumentIndex getDocumentIndex() {
		return parent.getDocumentIndex();
	}

	@Override
	public boolean hasIndexStructure(String structureName) {
        return parent.hasIndexStructure(structureName);
    }

	@Override
	public Object getIndexStructure(String structureName) {
		return parent.getIndexStructure(structureName);
	}

	@Override
	public Object getIndexStructureInputStream(String structureName) {
		return parent.getIndexStructureInputStream(structureName);
	}

	@Override
	public PostingIndex<?> getInvertedIndex() {
		return parent.getInvertedIndex();
	}

	@Override
	public Lexicon<String> getLexicon() {
		return parent.getLexicon();
	}

	@Override
	public MetaIndex getMetaIndex() {
		return parent.getMetaIndex();
	}

	@Override
	public String toString() {
		return parent.toString();
	}

}
