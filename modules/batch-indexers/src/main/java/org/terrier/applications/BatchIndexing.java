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
 * The Original Code is BatchIndexing.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.applications;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import org.terrier.indexing.Collection;
import org.terrier.indexing.CollectionFactory;
import org.terrier.utility.TagSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.applications.CLITool.CLIParsedCLITool;
import org.terrier.utility.ApplicationSetup;

import com.google.common.collect.Sets;
/** Abstract class for all code that set up the batch indexers */
public abstract class BatchIndexing {


	public static class Command extends CLIParsedCLITool
	{
		@Override
		protected Options getOptions()
		{
			Options options = super.getOptions();
			options.addOption(Option.builder("C")
					.argName("collection")
					.longOpt("collection")
					.desc("specify the Collection class to use. This overrides the trec.collection.class property")
					.hasArg()
					.build());
			options.addOption(Option.builder("F")
					.argName("fields")
					.longOpt("fields")
					.desc("specify the fields to index. This overrides the FieldTags.process property")
					.hasArg()
					.build());
			options.addOption(Option.builder("j")
					.argName("singlepass")
					.longOpt("singlepass")
					.desc("use the single-pass indexer")
					.build());
			options.addOption(Option.builder("p")
					.argName("parallel")
					.longOpt("parallel")
					.optionalArg(true)
					.desc("use multiple threads for the indexer, and optionally specify the number of threads")
					.build());
			options.addOption(Option.builder("b")
					.argName("blocks")
					.longOpt("blocks")
					.desc("record block (positions) in the index")
					.build());
			options.addOption(Option.builder("s")
					.argName("spec")
					.longOpt("spec")
					.desc("filename of the collection.spec file -- containing the list of files to index -- which is usually found in etc/")
					.build());
			return options;
		}
		
		
		@Override
		public String commandname() {
			return "batchindexing";
		}

		@Override
		public Set<String> commandaliases() {
			return Sets.newHashSet("bi");
		}

		@Override
		public String help() {
			return super.help() + "\n" + "Files to index can also be specified after command line arguments.";
		}

		@Override
		public String helpsummary() {
			return "allows a static collection of documents to be indexed";
		}

		@Override
		public String sourcepackage() {
			return CLITool.PLATFORM_MODULE;
		}

		@Override
		public int run(CommandLine line) throws Exception {
			
			final long starttime = System.currentTimeMillis();
			BatchIndexing indexing;
			
			//which indexer
			if (line.hasOption("parallel"))
			{
				indexing = new ThreadedBatchIndexing(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX, line.hasOption("singlepass"));
				String threads = line.getOptionValue("parallel");
				if (threads != null)
					((ThreadedBatchIndexing)indexing).setMaxThreads(Integer.parseInt(threads));
			}
			else
			{
				indexing = line.hasOption("singlepass")
						? new TRECIndexingSinglePass(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX)
						: new TRECIndexing(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
			}

			//name of the collection class
			if (line.hasOption("C"))
			{
				indexing.collectionClassName = line.getOptionValue("C");
			}

			//to record positions or not
			if (line.hasOption("blocks"))
				indexing.blocks = true;

			//which fields to index
			if (line.hasOption("F"))
			{
				ApplicationSetup.setProperty("FieldTags.process", line.getOptionValue("F"));
			}

			//which files to index
			if (line.getArgList().size() > 0)
			{
				if (line.hasOption("s"))
				{
					System.err.println("Specifying file argments and -s option at same time is not supported");
					return -1;
				}
				indexing.collectionFiles = line.getArgList();
			}
			else if (line.hasOption("s"))
			{
				indexing.collectionSpec = line.getOptionValue("s");
			}

			indexing.index();	
			final long endtime = System.currentTimeMillis();
			final long seconds = (endtime - starttime) / 1000l;
			System.err.println("Total time elaped: " + seconds + " seconds");
			return 0;
		}
		
	}

	/** The logger used */
	protected static Logger logger = LoggerFactory.getLogger(BatchIndexing.class);
	protected final String path;
	protected final String prefix;
	protected boolean blocks = ApplicationSetup.BLOCK_INDEXING;
	protected String collectionClassName = ApplicationSetup.getProperty("trec.collection.class", "TRECCollection");
	protected String collectionSpec = ApplicationSetup.COLLECTION_SPEC;
	protected List<String> collectionFiles = new ArrayList<>();
	//how many instances are being used by the code calling this class in parallel
	protected int externalParalllism = 1;

	public BatchIndexing(String _path, String _prefix) {
		super();
		this.path = _path;
		this.prefix = _prefix;
	}

	public int getExternalParalllism() {
		return externalParalllism;
	}

	public void setExternalParalllism(int externalParalllism) {
		this.externalParalllism = externalParalllism;
	}

	public void setCollectionName(String collName) {
		this.collectionClassName = collName;
	}

	public void setCollectionSpec(String specFile) {
		this.collectionSpec = specFile;
	}

	public abstract void index();

	/** open a collection when given a list of files */
	protected Collection loadCollection(List<String> files) {
		//load the appropriate collection
		final String collectionName = this.collectionClassName;
		
		Class<?>[] constructerClasses = {List.class,String.class,String.class,String.class};
		Object[] constructorValues = {files,TagSet.TREC_DOC_TAGS,
			ApplicationSetup.makeAbsolute(
				ApplicationSetup.getProperty("trec.blacklist.docids", ""), 
				ApplicationSetup.TERRIER_ETC), 
		    ApplicationSetup.makeAbsolute(
			ApplicationSetup.getProperty("trec.collection.pointers", "docpointers.col"), 
				ApplicationSetup.TERRIER_INDEX_PATH)
		};
		Collection rtr = CollectionFactory.loadCollection(collectionName, constructerClasses, constructorValues);
		if (rtr == null)
		{
			throw new IllegalArgumentException("Collection class named "+ collectionName + " not loaded, aborting");
		}
		return rtr;
	}

	/** open a collection when given the collection.spec name */
	protected Collection loadCollection(String collectionSpec) {
		//load the appropriate collection
		final String collectionName = this.collectionClassName;
		
		Class<?>[] constructerClasses = {String.class,String.class,String.class,String.class};
		String[] constructorValues = {collectionSpec,TagSet.TREC_DOC_TAGS,
			ApplicationSetup.makeAbsolute(
				ApplicationSetup.getProperty("trec.blacklist.docids", ""), 
				ApplicationSetup.TERRIER_ETC), 
		    ApplicationSetup.makeAbsolute(
			ApplicationSetup.getProperty("trec.collection.pointers", "docpointers.col"), 
				ApplicationSetup.TERRIER_INDEX_PATH)
		};
		Collection rtr = CollectionFactory.loadCollection(collectionName, constructerClasses, constructorValues);
		if (rtr == null)
		{
			throw new IllegalArgumentException("Collection class named "+ collectionName + " not loaded, aborting");
		}
		return rtr;
	}
	
}
