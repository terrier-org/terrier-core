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
 * The Original Code is TerrierTimer.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Ben He <ben{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a timer.
 */
public class TerrierTimer extends Thread {
	
	private static Logger logger = LoggerFactory.getLogger(TerrierTimer.class);
	
	/** The starting system time in millisecond. */ 
	protected final long startingTime;
	/** The total number of items to process in a task. */
	protected final double total;
	/** Number of items in task completed */
	protected volatile double done;
	/** the last percentage that was output. Set to a negative value, so that a value is always determined */
	protected short lastPercentage = -1;
	/** the message that should be output with the progress */
	protected final String message ;
	
	public TerrierTimer(){
		this("", 1);
	}
	
	public TerrierTimer(String message, double total){
		this.total = total;
		this.message = message;
		this.startingTime = System.currentTimeMillis();
	}
	
	public void setDone(double done)
	{
		this.done = done;
	}

	public short getPercentage()
	{
		return(short)(100.0d*(done / total));
	}
	
	@Override
	public void run()
	{
		//System.err.println("Thread started");
		while(done < total)
		{
			//System.err.println("pctg == " + getPercentage());
			final short pctg = getPercentage();
			if (pctg != lastPercentage)
			{
				if (pctg > 0)
					logger.info(message + " "+ pctg + "% done. Estimated finished in " + getRemainingTime());
				lastPercentage = pctg;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	public void increment()
	{
		done++;
	}
	
	public void increment(double dIncrememt)
	{
		done += dIncrememt;
	}
	
	public void finished()
	{
		done = total;
	}
	
	public String getRemainingTime(){
		long processingEnd = System.currentTimeMillis();
		long processingTime = (processingEnd - this.startingTime) / 1000;
		processingTime *= total/done - 1;
		int minutes = (int) (processingTime / 60.0d);
		int seconds = (int) (processingTime % 60.0d);
		return String.format("%dm%02ds", minutes, seconds);
	}
	
	/** Get a string summarising the processing/remaining time in minutes and seconds. */
	public String elapsed()
	{
		long processingEnd = System.currentTimeMillis();
		long processingTime = (processingEnd - this.startingTime) / 1000;
		int minutes = (int) (processingTime / 60.0d);
		int seconds = (int) (processingTime % 60.0d);		
		return minutes + " minutes " + seconds + " seconds elapsed";
	}
	
//	/**
//	 * Compute the processing time.
//	 *
//	 */
//	public void setBreakPoint(){
//		long processingEnd = System.currentTimeMillis();
//		long processingTime = (processingEnd - this.startingTime) / 1000;
//		minutes = (int) (processingTime / 60.0d);
//		seconds = (int) (processingTime % 60.0d);
//	}
//	/** Get the processing time in minutes. */
//	public int getMinutes(){
//		return this.minutes;
//	}
//	/** Set the overall quantitative workload of the task. */
//	public void setTotalNumber(double _total){
//		this.total = _total;
//	}
//	/**
//	 * Estimate the remaining time.
//	 * @param finished The quantitative finished workload.
//	 */
//	public void setRemainingTime(double finished){
//		long processingEnd = System.currentTimeMillis();
//		long processingTime = (processingEnd - this.startingTime) / 1000;
//		processingTime *= total/finished - 1;
//		percentage = 100 * (finished/total);
//		minutes = (int) (processingTime / 60.0d);
//		seconds = (int) (processingTime % 60.0d);
//	}
//	
//	/** Get the processing time in seconds. */
//	public int getSeconds(){
//		return this.seconds;
//	}
//	/** Get a string summarising the processing/remaining time in minutes and seconds. */
//	public String toStringMinutesSeconds(){
//		return getMinutes() + " minutes " + getSeconds() + " seconds remaining - "+getPercentage()+"% done";
//	}
//
//	/**
//	 * get percentage
//	 * @return
//	 */
//	public String getPercentage()
//	{
//		return Rounding.toString(percentage, 1);
//	}
	
	public static String longToText(long timems) {
		int days = 0;
		int hours = 0;
		int mins = 0;
		int secs = 0;
		while (timems>86400000) {
			timems=timems-86400000;
			days++;
		}
		while (timems>3600000) {
			timems=timems-3600000;
			hours++;
		}
		while (timems>60000) {
			timems=timems-60000;
			mins++;
		}
		while (timems>1000) {
			timems=timems-1000;
			secs++;
		}
		return days+" days, "+hours+" hours, "+mins+" minutes, "+secs+" seconds and "+timems+" milliseconds";
	}
}
