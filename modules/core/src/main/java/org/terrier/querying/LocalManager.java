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
 * The Original Code is Manager.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.querying;
import static org.terrier.querying.SearchRequest.CONTROL_MATCHING;
import static org.terrier.querying.SearchRequest.CONTROL_WMODEL;
import gnu.trove.TIntArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.Matching;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.Model;
import org.terrier.matching.QueryResultSet;
import org.terrier.matching.ResultSet;
import org.terrier.matching.models.WeightingModel;
import org.terrier.matching.models.WeightingModelFactory;
import org.terrier.querying.parser.Query;
import org.terrier.structures.Index;
import org.terrier.structures.IndexFactory;
import org.terrier.terms.BaseTermPipelineAccessor;
import org.terrier.terms.TermPipelineAccessor;
import org.terrier.utility.ApplicationSetup;
/**
  * This class is responsible for handling/co-ordinating the main high-level
  * operations of a query. These are:
  * <ul>
  * <li>Processing (Term Pipeline, Control finding, term aggregation, Matching) @see org.terrier.querying.Process</li>
  * <li>Post-filtering @see org.terrier.querying.PostFilter </li>
  * </ul>
  * Example usage:
  * <pre>
  * IndexRef indexRef = IndexRef.of("/path/to/data.properties");
  * Manager m = ManagerFactory.from(indexRef);
  * SearchRequest srq = m.newSearchRequest("Q1", "term1 title:term2");
  * m.runSearchRequest(srq);
  * </pre>
  * <p>
  * <b>Properties</b><ul>
  * <li><tt>querying.default.controls</tt> - sets the default controls for each query</li>
  * <li><tt>querying.allowed.controls</tt> - sets the controls which a users is allowed to set in a query</li>
  * <li><tt>querying.processes</tt> - mappings between controls and the processes they should cause, in order that they should execute</li>
  * <li><tt>querying.postfilters</tt> - mappings between controls and the post filters they should cause, in order that they should execute</li>
  * </ul>
  * <p><b>Controls</b><ul>
  * <li><tt>start</tt> : The result number to start at - defaults to 0 (1st result)</li>
  * <li><tt>end</tt> : the result number to end at - defaults to 0 (display all results)</li>
  * <li><tt>c</tt> : the c parameter for the DFR models, or more generally, the parameters for weighting models</li>
  * <li><tt>c_set</tt> : "yes" if the c control has been set</li>
  * </ul>
  */
public class LocalManager implements Manager
{
	static final String DEFAULT_MATCHING = org.terrier.matching.daat.Full.class.getName();
	public static class Builder implements ManagerFactory.Builder
	{
		@Override
		public boolean supports(IndexRef ref) {
			return IndexFactory.whoSupports(ref) != null 
					&& ! ref.toString().startsWith("concurrent:"); //this is a small hack
		}

		@Override
		public Manager fromIndex(IndexRef ref) {
			Index index = IndexFactory.of(ref);
			assert index != null;
			return new LocalManager(index);
		}
		
	}
	
	protected static final Logger logger = LoggerFactory.getLogger(LocalManager.class);

	/* ------------Module default namespaces -----------*/
	/** The default namespace for Process instances to be loaded from */
	public static final String NAMESPACE_PROCESS
		= "org.terrier.querying";
	
	/** The default namespace for PostFilters to be loaded from */
	public static final String NAMESPACE_POSTFILTER
		= "org.terrier.querying.";
	/** The default namespace for Matching models to be loaded from */
	public static final String NAMESPACE_MATCHING
		= "org.terrier.matching.";
	/** The default namespace for Weighting models to be loaded from */
	public static final String NAMESPACE_WEIGHTING
		= "org.terrier.matching.models.";

	/** Class that keeps a cache of processes
	 * @since 5.0
	 */
	static class ModuleCache<K> {
		
		Map<String, K> classCache = new HashMap<>();
		boolean caching;
		String def_namespace;
		
