/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * Information Retrieval Group
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
 * The Original Code is SiteFilter.java.
 *
 * The Original Code is Copyright (C) 2004-2010 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
  *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.querying;
import java.net.MalformedURLException;
import java.net.URL;

import org.terrier.matching.ResultSet;
/** Filter that removes hosts which dont match an appropriate site: constraint, as specified in a control.
 * E.g. site:uk will remove any documents which do not have a hostname ending in uk
 * Assumes that the metadata set has already been decorated with the url.
 * @author Craig Macdonald
 * @since 3.0
 */
public class SiteFilter implements PostFilter
{
	protected String site = "";
	
	/** {@inheritDoc} */
	public void new_query(Manager m, SearchRequest srq, ResultSet rs)
	{
		site = srq.getControl("site").toLowerCase();
	}
	
	/** {@inheritDoc} */
	public byte filter(Manager m, SearchRequest srq, ResultSet rs, int rank, int docid)
	{
		try{
			URL url = new URL("http://" + rs.getMetaItem("url", docid));
			if(!url.getHost().toLowerCase().endsWith(site))
				return FILTER_REMOVE;
			return FILTER_OK;
		}catch(MalformedURLException mue) {
			return FILTER_OK;
		}
	}
}
