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
 * The Original Code is CrawlStrategy.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.services.websitesearch.crawler4j;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.terrier.indexing.TaggedDocument;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.realtime.UpdatableIndex;
import org.terrier.utility.ApplicationSetup;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * Overrides Crawler4J methods in WebCrawler to enable
 * restriction to a named host and to connect to the
 * Terrier index.
 * 
 * This class auto-configures by overriding variables normally loaded from the
 * terrier.properties file as follows:
 * <ul>
 * <li>TaggedDocument.abstracts</tt> = title,content</li>
 * <li>TaggedDocument.abstracts.tags</tt> = title,ELSE</li>
 * <li>TaggedDocument.abstracts.lengths</tt> = 140,5000</li>
 * <li>WebCrawlerTags.process</tt> = p,title</li>
 * <li>WebCrawlerTags.skip</tt> = ""</li>
 * <li>WebCrawlerTags.casesensitive</tt> = false</li>
 * <li>trec.model</tt> = DirichletLM</li>
 * </ul>
 * 
 * @author Richard McCreadie
 * @since 4.0
 */
public class CrawlStrategy extends WebCrawler {

    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" 
                                                      + "|png|tiff?|mid|mp2|mp3|mp4"
                                                      + "|wav|avi|mov|mpeg|ram|m4v|pdf" 
                                                      + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    boolean configured = false;
    Object configurelock = new Object();

	Tokeniser tokeniser;
	String doctags = "WebCrawlerTags";
	String exacttags = "WebCrawlerExactTags";
	String fieldtags = "WebCrawlerFieldTags";
    
    String hostname;
    UpdatableIndex index;
    Calendar c = Calendar.getInstance();
    
    public void init() {
    	synchronized (configurelock) {
    		tokeniser = Tokeniser.getTokeniser();
    		ApplicationSetup.setProperty("TaggedDocument.abstracts", "title,content");
    		ApplicationSetup.setProperty("TaggedDocument.abstracts.tags", "title,ELSE");
    		ApplicationSetup.setProperty("TaggedDocument.abstracts.lengths", "140,5000");
    		ApplicationSetup.setProperty("WebCrawlerTags.process", "p,title");
    		ApplicationSetup.setProperty("WebCrawlerTags.skip", "");
    		ApplicationSetup.setProperty("WebCrawlerTags.casesensitive", "false");
    		
    		CustomIndexData data = (CustomIndexData)this.getMyController().getCustomData();
    	
    		hostname = data.getHost();
    		index = data.getIndex();
    		
    		configured=true;
    	}
    }
    
    /**
     * Check to see if the page is on the specified host
     */
    @Override
    public boolean shouldVisit(Page page,WebURL url) {
    	if (!configured) init();
            String href = url.getURL().toLowerCase();
            
            System.err.println("Considering "+href +"("+hostname+")");
            
            this.getMyController().getCustomData();
            return !FILTERS.matcher(href).matches() && href.contains(hostname);
    }

    /**
     * Get the page and make a Terrier document from it
     */
    @SuppressWarnings("deprecation")
	@Override
    public void visit(Page page) {       
    	if (!configured) init();
            String url = page.getWebURL().getURL();
            System.out.println("URL: " + url);

            if (page.getParseData() instanceof HtmlParseData) {
                    HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                    String html = htmlParseData.getHtml();
                    
                	Map<String,String> docProperties = new HashMap<String,String>();
                	docProperties.put("encoding", "UTF-8");
                	docProperties.put("URL", url);
                	//docProperties.put("content", text);
                	c.setTimeInMillis(System.currentTimeMillis());
                	docProperties.put("time", c.getTime().toGMTString());
                	
                	ByteArrayInputStream inputStream = null;
					try {
						inputStream = new ByteArrayInputStream(html.getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e1) {
						//UTF-8 must always be supported
						throw new AssertionError(e1);
					}
                	
                    TaggedDocument tg = new TaggedDocument(inputStream,docProperties,tokeniser,doctags,exacttags,fieldtags);
                    try {
						index.indexDocument(tg);
					} catch (Exception e) {
						e.printStackTrace();
					}
            }
    }
}