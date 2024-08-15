package org.terrier.matching;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.terrier.structures.DocumentIndex;
import org.terrier.structures.DocumentIndexEntry;
import org.terrier.structures.FieldDocumentIndex;
import org.terrier.structures.Index;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.ArrayOfBasicIterablePosting;
import org.terrier.structures.postings.ArrayOfBlockFieldIterablePosting;
import org.terrier.structures.postings.ArrayOfBlockIterablePosting;
import org.terrier.structures.postings.ArrayOfFieldIterablePosting;
import org.terrier.structures.postings.ArrayOfIdsIterablePosting;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.FieldPosting;
import org.terrier.structures.postings.IterablePosting;

/** This class makes an inverted index for a subset of documents based upon a set of documents */
public class Direct2InvIndex  {
	
	/* NB: the docids array will be sorted */
	public static PostingIndex<Pointer> process(Index index, int[] docids, final boolean blocks, final boolean fields) throws IOException
	{
		assert(index.hasIndexStructure("direct"));
		
		final DocumentIndex doi = index.getDocumentIndex();
		final FieldDocumentIndex fdoi = fields ? (FieldDocumentIndex) index.getDocumentIndex() : null;
		
		PostingIndex<?> di = index.getDirectIndex();
		Arrays.sort(docids);
		TIntObjectHashMap<TIntArrayList> term2docids = new TIntObjectHashMap<>();
		TIntObjectHashMap<TIntArrayList> term2freqs = new TIntObjectHashMap<>();
		TIntObjectHashMap<ArrayList<int[]>> term2fieldfreqs = new TIntObjectHashMap<>();
		
		TIntObjectHashMap<TIntArrayList> term2poscount = new TIntObjectHashMap<>();
		TIntObjectHashMap<TIntArrayList> term2allpos = new TIntObjectHashMap<>();
		
		for(int docid : docids)
		{
			DocumentIndexEntry doiE = doi.getDocumentEntry(docid);
			IterablePosting docPostings = di.getPostings(doiE);
			int termid;
			while( (termid = docPostings.next()) != IterablePosting.END_OF_LIST)
			{
				TIntArrayList ids = term2docids.get(termid);
				TIntArrayList freqs = term2freqs.get(termid);
				ArrayList<int[]> field_freqs = term2fieldfreqs.get(termid);
				TIntArrayList poscount = term2poscount.get(termid);
				TIntArrayList allpos = term2allpos.get(termid);
				
				if (ids == null) //defaultdict impementation :-)
				{
					assert freqs == null;
					term2docids.put(termid, ids = new TIntArrayList());
					term2freqs.put(termid,  freqs = new TIntArrayList());
					if (fields)
						term2fieldfreqs.put(termid, field_freqs = new ArrayList<>());
					if (blocks)
					{
						term2poscount.put(termid, poscount = new TIntArrayList());
						term2allpos.put(termid, allpos = new TIntArrayList());
					}
				}
				ids.add(docid);
				freqs.add(docPostings.getFrequency());
				if (fields)
					field_freqs.add( ((FieldPosting)docPostings).getFieldFrequencies() );
				if (blocks)
				{
					poscount.add( ((BlockPosting)docPostings).getPositions().length );
					allpos.add( ((BlockPosting)docPostings).getPositions() );
				}
			}
		}
		DVInv rtr = new DVInv();
		for(int termid : term2docids.keys())
		{
			int[] ids = term2docids.get(termid).toNativeArray();
			int[] freqs = term2freqs.get(termid).toNativeArray();
			
			int[][] field_freqs = fields ? term2fieldfreqs.get(termid).toArray(new int[0][]) : null;
			int[] pos_count = blocks ? term2poscount.get(termid).toNativeArray() : null;
			int[] allpos = blocks ? term2allpos.get(termid).toNativeArray() : null;
			
			assert ids.length == freqs.length;
			
			if (blocks)
				if (fields)
					rtr.term2postings.put(termid, new ArrayOfBlockFieldIterablePosting(ids, freqs, null, field_freqs, null, pos_count, allpos)
					{
						@Override
						public int getDocumentLength() {
							try {
								return doi.getDocumentLength(this.getId());
							} catch (IOException e) {
								e.printStackTrace();
								return 0;
							}
						}
						
						@Override
						public int[] getFieldLengths() {
							try {
								return fdoi.getFieldLengths(this.getId());
							} catch (IOException e) {
								e.printStackTrace();
								return null;
							}
						}
						
					});
				else 
					rtr.term2postings.put(termid, new ArrayOfBlockIterablePosting(ids, freqs, pos_count, allpos)
					{
						@Override
						public int getDocumentLength() {
							try {
								return doi.getDocumentLength(this.getId());
							} catch (IOException e) {
								e.printStackTrace();
								return 0;
							}
						}
						
					});
			else 
				if (fields)
					rtr.term2postings.put(termid, new ArrayOfFieldIterablePosting(ids, freqs, null, field_freqs, null)
					{
						@Override
						public int getDocumentLength() {
							try {
								return doi.getDocumentLength(this.getId());
							} catch (IOException e) {
								e.printStackTrace();
								return 0;
							}
						}

						@Override
						public int[] getFieldLengths() {
							try {
								return fdoi.getFieldLengths(this.getId());
							} catch (IOException e) {
								e.printStackTrace();
								return null;
							}
						}
						
					});
				else
					rtr.term2postings.put(termid, new ArrayOfBasicIterablePosting(ids, freqs)
					{
						@Override
						public int getDocumentLength() {
							try {
								return doi.getDocumentLength(this.getId());
							} catch (IOException e) {
								e.printStackTrace();
								return 0;
							}
						}
						
						
						
					});
		}
		return rtr;
	}
	
	public static class DVInv implements PostingIndex<Pointer>
	{

		TIntObjectHashMap<ArrayOfIdsIterablePosting> term2postings = new TIntObjectHashMap<>();
		
		@Override
		public void close() throws IOException {
			term2postings.clear();
		}

		@Override
		public IterablePosting getPostings(Pointer lEntry) throws IOException {
			int termid = ((LexiconEntry) lEntry).getTermId();
			ArrayOfIdsIterablePosting ip = term2postings.get(termid);
			ip = ip.clone();
			ip.reset();
			return ip;
		}
		
	}
	
}
