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
 * The Original Code is ApplyTermPipeline.java.
 *
 * The Original Code is Copyright (C) 2017-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.querying;

import java.util.Iterator;

import gnu.trove.TIntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.MatchingQueryTerms.MatchingTerm;
import org.terrier.matching.matchops.MultiTermOp;
import org.terrier.matching.matchops.Operator;
import org.terrier.matching.matchops.SingleTermOp;
import org.terrier.terms.BaseTermPipelineAccessor;
import org.terrier.structures.Index;
import org.terrier.structures.IndexConfigurable;
import org.terrier.structures.PropertiesIndex;
import org.terrier.terms.TermPipelineAccessor;
import org.terrier.utility.ApplicationSetup;

@ProcessPhaseRequisites(ManagerRequisite.MQT)
/** Applies the TermPipeline to the MatchingQueryTerms object.
 * The configuration of the TermPipeline is obtained based on the following configurations:
 * 
 * 1. A control setting for the query, if present
 * 2. The "termpiplines" property of the index, if present
 * 3. Failing that, the "termpipelines" property in ApplicationSetup
 * 
 * These configuration rules were introduced in Terrier 5.6. This means that previously where a property was sufficient
 * to override any stemming configuration recorded in the index at retrieval, now a control is required. However, as the index termpipeline configuration
 * is used by default, the scenarios where explicit configuration is needed are less frequent. In particular, if you create an index
 * using a particular stemmer, that stemmer should be automatically picked up from the index (some exceptions - realtime and
 * composed indices).
 */
public class ApplyTermPipeline implements Process {

	/** Logging error messages */
	private static Logger LOG = LoggerFactory.getLogger(ApplyTermPipeline.class);

	TermPipelineAccessor defaultTpa = null;
	String info = null;
	String defaultPipelineSource = null;
	String lastPipeline = null;
	TermPipelineAccessor lastTpa = null;
	boolean warnNoIndexNoControl = false;
	
	public ApplyTermPipeline()
	{
		this(ApplicationSetup.getProperty("termpipelines", "Stopwords,PorterStemmer").trim());
	}

	public ApplyTermPipeline(String pipeline)
	{
		synchronized (this) {
			defaultTpa = this.load_pipeline(pipeline);
			defaultPipelineSource = pipeline;
		}
	}
	
	/** load in the term pipeline */
	protected TermPipelineAccessor load_pipeline(final String tp)
	{
		final String[] pipes = tp.split("\\s*,\\s*");
		info = "termpipelines=" + tp;
		return new BaseTermPipelineAccessor(pipes);	
	}
	
	interface Visitor {
		boolean visit(Operator qt);
		boolean visit(SingleTermOp sqt);
		boolean visit(MultiTermOp mqt);
	}
	
	TermPipelineAccessor getPipeline(Manager manager, Request q) {
		
		assert defaultPipelineSource != null;

		String tp = null;
		Index index = q.getIndex();
		boolean hasControl = q.hasControl("termpipelines");
		boolean noIndexNoControl = false;
		
		if (! hasControl && index != null) {
			if (index instanceof PropertiesIndex) {
				PropertiesIndex pi = (PropertiesIndex) index;
				String indexProp = pi.getIndexProperty("termpipelines", null); 
				if (indexProp != null) {
					tp = indexProp;
				} else {
					// no property found
					noIndexNoControl = true;
				}
			} else {
				// not a properties index
				noIndexNoControl = true;
			}	
		}
		if (hasControl)
		{
			tp = q.getControl("termpipelines");
			noIndexNoControl = false;
		}
		if (tp == null || defaultPipelineSource.equals(tp)) {
			
			//we want to promote the use of controls over ApplicationSetup. This will warn if ApplicationSetup is used.
			//it will likely only occur for settings of MultiIndex and RealtimeIndex etc.
			if (noIndexNoControl && ! warnNoIndexNoControl)
			{
				LOG.warn("The index has no termpipelines configuration, and no control configuration is found. "+
					"Defaulting to global termpipelines configuration of '"+defaultPipelineSource+"'. " +  
					"Set a termpipelines control to remove this warning.");
				warnNoIndexNoControl = true;
			}
			
			return defaultTpa;
		}
		if (tp.equals(lastPipeline))
		{
			assert lastTpa != null;
			return lastTpa;
		}
		synchronized(this) {
			lastPipeline = tp;
			info = tp;
			lastTpa = load_pipeline(tp);
			if (index != null && lastTpa instanceof IndexConfigurable) {
				((IndexConfigurable)lastTpa).setIndex(index);
			}
			return lastTpa;
		}
		
	}
	
	@Override
	public void process(Manager manager, Request q) {
		
		TermPipelineAccessor tpa = getPipeline(manager, q);
		assert tpa != null;
		
		TIntArrayList toDel = new TIntArrayList();
		int i=-1;
		
		Visitor visitor = new Visitor()
		{
			@Override
			public boolean visit(Operator qt) {
				if(qt instanceof SingleTermOp)
				{
					return this.visit((SingleTermOp)qt);
				}
				else if(qt instanceof MultiTermOp)
				{
					return this.visit((MultiTermOp)qt);
				}
				return true;
			}
			
			@Override
			public boolean visit(SingleTermOp sqt) {
				String origTerm = sqt.getTerm();
				String newTerm;
				synchronized(tpa) {
					newTerm = tpa.pipelineTerm(origTerm);
				}
				if (newTerm == null)
					return false;
				sqt.setTerm(newTerm);
				return true;
			}

			@Override
			public boolean visit(MultiTermOp mqt) {
				Operator[] qts = mqt.getConstituents();
				boolean OK = true;
				for(Operator qt : qts) {
					//boolean OKqt = 
					this.visit(qt);
				}
				//TODO check if all required?
				return OK;
			}
			
		};
		
		MatchingQueryTerms mqt = q.getMatchingQueryTerms();
		String lastTerm = null;
		boolean dups = false;
		for(MatchingTerm t : mqt)
		{
			i++;
			boolean OK = visitor.visit(t.getKey());
			if (! OK)
				toDel.add(i);
			else
			{
				dups = dups || (t.getKey().toString().equals(lastTerm));
				lastTerm = t.getKey().toString();
			}
		}
		toDel.reverse();
		for(int removeIndex : toDel.toNativeArray())
		{
			mqt.remove(removeIndex);
		}
		
		if (! dups)
			return;
		
		MatchingTerm prev = null;
		Iterator<MatchingTerm> iter = mqt.iterator();
		while(iter.hasNext())
		{
			MatchingTerm t = iter.next();
			if (prev != null 
					&& t.getKey().toString().equals(prev.getKey().toString())  // this and the previous have the same string
					&& t.getValue().equals(prev.getValue()) // this previous word has the same models, tags and requirements
					)
			{
				prev.getValue().setWeight(prev.getValue().getWeight() + t.getValue().getWeight());
				iter.remove();
			}
			prev = t;
		}
	}

	@Override
	public String getInfo() {
		return this.getClass().getSimpleName() + '(' + this.info + ')';
	}

}
