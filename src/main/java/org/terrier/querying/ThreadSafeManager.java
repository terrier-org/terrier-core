package org.terrier.querying;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.terrier.matching.Matching;
import org.terrier.structures.Index;

public class ThreadSafeManager extends LocalManager
{
	static class TSApplyLocalMatching extends ApplyLocalMatching {
		TSApplyLocalMatching() {
			Cache_Matching = Collections.synchronizedMap(Cache_Matching);
		}
		
		@Override
		protected Matching getMatchingModel(Request rq) {
			synchronized (this) {
				Cache_Matching.clear();//TODO why is this here?
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
	}
	
	void synchronizeCaches() {
		processModuleManager = new TSModuleManager<>("processes", NAMESPACE_PROCESS, true);
		
	}
}