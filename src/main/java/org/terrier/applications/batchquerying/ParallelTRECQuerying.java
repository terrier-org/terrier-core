package org.terrier.applications.batchquerying;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.terrier.querying.SearchRequest;
import org.terrier.structures.Index;
import org.terrier.structures.concurrent.ConcurrentIndexUtils;
import org.terrier.structures.outputformat.NullOutputFormat;
import org.terrier.structures.outputformat.OutputFormat;

/** An instance of TRECQuerying that will invoke multiple threads concurrently */
public class ParallelTRECQuerying extends TRECQuerying {

	final ExecutorService pool;
	
	//First line is valid for multiple processors machine. If the machine has only one, it only launches one thread
	//final static int NUM_PROC = Runtime.getRuntime().availableProcessors();
	final static int NUM_PROC = 20;
	List<Future<?>> runningQueries = Collections.synchronizedList(new ArrayList<Future<?>>());

	public ParallelTRECQuerying() {
		super(ConcurrentIndexUtils.makeConcurrentForRetrieval(Index.createIndex()));
		pool = Executors.newFixedThreadPool(NUM_PROC);
		if (! (super.printer instanceof NullOutputFormat))
			super.printer = new SynchronizedOutputFormat(super.printer);
	}

	public ParallelTRECQuerying(boolean _queryexpansion) {
		super(ConcurrentIndexUtils.makeConcurrentForRetrieval(Index.createIndex()));
		this.queryexpansion = _queryexpansion;
		pool = Executors.newFixedThreadPool(NUM_PROC);
		if (! (super.printer instanceof NullOutputFormat))
			super.printer = new SynchronizedOutputFormat(super.printer);
	}


	public ParallelTRECQuerying(Index i) {
		super(ConcurrentIndexUtils.makeConcurrentForRetrieval(i));
		pool = Executors.newFixedThreadPool(NUM_PROC);
		if (! (super.printer instanceof NullOutputFormat))
			super.printer = new SynchronizedOutputFormat(super.printer);
	}

	final void _processQueryAndWrite(final String queryId, final String query,
			final double cParameter, final boolean c_set) 
	{
		super.processQueryAndWrite(queryId, query, cParameter, c_set);
	}

	@Override
	protected void processQueryAndWrite(final String queryId, final String query,
			final double cParameter, final boolean c_set) 
	{
		final ParallelTRECQuerying me = this;
		runningQueries.add(pool.submit(new Runnable() {
			
			public void run() {
				me._processQueryAndWrite(queryId, query, cParameter, c_set);
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
	public String processQueries(double c, boolean c_set) {
		//completionService = new ExecutorCompletionService<Object>(pool);
		return super.processQueries(c, c_set);		
	}
	
	static class SynchronizedOutputFormat implements OutputFormat
	{
		OutputFormat parent;
		public SynchronizedOutputFormat(OutputFormat _parent)
		{
			parent = _parent;
		}
		
		public void printResults(PrintWriter pw, SearchRequest q,
				String method, String iteration, int numberOfResults)
				throws IOException {
			parent.printResults(pw, q, method, iteration, numberOfResults);
		}
		
		
		
	}
	
	public static void main(String[] args)
	{
		TRECQuerying me = new ParallelTRECQuerying();
		me.processQueries();
		me.close();
	}

}
