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
 * The Original Code is Inverted2DirectCommand.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.structures.indexing.singlepass;

import java.util.Set;

import org.apache.commons.cli.CommandLine;
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
		return "makes a direct index from a disk index with only an inverted index";
	}

	@Override
	public String commandname() {
		return "inverted2direct";
	}

	@Override
	protected Options getOptions() {
		Options options = super.getOptions();
		options.addOption(Option.builder("b")
				.argName("blocks")
				.longOpt("blocks")
				.desc("record block (positions) in the index")
				.build());
		return options;
	}

	@Override
	public int run(CommandLine line) throws Exception {
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
