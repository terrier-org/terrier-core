/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - Department of Computing Science
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
 * The Original Code is SimpleJettyHTTPServer.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 *   Ben He <ben{a.}dcs.gla.ac.uk>
 */
package org.terrier.querying;

import gnu.trove.TObjectIntHashMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.ResultSet;
import org.terrier.querying.summarisation.Summariser;
import org.terrier.structures.MetaIndex;
import org.terrier.structures.collections.LRUMap;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.StringTools;
import org.terrier.utility.StringTools.ESCAPE;

/** This class decorates a result set with metadata. This metadata can be highlighted, 
 * can have a query biased summary created, and
 * also be escaped for display in another format.
 * <b>Controls:</b>
 * <ul>
 * <li><tt>summaries</tt> - comma or semicolon delimited list of the key names for 
 * which a query biased summary should be created. e.g. <tt>summaries:snippet</tt></li>
 * <li><tt>emphasis</tt> - comma or semicolon delimited list of they key names that
 *  should have boldened for occurrences of the query terms. 
 *  e.g. <tt>emphasis:title;snippet</tt></li>
 * <li><tt>earlyDecorate</tt> - comma or semicolon delimited list of the key names
 * that should be decorated early, e.g. to support another PostProcess using them.</li>
 * <li><tt>escape</tt> - comma or semicolon delimited list of the key names that 
 * should be escaped e.g. <tt>escape:title;snippet;url</tt>. Currently, per-key 
 * type escaping is not supported. The default escape type is 
 * defined using the property <tt>decorate.escape</tt>.</li>
 * </ul>
 * <b>Properties:</b>
 * <ul>
 * <li><tt>decorate.escape</tt> - default escape type for metadata. Default is HTML. 
 * Possible escape types include XML, JAVASCRIPT, and URL. See utility.StringTools.ESCAPE
 * </ul>
 * 
 * @author Craig Macdonald, Vassilis Plachouras, Ben He
 * @since 3.0
 */
public class Decorate implements PostProcess, PostFilter {

	/** delimiters for breaking down the values of controls further */
	protected static final String[] CONTROL_VALUE_DELIMS = new String[]{";", ","};

	/** Logging error messages */
	private static Logger logger = LoggerFactory.getLogger(Decorate.class);
		
	/** The cache used for the meta data. Implements a 
	 * Least-Recently-Used policy for retaining the most 
	 * recently accessed metadata. */ 
	protected LRUMap<Integer,String[]> metaCache = null;
	
	/** The meta index server. It is provided by the manager. */
	protected MetaIndex metaIndex = null;
	
	//a regular expression that detects the existence of a 
	//control character or a non-visible character in a string
	protected static final Pattern controlNonVisibleCharacters = Pattern.compile("[\\p{Cntrl}\uFFFD]|[^\\p{Graph}\\p{Space}]");
	
	/** what is the default escape sequence */
	protected static final ESCAPE defaultEscape = ESCAPE.parse(ApplicationSetup.getProperty("decorate.escape", "html"));
	
	//the matcher that corresponds to the above regular expression, initialised
	//for an empty string. This variable is defined in order to avoid creating
	//a new object every time it is required to check for and remove control characters, or non-visible characters.
	protected Matcher controlNonVisibleCharactersMatcher = controlNonVisibleCharacters.matcher("");
	
	protected static final Pattern cleanQuery = Pattern.compile(" \\w+\\p{Punct}\\w+ ");
	
	/** highlighting pattern for the current query */
	protected Pattern highlight;
	
	/** query terms of the current query */
	protected String[] qTerms;
	
	//the metadata keys
	protected TObjectIntHashMap<String> keys = new TObjectIntHashMap<String>();
	//the keys which should be summarised
	protected Set<String> summaryKeys = new HashSet<String>();
	//keys which should be emphasised
	protected Set<String> emphasisKeys = new HashSet<String>();
	//keys which should be escaped
	protected Map<String, ESCAPE> escapeKeys = new HashMap<String,ESCAPE>();
	//keys which should be decorated at PostProcess rather than filter
	protected Set<String> earlyKeys = new HashSet<String>();
	
