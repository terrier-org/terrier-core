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
 * The Original Code is CLITool.java.
 *
 * The Original Code is Copyright (C) 2017-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.applications;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FilenameUtils;
import org.terrier.Version;
import org.terrier.utility.ApplicationSetup;

import com.google.common.collect.Lists;

/** CLITool is an abstract class for all classes that are Terrier commands.
 * These can generally be easily run through the <tt>bin/terrier</tt> command
 * script.
 * 
 * To advertise a new functionality, list the class in the 
 * <tt>resources/META-INF/services/org.terrier.applications.CLITool</tt> file
 * corresponding to your package.
 *  
 * @since 5.0
 */

public abstract class CLITool {
	
	static boolean DEBUG = Boolean.parseBoolean(System.getProperty("org.terrier.desktop.CLITool.debug", "false"));
	
	//we use strings of classnames here so that no dependency arises
	public static String[] POPULAR_COMMANDS = new String[]{
		//batch-indexers
		"org.terrier.applications.CLITool$HelpCLITool",
		"org.terrier.applications.BatchIndexing$Command",
		"org.terrier.applications.InteractiveQuerying$Command",		
		//batch-retrieval
		"org.terrier.applications.batchquerying.TRECQuerying$Command",
		"org.terrier.evaluation.BatchEvaluationCommand"
		
	};
	
	static final Comparator<CLITool> byName =
			(CLITool o1, CLITool o2)->o1.commandname().compareTo(o2.commandname());
	
	public static abstract class CLIParsedCLITool extends CLITool
	{
		protected Options getOptions()
		{
			Options options = new Options();
			options.addOption(Option.builder("D")
					.argName("property")
					.valueSeparator()
					.hasArgs()
					.desc("specify property name=value")
					.build());
			options.addOption(Option.builder("I")
					.argName("indexref")
					.hasArg(true)
					.desc("override the default indexref (location)")
					.build());
			return options;
		}
		
		@Override
		public String help() {
			String rtr = "Command: " + commandname() + " -- " + helpsummary();
			
			if (this.commandaliases().size() > 0)
			{
				rtr += "\n";
				rtr += "Aliases: " + String.join(", ", this.commandaliases());
			}
			HelpFormatter formatter = new HelpFormatter();
			StringWriter st = new StringWriter();
			formatter.printUsage(new PrintWriter(st), HelpFormatter.DEFAULT_WIDTH, commandname(), getOptions());
			
			String usage = st.toString().replaceFirst("usage:", "");
			st = new StringWriter();
			//st.append('\n');
			formatter.printHelp(new PrintWriter(st), HelpFormatter.DEFAULT_WIDTH+8, usage, "", getOptions(), 3, 4, "", false);
			rtr += "\n";
			rtr += st.toString();
			return rtr;
		}

		@Override
		public final int run(String[] args) throws Exception {
			if (DEBUG)
				System.err.println("args=" + Arrays.toString(args));
			CommandLineParser parser = new DefaultParser();
			CommandLine line = parser.parse(getOptions(), args);

			if (DEBUG)
			{
				System.err.println("Enabled options:");
				for(Option o : getOptions().getOptions())
					System.err.println("\t"+o.getArgName());
				System.err.println("Found options:");
				for(Option o : line.getOptions())
					System.err.println("\t"+o.getArgName());
			}
			
			Properties props = null;
			if (line.hasOption("D"))
			{
				props = line.getOptionProperties("D");
				props.forEach( (k,v) -> org.terrier.utility.ApplicationSetup.setProperty((String)k, (String)v));
				ApplicationSetup.loadCommonProperties();
			}
			if (line.hasOption("I"))
			{
				String indexLocation = line.getOptionValue("I");
				ApplicationSetup.TERRIER_INDEX_PATH = FilenameUtils.getFullPath(indexLocation);
				ApplicationSetup.TERRIER_INDEX_PREFIX = FilenameUtils.getBaseName(indexLocation);
			}
			return this.run(line);
		}
		
		public abstract int run(CommandLine line) throws Exception;
		
	}
	
	public static class HelpAliasCLITool extends CLITool {
		@Override
		public int run(String[] args) {
			System.err.println("All possible commands & aliases:");
			displayCommandAndAliases(getServiceIterator(false));
			
			return 0;
		}
		
		@Override
		public String commandname() {
			return "help-aliases";
		}
		
		@Override
		public String help() {
			return helpsummary();
		}
		
		@Override
		public String helpsummary() {
			return "provides a list of all available commands and their aliases";
		}
		
		@Override
		public Set<String> commandaliases() {
			return new HashSet<String>(Arrays.asList("aliases", "alias"));
		}

		protected void displayCommandAndAliases(Iterable<CLITool> commandIter) {
			List<CLITool> list = Lists.newArrayList(commandIter);
			Collections.sort(list, byName);
			for(CLITool tool : list) {
				String name = tool.commandname();
				if (name.length() <= 5)
					name += '\t';
				System.err.println("\t" + name + "\t" + String.join(" ", tool.commandaliases()));
			}
		}
	}
	
	public static class HelpCLITool extends CLITool {
		
		protected void displayCommandSummaries(Iterable<CLITool> commandIter) {
			
			List<CLITool> list = Lists.newArrayList(commandIter);
			Collections.sort(list, byName);
			for(CLITool tool : list) {
				String name = tool.commandname();
				if (name.length() <= 5)
					name += '\t';
				System.err.println("\t" + name + "\t" + tool.helpsummary());
			}
			
		}

