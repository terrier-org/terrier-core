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
 * The Original Code is AbstractQuerying.java.
 *
 * The Original Code is Copyright (C) 2017-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.applications;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.terrier.applications.CLITool.CLIParsedCLITool;
import org.terrier.querying.IndexRef;
import org.terrier.querying.Manager;
import org.terrier.querying.ManagerFactory;
import org.terrier.querying.SearchRequest;
import org.terrier.utility.ApplicationSetup;

public class AbstractQuerying {

	private String appName;
	
	protected boolean matchopQl = false;
	protected Map<String,String> controls = new HashMap<>();
	/** the number of processed queries. */
	protected int matchingCount = 0;
	/** The query manager.*/
	protected Manager queryingManager;
	/** The data structures used.*/
	protected IndexRef indexref;
//	
//	/** The weighting model used. */
//	protected String 
//	

	public AbstractQuerying(String _appName, IndexRef iRef) {
		this.indexref = iRef;
		this.appName = _appName;
		String wModel = ApplicationSetup.getProperty(appName +".model", null);
		if (wModel != null)
			controls.put(SearchRequest.CONTROL_WMODEL, wModel);
	}

	@SuppressWarnings("deprecation")
	public AbstractQuerying(String _appName) {
		this(_appName, IndexRef.of(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX));	
	}

	/**
	* Create a querying manager.
	*/
	protected void createManager() {
		queryingManager = ManagerFactory.from(indexref);
	}
	
	/**
	 * According to the given parameters, it sets up the correct matching class.
	 * @param queryId String the query identifier to use.
	 * @param query String the query to process.
	 */
	public SearchRequest processQuery(String queryId, String query) {
		SearchRequest srq = queryingManager.newSearchRequest(queryId, query);
		if (matchopQl)
		{
			srq.setControl("parsecontrols", "off");
			srq.setControl("parseql", "off");
			srq.setControl("terrierql", "off");
			srq.setControl("matchopql", "on");
		}
		this.controls.forEach((k,v) -> srq.setControl(k, v));
		matchingCount++;
		queryingManager.runSearchRequest(srq);
		return srq;
	}

	public Map<String,String> controls() {
		return controls;
	}
	
	public static abstract class AbstractQueryingCommand extends CLIParsedCLITool
	{
		Class<? extends AbstractQuerying> baseQuerying;
		
		protected AbstractQueryingCommand(Class<? extends AbstractQuerying> _baseQuerying)
		{
			baseQuerying = _baseQuerying;
		}
		
		@Override
		protected Options getOptions() {
			Options options = super.getOptions();
			options.addOption(Option.builder("c")
					.argName("controls")
					.longOpt("controls")
					.hasArgs()
					.valueSeparator(';')
					.desc("allows one of more controls to be set (keys & values separated by colon, control pairs separated by a semicolon)")
					.build());
			options.addOption(Option.builder("q")
					.argName("queryexpansion")
					.longOpt("queryexpansion")
					.desc("apply query expansion to all queries (equivalent to -c qe:on)")
					.build());
			options.addOption(Option.builder("m")
					.argName("matchingql")
					.longOpt("matchingql")
					.desc("specifies that queries are presumed to be formatted be as the matchingop (Indri-esque) QL, rather than the (default) Terrier QL")
					.build());
			options.addOption(Option.builder("w")
					.argName("wmodel")
					.longOpt("wmodel")
					.hasArgs()
					.desc("allows the default weighting model to be specified (equivalent to using -c wmodel:<wmodel>)")
					.build());
			return options;
		}
		
		public abstract int run(CommandLine line, AbstractQuerying querying) throws Exception;
		
		@Override
		public final int run(CommandLine line) throws Exception {
			
			IndexRef iR = getIndexRef(line);	
			AbstractQuerying aq = baseQuerying.getConstructor(IndexRef.class).newInstance(iR);
			
			if (line.hasOption("c"))
			{
				String[] controlCVs = line.getOptionValues("c");
				for(String tuple : controlCVs)
				{
					String[] kv = tuple.split((":|="));
					if (kv.length != 2)
						throw new IllegalArgumentException("Control spec invalid: "+tuple);
					aq.controls.put(kv[0], kv[1]);
				}
			}
			if (line.hasOption("m"))
			{
				aq.matchopQl = true;
			}
			if (line.hasOption("q"))
			{
				aq.controls.put("qe", "on");
			}
			if (line.hasOption('w'))
				aq.controls.put(SearchRequest.CONTROL_WMODEL, line.getOptionValue('w'));
			//System.err.println(aq.controls.toString());
			return run(line, aq);
		}

		@Override
		public String sourcepackage() {
			return CLITool.PLATFORM_MODULE;
		}
	}

}
