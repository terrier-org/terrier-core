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
 * The Original Code is DependenceModelPreProcess.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.querying;

import java.util.ArrayList;
import java.util.List;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;
import org.terrier.matching.matchops.PhraseOp;
import org.terrier.matching.matchops.SingleTermOp;
import org.terrier.matching.matchops.UnorderedWindowOp;
import org.terrier.matching.models.WeightingModel;
import org.terrier.matching.models.dependence.pBiL;
import org.terrier.querying.parser.Query.QTPBuilder;
import org.terrier.utility.ApplicationSetup;

@ProcessPhaseRequisites(ManagerRequisite.MQT)
public class DependenceModelPreProcess implements Process {
	
	static final String DEFAULT_DEPENDENCE_WEIGHTING_MODEL = pBiL.class.getName();
	public static final String CONTROL_MODEL = "dependencemodel";
	public static final String CONTROL_MODEL_PARAM = "dependencemodelparam";
	public static final String DEPENDENCE_TAG = "sdm";
	
	Double param = null;
	
	protected void initialise(SearchRequest q) {}
	
	@Override
	public void process(Manager manager, SearchRequest q) {
		initialise(q);
		String modelName = q.getControl(CONTROL_MODEL);
		if (modelName == null || modelName.length() == 0)
			modelName = DEFAULT_DEPENDENCE_WEIGHTING_MODEL;
		
		String paramValue = q.getControl(CONTROL_MODEL_PARAM);
		param = paramValue != null && paramValue.length() > 0 ? Double.parseDouble(paramValue) : null;	
		this.process(((Request)q).getMatchingQueryTerms(), modelName);
	}
	
	WeightingModel getModel(String name, int ngramLength) {
		if (! name.contains("."))
			name = "org.terrier.matching.models.dependence." + name;
		WeightingModel rtr = null;
		try{
			rtr = ApplicationSetup.getClass(name).asSubclass(WeightingModel.class).getConstructor(Integer.TYPE).newInstance(ngramLength);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		if (param != null)
			rtr.setParameter(param);
		return rtr;
	}
	
	public void process(MatchingQueryTerms mqt, String modelName)
	{
		assert mqt != null;
		List<String> queryTerms = new ArrayList<>();
		for(MatchingTerm e : mqt)
		{
			if (! ( e.getKey() instanceof SingleTermOp))
				continue;
			queryTerms.add(e.getKey().toString());
		}
		
		if (queryTerms.size() < 2)
			return;
		
		List<MatchingTerm> newEntries = SD(modelName, queryTerms);
		
		//finally add the new entries
		mqt.addAll(newEntries);
	}

	protected List<MatchingTerm> SD(String modelName, List<String> queryTerms) {
		List<MatchingTerm> newEntries = new ArrayList<>();
		
		//#1
		for(int i=0;i<queryTerms.size()-1;i++)
		{
			QTPBuilder qtp = QTPBuilder.of(new PhraseOp(new String[]{queryTerms.get(i), queryTerms.get(i+1)}));
			qtp.setWeight(0.1d);
			qtp.addWeightingModel(getModel(modelName,2));
			qtp.setTag(DEPENDENCE_TAG);
			newEntries.add(qtp.build());
		}
		
		//#uw8
		for(int i=0;i<queryTerms.size()-1;i++)
		{
			QTPBuilder qtp = QTPBuilder.of(new UnorderedWindowOp(new String[]{queryTerms.get(i), queryTerms.get(i+1)}, 8));
			qtp.setWeight(0.1d);
			qtp.addWeightingModel(getModel(modelName,8));
			qtp.setTag(DEPENDENCE_TAG);
			newEntries.add(qtp.build());
		}
		
		//#uw12
		List<String> allTerms = queryTerms;
		if (allTerms.size() > 12)
			allTerms = allTerms.subList(0, 11);
		QTPBuilder qtp = QTPBuilder.of(new UnorderedWindowOp(allTerms.toArray(new String[allTerms.size()]), 12));
		qtp.setWeight(0.1d);
		qtp.addWeightingModel(getModel(modelName,12));
		qtp.setTag(DEPENDENCE_TAG);
		newEntries.add(qtp.build());
		return newEntries;
	}

	

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName();
	}
	
}
