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
	
	/** The weighting model used. */
	protected String wModel = ApplicationSetup.getProperty(appName +".model", "PL2");
	

	public AbstractQuerying(String _appName) {
		super();
		this.appName = _appName;
	}

	/**
	* Create a querying manager. This method should be overriden if
	* another matching model is required.
	*/
	@SuppressWarnings("deprecation")
	protected void createManager() {
		queryingManager = ManagerFactory.from(IndexRef.of(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX));
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
		srq.setControl(SearchRequest.CONTROL_WMODEL, wModel);
		this.controls.forEach((k,v) -> srq.setControl(k, v));
		matchingCount++;
		queryingManager.runSearchRequest(srq);
		return srq;
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
					.valueSeparator(',')
					.desc("allows one of more controls to be set")
					.build());
			options.addOption(Option.builder("q")
					.argName("queryexpanion")
					.longOpt("queryexpanion")
					.desc("apply query expansion to all queries (equivalent to -c qe:on)")
					.build());
			options.addOption(Option.builder("m")
					.argName("matchingql")
					.longOpt("matchingql")
					.desc("specifies that queries are presumed to be formatted be as the matchingop (Indri-esque) QL, rather than the (default) Terrier QL")
					.build());
			return options;
		}
		
		public abstract int run(CommandLine line, AbstractQuerying querying) throws Exception;
		
		@Override
		public final int run(CommandLine line) throws Exception {
			AbstractQuerying aq = baseQuerying.newInstance();
			if (line.hasOption("c"))
			{
				String[] controlCVs = line.getOptionValues("c");
				for(String tuple : controlCVs)
				{
					String[] kv = tuple.split((":|="));
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
				aq.wModel = line.getOptionValue('w');
			return run(line, aq);
		}
	}

}