	protected Summariser summariser;
	protected String[] metaKeys;
	
	/** 
	 * {@inheritDoc} 
	 */
	@SuppressWarnings("unchecked")
	public void new_query(Manager m, SearchRequest q, ResultSet rs)
	{
		metaIndex = m.getIndex().getMetaIndex();
		int i=0;
		for(String k : metaIndex.getKeys())
		{
			keys.put(k,i++);
		}
		
		for(String summarykey : ArrayUtils.parseDelimitedString(q.getControl("summaries"),CONTROL_VALUE_DELIMS))
		{
			summaryKeys.add(summarykey);
		}
		for(String emphKey : ArrayUtils.parseDelimitedString(q.getControl("emphasis"),CONTROL_VALUE_DELIMS))
		{
			emphasisKeys.add(emphKey);
		}
		for(String earlyKey : ArrayUtils.parseDelimitedString(q.getControl("earlyDecorate"),CONTROL_VALUE_DELIMS))
		{
			earlyKeys.add(earlyKey);
		}
		for(String escapeKey : ArrayUtils.parseDelimitedString(q.getControl("escape"),CONTROL_VALUE_DELIMS))
		{
			escapeKeys.put(escapeKey, defaultEscape);
		}
		
		if (m.getIndex().hasIndexStructure("metacache"))
			metaCache = (LRUMap<Integer,String[]>) m.getIndex().getIndexStructure("metacache");
		else
			metaCache = new LRUMap<Integer,String[]>(1000);

		//preparing the query terms for highlighting
		String original_q = q.getOriginalQuery();
		if (original_q == null)
		{
			return;
		}
		highlight = generateEmphasisPattern(original_q.trim().toLowerCase().split("\\s+"));
		summariser = Summariser.getSummariser();
		metaKeys = keys.keys(new String[keys.size()]);
		
		qTerms = cleanQuery.matcher(q.getOriginalQuery()).replaceAll(" ").toLowerCase().split(" ");
		for(int p = 0; p < qTerms.length; p++)
			if(qTerms[p].contains(":"))
				qTerms[p] = qTerms[p].substring(qTerms[p].indexOf(':')+1);
	}
	
	/** 
	 * {@inheritDoc} 
	 */
	//decoration at the postfilter stage
	public byte filter(Manager m, SearchRequest q, ResultSet rs, int rank, int docid)
	{		
		String[] metadata = getMetadata(metaKeys, docid);
		int keyID = 0;
		for(String key : metaKeys)
		{
			//get the desired metdata value
			String value = metadata[keyID];
			//is it a snippet? if so, do create query biassed summary
			if (summaryKeys.contains(key))
			{
				value =  summariser.generateSummary(value, qTerms);
			}
			//do some cleaning of the snippet
			controlNonVisibleCharactersMatcher.reset(value);
			value = controlNonVisibleCharactersMatcher.replaceAll("");
			//is escaping needed?
			StringTools.ESCAPE e = escapeKeys.get(key);
			if (e != null)
			{
				value = StringTools.escape(e, value);
			}
			//add to the result set
			rs.addMetaItem(key, rank, value);
			
			//should it be highlighted?
			if (emphasisKeys.contains(key))
			{
				String value_highlight = highlight.matcher(value).replaceAll("$1<b>$2</b>$3");
				rs.addMetaItem(key+ "_emph", rank, value_highlight);
			}
			keyID++;
		}
		return FILTER_OK;
	}

	/** decoration at the postprocess stage. only decorate if required for future postfilter or postprocesses.
	  * @param manager The manager instance handling this search session.
	  * @param q the current query being processed
	  */
	public void process(Manager manager, SearchRequest q)
	{
		ResultSet rs = q.getResultSet();
		new_query(manager, q, rs);
		if (earlyKeys.size() == 0)
		{
			logger.warn(this.getClass().getSimpleName() + " was called as a PostProcess, but no early keys to decorate were defined");
			return;
		}
		
		
		int docids[] = rs.getDocids();
		int resultsetsize = docids.length;
		logger.info("Early decorating resultset with metadata for " + resultsetsize + " documents");
		
		String[] earlykeys = earlyKeys.toArray(new String[earlyKeys.size()]);
		String[] allKeys = manager.getIndex().getMetaIndex().getKeys();
		String[][] metadata = getMetadata(allKeys, docids);
		int keyId = 0;
		for(String k : allKeys)
		{
			keys.put(k, keyId++);
		}
		for(String k : earlykeys)
		{
			for (int i = 0; i<docids.length; i++) {
				rs.addMetaItem(k, i, metadata[i][keys.get(k)]);
			}
		}
	}
	