		public ModuleCache(String namespace, boolean caching) {
			this.caching = caching;
			this.def_namespace = namespace;
		}
		
		/** Returns the post filter class named ModelName. Caches already
		  * instantiated matching models in map Cache_PostFilter.
		  * If the matching model name doesn't contain '.',
		  * then def_namespace is prefixed to the name.
		  * @param Name The name of the post filter to return */
		@SuppressWarnings("unchecked")
		K getModule(String Name)
		{
			K rtr = null;
			if (Name.indexOf(".") < 0 )
				Name = def_namespace +'.' +Name;
			
			//check for already loaded post filters
			if (caching)
				rtr = classCache.get(Name);
			if (rtr != null)
			{
				return rtr;
			}
			try 
			{		
				rtr = (K) ApplicationSetup.getClass(Name).newInstance();
			}
			catch(Exception e)
			{
				logger.error("Problem with class named: "+Name,e);
				return null;
			}
			if (caching)
				classCache.put(Name, rtr);
			return rtr;
		}
	}
	
	/** Class that keeps a cache of queries, and helps parse controls to identify them
	 * @since 5.0
	 */
	static class ModuleManager<K> extends ModuleCache<K> {
		
		protected static final String[] tinySingleStringArray = new String[0];
		protected static final String[][] tinyDoubleStringArray = new String[0][0];
		
		/** An ordered list of post filters names. The controls at the same index in the  PostFilters_Controls
		  * list turn on the post process in this list. */
		protected String[] Class_Order;

		/** A 2d array, contains (on 2nd level) the list of controls that turn on the PostFilters
		  * at the same 1st level place on PostFilters_Order */
		protected String[][] Class_Controls;
		
		
		protected String typeName;
		
		ModuleManager(String _typeName, String namespace, boolean _caching){
			super(namespace, _caching);
			this.typeName = _typeName;
			this.load_module_controls();
			//System.err.println(Arrays.deepToString(Class_Controls));
		}
		
		final int getSize()
		{
			return Class_Order.length;
		}
		
		List<K> getActive(Map<String,String> controls) {
			List<K> classes = new ArrayList<>();
			for(int i=0; i<Class_Order.length; i++)
			{
				String PostFilter_Name = Class_Order[i];
				for(int j=0; j<Class_Controls[i].length; j++)
				{
					String ControlName = Class_Controls[i][j];
					String value = (String)controls.get(ControlName);
					if (logger.isDebugEnabled()){
						logger.debug(ControlName+ "("+PostFilter_Name+") => "+value);
					}
					if (value == null)
						continue;
					value = value.toLowerCase();
					if(! (value.equals("off") || value.equals("false")))
					{
						classes.add(getModule(PostFilter_Name));
						//we've now run this post process module, no need to check the rest of the controls for it.
						break;
					}
				}
			}
			return classes;
		}
		
		/** parses the controls hashtable, looking for references to controls, and returns the appropriate
		  * postfilters to be run. */
		Iterator<K> getActiveIterator(Map<String,String> controls) {
			//TODO this implementation should check if controls have been updated since the iterator was created.
			return getActive(controls).iterator();
		}
		
		/** load in the allowed post filter controls, and the order to run post processes in */
		protected void load_module_controls()
		{
			if (ApplicationSetup.getProperty("querying."+typeName, "").length() == 0 
					&& ApplicationSetup.getProperty("querying."+typeName+".order", "").length() > 0)
			{
				load_module_controls_old();
				return;
			}
			String[] control_pf;
			List<String> _Class_Order = new ArrayList<>();
			List<String[]> _controls = new ArrayList<>();
			String tmp = ApplicationSetup.getProperty("querying."+typeName, "").trim();
			if (tmp.length() > 0)
				control_pf = tmp.split("\\s*,\\s*");
			else
				control_pf = new String[0];
			for(String pair : control_pf)
			{
				String[] tuple = pair.split(":", 2);
				_Class_Order.add(tuple[1]);
				_controls.add(new String[]{tuple[0]});
			}
			Class_Order = _Class_Order.toArray(new String[0]);
			Class_Controls = _controls.toArray(new String[0][]);
			
		}
		
