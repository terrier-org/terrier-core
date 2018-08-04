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
 * The Original Code is TerrierQLParser.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.querying;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.querying.parser.QueryParser;

@ProcessPhaseRequisites(ManagerRequisite.RAWQUERY)
public class TerrierQLParser implements Process {

	protected static final Logger logger = LoggerFactory.getLogger(TerrierQLParser.class);
	
	@Override
	public void process(Manager manager, Request q) {
		QueryParser.parseQuery(q.getOriginalQuery(), q);	
	}

}
