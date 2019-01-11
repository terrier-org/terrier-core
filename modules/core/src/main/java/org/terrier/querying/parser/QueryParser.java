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
 * The Original Code is QueryParser.java.
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author) 
 */

package org.terrier.querying.parser;

import org.terrier.querying.Request;

/** Useful class to parse the query. (We should have had this class years ago).
 * This class replaces all replicated code about how to parse a String query into
 * a Query tree, and add it to a pre-existing search request. This is most often
 * called from Manager.newSearchRequest(String,String), although client code can
 * use this method when other forms of Manager.newSearchRequest() are used.
 * Note that this class throws QueryParserException when it gets upset.
 * @since 2.0
  * @author Craig Macdonald
 */
public class QueryParser
{
	/** Parse the query specified in String query, and use it for the specified search request.
	 * Under normal usage, called by newSearchRequest(String,String).
	 * @since 2.0
	 * @param query The string query to parse
	 * @param srq The request object that the manager can use
	 * @throws QueryParserException when the query cannot be parsed */
	public static void parseQuery(final String query, final Request srq) throws QueryParserException
	{
		Query q = parseQuery(query);
		srq.setQuery(q);
	}

    /** Parse the specified query.
	  * @since 2.0
	  * @param query The string query to parse
	  * @throws QueryParserException when the query cannot be parsed
	  */
    public static Query parseQuery(String query) throws QueryParserException
    {
        Query rtr = null;
        
        try{
        	rtr = new TerrierQLParser(query).parse();
        }catch (Exception e) {
            throw new QueryParserException("Failed to process '"+query+"'",e);
        }
		if (rtr == null)
		{
			throw new QueryParserException("Failed to process '"+query+"'");
		}
        return rtr;
    }
}