		/** load in the allowed post filter controls, and the order to run post processes in */
		protected void load_module_controls_old()
		{
			/* what we have is a mapping of controls to processes, and an order processes should
			   be run in.
			   what we need is the order to check the controls in, and which pp to run for each
			*/

			String[] order_pf, control_pf;
			String tmp = ApplicationSetup.getProperty("querying."+typeName+".order", "").trim();
			if (tmp.length() > 0)
				order_pf = tmp.split("\\s*,\\s*");
			else
				order_pf = new String[0];
			
			tmp = ApplicationSetup.getProperty("querying."+typeName+".controls", "").trim();
			if (tmp.length() > 0)
				control_pf = tmp.split("\\s*,\\s*");
			else
				control_pf = new String[0];
			
			String[] control_and_pf = new String[control_pf.length*2]; int count = 0;
			//iterate through controls and pf names putting in 1d array
			for(int i=0; i<control_pf.length; i++)
			{
				if (control_pf[i].indexOf(":") > 0)
				{
					String[] control_and_postfilter = control_pf[i].split(":");
					control_and_pf[count] = control_and_postfilter[0];//control
					control_and_pf[count+1] = control_and_postfilter[1];//postfilter
					count+=2;
				}
			}

			/* basically, we now invert, so we have an array of pf names, in a separate array, a list
			of controls that can turn that pf on */
			List<String> pf_order = new ArrayList<String>();
			List<String[]> pf_controls = new ArrayList<String[]>();
			for(int i=0; i<order_pf.length; i++)
			{
				List<String> controls_for_this_pf = new ArrayList<String>();
				String tmpPF = order_pf[i];
				for(int j=0;j<count;j+=2)
				{
					if (tmpPF.equals(control_and_pf[j+1]))
					{
						controls_for_this_pf.add(control_and_pf[j]);
					}
				}
				//ok, there are controls that can turn this pf on, so lets enable it
				if (controls_for_this_pf.size() > 0)
				{
					pf_controls.add(controls_for_this_pf.toArray(tinySingleStringArray));
					pf_order.add(tmpPF);
				}			
			}
			//cast back to arrays
			Class_Order = pf_order.toArray(tinySingleStringArray);
			Class_Controls = pf_controls.toArray(tinyDoubleStringArray);
		}
		
	}
	

	@ProcessPhaseRequisites(ManagerRequisite.MQT)
	static class ApplyLocalMatching implements Process {	
	
		protected final boolean MATCH_EMPTY_QUERY = Boolean.parseBoolean(ApplicationSetup.getProperty("match.empty.query","false"));
		protected Map<Index, Map<String, Matching>> Cache_Matching = new HashMap<Index, Map<String, Matching>>();
		
