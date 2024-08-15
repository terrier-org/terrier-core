package org.terrier.matching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gnu.trove.TIntDoubleHashMap;

import org.terrier.learning.FeaturedQueryResultSet;
import org.terrier.matching.matchops.Operator;
import org.terrier.matching.models.WeightingModel;
import org.terrier.querying.MQTRewritingProcess;
import org.terrier.querying.Request;
import org.terrier.structures.FilterIndex;
import org.terrier.structures.Index;
import org.terrier.structures.PostingIndex;
import org.terrier.structures.postings.ArrayOfIdsIterablePosting;
import org.terrier.structures.postings.BlockPosting;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.ApplicationSetup;

/** Implements the DocVectors style of computing additional features. Uses Direct2InvIndex to make a mock InvertedIndex 
 * (from a DirectIndex) for rescoring documents for computing additional features.
 * Behaves the same way as FatFeaturedScoringMatching. This class can also support retrieval for only some "tagged" query terms.
 */
public class DVFeaturedScoringMatching extends FeaturedScoringMatching {

	MQTRewritingProcess[] processes = new MQTRewritingProcess[0];
	String[] processTags = new String[0];
	
	public DVFeaturedScoringMatching(Index _index, Matching _parent,
			String[] _featureNames, String[] _rewriterNames) throws Exception {
		super(_index, _parent, _featureNames, ScoringMatching.class);
		parseRewriters(_rewriterNames);
	}
	
	public DVFeaturedScoringMatching(Index _index, Matching _parent,
			String[] _featureNames) throws Exception {
		super(_index, _parent, _featureNames, ScoringMatching.class);
		parseRewriters(getModelNames("dv.featured.scoring.matching.rewriters", true));
	}

	public DVFeaturedScoringMatching(Index _index, Matching _parent)
			throws Exception {
		super(_index, _parent, ScoringMatching.class);
		parseRewriters(getModelNames("dv.featured.scoring.matching.rewriters", true));
	}
	
	protected void parseRewriters(String[] rewriterName) {
		List<String> _tags = new ArrayList<>();
		List<MQTRewritingProcess> _processes = new ArrayList<>();
		for(String line : rewriterName)
		{
			String[] parts = line.split(":", 2);
			String tag = parts[0];
			String[] parts2 = parts[1].split("\\(");
			String classname = parts2[0];
			if (! classname.contains("."))
				classname = "org.terrier.querying." + classname;
			String[] params  = new String[0];
			if (parts2.length > 1)
				params = parts2[1].replaceAll("\\)$", "").split(",");
			
			
			Class<? extends MQTRewritingProcess> clz;
			MQTRewritingProcess rtr;
			try{
				clz= ApplicationSetup.getClass(classname).asSubclass(MQTRewritingProcess.class);
				if (params.length > 0)
					rtr = clz.getConstructor(new Class[]{String[].class}).newInstance(new Object[]{params});
				else
					rtr = clz.newInstance();
			}catch (Exception e) {
				throw new IllegalArgumentException("when configuring " + classname, e);
			}
			_tags.add(tag);
			_processes.add(rtr);
			
		}
		processTags = _tags.toArray(new String[0]);
		processes = _processes.toArray(new MQTRewritingProcess[0]);
	}

	@Override
	public ResultSet match(String queryNumber, MatchingQueryTerms queryTerms)
			throws IOException {
		
		final ResultSet res = parent.match(queryNumber, queryTerms);
		if (res == null)
		{
			logger.warn("I got NO ResultSet from parent " + parent.getInfo() );
			return new FeaturedQueryResultSet(0);
		}
		return doMatch(queryNumber, queryTerms, res);
	}

