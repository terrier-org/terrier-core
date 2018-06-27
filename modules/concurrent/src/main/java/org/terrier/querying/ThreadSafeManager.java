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
 * The Original Code is ThreadSafeManager.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.querying;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.terrier.matching.Matching;
import org.terrier.structures.ConcurrentIndexLoader;
import org.terrier.structures.Index;
import org.terrier.structures.IndexFactory;

public class ThreadSafeManager extends LocalManager
{
	public static class Builder implements ManagerFactory.Builder {

		@Override
		public boolean supports(IndexRef ref) {
			return ConcurrentIndexLoader.isConcurrent(ref);
		}

		@Override
		public Manager fromIndex(IndexRef ref) {
			return new ThreadSafeManager(IndexFactory.of(ref));
		}
		
	}
	
	static class TSApplyLocalMatching extends ApplyLocalMatching {
		TSApplyLocalMatching() {
			Cache_Matching = Collections.synchronizedMap(Cache_Matching);
		}
		
		@Override
		protected Matching getMatchingModel(Request rq) {
			synchronized (this) {
				//matchings are not re-entrant, so we need to make a new one each time.
				Cache_Matching.clear();
				return super.getMatchingModel(rq);
			}
		}
	}
	
	static class TSPostFilterProcess extends PostFilterProcess {
		TSPostFilterProcess() {
			postfilterModuleManager.classCache = Collections.synchronizedMap(postfilterModuleManager.classCache);
		}
	}
	
	class TSModuleManager<K> extends ModuleManager<K>
	{

		TSModuleManager(String _typeName, String namespace, boolean _caching) {
			super(_typeName, namespace, _caching);
			classCache = Collections.synchronizedMap(super.classCache);
		}

		@SuppressWarnings("unchecked")
		@Override
		List<K> getActive(Map<String, String> controls) {
			List<K> rtr = super.getActive(controls);
			rtr.replaceAll(clz -> 
			{	
				if ((clz instanceof ApplyLocalMatching) && !( clz instanceof TSApplyLocalMatching) )
					return (K) tslm;
				if ((clz instanceof PostFilterProcess) && !( clz instanceof TSPostFilterProcess) )
					return (K) tspfp;
				return clz;
			});
			return rtr;
		}
		
	}

	TSApplyLocalMatching tslm = new TSApplyLocalMatching();
	TSPostFilterProcess tspfp = new TSPostFilterProcess();
	
	public ThreadSafeManager(Index _index) {
		super(_index);
		synchronizeCaches();
		logger.info("Using " + this.getClass().getSimpleName());
	}
	
	void synchronizeCaches() {
		processModuleManager = new TSModuleManager<>("processes", NAMESPACE_PROCESS, true);
		
	}
}
