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
 * The Original Code is ParallelTRECQuerying.java.
 *
 * The Original Code is Copyright (C) 2017-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.applications.batchquerying;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.terrier.applications.AbstractQuerying;
import org.terrier.querying.IndexRef;
import org.terrier.querying.SearchRequest;
import org.terrier.querying.ThreadSafeManager;
import org.terrier.structures.Index;
import org.terrier.structures.IndexFactory;
import org.terrier.structures.concurrent.ConcurrentIndexUtils;
import org.terrier.structures.outputformat.NullOutputFormat;
import org.terrier.structures.outputformat.OutputFormat;

/** An instance of TRECQuerying that will invoke multiple threads concurrently */
public class ParallelTRECQuerying extends TRECQuerying implements Closeable {

	public static class Command extends TRECQuerying.Command
	{
		public Command() {
			super(ParallelTRECQuerying.class);
		}

		@Override
		public String commandname() {
			return "p" + super.commandname();
		}

		@Override
		public String helpsummary() {
			return "performs a parallelised batch retrieval \"run\" over a set of queries";
		}

		@Override
		public Set<String> commandaliases() {
			Set<String> rtr = new HashSet<>();
			for (String cmd : super.commandaliases())
			{
				rtr.add('p'+cmd);
			}
			return rtr;
		}

		@Override
		public int run(CommandLine line, AbstractQuerying q) throws Exception {
			
			if (line.hasOption("parallelism"))
			{
				int threads = Integer.parseInt(line.getOptionValue("parallelism"));
				((ParallelTRECQuerying)q).pool = Executors.newFixedThreadPool(threads);
			}			
			return super.run(line, q);
		}

		@Override
		protected Options getOptions() {
			Options rtr = super.getOptions();
			rtr.addOption(Option.builder("p")
					.argName("parallelism")
					.hasArg()
					.desc("specify the level of parallelism")
					.build());
			return rtr;
		}
			
	}
	
	
	ExecutorService pool;
	
	//First line is valid for multiple processors machine. If the machine has only one, it only launches one thread
	//final static int NUM_PROC = Runtime.getRuntime().availableProcessors();
	final static int NUM_PROC = 20;
	List<Future<?>> runningQueries = Collections.synchronizedList(new ArrayList<Future<?>>());

	@SuppressWarnings("deprecation")
	public ParallelTRECQuerying() {
		super();
		pool = Executors.newFixedThreadPool(NUM_PROC);
		if (! (super.printer instanceof NullOutputFormat))
			super.printer = new SynchronizedOutputFormat(super.printer);
	}

	public ParallelTRECQuerying(IndexRef i) {
		super(i);
		pool = Executors.newFixedThreadPool(NUM_PROC);
		if (! (super.printer instanceof NullOutputFormat))
			super.printer = new SynchronizedOutputFormat(super.printer);
	}

	@Deprecated
	public ParallelTRECQuerying(boolean _queryexpansion) {
		super();
		pool = Executors.newFixedThreadPool(NUM_PROC);
		if (_queryexpansion)
			controls.put("qe", "on");
		if (! (super.printer instanceof NullOutputFormat))
			super.printer = new SynchronizedOutputFormat(super.printer);
	}
	
	@Override
	protected void createManager() {		
		if (! IndexFactory.isLocal(super.indexref)){
			throw new IllegalArgumentException("Must have local index");
		}
		Index index = IndexFactory.of(super.indexref);
		ConcurrentIndexUtils.makeConcurrentForRetrieval(index);
		this.queryingManager = new ThreadSafeManager(index);
	}

	final void _processQueryAndWrite(final String queryId, final String query) 
	{
		super.processQueryAndWrite(queryId, query);
	}

	@Override
	protected void processQueryAndWrite(final String queryId, final String query) 
	{
		@SuppressWarnings("resource")
		final ParallelTRECQuerying me = this;
		runningQueries.add(pool.submit(new Runnable() {
			
			public void run() {
				me._processQueryAndWrite(queryId, query);
			}
		}));
	}
	
	@Override
	protected void finishedQueries() {
		
		//block here awaiting all threads to write results before closing the file
		for (Future<?> f : runningQueries)
			try {
				f.get();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		runningQueries.clear();
		super.finishedQueries();
	}

	//CompletionService<Object> completionService = null;//= new ExecutorCompletionService<Object>(pool);
	
	@Override
	public PrintWriter getResultFile(String predefinedName) {
		synchronized (this) {
			return super.getResultFile(predefinedName);
		}		
	}

	@Override
	public String processQueries() {
		//completionService = new ExecutorCompletionService<Object>(pool);
		return super.processQueries();		
	}
	
	@Override
	public void close() {
		pool.shutdown();
		try { 
			pool.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException ie){}
	}



	static class SynchronizedOutputFormat implements OutputFormat
	{
		OutputFormat parent;
		public SynchronizedOutputFormat(OutputFormat _parent)
		{
			parent = _parent;
		}
		
		public synchronized void printResults(PrintWriter pw, SearchRequest q,
				String method, String iteration, int numberOfResults)
				throws IOException {
			parent.printResults(pw, q, method, iteration, numberOfResults);
		}
	}
	
	public static void main(String[] args)
	{
		TRECQuerying me = new ParallelTRECQuerying();
		me.intialise();
		me.processQueries();
		me.close();
	}

}
