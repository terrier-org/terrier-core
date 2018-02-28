package org.terrier.querying;

import gnu.trove.TIntArrayList;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;
import org.terrier.matching.indriql.SingleQueryTerm;
import org.terrier.matching.indriql.SynonymTerm;
import org.terrier.structures.AbstractPostingOutputStream;
import org.terrier.structures.BitIndexPointer;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.Lexicon;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.Pointer;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.SimpleBitIndexPointer;
import org.terrier.structures.collections.FSArrayFile;
import org.terrier.structures.collections.FSArrayFile.ArrayFileWriter;
import org.terrier.structures.collections.FSArrayFileInMem;
import org.terrier.structures.indexing.CompressionFactory.BitIdOnlyCompressionConfiguration;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;
import org.terrier.structures.postings.ArrayOfIdsIterablePosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.postings.PostingUtil;
import org.terrier.terms.PorterStemmer;
import org.terrier.terms.Stemmer;
import org.terrier.utility.ApplicationSetup;

@ProcessPhaseRequisites({ManagerRequisite.MQT})
public class QueryTimeStemmer implements Process {
	
	protected static final Logger logger = LoggerFactory.getLogger(QueryTimeStemmer.class);

	static final String EQUIVSTEM_STRUCTURE_NAME = "equivstems";
	static final String EQUIVSTEM_POINTERS_STRUCTURE_NAME = "equivstems-pointers";

	boolean init = false;
	List<Pointer> stemPointers;

	Lexicon<String> lexicon;
	PostingIndex<Pointer> stemmerInv;
	
	public QueryTimeStemmer(){}
	public QueryTimeStemmer(Index indx){
		this.init(indx);
	}
	
	
	@SuppressWarnings("unchecked")
	protected void init(Index index) {
		lexicon = index.getLexicon();
		stemmerInv = (PostingIndex<Pointer>) index.getIndexStructure(EQUIVSTEM_STRUCTURE_NAME);		
		stemPointers = (List<Pointer>) index.getIndexStructure(EQUIVSTEM_POINTERS_STRUCTURE_NAME);
		init = true;
	}
	
	@Override
	public void process(Manager manager, SearchRequest q) {
		if (! init)
		{
			init(((Request)q).getIndex());
		}

		try{
			int i=-1;
			MatchingQueryTerms mqt = ((Request)q).getMatchingQueryTerms();
			for(MatchingTerm t : mqt)
			{
				i++;
				if (t.getKey() instanceof SingleQueryTerm)
				{
					SingleQueryTerm sqt = ((SingleQueryTerm)t.getKey());
					String origTerm = sqt.toString();
					String[] eqivs = getEquiv(origTerm);
					if (eqivs.length == 1 && eqivs[0].equals(origTerm))
						continue;
					mqt.set(i, new MatchingTerm(new SynonymTerm(eqivs), t.getValue()));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName();
	}

	String[] getEquiv(String origTerm) throws IOException {
		LexiconEntry le = lexicon.getLexiconEntry(origTerm);
		if (le == null)
			return new String[0];
		Pointer p = stemPointers.get(le.getTermId());
		// if (p.getNumberOfEntries() == 1)
		// return new String[]{origTerm};

		IterablePosting ip = stemmerInv.getPostings(p);
		int[] termIds = PostingUtil.getIds(ip);
		ip.close();
		String[] terms = new String[termIds.length];
		for (int i = 0; i < termIds.length; i++) {
			terms[i] = lexicon.getLexiconEntry(termIds[i]).getKey();
		}
		return terms;
	}
	
	static CompressionConfiguration compressionConfig = new BitIdOnlyCompressionConfiguration(EQUIVSTEM_STRUCTURE_NAME, new String[0], 0, 0);
	static int maxDocsEncodedDocid = Integer.parseInt( ApplicationSetup.getProperty("indexing.max.encoded."+EQUIVSTEM_POINTERS_STRUCTURE_NAME+"index.docs","5000"));
	
	
	public static void main(String[] args) throws IOException {

		IndexOnDisk currentIndex = Index.createIndex();
		Stemmer stemmer = new PorterStemmer();
		createStemEquivsIndex(currentIndex, stemmer);
	}
	
	@SuppressWarnings("unchecked")
	public static void createStemEquivsIndex(IndexOnDisk currentIndex, Stemmer stemmer)  throws IOException {
			
		Iterator<Entry<String, LexiconEntry>> lexIn = (Iterator<Map.Entry<String, LexiconEntry>>) currentIndex
				.getIndexStructureInputStream("lexicon");
		Map<String, TIntArrayList> stem2termids = new HashMap<>();
		int termCount = 0;
		while (lexIn.hasNext()) {
			Entry<String, LexiconEntry> ee = lexIn.next();
			String stem = stemmer.stem(ee.getKey());
			//System.out.println(ee.getKey() + "=>" + stem );
			TIntArrayList ids = stem2termids.get(stem);
			if (ids == null)
				stem2termids.put(stem, ids = new TIntArrayList());
			ids.add(ee.getValue().getTermId());
			termCount++;
		}
		IndexUtil.close(lexIn);
		
		Map<String, Writable> stem2pointer = new HashMap<>();
		AbstractPostingOutputStream posOut = compressionConfig.getPostingOutputStream(
				currentIndex.getPath() + ApplicationSetup.FILE_SEPARATOR + currentIndex.getPrefix() + "." + EQUIVSTEM_STRUCTURE_NAME + compressionConfig.getStructureFileExtension());

		BitIndexPointer[] pointers = new BitIndexPointer[termCount];
		for (Entry<String, TIntArrayList> stemEntry : stem2termids.entrySet()) 
		{
			stemEntry.getValue().sort();
			int[] ids = stemEntry.getValue().toNativeArray();
			BitIndexPointer p = posOut.writePostings(new ArrayOfIdsIterablePosting(ids));
			for(int termid : ids)
			{
				pointers[termid] = p;
			}
			//System.out.println(stemEntry.getKey() + "=>" + Arrays.toString(ids));
		}
		posOut.close();
		compressionConfig.writeIndexProperties(currentIndex, EQUIVSTEM_POINTERS_STRUCTURE_NAME);
		stem2termids.clear();
		
		String offsetFilePointers = currentIndex.getPath() + "/" + currentIndex.getPrefix() + "."+ EQUIVSTEM_POINTERS_STRUCTURE_NAME + FSArrayFile.USUAL_EXTENSION;
		ArrayFileWriter offsetOut = FSArrayFile.writeFSArrayFile(offsetFilePointers);
		for(BitIndexPointer p : pointers)
			offsetOut.write(p);
		offsetOut.close();
		
		currentIndex.addIndexStructure(EQUIVSTEM_POINTERS_STRUCTURE_NAME, 
				termCount > maxDocsEncodedDocid 
					? FSArrayFile.class.getName()
					: FSArrayFileInMem.class.getName(),
				"org.terrier.structures.IndexOnDisk,java.lang.String",
				"index,structureName");
		currentIndex.addIndexStructure(EQUIVSTEM_POINTERS_STRUCTURE_NAME+"-factory", SimpleBitIndexPointer.Factory.class.getName(), "", "");
		currentIndex.flush();
		logger.info("Created " + EQUIVSTEM_STRUCTURE_NAME + " and " + EQUIVSTEM_POINTERS_STRUCTURE_NAME + " mapping " + termCount + " terms into " + stem2pointer.size() + " stems" );
		
	}

}