	@Override
	public ResultSet doMatch(String queryNumber,
			MatchingQueryTerms queryTerms, ResultSet res, boolean keepInputScores) throws IOException 
	{
		final int numResults = res.getResultSize();
		final FeaturedQueryResultSet rtr = new FeaturedQueryResultSet(res);
		int featureCount = 0;
		if (res.getResultSize() == 0)
		{
			rtr.scores = new double[0];
			rtr.docids = new int[0];
			rtr.occurrences = new short[0];
			return rtr;
		}
		
		final int[] docids = res.getDocids().clone();
		
		
		boolean fields = index.getCollectionStatistics().getNumberOfFields() > 0;
		//this is a hack
		//nb, we use direct as perhaps the inverted has blocks and the direct does not
		boolean blocks = index.getDirectIndex().getPostings(index.getDocumentIndex().getDocumentEntry(0)) instanceof BlockPosting;
		
		logger.info(this.getClass().getSimpleName() + " analysing " + docids.length + " documents blocks="+blocks+" fields="+fields);
		final PostingIndex<?> dvInv = Direct2InvIndex.process(index, docids.clone(), blocks, fields);
		
		Index dvIndex = new FilterIndex(this.index){
			@Override public PostingIndex<?> getInvertedIndex() {return dvInv;}
		};
		
		int pi = -1;
		//this newMQT is the target of our rewriting operations
		MatchingQueryTerms newMQT = queryTerms.clone();
		Set<Operator> existingOps = newMQT.stream().map(me -> me.getKey()).collect(Collectors.toSet());
		for (MQTRewritingProcess p : processes) 
		{
			pi++;
			Request r = new Request();
			r.setIndex(dvIndex);
			r.setMatchingQueryTerms(queryTerms.clone());
			r.setResultSet(res);
			p.configureIndex(dvIndex);
			boolean changed = p.expandQuery(r.getMatchingQueryTerms(), r);
			if (changed)
			{
				MatchingQueryTerms mqtNew = r.getMatchingQueryTerms();
				String tag = processTags[pi];
				mqtNew.stream()
					.filter(me -> ! existingOps.contains( me.getKey()))
					.forEach(me -> { me.getValue().tags.add(tag); newMQT.add(me); });
			}
		}
		queryTerms = newMQT;
		
		if (sampleFeature)
		{
			rtr.putFeatureScores("SAMPLE", res.getScores());
			featureCount++;
		}

		//for each WMODEL feature
		for(int fid=0;fid<wModels.length;fid++)
		{
			wModels[fid].index = dvIndex;
			MatchingQueryTerms mqtLocal = queryTerms.clone();
			final ResultSet thinChild = wModels[fid].doMatch(queryNumber, mqtLocal, res.getResultSet(0, res.getResultSize()), false);
			// restore any sort back to the expected ordering
			final double[] sortedScores = sortById(resultsToMap(thinChild), docids);
			rtr.putFeatureScores(wModelNames[fid], sortedScores);
			featureCount++;
		}
		
		//for each QI features
		if (qiFeatures.length > 0)
		{
			IterablePosting ip = new ArrayOfIdsIterablePosting(docids);
			for(int fid=0;fid<qiFeatures.length;fid++)
			{
				WeightingModel wm = qiFeatures[fid];
				double[] scores = new double[numResults];
				int di=0;
				while(ip.next() != IterablePosting.EOL)
				{
					assert ip.getId() == docids[di];
					scores[di++] = wm.score(ip);
				}
				rtr.putFeatureScores(qiFeatureNames[fid], scores);
				featureCount++;
			}
		}
		
		//for each DSM feature
		if (dsms.length > 0)
		{
			final MatchingQueryTerms mqtLocal = queryTerms.clone();
			featureCount += applyDSMs(dvIndex, queryNumber, mqtLocal, numResults, docids, res.getOccurrences().clone(), rtr);
		}
		
		if (keepInputScores) {
			System.arraycopy(res.getScores(), 0, rtr.getScores(), 0, res.getResultSize());
		}
		
		//labels
		final String[] labels = new String[rtr.getResultSize()];
		Arrays.fill(labels, "-1");
		rtr.setLabels(labels);
		
		//metadata
		if (res.hasMetaItems("docno"))
		{
			rtr.addMetaItems("docno", res.getMetaItems("docno"));
		}
		if (res.hasMetaItems("label"))
			rtr.setLabels(res.getMetaItems("label"));
		logger.info("Finished decorating " + queryNumber + " with " + featureCount + " features");
		return rtr;
		
	}

	static final TIntDoubleHashMap resultsToMap(final ResultSet rs) {
		
		final int l = rs.getResultSize();
		final int[] docids = rs.getDocids();
		final double[] scores = rs.getScores();
		final TIntDoubleHashMap rtr = new TIntDoubleHashMap(l);
		for(int i=0;i<l;i++)
			rtr.put(docids[i], scores[i]);
		return rtr;
	}

	static final double[] sortById(final TIntDoubleHashMap docid2score, final int[] docids) {
		final int l = docid2score.size();
		assert l == docids.length;
		final double[] rtr = new double[l];
		for(int i=0;i<l;i++) {
			rtr[i] = docid2score.get(docids[i]);
		} 
		return rtr;
	}

}