		/** Returns the matching model indicated to be used, based on the Index and the Matching
		 * name specified in the passed Request object. Caches already 
		  * instantiated matching models in Map Cache_Matching.
		  * If the matching model name doesn't contain '.', then NAMESPACE_MATCHING
		  * is prefixed to the name. 
		  * @param rq The request indicating the Matching class, and the corresponding
		  * instance to use
		  * @return null If an error occurred obtaining the matching class
		  */
		protected Matching getMatchingModel(Request rq)
		{
			Matching rtr = null;
			Index _index = rq.getIndex();
			String ModelName = rq.getControl(CONTROL_MATCHING, DEFAULT_MATCHING);
			//add the namespace if the modelname is not fully qualified
			
			if (ModelName == null || ModelName.length() == 0)
				throw new IllegalArgumentException("matching model must be set in request");
			final String ModelNames[] = ModelName.split("\\s*,\\s*");
			final int modelCount = ModelNames.length;
			StringBuilder entireSequence = new StringBuilder();
			for(int i =0;i<modelCount;i++)
			{
				if (ModelNames[i].indexOf(".") < 0 )
					ModelNames[i]  = NAMESPACE_MATCHING + ModelNames[i];
				entireSequence.append(ModelNames[i]);
				entireSequence.append(",");
			}
			ModelName = entireSequence.substring(0,entireSequence.length() -1);
			//check for already instantiated class
			Map<String, Matching> indexMap = Cache_Matching.get(_index);
			if (indexMap == null)
			{
				Cache_Matching.put(_index, indexMap = new HashMap<String, Matching>());
			}
			else
			{
				rtr = indexMap.get(ModelName);
			}
			if (rtr == null)
			{
				boolean first = true;
				for(int i=modelCount-1;i>=0;i--)
				{
					try
					{
						//load the class
						if (ModelNames[i].equals("org.terrier.matching.Matching"))
							ModelNames[i] = "org.terrier.matching.daat.Full";
						Class<? extends Matching> formatter = ApplicationSetup.getClass(ModelNames[i]).asSubclass(Matching.class);
						//get the correct constructor - an Index class in this case
						
						Class<?>[] params;
						Object[] params2;
						if (first)
						{
							params = new Class[]{Index.class};
							params2  = new Object[]{_index};
						}
						else
						{
							params = new Class[]{Index.class, Matching.class};
							params2 = new Object[]{_index, rtr};
						}
						//and instantiate
						rtr = (Matching) (formatter.getConstructor(params).newInstance(params2));
						first = false;
					}
					catch(java.lang.reflect.InvocationTargetException ite)
					{
						logger.error("Recursive problem with matching model named: "+ModelNames[i],ite);
						return null;
					}
					catch(Exception e)
					{
						logger.error("Problem with matching model named: "+ModelNames[i],e);
						return null;
					}
				}
			}
			Cache_Matching.get(_index).put(ModelName, rtr);
			return rtr;
		}
		
		@Override
		public void process(Manager manager, Request rq) {
			if (rq.isEmpty() && ! MATCH_EMPTY_QUERY )
			{
				logger.warn("Returning empty result set as query "+rq.getQueryID()+" is empty");
				rq.setResultSet(new QueryResultSet(0));
			}
			
			MatchingQueryTerms mqt = rq.getMatchingQueryTerms();
			
			//TODO some exception handling here for not found models
			Model wmodel = getWeightingModel(rq);
			
			/* craigm 05/09/2006: only set the parameter of the weighting model
			 * if it was explicitly set if c_set control is set to true. Otherwise
			 * allow the weighting model to assume it's default value.
			 * This changes previous behaviour. TODO: some consideration for
			 * non TREC applications */
			if (rq.getControl("c_set").equals("true"))
			{
				wmodel.setParameter(Double.parseDouble(rq.getControl("c")));
			}
			
			// this allows matching to only operate on scoring a subset of the query terms
//			if (rq.getControl("matchingtags").length() > 0)
//			{
//				String[] tags = rq.getControl("matchingtags").split(";");
//				mqt.getMatchingTags().addAll(Lists.newArrayList(tags));
//			}
			
			Matching matching = getMatchingModel(rq);
			if (logger.isDebugEnabled()){
				logger.debug("weighting model: " + wmodel.getInfo());
			}
			mqt.setDefaultTermWeightingModel((WeightingModel)wmodel);
			Query q = rq.getQuery();
			logger.info(mqt.toString());
				
	
			mqt.setQuery(q);
			mqt.normaliseTermWeights();
			try{
				ResultSet outRs = matching.match(rq.getQueryID(), mqt);
				//check to see if we have any negative infinity scores that should be removed
				int badDocuments = 0;
				for (int i = 0; i < outRs.getResultSize(); i++) {
					if (outRs.getScores()[i] == Double.NEGATIVE_INFINITY)
						badDocuments++;
				}
				if (badDocuments > 0)
					logger.debug("Found "+badDocuments+" documents with a score of negative infinity in the result set returned, they will be removed.");
				
				//now crop the resultset down to a query result set.
				rq.setResultSet(outRs.getResultSet(0, outRs.getResultSize()-badDocuments));
			} catch (IOException ioe) {
				logger.error("Problem running Matching, returning empty result set as query "+rq.getQueryID(), ioe);
				rq.setResultSet(new QueryResultSet(0));
			}
		}
	}
	

