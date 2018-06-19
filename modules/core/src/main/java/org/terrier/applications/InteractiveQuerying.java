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
 * The Original Code is InteractiveQuerying.java.
 *
 * The Original Code is Copyright (C) 2004-2016 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 *   Ben He <ben{a.}dcs.gla.ac.uk>
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.applications;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.applications.CLITool.CLIParsedCLITool;
import org.terrier.querying.IndexRef;
import org.terrier.querying.Manager;
import org.terrier.querying.ManagerFactory;
import org.terrier.querying.ScoredDoc;
import org.terrier.querying.SearchRequest;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
/**
 * This class performs interactive querying at the command line. It asks
 * for a query on Standard Input, and then displays the document IDs that
 * match the given query.
 * <p><b>Properties:</b>
 * <ul><li><tt>interactive.model</tt> - which weighting model to use, defaults to PL2</li>
 * <li><tt>interactive.matching</tt> - which Matching class to use, defaults to Matching</li>
 * <li><tt>interactive.manager</tt> - which Manager class to use, defaults to Matching</li>
 * </ul>
 * @author Gianni Amati, Vassilis Plachouras, Ben He, Craig Macdonald
 */
public class InteractiveQuerying {
	/** The logger used */
	protected static final Logger logger = LoggerFactory.getLogger(InteractiveQuerying.class);
	
	/** Change to lowercase? */
	protected final static boolean lowercase = Boolean.parseBoolean(ApplicationSetup.getProperty("lowercase", "true"));
	/** display user prompts */
	protected boolean verbose = true;
	protected boolean matchopQl = false;
	protected Map<String,String> controls = new HashMap<>();
	
	/** the number of processed queries. */
	protected int matchingCount = 0;
	/** The file to store the output to.*/
	protected PrintWriter resultFile = new PrintWriter(System.out);
	/** The query manager.*/
	protected Manager queryingManager;
	/** The weighting model used. */
	protected String wModel = ApplicationSetup.getProperty("interactive.model", "PL2");
	/** The data structures used.*/
	protected IndexRef indexref;
	/** The maximum number of presented results. */
	protected static int RESULTS_LENGTH = 
		Integer.parseInt(ApplicationSetup.getProperty("interactive.output.format.length", "1000"));
	
	protected String[] metaKeys = ApplicationSetup.getProperty("interactive.output.meta.keys", "docno").split("\\s*,\\s*");

	protected boolean printDocid = Boolean.parseBoolean(ApplicationSetup.getProperty("interactive.output.docids", "false"));
	
	/** A default constructor initialises the index, and the Manager. */
	public InteractiveQuerying() {
		createManager();		
	}

	/**
	* Create a querying manager. This method should be overriden if
	* another matching model is required.
	*/
	@SuppressWarnings("deprecation")
	protected void createManager(){
		queryingManager = ManagerFactory.from(IndexRef.of(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX));
	}
	
