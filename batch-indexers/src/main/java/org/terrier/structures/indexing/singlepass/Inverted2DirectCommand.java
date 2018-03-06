package org.terrier.structures.indexing.singlepass;

import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.terrier.applications.CLITool.CLIParsedCLITool;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;

import com.google.common.collect.Sets;

public class Inverted2DirectCommand extends CLIParsedCLITool {

	@Override
	public Set<String> commandaliases() {
		return Sets.newHashSet("i2d");
	}

	@Override
	public String helpsummary() {
		return "makes a direct index from an index with only an inverted index";
	}

	@Override
	public String commandname() {
		return "invert2direct";
	}

	@Override
	protected Options getOptions() {
		Options options = new Options();
		options.addOption(Option.builder("b")
				.argName("blocks")
				.longOpt("blocks")
				.desc("record block (positions) in the index")
				.build());
		return options;
	}

	@Override
	public int run(String[] args) throws Exception {
		CommandLineParser clp = new DefaultParser();
		CommandLine line = clp.parse(getOptions(), args);
		Index.setIndexLoadingProfileAsRetrieval(false);
		IndexOnDisk i = Index.createIndex();
		if (i== null)
		{
			System.err.println("Sorry, no index could be found in default location");
			return 1;
		}
		Inverted2DirectIndexBuilder i2d = null;
		//disabling TR-279 optimisation
		//LexiconBuilder.reAssignTermIds(i, "lexicon", i.getCollectionStatistics().getNumberOfUniqueTerms());
		if (line.hasOption('b'))
			i2d = new BlockInverted2DirectIndexBuilder(i);
		i2d = new Inverted2DirectIndexBuilder(i);
		i2d.createDirectIndex();
		i.close();
		return 0;
	}

}
