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
 * The Original Code is CustomIndexData.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.services.websitesearch.crawler4j;

import org.terrier.realtime.UpdatableIndex;

/**
 * This is a data structure that holds all of the information 
 * that the crawler needs to determine what to crawl and what
 * to do with the pages when done crawling
 * @author Richard McCreadie
 *
 */
public class CustomIndexData {

	String host;
	UpdatableIndex index;
	public CustomIndexData(String host, UpdatableIndex index) {
		super();
		this.host = host;
		this.index = index;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public UpdatableIndex getIndex() {
		return index;
	}
	public void setIndex(UpdatableIndex index) {
		this.index = index;
	}
	
}
