/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.ac.gla.uk
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
 * The Original Code is BatchIndexing.java.
 *
 * The Original Code is Copyright (C) 2004-2016 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.applications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/** Abstract class for all code that set up the batch indexers */
public abstract class BatchIndexing {

	/** The logger used */
	protected static Logger logger = LoggerFactory.getLogger(BatchIndexing.class);
	protected final String path;
	protected final String prefix;

	public BatchIndexing(String _path, String _prefix) {
		super();
		this.path = _path;
		this.prefix = _prefix;
	}

	public abstract void index();
	
}
