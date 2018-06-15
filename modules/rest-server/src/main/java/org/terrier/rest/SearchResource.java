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

@Path("/search")
public class SearchResource {

	static final String DEFAULT_FORMAT = "trec";
	
	static IndexRef indexRef = IndexRef.of(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
	static Manager m = ManagerFactory.from(indexRef);
	
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
		if (format == null)
			format = DEFAULT_FORMAT;
		
		SearchRequest srq = null;
		try{
			srq = m.newSearchRequestFromQuery(query);
			if (controls.length() > 0)
			{
				System.err.println("controls="+ controls);
				String[] controlKVs = controls.split(",");
				for(String kv : controlKVs)
				{
					String[] kvs = kv.split(":");
					srq.setControl(kvs[0], kvs[1]);
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
	        return Response.ok(sw.toString()).build();
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
		}
		throw new IllegalArgumentException("Format " + format + " is unknown");
	}
}
