package org.terrier.querying;

import java.util.ArrayList;
import java.util.List;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;
import org.terrier.matching.indriql.PhraseTerm;
import org.terrier.matching.indriql.SingleQueryTerm;
import org.terrier.matching.indriql.UnorderedWindowTerm;
import org.terrier.matching.models.dependence.pBiL;
import org.terrier.querying.parser.Query.QTPBuilder;

public class DependenceModelPreProcess implements Process {

	@Override
	public void process(Manager manager, SearchRequest q) {
		this.process(((Request)q).getMatchingQueryTerms());
	}
	
	public void process(MatchingQueryTerms mqt)
	{
		assert mqt != null;
		List<String> queryTerms = new ArrayList<>();
		for(MatchingTerm e : mqt)
		{
			if (! ( e.getKey() instanceof SingleQueryTerm))
				continue;
			queryTerms.add(e.getKey().toString());
		}
		
		if (queryTerms.size() < 2)
			return;
		
		List<MatchingTerm> newEntries = new ArrayList<>();
		
		//#1
		for(int i=0;i<queryTerms.size()-1;i++)
		{
			QTPBuilder qtp = QTPBuilder.of(new PhraseTerm(new String[]{queryTerms.get(i), queryTerms.get(i+1)}));
			qtp.setWeight(0.1d);
			qtp.addWeightingModel(new pBiL(2));
			newEntries.add(qtp.build());
		}
		
		//#uw8
		for(int i=0;i<queryTerms.size()-1;i++)
		{
			QTPBuilder qtp = QTPBuilder.of(new UnorderedWindowTerm(new String[]{queryTerms.get(i), queryTerms.get(i+1)}, 8));
			qtp.setWeight(0.1d);
			qtp.addWeightingModel(new pBiL(8));
			newEntries.add(qtp.build());
		}
		
		//#uw12
		QTPBuilder qtp = QTPBuilder.of(new UnorderedWindowTerm(queryTerms.toArray(new String[queryTerms.size()]), 12));
		qtp.setWeight(0.1d);
		qtp.addWeightingModel(new pBiL(12));
		newEntries.add(qtp.build());
		
		//finally add the new entries
		mqt.addAll(newEntries);
	}

	

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName();
	}
	
}
