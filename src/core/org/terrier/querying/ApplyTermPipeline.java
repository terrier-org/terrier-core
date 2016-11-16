package org.terrier.querying;

import gnu.trove.TIntArrayList;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;
import org.terrier.matching.indriql.SingleQueryTerm;
import org.terrier.terms.BaseTermPipelineAccessor;
import org.terrier.terms.TermPipelineAccessor;
import org.terrier.utility.ApplicationSetup;

@ProcessPhaseRequisites(ManagerRequisite.MQT)
public class ApplyTermPipeline implements Process {

	TermPipelineAccessor tpa = null;
	String info = null;
	
	public ApplyTermPipeline()
	{
		this.load_pipeline();
	}
	
	/** load in the term pipeline */
	protected void load_pipeline()
	{
		final String[] pipes = ApplicationSetup.getProperty(
				info = "termpipelines", "Stopwords,PorterStemmer").trim()
				.split("\\s*,\\s*");
		synchronized (this) {
			tpa = new BaseTermPipelineAccessor(pipes);
		}		
	}
	
	@Override
	public void process(Manager manager, SearchRequest q) {
		
		
		TIntArrayList toDel = new TIntArrayList();
		int i=-1;
		MatchingQueryTerms mqt = ((Request)q).getMatchingQueryTerms();
		for(MatchingTerm t : mqt)
		{
			i++;
			if (t.getKey() instanceof SingleQueryTerm)
			{
				SingleQueryTerm sqt = ((SingleQueryTerm)t.getKey());
				String origTerm = sqt.toString();
				String newTerm = tpa.pipelineTerm(origTerm);
				if (newTerm == null)
					toDel.add(i);
				else if (! newTerm.equals(origTerm))
					sqt.setTerm(newTerm);
			}
		}
		toDel.reverse();
		for(int removeIndex : toDel.toNativeArray())
		{
			mqt.remove(removeIndex);
		}
	}

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName() + '(' + this.info + ')';
	}

}