	/**
	 * Closes the used structures.
	 */
	public void close() {
		
	}
	/**
	 * According to the given parameters, it sets up the correct matching class.
	 * @param queryId String the query identifier to use.
	 * @param query String the query to process.
	 */
	public void processQuery(String queryId, String query) {
		SearchRequest srq = queryingManager.newSearchRequest(queryId, query);
		if (matchopQl)
		{
			srq.setControl("parsecontrols", "off");
			srq.setControl("parseql", "off");
			srq.setControl("terrierql", "off");
			srq.setControl("matchopql", "on");
		}
		srq.setControl(SearchRequest.CONTROL_WMODEL, wModel);
		this.controls.forEach((k,v) -> srq.setControl(k, v));
		matchingCount++;
		queryingManager.runSearchRequest(srq);
		try{
			printResults(resultFile, srq);
		} catch (IOException ioe) {
			logger.error("Problem displaying results", ioe);
		}
	}
	/**
	 * Performs the matching using the specified weighting model 
	 * from the setup and possibly a combination of evidence mechanism.
	 * It parses the file with the queries (the name of the file is defined
	 * in the address_query file), creates the file of results, and for each
	 * query, gets the relevant documents, scores them, and outputs the results
	 * to the result file.
	 */
	public void processQueries() {
		try {
			//prepare console input
			String query; int qid=1;
			ConsoleReader console = new ConsoleReader();
			if (verbose)
				console.setPrompt("terrier query> ");
			if (this.matchopQl) {
				console.setPrompt("matchop query> ");
				console.addCompleter(new StringsCompleter("#combine(", "#syn(","#uw("));
			}
			while ((query = console.readLine()) != null) {
				if (query.length() == 0 || 
					query.toLowerCase().equals("quit") ||
					query.toLowerCase().equals("exit")
				)
				{
					return;
				}
				processQuery("interactive"+(qid++), (lowercase && ! matchopQl) ? query.toLowerCase() : query);
			}
		} catch(IOException ioe) {
			logger.error("Input/Output exception while performing the matching. Stack trace follows.",ioe);
		} finally {
			try{
				TerminalFactory.get().restore();
			} catch(Exception e) {
                logger.warn("problem closing console", e);
            }
		}
	}
	/**
	 * Prints the results
	 * @param pw PrintWriter the file to write the results to.
	 * @param q SearchRequest the search request to get results from.
	 */
	public void printResults(PrintWriter pw, SearchRequest q) throws IOException {
		List<ScoredDoc> results = q.getResults();
		if (results.size() > RESULTS_LENGTH)
			results = results.subList(0, RESULTS_LENGTH-1);
		
		if (verbose)
			if(results.size()>0)
			{
				pw.write("\n\tDisplaying 1-"+results.size()+ " results\n");
				pw.flush();
			}
			else
			{
				pw.write("\n\tNo results\n");
				pw.flush();
				return;
			}
		
		
		StringBuilder sbuffer = new StringBuilder();
		int i = -1;
		for(ScoredDoc doc : results)
		{
			i++;
			
			if (Double.isInfinite(doc.getScore()) && doc.getScore()< 0)
				continue;
			sbuffer.append(i);
			sbuffer.append(" ");
			sbuffer.append(ArrayUtils.join(doc.getAllMetadata(), ' '));
			sbuffer.append(" ");
			if (printDocid)
			{
				sbuffer.append(doc.getDocid());
				sbuffer.append(" ");
			}
			sbuffer.append(doc.getScore());
			sbuffer.append('\n');
		}
		pw.write(sbuffer.toString());
		pw.flush();
	}
	
	public static class Command extends CLIParsedCLITool
	{
		
		@Override
		public String commandname() {
			return "interactive";
		}
	
		@Override
		public String helpsummary() {
			return "runs an interactive querying session on the commandline";
		}
		
	
		@Override
		protected Options getOptions() {
			Options options = super.getOptions();
			options.addOption(Option.builder("c")
					.argName("controls")
					.longOpt("controls")
					.hasArgs()
					.valueSeparator(',')
					.desc("allows one of more controls to be set")
					.build());
			options.addOption(Option.builder("m")
					.argName("matchingql")
					.longOpt("matchingql")
					.desc("specifies that queries are presumed to be formatted be as the matchingop (Indri-esque) QL, rather than the (default) Terrier QL")
					.build());
			options.addOption(Option.builder("q")
					.argName("quiet")
					.longOpt("quiet")
					.desc("be quiet, dont issue prompts")
					.build());			
			return options;
		}

		@Override
		public int run(CommandLine line) throws Exception {
			InteractiveQuerying iq = new InteractiveQuerying();
			if (line.hasOption("c"))
			{
				String[] controlCVs = line.getOptionValues("c");
				for(String tuple : controlCVs)
				{
					String[] kv = tuple.split((":|="));
					iq.controls.put(kv[0], kv[1]);
				}
			}
			if (line.hasOption("m"))
			{
				iq.matchopQl = true;
			}
			if (line.hasOption("q"))
			{
				iq.verbose = false;
			}			
			if (line.hasOption('w'))
				iq.wModel = line.getOptionValue('w');
			String[] args = line.getArgs();
			if (args.length > 0)
			{
				StringBuilder s = new StringBuilder();
				for(int i=0; i<args.length;i++)
				{
					s.append(args[i]);
					s.append(" ");
				}	
				iq.verbose = false;
				iq.processQuery("CMDLINE", s.toString());
			} else {
				iq.processQueries();
			}
			return 0;
		}
	}
	/**
	 * Starts the interactive query application.
	 * @param args the command line arguments.
	 */
	public static void main(String[] args) {
		CLITool.run(InteractiveQuerying.Command.class, args);
	}
	
}
