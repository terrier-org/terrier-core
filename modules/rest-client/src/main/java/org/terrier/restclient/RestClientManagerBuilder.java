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
 * The Original Code is RestClientManagerBuilder.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.restclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.querying.IndexRef;
import org.terrier.querying.Manager;
import org.terrier.querying.ManagerFactory;
import org.terrier.querying.ScoredDoc;
import org.terrier.querying.ScoredDocList;
import org.terrier.querying.SearchRequest;

/**
 * This class facilitates a Manager to be obtained for a remote HTTP REST index
 * reference. There is NO NEED to refer to the class directly - It is sufficient
 * that terrier-rest-client is included in the classpath.
 * 
 * <pre>
 * <code>
 *  IndexRef ref = IndexRef.of("http://server:8080/");
 *  Manager m = ManagerFactory.from(ref);
 *  </code>
 * </pre>
 * 
 * @since 5.0
 */
public class RestClientManagerBuilder implements ManagerFactory.Builder {

	@Override
	public boolean supports(IndexRef ref) {
		return ref.toString().startsWith("http");
	}

	@Override
	public Manager fromIndex(IndexRef ref) {
		return new RESTManagerProxy(ref);
	}

	class RESTRequest implements SearchRequest {
		private static final long serialVersionUID = 1L;
		String qid, query;
		Map<String, String> controls = new HashMap<>();
		ScoredDocList results;
		long starttime;

		@Override
		public void addMatchingModel(String MatchingModelName,
				String WeightingModelName) {
			setControl(CONTROL_MATCHING, MatchingModelName);
			setControl(CONTROL_WMODEL, WeightingModelName);
		}

		@Override
		public void setQueryID(String _qid) {
			qid = _qid;
		}

		@Override
		public void setControl(String Name, String Value) {
			controls.put(Name, Value);
		}

		@Override
		public String getQueryID() {
			return qid;
		}

		@Override
		public String getControl(String Name) {
			String rtr = controls.get(Name);
			return rtr != null ? rtr : "";
		}

		@Override
		public boolean isEmpty() {
			return query.length() == 0;
		}

		@Override
		public void setOriginalQuery(String q) {
			query = q;
		}

		@Override
		public String getOriginalQuery() {
			return query;
		}

		@Override
		public void setNumberOfDocumentsAfterFiltering(int n) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getNumberOfDocumentsAfterFiltering() {
			return results.size();
		}

		@Override
		public long getStartedProcessingTime() {
			return starttime;
		}

		@Override
		public void setStartedProcessingTime(long time) {
			starttime = time;
		}

		@Override
		public ScoredDocList getResults() {
			return results;
		}

		@Override
		public void setContextObject(String key, Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object getContextObject(String key) {
			return null;
		}

		@Override
		public Map<String, String> getControls() {
			return controls;
		}

		@Override
		public String getControl(String Name, String Default) {
			return controls.getOrDefault(Name, Default);
		}

	}

	class RESTManagerProxy implements Manager {
		protected final Logger logger = LoggerFactory.getLogger(RESTManagerProxy.class);
		IndexRef ref;

		RESTManagerProxy(IndexRef _ref) {
			this.ref = _ref;
		}

		@Override
		public SearchRequest newSearchRequest() {
			return new RESTRequest();
		}

		@Override
		public SearchRequest newSearchRequest(String QueryID) {
			SearchRequest srq = new RESTRequest();
			srq.setQueryID(QueryID);
			return srq;
		}

		@Override
		public SearchRequest newSearchRequest(String QueryID, String query) {
			SearchRequest srq = new RESTRequest();
			srq.setQueryID(QueryID);
			srq.setOriginalQuery(query);
			return srq;
		}

		@Override
		public SearchRequest newSearchRequestFromQuery(String query) {
			SearchRequest srq = new RESTRequest();
			srq.setOriginalQuery(query);
			return srq;
		}

		@Override
		public void setProperty(String key, String value) {
			throw new UnsupportedOperationException(
					"sorry, the rest client and server do no yet support changing properties");
		}

		@Override
		public void setProperties(Properties p) {
			throw new UnsupportedOperationException(
					"sorry, the rest client and server do no yet support changing properties");
		}

		@Override
		public void runSearchRequest(SearchRequest srq) {
			logger.info("Requesting results for query " + srq.getQueryID());
			String url = null;
			try {
				srq.setStartedProcessingTime(System.currentTimeMillis());
				url = ref.toString() + "/search/trec?" + "query="
						+ URLEncoder.encode(srq.getOriginalQuery(), "UTF-8");
				final RESTRequest rrq = (RESTRequest) srq;
				Map<String, String> controls = rrq.controls;
				// controls
				if (controls.size() > 0) {
					url = url
							+ "&controls="
							+ URLEncoder.encode(
									controls.entrySet()
											.stream()
											.map(entry -> entry.getKey() + ":"
													+ entry.getValue())
											.collect(Collectors.joining(";")),
									"UTF-8");
				}
				// //wmodel
				// if (rrq.hasControl(CONTROL_WMODEL))
				// {
				// url += "&wmodel="+rrq.getControl(CONTROL_WMODEL);
				// }
				// //matching
				// if (rrq.hasControl(CONTROL_MATCHING))
				// {
				// url += "&matching="+rrq.getControl(CONTROL_MATCHING);
				// }
				// qid
				if (rrq.qid != null) {
					url += "&qid=" + rrq.qid;
				}

				logger.debug("URL is " + url);
				HttpClient httpclient = HttpClients.createDefault();
				HttpGet httpGet = new HttpGet(url);
				HttpResponse response = httpclient.execute(httpGet);
				BufferedReader br = null;
				try {
					int code = response.getStatusLine().getStatusCode();
					if (code != 200)
						throw new IOException(
								"Could not contact REST server at " + url
										+ " : " + code);

					br = new BufferedReader(new InputStreamReader(response
							.getEntity().getContent(), "UTF-8"));

					ScoredDocList rtr = new ScoredDocListType();
					Map<String, Integer> metaOffset = new HashMap<String, Integer>();
					metaOffset.put("docno", 0);

					String line;
					while ((line = br.readLine()) != null) {
						line = line.trim();
						String[] parts = line.split("\\s+", 6);
						rtr.add(new ScoredDoc(0, Double.parseDouble(parts[4]),
								(short) 0, new String[] { parts[2] },
								metaOffset));
					}

					((RESTRequest) srq).results = rtr;
				} finally {
					if (br != null)
						br.close();
				}
			} catch (Exception e) {
				throw new RuntimeException("Could not access " + url, e);
			}
			logger.info("Finished query " + srq.getQueryID());
		}

		@Override
		public IndexRef getIndexRef() {
			return ref;
		}

	}

	static class ScoredDocListType extends ArrayList<ScoredDoc> implements
			ScoredDocList {
		private static final long serialVersionUID = 1L;

		@Override
		public String[] getMetaKeys() {
			return new String[] { "docno" };
		}

	}

}
