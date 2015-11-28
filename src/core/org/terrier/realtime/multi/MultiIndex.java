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
 * The Original Code is MultiIndex.java.
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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.realtime.matching.IncrementalSelectiveMatching;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.DocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.Lexicon;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.utility.ApplicationSetup;

/**
 * The central MultiIndex structure. MultiIndex is a wrapper around 
 * multiple indices such that they appear as one single index. Matching
 * over a MultiIndex can either be performed by a normal matching across
 * all index shards or using a special selective matching class that only
 * uses a subset of the shards this contains.
 * 
 * <p><b>Properties</b></p>
 * <ul><li>multiindex.selectivematching</tt> - What policy should be used to perform matching. Two options are supported: all (default), mostrecent</li></ul>
 * 
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
public class MultiIndex extends Index {
	private static final Logger logger = LoggerFactory.getLogger(MultiIndex.class);

	/*
	 * List of the underlying indices.
	 */
	protected List<Index> indices;
	
	/**
	 * Selective Matching policy, a policy for accessing only subsets of the indices within this multi-index.
	 */
	protected IncrementalSelectiveMatching selectiveMatchingPolicy;

	/**
	 * Constructor.
	 */
	public MultiIndex(Index[] indices) {
		super(0l, 0l, 0l);
		ArrayList<Index> in = new ArrayList<Index>(indices.length);
		for (Index i : indices)
			in.add(i);
		this.indices = in;
		
		// Selective Matching Policy
		String policy = ApplicationSetup.getProperty("multiindex.selectivematching", "all");
		selectiveMatchingPolicy = IncrementalSelectiveMatching.get(policy);

		logger.info("***REALTIME*** MultiIndex (NEW)");
	}

	/** {@inheritDoc} */
	public String toString() {
		return "MultiIndex";
	}

	/** {@inheritDoc} */
	public Object getIndexStructure(String structureName) {
		if (structureName.equalsIgnoreCase("lexicon"))
			return getLexicon();
		if (structureName.equalsIgnoreCase("meta"))
			return getMetaIndex();
		if (structureName.equalsIgnoreCase("inverted"))
			return getInvertedIndex();
		if (structureName.equalsIgnoreCase("document"))
			return getDocumentIndex();
		if (structureName.equalsIgnoreCase("direct"))
			return getDirectIndex();
		if (structureName.equalsIgnoreCase("collectionstatistics"))
			return getCollectionStatistics();
		else
			return null;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	public Lexicon<String> getLexicon() {
		int indexCount = indices.size();
		int[] offsets = new int[indexCount];
		Lexicon<String>[] lexicons = new Lexicon[indexCount];

		int i = 0;
		for (Index index : selectiveMatchingPolicy.getSelectedIndices(indices)) {
			lexicons[i] = index.getLexicon();
			offsets[i] = index.getCollectionStatistics()
					.getNumberOfUniqueTerms();
			i++;
		}

		return new MultiLexicon(lexicons, offsets);
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	public PostingIndex<?> getInvertedIndex() {
		int ondisk = indices.size();
		int[] offsets = new int[ondisk];
		PostingIndex<?>[] postings = new PostingIndex[ondisk];

		int currentoffset = 0;
		int i = 0;
		for (Index index : selectiveMatchingPolicy.getSelectedIndices(indices)) {
			postings[i] = index.getInvertedIndex();
			offsets[i] = currentoffset;
			currentoffset += index.getCollectionStatistics()
					.getNumberOfDocuments();
		i++;
		}

		return new MultiInverted((PostingIndex<Pointer>[]) postings, offsets);
	}

	/** {@inheritDoc} */
	public MetaIndex getMetaIndex() {
		int ondisk = indices.size();
		int[] offsets = new int[ondisk];
		MetaIndex[] metas = new MetaIndex[ondisk];

		int i =0;
		for (Index index : selectiveMatchingPolicy.getSelectedIndices(indices)) {
			metas[i] = index.getMetaIndex();
			offsets[i] = index.getCollectionStatistics()
					.getNumberOfDocuments();
			i++;
		}

		return new MultiMeta(metas, offsets);
	}

	/** {@inheritDoc} */
	public DocumentIndex getDocumentIndex() {
		int ondisk = indices.size();
		int[] offsets = new int[ondisk];
		DocumentIndex[] docs = new DocumentIndex[ondisk];

		int i =0;
		for (Index index : selectiveMatchingPolicy.getSelectedIndices(indices)) {
			docs[i] = index.getDocumentIndex();
			offsets[i] = index.getCollectionStatistics()
					.getNumberOfDocuments();
			i++;
		}

		return new MultiDoc(docs, offsets);
	}

	/** {@inheritDoc} */
	public CollectionStatistics getCollectionStatistics() {
		int ondisk = indices.size();
		CollectionStatistics[] stats = new CollectionStatistics[ondisk];

		int i =0;
		for (Index index : selectiveMatchingPolicy.getSelectedIndices(indices)) {
			stats[i] = index.getCollectionStatistics();
			i++;
		}
			
		return MultiStats.factory(stats);
	}
	
	@SuppressWarnings("unchecked")
	public PostingIndex<?> getDirectIndex() {
		int ondisk = indices.size();
		int[] offsets = new int[ondisk];
		PostingIndex<?>[] postings = new PostingIndex[ondisk];

		int i = 0;
		for (Index index : selectiveMatchingPolicy.getSelectedIndices(indices)) {
			postings[i] = index.getDirectIndex();
			offsets[i] = index.getCollectionStatistics().getNumberOfUniqueTerms();
			i++;
		}

		return new MultiDirect((PostingIndex<Pointer>[]) postings, offsets);
	}

	/** Not implemented. */
	public Object getIndexStructureInputStream(String structureName) {
		return null;
	}

	/** {@inheritDoc} */
	public void close() throws IOException {
		for (Index i : this.indices)
			i.close();
	}

	/** {@inheritDoc} */
	public void flush() throws IOException {
		for (Index i : this.indices)
			i.flush();
	}
	
	public Index getIthShard(int i) {
		return indices.get(i);
	}
	
	/**
	 * Returns the number of index shards that this incremental index contains
	 * @return integer number of shards
	 */
	public int getNumberOfShards() {
		return indices.size();
	}

}