	/** A generic query id for when no query id is given **/
	private static final String GENERICQUERYID = "GenericQuery";
	
	/* ------------------------------------------------*/
	/* ------------Instantiation caches --------------*/
	/** Cache loaded Matching models per Index in this map */
		
	
	/** TermPipeline processing */
	protected TermPipelineAccessor tpa;
	
	/** The index this querying comes from */
	protected Index index;
	/** This contains a list of controls that may be set in the querying API */
	protected Set<String> Allowed_Controls;
	/** This contains the mapping of controls and their values that should be 
	  * set identically for each query created by this Manager */
	protected Map<String, String> Default_Controls;
	/** How many default controls exist.
	  * Directly corresponds to Default_Controls.size() */
	protected int Defaults_Size = 0;
	
	
	ModuleManager<Process> processModuleManager = new ModuleManager<>("processes", NAMESPACE_PROCESS, true);
	
	
	/** This class is used as a TermPipelineAccessor, and this variable stores
	  * the result of the TermPipeline run for that term. */
	protected String pipelineOutput = null;

	
	/** Construct a Manager using the specified Index
	  * Throws IllegalArgumentException if the specified index is null
	  * @param _index The index to use in this manage
	  */
	public LocalManager(Index _index)
	{
		if (_index == null)
			throw new IllegalArgumentException("Null index specified to manager. Did the index load?");
		this.useThisIndex(_index);
		this.load_pipeline();
		this.load_controls_allowed();
		this.load_controls_default();
	}
	/* ----------------------- Initialisation methods --------------------------*/

	/** use the index specified for the Manager */
	protected void useThisIndex(final Index i)
	{
		index = i;
	}

	/** load in the controls that user is allowed to set */
	protected void load_controls_allowed()
	{
		/* load in the controls that user is allowed to set */
		String allowed = ApplicationSetup.getProperty("querying.allowed.controls", "c,start,end").trim().toLowerCase();
		String[] controls = allowed.split("\\s*,\\s*");
		Allowed_Controls = new HashSet<String>();
		for(String control : controls)
		{
			Allowed_Controls.add(control);
		}
	}
	
	/** load in the control defaults */
	protected void load_controls_default()
	{
		String defaults = ApplicationSetup.getProperty("querying.default.controls", "").trim();
		String[] controls = defaults.split("\\s*,\\s*");
		Default_Controls = new HashMap<String, String>();
		for(String kv : controls)
		{
			String control[] = kv.split(":", 2);
			/* control[0] contains the control name, control[1] contains the value */
			if (control.length < 2)
			{
				continue;
			}
			Default_Controls.put(control[0].toLowerCase(), control[1]);
		}
		//String def_c = null;
		Defaults_Size = Default_Controls.size();
	}

	/** load in the term pipeline */
	protected void load_pipeline()
	{
		final String[] pipes = ApplicationSetup.getProperty(
				"termpipelines", "Stopwords,PorterStemmer").trim()
				.split("\\s*,\\s*");
		synchronized (this) {
			tpa = new BaseTermPipelineAccessor(pipes);
		}		
	}

	/* -------------- factory methods for SearchRequest objects ---------*/
	/* (non-Javadoc)
	 * @see org.terrier.querying.IManager#newSearchRequest()
	 */
	@Override
	public SearchRequest newSearchRequest()
	{
		Request q = new Request();
		if (Defaults_Size >0)
			setDefaults(q);
		q.setIndex(this.index);
		return (SearchRequest)q;
	}
	/* (non-Javadoc)
	 * @see org.terrier.querying.IManager#newSearchRequest(java.lang.String)
	 */
	@Override
	public SearchRequest newSearchRequest(String QueryID)
	{
		Request q = new Request();
		if (Defaults_Size >0)
			setDefaults(q);
		q.setQueryID(QueryID);
		q.setIndex(this.index);
		return (SearchRequest)q;
	}

