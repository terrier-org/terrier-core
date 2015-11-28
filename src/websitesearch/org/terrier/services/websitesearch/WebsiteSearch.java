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
 * The Original Code is WebsiteSearch.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.services.websitesearch;

import java.io.IOException;

import org.terrier.querying.Manager;
import org.terrier.querying.SearchRequest;
import org.terrier.realtime.memory.MemoryIndex;
import org.terrier.services.websitesearch.crawlers.CrawlerProcess;
import org.terrier.structures.CollectionStatistics;
import org.terrier.utility.ApplicationSetup;

/**
 * Represents a search engine that can search a specified website. It downloads
 * all of the pages of the website, indexes them and then makes them available
 * for search.
 * 
 * This class auto-configures by overriding variables normally loaded from the
 * terrier.properties file as follows:
 * <ul>
 * <li>indexer.meta.forward.keys</tt> = title,URL,time,content</li>
 * <li>indexer.meta.forward.keylens</tt> = 140,120,64,5000</li>
 * <li>querying.postfilters.controls</tt> = decorate:org.terrier.querying.Decorate</li>
 * <li>querying.postfilters.order</tt> = org.terrier.querying.Decorate</li>
 * <li>metaindex.crop</tt> = true</li>
 * <li>ignore.low.idf.terms</tt> = false</li>
 * <li>querying.default.controls</tt> = start,end,decorate:on,summaries:content,emphasis:title;content</li>
 * <li>trec.model</tt> = DirichletLM</li>
 * </ul>
 *
 * @author Richard McCreadie
 *
 */
public class WebsiteSearch {

	MemoryIndex memIndex;
	
	/**
	 * Creates a new website search system
	 */
	public WebsiteSearch() {
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "title,URL,time,content");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "140,120,64,5000");
		ApplicationSetup.setProperty("querying.postfilters.controls", "decorate:org.terrier.querying.Decorate");
		ApplicationSetup.setProperty("querying.postfilters.order", "org.terrier.querying.Decorate");
		ApplicationSetup.setProperty("metaindex.crop", "true");
		ApplicationSetup.setProperty("ignore.low.idf.terms", "false");
		ApplicationSetup.setProperty("querying.default.controls", "start,end,decorate:on,summaries:content,emphasis:title;content");

		
		memIndex = new MemoryIndex();
	}
	
	public CollectionStatistics getCollectionStatistics() {
		return memIndex.getCollectionStatistics();
	}
	
	public void writeIndex(String path, String prefix) {
		
		try {
			memIndex.write(path, prefix);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public SearchRequest search(String query, int rankingDepth) {
		System.err.println("Running search for '"+query+"' to depth "+rankingDepth);
		StringBuffer sb = new StringBuffer();
		
		sb.append(query);
		
		Manager queryingManager = new Manager(memIndex);

		SearchRequest srq = queryingManager.newSearchRequest("query", sb.toString());
		srq.addMatchingModel("Matching","DirichletLM");
		srq.setOriginalQuery(sb.toString());
		srq.setControl("decorate", "on");
		queryingManager.runPreProcessing(srq);
		queryingManager.runMatching(srq);
		queryingManager.runPostProcessing(srq);
		queryingManager.runPostFilters(srq);
		System.err.println("Returned "+srq.getResultSet().getDocids().length+" documents");
		return srq;
	}
	
	/**
	 * Crawls a specified website, following links to a maximum depth of pageDepth.
	 * @param website
	 * @param pageDepth
	 */
	public void crawlWebsite(String website, int pageDepth) {
		Thread crawlerThread = new Thread(new WebsiteCrawlerThread(website, pageDepth));
		crawlerThread.start();
	}
	
	/**
	 * This represents an instance that performs the crawling of
	 * a specified website.
	 * @author Richard McCreadie
	 *
	 */
	public class WebsiteCrawlerThread implements Runnable {

		String websiteAddress;
		int pageDepth;
		
		public WebsiteCrawlerThread(String websiteAddress, int pageDepth) {
			super();
			this.websiteAddress = websiteAddress;
			this.pageDepth = pageDepth;
		}



		@Override
		public void run() {
			CrawlerProcess sc = new CrawlerProcess(ApplicationSetup.TERRIER_INDEX_PATH);
			sc.indexWebsite(memIndex,pageDepth,websiteAddress);
		}
		
	}
	
	
}