		@Override
		public int run(String[] args) {
			System.err.println("Terrier version " + Version.VERSION);
			if (args.length == 1 && args[0].equals("no-command-specified")) {
				System.err.println("No command specified. You must specify a command.");
				args = new String[0];
			}
			if (args.length == 0) {
				System.err.println("Popular commands:");
				displayCommandSummaries(getServiceIterator(true));
				System.err.println();
				System.err.println("All possible commands:");
				displayCommandSummaries(getServiceIterator(false));
				System.err.println();
				System.err.println("See 'terrier help <command>' to read about a specific command.");
			} else if (args.length >= 1) {
				Optional<CLITool> tool = getTool(args[0]);
				if (tool.isPresent()) {
					System.err.println(tool.get().help());
				} else {
					System.err.println("No such known command " + args[0] + ". Use 'terrier help' to get a list of commands.");
				}
			}
			return 0;
		}

		@Override
		public String commandname() {
			return "help";
		}
		
		@Override
		public String help() {
			return helpsummary();
		}
		
		@Override
		public String helpsummary() {
			return "provides a list of available commands";
		}
		
	}
	
	public void setConfiguration(Object o){}
	
	/** What short commands aliases should this command respond to */
	public Set<String> commandaliases() {
		return new HashSet<String>();
	}

	public abstract int run(String[] args) throws Exception;
	
	/** What commandname should this command respond to */
	public String commandname() {
		return this.getClass().getName();
	}
	
	/** Return a long message about how to use this command */
	public String help() {
		String rtr = this.commandname() + " - " +  this.helpsummary();
		if (this.commandaliases().size() > 0)
		{
			rtr += "\n";
			rtr += "Command aliases: " + String.join(", ", this.commandaliases());
		}
		return rtr;
	}
	
	/** Returns a short sentence about what this command is for */
	public String helpsummary() {
		return "(no summary provided)";
	}
	
	
	public static void main(String[] args) throws Exception {
		if (args.length == 0)
		{
			args = new String[]{"help", "no-command-specified"};
		}
		//this forces the ivy classloader to be in place
		//org.terrier.utility.ApplicationSetup.bootstrapInitialisation();
		@SuppressWarnings("unused")
		String x = org.terrier.utility.ApplicationSetup.TERRIER_VERSION;
		String commandname = args[0];
		args = Arrays.copyOfRange(args, 1, args.length);
		//System.err.println("AppSetup should have Clzloader " + org.terrier.utility.ApplicationSetup.getClassLoader());
		//
		//System.err.println( ((MutableURLClassLoader) org.terrier.utility.ApplicationSetup.getClassLoader()).isLoaded("org.terrier.querying.IndexRef"));
		//ApplicationSetup.getClass("org.terrier.querying.IndexRef");
		//ApplicationSetup.getClassLoader().loadClass("org.terrier.querying.IndexRef");
		//System.err.println(org.terrier.querying.IndexRef.class.getClassLoader());
		//System.err.println( ((MutableURLClassLoader) org.terrier.utility.ApplicationSetup.getClassLoader()).isLoaded("org.terrier.querying.IndexRef"));
		
		
		Optional<CLITool> c = getTool(commandname);
		if (c.isPresent()) {
			try{
				//System.err.println(c.get().getClass().getName()  + " => " + c.get().getClass().getClassLoader());
				c.get().run(args);
			}catch (Exception e) {
				throw e;
			}
			return;
		} else {
			System.err.println("WARN: " + commandname + " did not match any known commands, checking for class definitions..");
		}
		Class<?> clz = getClassName(commandname);
		try{
			if (clz.isAssignableFrom(CLITool.class))
			{
				clz.asSubclass(CLITool.class).newInstance().run(args);
			}
			else
			{
				Method thisMethod = clz.getDeclaredMethod("main",String[].class);
				thisMethod.invoke(null, (Object) args);
			}
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	static Class<?> getClassName(String classname) {
		try {
			return org.terrier.utility.ApplicationSetup.getClass(classname);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		//return Optional.empty();
	}
	
	static Iterable<CLITool> getServiceIterator(boolean popular)
	{
		if (popular)
		{
			List<CLITool> list = new ArrayList<CLITool>();
			for(String toolClass : POPULAR_COMMANDS)
			{
				try{
					list.add(org.terrier.utility.ApplicationSetup.getClass(toolClass).asSubclass(CLITool.class).newInstance());
				}catch (Exception e) {}
				
			}
			return list;
		}
		return ServiceLoader.load(CLITool.class, org.terrier.utility.ApplicationSetup.getClassLoader());
	}
	
	static Optional<CLITool> getTool(String commandname) {
		Iterable<CLITool> toolLoader = getServiceIterator(false);
		for(CLITool tool : toolLoader)
		{
			//System.err.println(tool.getClass().getClassLoader());
			if (DEBUG)
				System.err.println("Checking" + tool.getClass().getName());
			if (tool.commandname().equals(commandname))
				return Optional.of(tool);
			if (tool.commandaliases().contains(commandname))
				return Optional.of(tool);
		}
		return Optional.empty();
	}
	
	
	public static void run(Class<? extends CLITool> clz, String[] args) {
		try {
			run(clz.newInstance(), args);
		} catch (InstantiationException|IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void run(CLITool tool, String[] args) {
		try{
			tool.run(args);
		}catch (Throwable t) {
			if (t instanceof RuntimeException)
				throw (RuntimeException)t;
			throw new RuntimeException(t);
		}
	}

}
