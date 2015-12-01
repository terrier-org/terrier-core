/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.uk
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
 * The Original Code is RuntimeMemoryChecker.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/** A memory checker that uses the Java Runtime instance to check the amount of available memory.
 * A given amount of memory must be kept free, however, this condition is only signalled if a
 * given percentage of the JVM's potential memory usage has been exhausted. */
public class RuntimeMemoryChecker implements MemoryChecker
{
	/** Memory threshold. If a memory check falls below this threshold, the postings in memory are flushed to disk */
	final long MEMORY_RESERVED;
	/** how much of the maximum allowed heap must be in use before a run can be passed */
	final double MEMORY_HEAP_USAGE_MIN_THRESHOLD;

	/** the logger for this class */
	protected static final Logger logger = LoggerFactory.getLogger(RuntimeMemoryChecker.class);
	
	/** JVM runtime */
	protected static final Runtime runtime = Runtime.getRuntime();
	
	/** set when a low memory condition has occurred */
	boolean lowMemory = false;
	
	/** Default constructor. Uses ApplicationSetup.MEMORY_THRESHOLD_SINGLEPASS and
	 * <tt>memory.heap.usage</tt> for the default memory threshold amount (default 0.70). 
	 */
	public RuntimeMemoryChecker()
	{
		this(
				ApplicationSetup.MEMORY_THRESHOLD_SINGLEPASS, 
				Double.parseDouble(ApplicationSetup.getProperty("memory.heap.usage", "0.70"))
				);
	}
	
	/** Construct a RuntimeMemoryChecker. 
	 * @param _reserved The amount of memory that must be kept available.
	 * @param _threshold Percentage of possible memory that must be allocated before a lowMemory condition is allowed.
	 */
	public RuntimeMemoryChecker(long _reserved, double _threshold)
	{
		MEMORY_RESERVED = _reserved;
		if (MEMORY_RESERVED < 100000)
			throw new IllegalArgumentException("memory.reserved should be expressed in bytes - " + MEMORY_RESERVED + " is too litle reserved for during indexing");
		
		MEMORY_HEAP_USAGE_MIN_THRESHOLD = _threshold;
		if (MEMORY_HEAP_USAGE_MIN_THRESHOLD > 1)
			throw new IllegalArgumentException("memory.heap.usage should be expressed as a float, not a percentage: " + MEMORY_HEAP_USAGE_MIN_THRESHOLD + " is too big!");
		logger.debug("memory.reserved=" + MEMORY_RESERVED + " memory.heap.usage="+MEMORY_HEAP_USAGE_MIN_THRESHOLD);
	}
	
	/** Returns true if memory is running low */
	public boolean checkMemory()
	{
	     long memoryFree = runtime.freeMemory();
	     /* For some JVMs,  runtime.totalMemory() = Long.MAX_VALUE as the memory usage of java is not suppressed
	      * in this scenario, assume that Java has grown to full adult size */
	     final double memoryAllocated = (runtime.maxMemory() == Long.MAX_VALUE )
	         ? 1.0d
	         : (double)(runtime.totalMemory()) / (double)(runtime.maxMemory());
	     logger.debug("Memory Check Free: "+memoryFree/1000000+"M, heap allocated "+(memoryAllocated*100)+"%");
	     if(memoryAllocated > MEMORY_HEAP_USAGE_MIN_THRESHOLD && memoryFree < MEMORY_RESERVED)
	     {
	         logger.debug("Free memory ("+memoryFree/1000000+"M) below threshold ("+MEMORY_RESERVED/1000000+"M)");
	         lowMemory = true;
	     }
		return lowMemory;
	}
	
	
	/** Reset the out of memory flag */
	public void reset()
	{
		lowMemory = false;
	}

}
