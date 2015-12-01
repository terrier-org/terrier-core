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
 * The Original Code is CrawlerProcess.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.services.websitesearch.crawlers;

import org.terrier.realtime.UpdatableIndex;
import org.terrier.services.websitesearch.CrawlerAPI;
import org.terrier.services.websitesearch.crawler4j.CrawlStrategy;
import org.terrier.services.websitesearch.crawler4j.CustomIndexData;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

/**
 * A simple API class to access the functionality of Crawler4J
 * @author Richard McCreadie
 *
 */
public class CrawlerProcess implements CrawlerAPI{

	String host;
	String tmpFolder;
	UpdatableIndex index;
	public CrawlerProcess(String tmpFolder) {
		
		this.tmpFolder = tmpFolder;
		
	}
	
	@Override
	public int indexWebsite(UpdatableIndex index, int linkdepth, String url) {
		host = url;
		this.index= index;
		String crawlStorageFolder = tmpFolder;
        int numberOfCrawlers = 1;

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxDepthOfCrawling(linkdepth);

        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = null;
		try {
			controller = new CrawlController(config, pageFetcher, robotstxtServer);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        controller.addSeed(host);

        /*
         * Set the custom data for the crawler 
         */
        CustomIndexData dataPacket = new CustomIndexData(host,index);
        controller.setCustomData(dataPacket);
        
        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(CrawlStrategy.class, numberOfCrawlers);
        
        
        return 0;
		
	}

	
	
}
