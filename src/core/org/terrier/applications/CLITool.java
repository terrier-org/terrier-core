package org.terrier.applications;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.terrier.Version;
import org.terrier.utility.ApplicationSetup;

import com.google.common.collect.Lists;

public abstract class CLITool {
	
	public static abstract class CLIParsedCLITool extends CLITool
	{
		protected abstract Options getOptions();
		
		@Override
		public String help() {
			String rtr = helpsummary();
			HelpFormatter formatter = new HelpFormatter();
			StringWriter st = new StringWriter();
			formatter.printUsage(new PrintWriter(st), HelpFormatter.DEFAULT_WIDTH, commandname(), getOptions());
			
			String usage = st.toString();
			st = new StringWriter();
			st.append('\n');
			formatter.printHelp(new PrintWriter(st), HelpFormatter.DEFAULT_WIDTH+8, usage, "", getOptions(), HelpFormatter.DEFAULT_WIDTH, 0, "");
			rtr += "\n";
			rtr += st.toString();
			return rtr;
		}
	}
	
	public static class HelpCLITool extends CLITool {
		
		static final Comparator<CLITool> byName =
				(CLITool o1, CLITool o2)->o1.commandname().compareTo(o2.commandname());

		@Override
		public int run(String[] args) {
			System.err.println("Terrier version " + Version.VERSION);
			if (args.length == 1 && args[0].equals("no-command-specified")) {
				System.err.println("You must specify a command");
				args = new String[0];
			}
			if (args.length == 0) {
				List<CLITool> list = Lists.newArrayList(getServiceIterator());
				Collections.sort(list, byName);
				for(CLITool tool : list) {
					String name = tool.commandname();
					if (name.length() <= 5)
						name += '\t';
					System.err.println("\t" + name + "\t" + tool.helpsummary());
				}
			} else if (args.length >= 1) {
				Optional<CLITool> tool = getTool(args[0]);
				if (tool.isPresent())
				{
					System.err.println(tool.get().help());
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
	
	public void setConfigurtion(Object o){}
	
	public abstract int run(String[] args) throws Exception;
	
	public String commandname() {
		return this.getClass().getName();
	}
	
	public String help() {
		return "(no help provided)";
	}
	
	public String helpsummary() {
		return "(no summary provided)";
	}
	
	
	public static void main(String[] args) throws Exception {
		if (args.length == 0)
		{
			args = new String[]{"help", "no-command-specified"};
		}
		String commandname = args[0];
		args = Arrays.copyOfRange(args, 1, args.length);
		Optional<CLITool> c = getTool(commandname);
		if (c.isPresent())
		{
			try{
				c.get().run(args);
			}catch (Exception e) {
				throw e;
			}
			return;
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
			return ApplicationSetup.getClass(classname);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		//return Optional.empty();
	}
	
	static Iterable<CLITool> getServiceIterator()
	{
		return ServiceLoader.load(CLITool.class);
	}
	
	static Optional<CLITool> getTool(String commandname) {
		Iterable<CLITool> toolLoader = getServiceIterator();
		for(CLITool tool : toolLoader)
		{
			if (tool.commandname().equals(commandname))
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