	/* (non-Javadoc)
	 * @see org.terrier.querying.IManager#newSearchRequest(java.lang.String, java.lang.String)
	 */
	@Override
	public SearchRequest newSearchRequest(String QueryID, String query)
	{
		Request q = new Request();
		if (Defaults_Size >0)
			setDefaults(q);
		q.setQueryID(QueryID);
		q.setIndex(this.index);
//		try{
//			QueryParser.parseQuery(query, q);	
//		} catch (QueryParserException qpe) {
//			logger.error("Error while parsing the query.",qpe);
//		}
		q.setOriginalQuery(query);
		return q;
	}
	
	/* (non-Javadoc)
	 * @see org.terrier.querying.IManager#newSearchRequestFromQuery(java.lang.String)
	 */
	@Override
	public SearchRequest newSearchRequestFromQuery(String query)
	{
		return newSearchRequest(GENERICQUERYID, query);
	}
	

	/** Set the default values for the controls of this new search request
	 *  @param srq The search request to have the default set to. This is
	 *  done using the Default_Controls table, which is loaded by the load_controls_default
	 *  method. The default are set in the properties file, by the <tt>querying.default.controls</tt> */
	protected void setDefaults(Request srq)
	{
		srq.setControls(new HashMap<String,String>(Default_Controls));
		srq.setIndex(this.index);
	}
	/**
	 * Returns the index used by the manager. It is used for matching
	 * and possibly post-processing.
	 * @return Index the index used by the manager.
	 */
	public Index getIndex() {
		return index;
	}
	
	@Override
	public IndexRef getIndexRef() {
		return index.getIndexRef();
	}
	
	/* (non-Javadoc)
	 * @see org.terrier.querying.IManager#setProperty(java.lang.String, java.lang.String)
	 */
	@Override
	public void setProperty(String key, String value)
	{
		ApplicationSetup.setProperty(key, value);
	}

	/* (non-Javadoc)
	 * @see org.terrier.querying.IManager#setProperties(java.util.Properties)
	 */
	@Override
	public void setProperties(Properties p) {
		//for(String k : ((Set<String>)p.keySet()))
		Enumeration<?> e = p.keys();
		while (e.hasMoreElements()) {
			String propertyName = (String)e.nextElement();
			String propertyValue = p.getProperty(propertyName);
			setProperty(propertyName, propertyValue);
		}
	}
	
	static boolean hasAnnotation(Class<?> clazz, ManagerRequisite req)
	{
		ProcessPhaseRequisites anno =  clazz.getAnnotation(ProcessPhaseRequisites.class);
		if (anno == null)
			return false;
		for(ManagerRequisite in :  anno.value())
			if (in == req)
				return true;
		return false;
	}
	
	/** Runs the PostFilter modules in order added. PostFilter modules
	  * filter the resultset. Examples might be removing results that don't have
	  * a hostname ending in the required postfix (site), or document ids that don't match
	  * a pattern (scope).
	  */
	static class PostFilterProcess implements Process
	{

		protected boolean CACHING_FILTERS = Boolean.parseBoolean(ApplicationSetup.getProperty("manager.caching.filters","false"));
		ModuleManager<Process> postfilterModuleManager = new ModuleManager<>("postfilters", "org.terrier.querying", CACHING_FILTERS);

