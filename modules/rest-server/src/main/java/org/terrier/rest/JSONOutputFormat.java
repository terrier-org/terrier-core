package org.terrier.rest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.querying.Request;
import org.terrier.querying.ScoredDoc;
import org.terrier.querying.ScoredDocList;
import org.terrier.querying.SearchRequest;

import java.io.IOException;
import java.io.PrintWriter;

public class JSONOutputFormat implements org.terrier.structures.outputformat.OutputFormat {


    protected static final Logger logger = LoggerFactory.getLogger(JSONOutputFormat.class);

    public JSONOutputFormat(Object o) { }

    @Override
    public void printResults(PrintWriter pw, SearchRequest q, String method, String iteration, int _RESULTS_LENGTH) throws IOException {

		try{
        ScoredDocList results = q.getResults();
        final int maximum = _RESULTS_LENGTH > results.size()
                || _RESULTS_LENGTH == 0 ? results.size()
                : _RESULTS_LENGTH;

        final String[] metakeys = q.getResults().getMetaKeys();
        JSONObject json = new JSONObject();
        json.put("qid", q.getQueryID());
        json.put("query", q.getOriginalQuery());
        if (q instanceof Request)
        {
            Request rq = (Request)q;
            json.put("matchopql", rq.getMatchingQueryTerms().toString() );
        }
        json.put("num_results", results.size());

        JSONArray array = new JSONArray();

        int rank = 0;
        for(ScoredDoc doc : results)
        {
            if (doc.getScore() == Double.NEGATIVE_INFINITY)
                continue;
            JSONObject result = new JSONObject();
            result.put("rank", rank);
            result.put("docid", doc.getDocid());
            result.put("score", doc.getScore());
            for(String meta : metakeys)
            {
                result.put(meta, doc.getMetadata(meta));
            }
            rank++;
            array.put(result);
            if (rank >= maximum)
                break;
        }
        json.put("results", array);

        pw.write(json.toString());
        pw.flush();

		} catch (Exception e) {
			throw new IOException(e);
		}
    }

    @Override
    public String contentType() {
        return "application/json";
    }
}
