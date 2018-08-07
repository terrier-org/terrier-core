package org.terrier.matching;

import gnu.trove.TIntIntHashMap;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.learning.FeaturedResultSet;
import org.terrier.matching.dsms.DocumentScoreModifier;
import org.terrier.matching.matchops.UnorderedWindowOp;
import org.terrier.matching.models.WeightingModel;
import org.terrier.matching.models.WeightingModelFactory;
import org.terrier.sorting.MultiSort;
import org.terrier.structures.CollectionStatistics;
import org.terrier.structures.Index;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.Files;

public abstract class FeaturedScoringMatching extends FilterMatching {
	
	protected static Logger logger = LoggerFactory.getLogger(FatFeaturedScoringMatching.class);
	protected Index index;
	protected AbstractScoringMatching[] wModels;
	protected String[] wModelNames;
	protected DocumentScoreModifier[] dsms;
	protected String[] dsmNames;
	protected WeightingModel[] qiFeatures;
	protected String[] qiFeatureNames;
	protected boolean sampleFeature = false;
	
	protected Class<? extends AbstractScoringMatching> scoringMatchingImpl;

	public FeaturedScoringMatching(Index _index, Matching _parent, String[] _featureNames,
			Class<? extends AbstractScoringMatching> _scoringMatchingImpl) throws Exception
	{
		super(_parent);
		this.index = _index;
		this.scoringMatchingImpl = _scoringMatchingImpl;
		loadFeatures(_featureNames);	
	}
	
	public FeaturedScoringMatching(Index _index, Matching _parent,
			Class<? extends AbstractScoringMatching> _scoringMatchingImpl) throws Exception
	{
		this(_index, _parent, getModelNames("fat.featured.scoring.matching.features"), _scoringMatchingImpl);
	}
	
	protected static String[] getModelNames(String property) throws Exception {
		String[] modelNames = 
			ArrayUtils.parseDelimitedString(
					ApplicationSetup.getProperty(property, ""), ";");
		if (modelNames.length == 1 && modelNames[0].equals("FILE"))
		{
			String filename = ApplicationSetup.getProperty(property + ".file", null);
			if (filename == null)
				throw new IllegalArgumentException("For "+FatFeaturedScoringMatching.class+", property "+property+"file is not set");
			filename = ApplicationSetup.makeAbsolute(filename, ApplicationSetup.TERRIER_ETC);
			String line = null;
			final BufferedReader br = Files.openFileReader(filename);
			final List<String> models = new ArrayList<String>();
			while((line = br.readLine()) != null)
			{
				//ignore lines starting with comments
				if (line.startsWith("#"))
					continue;
				//remove trailing comments
				line = line.replaceAll("#.+$", "");
				//TREC-445: Empty line in feature definition file causes exception
				if (line.length() == 0) 
					continue;
				models.add(line.trim());
			}
			br.close();
			modelNames = models.toArray(new String[models.size()]);
		}
		if (modelNames.length == 0)
		{
			System.err.println("WARN no features specified");
		}
		return modelNames;
	}

	public static final Predicate<Pair<String,Set<String>>> getTagPredictate(final String matches) {
		return queryTerm -> queryTerm.getRight().contains(matches);
	}