		@Override
		public void process(Manager manager, Request rq) {
			PostFilter[] filters = postfilterModuleManager.getActive(rq.getControls()).toArray(new PostFilter[0]);
			final int filters_length = filters.length;
			
			//the results to filter
			ResultSet results = rq.getResultSet();

			//the size of the results - this could be more than what we need to display
			int ResultsSize = results.getResultSize();

			//load in the lower and upper bounds of the resultset
			String tmp = rq.getControl("start");/* 0 based */
			if (tmp.length() == 0)
				tmp = "0";
			int Start = Integer.parseInt(tmp);
			tmp = rq.getControl("end");
			if (tmp.length() == 0)
				tmp = "0";
			int End = Integer.parseInt(tmp);/* 0 based */
			if (End == 0)
			{
				End = ResultsSize -1;
			}
			int length = End-Start+1;
			if (length > ResultsSize)
				length = ResultsSize-Start;
			//we've got no filters set, so just give the results ASAP
			if (filters_length == 0)
			{
				if (Start != 0 && length != ResultsSize)
					rq.setResultSet( results.getResultSet(Start, length) );
				if (logger.isDebugEnabled()) { 
					logger.debug("No filters, just Crop: "+Start+", length "+length);
					logger.debug("Resultset is now "+results.getScores().length + " long");
				}
				return;
			}

			//tell all the postfilters that they are processing another query
			for(int i=0;i<filters_length; i++)
			{
				filters[i].new_query(manager, rq, results);
			}
			
			int doccount = -1;//doccount is zero-based, so 0 means 1 document
			TIntArrayList docatnumbers = new TIntArrayList();//list of resultset index numbers to keep
			byte docstatus; int thisDocId;
			int[] docids = results.getDocids();
			//int elitesetsize = results.getExactResultSize();
			//the exact result size is the total number of
			//documents that would be retrieved if we
			//didn't do any cropping
			int elitesetsize = results.getResultSize();
			for(int thisdoc = 0; thisdoc < elitesetsize; thisdoc++)
			{
				//run this document through all the filters
				docstatus = PostFilter.FILTER_OK;
				thisDocId = docids[thisdoc];
				//run this doc through the filters
				for(int i=0;i<filters_length; i++)
				{
					if ( ( docstatus = filters[i].filter(manager, rq, results, thisdoc, thisDocId) )
						== PostFilter.FILTER_REMOVE
					)
						break;
						//break if the document has to be removed
				}
				//if it's not being removed, then
				if (docstatus != PostFilter.FILTER_REMOVE) //TODO this should always be true
				{
					//success, another real document
					doccount++;
					//check if it's in our results "WINDOW"
					if (doccount >= Start)
					{
						if (doccount <= End)
						{	//add to the list of documents to keep
							docatnumbers.add(thisdoc);
							//System.out.println("Keeping @"+thisdoc);
						}
						else
						{
							//we've now got enough results, break
							break;
						}
					}
				}
				else
				{
					//System.out.println("Removed");
				}
			}
			//since doccount is zero-based, we add one so that it
			//corresponds to the real number of documents.
			doccount++; 
			rq.setNumberOfDocumentsAfterFiltering(doccount);
			if (docatnumbers.size() < docids.length)
			{
				//result set is definently shorter, replace with new one
				rq.setResultSet( results.getResultSet(docatnumbers.toNativeArray()));
				rq.getResultSet().setExactResultSize(results.getExactResultSize());
			}
		}
		
	}
	
	
	public void runSearchRequest(SearchRequest srq)
	{	
		Request rq = (Request)srq;		
		logger.info("Starting to execute query " + srq.getQueryID());
		boolean mqtObtained = rq.getMatchingQueryTerms() != null;
		boolean hasRawQuery = rq.getOriginalQuery() != null;
		boolean hasTerrierQLquery = rq.getQuery() != null;
		boolean hasResultSet = rq.getResultSet() != null;
		logger.debug(rq.getControls().toString());
		
		if (processModuleManager.getSize() == 0)
		{
			RuntimeException e = new IllegalArgumentException("Property querying.processes was not set - you need to have some Process classes defined");
			logger.error("No Process classes were available for the Manager. Matching will likely fail. Do you have a terrier.properties file with querying.processes property configured?", e);
			throw e;	
		}
		
		Iterator<Process> iter = processModuleManager.getActiveIterator(rq.getControls());
		List<String> processesDone = new ArrayList<String>();
		int ran = 0;
		rq.setControl("runname", "");
		while(iter.hasNext())
		{
			
			Process p = iter.next();
			assert(p != null);
			if (hasAnnotation(p.getClass(), ManagerRequisite.MQT) && ! mqtObtained)
				throw new IllegalStateException("Process " + p.getInfo() + " required matchingqueryterms, but mqt not yet set for query qid " + rq.getQueryID()  + " previousProcess=" + processesDone.toString() + " controls=" + rq.getControls().toString());
			if (hasAnnotation(p.getClass(), ManagerRequisite.RAWQUERY) && ! hasRawQuery)
				throw new IllegalStateException("Process " + p.getInfo() + " required rawquery, but no raw query found for qid " + rq.getQueryID() + " previousProcess=" + processesDone.toString() + " controls=" + rq.getControls().toString());
			if (hasAnnotation(p.getClass(), ManagerRequisite.TERRIERQL) && ! hasTerrierQLquery)
				throw new IllegalStateException("Process " + p.getInfo() + " required TerrierQL query, but no TerrierQL query found for qid " + rq.getQueryID() + " previousProcess=" + processesDone.toString() + " controls=" + rq.getControls().toString());
			if (hasAnnotation(p.getClass(), ManagerRequisite.RESULTSET) && ! hasResultSet)
				throw new IllegalStateException("Process " + p.getInfo() + " required resultset, but none found for qid " + rq.getQueryID() + " previousProcess=" + processesDone.toString() + " controls=" + rq.getControls().toString());
			
			
			logger.info("running process " + p.getInfo());
			p.process(this, rq);
			hasTerrierQLquery = rq.getQuery() != null;
			mqtObtained = rq.getMatchingQueryTerms() != null;
			hasRawQuery = rq.getOriginalQuery() != null;
			hasResultSet = rq.getResultSet() != null;
			rq.setControl("previousprocess", p.getClass().getName());
			rq.setControl("runname", rq.getControl("runname")+ "_" + p.getInfo());
			processesDone.add(p.getInfo());
			ran++;
		}
		
		if (! mqtObtained)
		{
			logger.warn("After running " + ran + " processes, no MQT was obtained. Matching will likely fail. Controls were: " + rq.getControls().toString());
		}
		String msg = "";
		if (hasResultSet)
		{
			msg = " - " + rq.getResultSet().getResultSize() + " results retrieved";
		} else {
			logger.warn("After running " + ran + " processes, no ResultSet was obtained. Controls were: " + rq.getControls().toString());
		}
		logger.info("Finished executing query " + srq.getQueryID() + msg);
	 }
	
