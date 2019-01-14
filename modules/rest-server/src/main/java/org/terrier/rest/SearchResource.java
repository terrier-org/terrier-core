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
 * The Original Code is SearchResource.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.rest;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.terrier.querying.IndexRef;
import org.terrier.querying.Manager;
import org.terrier.querying.ManagerFactory;
import org.terrier.querying.Request;
import org.terrier.querying.SearchRequest;
import org.terrier.structures.Index;
import org.terrier.structures.IndexFactory;
import org.terrier.structures.outputformat.Normalised2LETOROutputFormat;
import org.terrier.structures.outputformat.OutputFormat;
import org.terrier.structures.outputformat.TRECDocnoOutputFormat;
import org.terrier.utility.ApplicationSetup;

import com.google.common.annotations.VisibleForTesting;

@Path("/search")
public class SearchResource {

	static final String DEFAULT_FORMAT = "trec";
	
	@SuppressWarnings("deprecation")
	static IndexRef indexRef = IndexRef.of(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
	static Manager m = ManagerFactory.from(indexRef);
	
	@VisibleForTesting @SuppressWarnings("deprecation")
	public static void reinit()
	{
		indexRef = IndexRef.of(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
		m = ManagerFactory.from(indexRef);
	}
	
	@GET
    @Produces(MediaType.TEXT_PLAIN)
	@Path("{format}")
    public Response search(
    	@QueryParam("query") String query,
    	@QueryParam("controls")@DefaultValue("") String controls,
    	@QueryParam("qid")@DefaultValue("") String qid,
    	@QueryParam("wmodel")@DefaultValue("") String wmodel,
    	@QueryParam("matching")@DefaultValue("") String matching,
    	@PathParam("format")@DefaultValue(DEFAULT_FORMAT) String format
    	) 
	{
		System.err.println("Querying " + indexRef.toString() + " for query " + query);
		if (format == null)
			format = DEFAULT_FORMAT;
		
		SearchRequest srq = null;
		try{
			srq = m.newSearchRequestFromQuery(query);
			if (controls.length() > 0)
			{
				System.err.println("controls="+ controls);
				String[] controlKVs = controls.split(";");
				for(String kv : controlKVs)
				{
					//stop trailing & being a problem
					if (kv.length() == 0)
						continue;
					String[] kvs = kv.split(":");
					if (kvs.length == 2)//stop no value being a problem
						srq.setControl(kvs[0], kvs[1]);
					else
						System.err.println("invalid control="+ kv);
				}				 
			}
			
			if (wmodel.length() > 0)
				srq.setControl(SearchRequest.CONTROL_WMODEL, wmodel);
			if (matching.length() > 0)
				srq.setControl(SearchRequest.CONTROL_MATCHING, matching);
			
			if (qid.length() != 0)
				srq.setQueryID(qid);
			
			m.runSearchRequest(srq);
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);			
			OutputFormat of = getOutputFormat(srq, format);
			of.printResults(pw, srq, "terrier-rest", "Q0", 0);
			pw.flush();
	        return Response.ok(sw.toString())
					.type(of.contentType())
					.header("Access-Control-Allow-Origin", "*")
					.build();
		} catch (Exception e) {
			StringWriter s = new StringWriter();
			PrintWriter p = new PrintWriter(s);
			p.println(e.toString());
			e.printStackTrace(p);
			System.err.println(e.toString());
			e.printStackTrace();
			p.flush();
			return Response.status(500).entity(s.toString()).build();
		}
    }
	
	OutputFormat getOutputFormat(SearchRequest srq, String format) {
		if (! IndexFactory.isLocal(indexRef))
			throw new IllegalArgumentException(indexRef + " does not refer to a local index");
		Index index = ((Request)srq).getIndex();
//		Index index = IndexFactory.of(indexRef);
//		if (index == null)
//			throw new IllegalArgumentException("No such index " + indexRef);

		switch (format) {
		case "trec": return new TRECDocnoOutputFormat(index);
		case "letor": return new Normalised2LETOROutputFormat(index);
		case "json": return new JSONOutputFormat(index);
		}
		throw new IllegalArgumentException("Format " + format + " is unknown");
	}
}