	protected String[] getMetadata(String[] metaKeys, int docid)
	{
		String[] metadata = null;
		synchronized(metaCache) {
			try {
				Integer docidObject = Integer.valueOf(docid);
				if (metaCache.containsKey(docidObject))
						metadata = metaCache.get(docidObject);
				else {
					metadata = metaIndex.getItems(metaKeys, docid);
					metaCache.put(docidObject,metadata);
				}
			} catch(IOException ioe) {
				logger.error("Problem getting metadata for docid " + docid);
			} 
		}
		return metadata;
	}
	
	protected String[][] getMetadata(String[] metaKeys, int[] docids)
	{
		String[][] metadata = null;
		try{
			metadata = metaIndex.getItems(metaKeys, docids);
			synchronized (metaCache) {
				for(int i=0;i<docids.length;i++)
				{
					metaCache.put(Integer.valueOf(docids[i]), metadata[i]);
				}
			}
		} catch (IOException ioe) {
			logger.error("Problem getting metadata for " + docids.length + " documents");
		}
		return metadata;
	}
	
	/** Creates a regular expression pattern to highlight query terms metadata.
	 * @param _qTerms query terms
	 * @return Pattern to apply
	 */
	protected Pattern generateEmphasisPattern(String[] _qTerms) {
		boolean atLeastOneTermToHighlight = false;
		StringBuilder pattern = new StringBuilder();
		if (_qTerms.length>0 ) {
				pattern.append("(\\b)(");
				if (!_qTerms[0].contains(":")) {
						String qTerm = _qTerms[0].replaceAll("\\W+", "");
						pattern.append(qTerm);
						atLeastOneTermToHighlight = true;
				} else if (!(_qTerms[0].startsWith("group:") || _qTerms[0].startsWith("related:"))) {
						String qTerm = _qTerms[0].substring(_qTerms[0].indexOf(':')+1).replaceAll("\\W+","");
						pattern.append(qTerm);
						atLeastOneTermToHighlight = true;
				}
		}

		for (int i=1; i<_qTerms.length; i++) {
				if (!_qTerms[i].contains(":")) {
						String qTerm = _qTerms[i].replaceAll("\\W+","");
						if (atLeastOneTermToHighlight) {
							pattern.append('|'); 
							pattern.append(qTerm);
						} else {
							pattern.append(qTerm);
						}
						atLeastOneTermToHighlight = true;
				} else if (!(_qTerms[i].startsWith("group:") || _qTerms[0].startsWith("related:"))) {
						String qTerm = _qTerms[i].substring(_qTerms[i].indexOf(':')+1).replaceAll("\\W+","");
						if (atLeastOneTermToHighlight) {
							pattern.append('|'); 
							pattern.append(qTerm);
						} else {
							pattern.append(qTerm);
						}
						atLeastOneTermToHighlight = true;
				}
		}

		if (_qTerms.length>0)
				pattern.append(")(\\b)");
		String _pattern = pattern.toString();
		if (!atLeastOneTermToHighlight) {
			_pattern = ("(\\b)()(\\b)");
		}
		return Pattern.compile(_pattern, Pattern.CASE_INSENSITIVE);
	}
	

	protected boolean checkControl(String control_name, SearchRequest srq)
	{
		String controlValue = srq.getControl(control_name);
		if (controlValue.length() == 0)
			return false;
		if (controlValue.equals("0") || controlValue.toLowerCase().equals("off")
			|| controlValue.toLowerCase().equals("false"))
			return false;
		return true;
	}
	
	/**
	 * Returns the name of the post processor.
	 * @return String the name of the post processor.
	 */
	public String getInfo()
	{
		return "Decorate";
	}
}