	/*-------------------------------- helper methods -----------------------------------*/
	
	/** Returns the weighting model requested by the Request from
	 * the WeightingModel factory.
	 * @param rq The name of the weighting model to instantiate */
	protected static Model getWeightingModel(Request rq) {
		String wmodel = rq.getControl(CONTROL_WMODEL, 
				ApplicationSetup.getProperty("trec.model", "DPH"));
		return WeightingModelFactory.newInstance(wmodel, rq.getIndex());
	}
	
	
	/**
	 * Returns information about the weighting models and 
	 * the post processors used for the given search request.
	 * @param srq the search request for which we obtain 
	 *		the information.
	 * @return String information about the weighting models 
	 *		 and the post processors used.
	 */
	public String getInfo(SearchRequest srq) {
		Request rq = (Request)srq; 
		StringBuilder info = new StringBuilder();
		
		//obtaining the weighting model information
		Model wmodel = getWeightingModel(rq);
		final String param = rq.getControl("c");
		if (rq.getControl("c_set").equals("true") && param.length() > 0)
			wmodel.setParameter(Double.parseDouble(param));
		info.append(wmodel.getInfo());
		
		info.append(srq.getControl("runname"));
		
		//obtaining the post-processors information
		
//		Map<String,String> controls = rq.getControlHashtable();
//		for(Process p : postprocessModuleManager.getActive(controls))
//		{
//			info.append("_"+p.getInfo());
//		}
		return info.toString();
	}
}
