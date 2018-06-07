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
import org.terrier.querying.SearchRequest;
import org.terrier.structures.Index;
import org.terrier.structures.IndexFactory;
import org.terrier.structures.outputformat.Normalised2LETOROutputFormat;
import org.terrier.structures.outputformat.OutputFormat;
import org.terrier.structures.outputformat.TRECDocnoOutputFormat;

@Path("/search")
public class SearchResource {

	static final String DEFAULT_FORMAT = "trec";
	static final String DEFAULT_MATCHING = org.terrier.matching.daat.Full.class.getName();
	static final String DEFAULT_WMODEL = org.terrier.matching.models.DPH.class.getName();
	
	
	IndexRef indexRef = IndexRef.of("/Users/craigm/git/Terrier/var/index/data.properties");
	Manager m = ManagerFactory.from(indexRef);
	
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
				String[] controlKVs = controls.split(",");
				for(String kv : controlKVs)
				{
					String[] kvs = kv.split(":");
					srq.setControl(kvs[0], kvs[1]);
				}				 
			}
			
			if (wmodel.length() == 0)
				wmodel = DEFAULT_WMODEL;
			if (matching.length() == 0)
				matching = DEFAULT_MATCHING;
			srq.addMatchingModel(DEFAULT_MATCHING, DEFAULT_WMODEL);
			
			if (qid.length() != 0)
				srq.setQueryID(qid);
			
			m.runSearchRequest(srq);
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);			
			OutputFormat of = getOutputFormat(format);
			of.printResults(pw, srq, "terrier-rest", "Q0", 0);
			pw.flush();
	        return Response.ok(sw.toString()).build();
		} catch (Exception e) {
			StringWriter s = new StringWriter();
			PrintWriter p = new PrintWriter(s);
			p.println(e.toString());
			e.printStackTrace(p);
			p.flush();
			return Response.status(500).entity(s.toString()).build();
		}
    }
	
	OutputFormat getOutputFormat(String format) {
		if (! IndexFactory.isLocal(indexRef))
			throw new IllegalArgumentException(indexRef + " does not refer to a local index");
		Index index = IndexFactory.of(indexRef);
		if (index == null)
			throw new IllegalArgumentException("No such index " + indexRef);

		switch (format) {
		case "trec": return new TRECDocnoOutputFormat(index);
		case "letor": return new Normalised2LETOROutputFormat(index);
		}
		throw new IllegalArgumentException("Format " + format + " is unknown");
	}
}