	protected void loadFeatures(final String[] featureNames) throws Exception { 		
		final int featureCount = featureNames.length;
		
		final List<AbstractScoringMatching> _childrenWmodels = new ArrayList<>();
		final List<String> _childrenWmodelNames = new ArrayList<String>();
		
		final List<WeightingModel> _childrenQiModels = new ArrayList<>();
		final List<String> _childrenQiNames = new ArrayList<String>();
				
		final List<DocumentScoreModifier> _childrenDsms = new ArrayList<>();
		final List<String> _childrenDsmNames = new ArrayList<String>();
		
		for(int i=0;i<featureCount;i++)
		{
			if (featureNames[i].startsWith("#"))
				continue;
			if (featureNames[i].equals("SAMPLE")){
				sampleFeature = true;
			}				
			else if (featureNames[i].startsWith("DSM:"))
			{
				String dsmName = featureNames[i].replaceFirst("DSM:", "");
				if (dsmName.contains("%"))
				{
					String[] parts = dsmName.split("%", 2);
					dsmName = parts[0];
					String[] props = parts[1].split(" ");
					for(String kv: props)
					{
						String[] part2 = kv.split("=");
						ApplicationSetup.setProperty(part2[0], part2[1]);
					}
				}
				if (! dsmName.contains("."))
					dsmName = DocumentScoreModifier.class.getPackage().getName() + '.' + dsmName;
				final DocumentScoreModifier dsm = ApplicationSetup.getClass(dsmName).asSubclass(DocumentScoreModifier.class).newInstance();
				_childrenDsms.add(dsm);
				_childrenDsmNames.add(featureNames[i]);
			}
			else if (featureNames[i].startsWith("QI:"))
			{
				final String qiName = featureNames[i].replaceFirst("QI:", "");
				final WeightingModel wm = WeightingModelFactory.newInstance(qiName);
				_childrenQiModels.add(wm);
				_childrenQiNames.add(featureNames[i]);
			}			
			else if (featureNames[i].startsWith("WMODEL") && featureNames[i].contains(":"))//assume WMODEL: WMODELp: WMODELt:
			{
				Predicate<Pair<String,Set<String>>> filter = null;
				final String[] parts = featureNames[i].split(":", 2);
				final String catchName = parts[0];
				final String wModelName = parts[1];
				if (catchName.startsWith("WMODEL$"))	
				{
					String requiredTag = catchName.split("\\$",2)[1];
					filter = getTagPredictate(requiredTag);
				}
				if (catchName.equals("WMODELp"))
					filter = filterProx;
				if (catchName.equals("WMODELt"))
					filter = filterTerm;
				if (catchName.equals("WMODELpuw"))
					filter = filterUW;
				if (catchName.equals("WMODELp1"))
					filter = filterOW;
				
				WeightingModel wm = WeightingModelFactory.newInstance(wModelName);
				AbstractScoringMatching fsm = scoringMatchingImpl
						.getConstructor(Index.class, Matching.class, WeightingModel.class, Predicate.class)
						.newInstance(null, parent, wm, filter);
				//		new FatScoringMatching(null, parent, wm, filter);
				fsm.sort = false;
				_childrenWmodels.add(fsm);
				_childrenWmodelNames.add(featureNames[i]);
			} else {
				throw new IllegalArgumentException("invalid feature definition: " + featureNames[i]);
			}
								
		}		
		dsms = _childrenDsms.toArray(new DocumentScoreModifier[0]);
		dsmNames = _childrenDsmNames.toArray(new String[0]);
		
		qiFeatures = _childrenQiModels.toArray(new WeightingModel[0]);
		qiFeatureNames = _childrenQiNames.toArray(new String[0]);
		
		wModels = _childrenWmodels.toArray(new AbstractScoringMatching[0]);
		wModelNames = _childrenWmodelNames.toArray(new String[0]);
		
	}
	
	protected int applyDSMs(Index localIndex,  String queryNumber, MatchingQueryTerms mqtLocal, int numResults,  int[] inputDocids, short[] inputOccurrences, FeaturedResultSet rtr)
	{
		int featureCount = 0;
		TIntIntHashMap docidMap = new TIntIntHashMap(numResults);
		int position = 0;
		for(int docid : inputDocids)
		{
			docidMap.put(docid, position++);
		}
		for(int fid=0;fid<dsms.length;fid++)
		{
			final double[] scores = new double[numResults];
			final int[] docids = new int[numResults];
			final short[] occurrences = new short[numResults];
			System.arraycopy(inputDocids, 0, docids, 0, numResults);
			System.arraycopy(inputOccurrences, 0, occurrences, 0, numResults);
			
			// Sort by docid so that term postings we have a recoverable score ordering
			MultiSort.ascendingHeapSort(docids, scores, occurrences, docids.length);
			
			final ResultSet thinChild = new QueryResultSet(docids, scores, occurrences);
			
			//apply the dsm on the temporary resultset
			dsms[fid].modifyScores(localIndex, mqtLocal, thinChild);						
			
			//map scores back into original ordering
			double[] scoresFinal = new double[numResults];
			for(int i=0;i<numResults;i++)
			{
				scoresFinal[ docidMap.get(docids[i])] = scores[i];
			}
			//add the feature, regardless of whether it has scores or not			
			rtr.putFeatureScores(dsmNames[fid], scoresFinal);
			featureCount++;
		}
		return featureCount;
	}

	public static final Predicate<Pair<String,Set<String>>> filterUW = queryTerm -> queryTerm.getLeft().contains(UnorderedWindowOp.STRING_PREFIX);
	public static final Predicate<Pair<String,Set<String>>> filterOW = queryTerm -> queryTerm.getLeft().matches("^.*#\\d+.*$");
	public static final Predicate<Pair<String,Set<String>>> filterProx = filterUW.or(filterOW);
	public static final Predicate<Pair<String,Set<String>>> filterTerm = filterProx.negate();

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName() +
			"["+ArrayUtils.join(wModelNames, ','+ArrayUtils.join(dsmNames, ','))+"]";
	}

	@Override
	public void setCollectionStatistics(CollectionStatistics cs) {
		throw new UnsupportedOperationException();
	}

}