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
 * The Original is in 'CheckClosedStreams.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.utility.io;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.terrier.tests.ApplicationSetupBasedTest;
import org.terrier.utility.ApplicationSetup.TerrierApplicationPlugin;
import org.terrier.utility.Files;

/** Utility class during testing to ensure that all files are closed */
public class CheckClosedStreams extends ApplicationSetupBasedTest implements TerrierApplicationPlugin {
	
	final Logger LOG = LoggerFactory.getLogger(CheckClosedStreams.class);
	
	@Test
	public void testSingleFileLeftOpen() throws Exception
	{
		String filename = super.writeTemporaryFile("test.txt", new String[]{"line1"});
		CheckClosedStreams.enable();
		BufferedReader br = Files.openFileReader(filename);
		assertFalse(CheckClosedStreams.allClosed());
		br.close();
		assertTrue(CheckClosedStreams.allClosed());
	}

	static Set<CloseCheckFilterInputStream> inputStreams = new HashSet<CloseCheckFilterInputStream>();
	static Set<CloseCheckFilterOutputStream> outputStreams = new HashSet<CloseCheckFilterOutputStream>();
	
	public static class CloseCheckFilterInputStream extends FilterInputStream
	{
		StackTraceElement[] openTrace;
		
		public CloseCheckFilterInputStream(InputStream in) {
			super(in);
			inputStreams.add(this);
			openTrace = Thread.currentThread().getStackTrace();
		}

		@Override
		public void close() throws IOException {
			super.close();
			inputStreams.remove(this);
		}	
	}
	
	public static class CloseCheckFilterOutputStream extends FilterOutputStream
	{
		StackTraceElement[] openTrace;
		
		public CloseCheckFilterOutputStream(OutputStream in) {
			super(in);
			outputStreams.add(this);
			openTrace = Thread.currentThread().getStackTrace();
		}

		@Override
		public void close() throws IOException {
			super.close();
			outputStreams.remove(this);
		}
	}
	
	
	public static void enable()
	{
		Files.addFilterInputStreamMapping(".*", CloseCheckFilterInputStream.class, CloseCheckFilterOutputStream.class);	
	}
	
	public static void finished()
	{
		for(CloseCheckFilterInputStream is : inputStreams)
		{
			assertFalse("InputStream is still open - opened at:\n"+ toString(is.openTrace), true);
		}
		for(CloseCheckFilterOutputStream is : outputStreams)
		{
			assertFalse("OutputStream is still open - opened at: "+ toString(is.openTrace), true);
		}
	}
	
	public void finishedLog()
	{
		for(CloseCheckFilterInputStream is : inputStreams)
		{
			LOG.warn("InputStream is still open - opened at:\n"+ toString(is.openTrace));
		}
		for(CloseCheckFilterOutputStream is : outputStreams)
		{
			LOG.warn("OutputStream is still open - opened at: "+ toString(is.openTrace));
		}
	}
	
	public static boolean allClosed()
	{
		return inputStreams.size() == 0 && outputStreams.size() == 0;
	}
	
	private static final Class<?>[] ignoredClasses = new Class[]{
		CheckClosedStreams.class,
		CloseCheckFilterInputStream.class,
		CloseCheckFilterOutputStream.class,
		Files.class,
		Thread.class
	};
	
	private static String toString(StackTraceElement[] trace)
	{
		StringBuilder s = new StringBuilder();
		ELEMENT: for (StackTraceElement t : trace)
		{
			for (Class<?> c : ignoredClasses)
			{
				if (c.equals(t.getClass()))
					continue ELEMENT;
			}
			s.append(t.toString());
			s.append('\n');
		}
		return s.toString();
	}

	@Override
	public void initialise() throws Exception {
		enable();
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				finishedLog();
			}			
		});
	}
}
