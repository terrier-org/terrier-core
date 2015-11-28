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
 * The Original is in 'MultiOSFileOpener.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
/* Surrogate for the 1.6 java.awt.Desktop "browse(url)" and "open(file)" methods. Provides platform-dependent fallback methods
 * where the Desktop class is unavailable. See http://www.davidc.net/programming/java/browsing-urls-and-opening-files
 * <p/>
 * Copyright (c) 2009 David C A Croft.
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.terrier.applications.desktop.filehandling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A file opener implementation that uses a Java 6 AWT API to open
 * the prescribed file.
 * @author Craig Macdonald
 * @since 3.5 
 */
public class MultiOSFileOpener implements FileOpener {
	
	private static Logger logger = LoggerFactory.getLogger(MultiOSFileOpener.class);
	
	  private static final String OS_MACOS = "Mac OS";
	  private static final String OS_WINDOWS = "Windows";

	 private static final String[] UNIX_OPEN_CMDS = {
         "run-mailcap", // many Unixes, run registered program from /etc/mailcap
         // Fall back to assuming it's a text file.
         "pager", // debian update-alternatives target
         "less", "more"};

	/** 
	 * {@inheritDoc} This is unsupported by this implemented class. 
	 */
	public void load() {}
	
	/**
	   * Opens the given File in the system default viewer application.
	   *
	   * @param file the File to open
	   * @throws IOException if an application couldn't be found or if the File failed to launch
	   */
	  public static void open(final File file) throws IOException
	  {
	    // Try Java 1.6 Desktop class if supported
	    if (openDesktop(file)) return;
	 
	    final String osName = System.getProperty("os.name");
	    logger.debug("Opening " + file + " for OS " + osName);
	 
	    if (osName.startsWith(OS_MACOS)) {
	      openMac(file);
	    }
	    else if (osName.startsWith(OS_WINDOWS)) {
	      openWindows(file);
	    }
	    else {
	      //assume Unix or Linux
	      openUnix(file);
	    }
	  }

	/** 
	 * {@inheritDoc} 
	 */
	public void open(String filename) throws Exception {
		open(new File(filename));
	}
	
	/**
	   * Attempts to locate a viewer from a predefined list under Unix.
	   *
	   * @param file the File to open
	   * @throws IOException if the open failed
	   */
	  private static void openUnix(final File file) throws IOException
	  {
	    for (final String cmd : UNIX_OPEN_CMDS) {
	      logger.debug("Unix looking for " + cmd);
	      if (unixCommandExists(cmd)) {
	        logger.debug("Unix found " + cmd);
	        Runtime.getRuntime().exec(new String[]{cmd, file.getAbsolutePath()});
	        return;
	      }
	    }
	    throw new IOException("Could not find a suitable viewer");
	  }
	 
	  /**
	   * Find the Desktop class if it exists in this JRE.
	   *
	   * @return the Desktop class object, or null if it could not be found
	   */
	  private static Class<?> getDesktopClass()
	  {
	    // NB The following String is intentionally not inlined to prevent ProGuard trying to locate the unknown class.
	    final String desktopClassName = "java.awt.Desktop";
	    try {
	      return Class.forName(desktopClassName);
	    }
	    catch (ClassNotFoundException e) {
	      logger.debug("Desktop class not found");
	      return null;
	    }
	  }
	 
	  /**
	   * Gets a Desktop class instance if supported. We check isDesktopSupported() but for convenience we don't bother to
	   * check isSupported(method); instead the caller handles any UnsupportedOperationExceptions.
	   *
	   * @param desktopClass the Desktop Class object
	   * @return the Desktop instance, or null if it is not supported
	   */
	  private static Object getDesktopInstance(final Class<?> desktopClass)
	  {
	    try {
	      final Method isDesktopSupportedMethod = desktopClass.getDeclaredMethod("isDesktopSupported");
	      logger.debug("invoking isDesktopSupported");
	      final boolean isDesktopSupported = (Boolean) isDesktopSupportedMethod.invoke(null);
	 
	      if (!isDesktopSupported) {
	    	  logger.debug("isDesktopSupported: no");
	        return null;
	      }
	 
	      final Method getDesktopMethod = desktopClass.getDeclaredMethod("getDesktop");
	      return getDesktopMethod.invoke(null);
	    }
	    catch (Exception e) {
	    	logger.warn("Exception in Desktop operation", e);
	      return null;
	    }
	  }
	 
	  /**
	   * Finds the com.apple.eio.FileManager class on a Mac.
	   *
	   * @return the FileManager instance
	   * @throws ClassNotFoundException if FileManager was not found
	   */
	  private static Class<?> getAppleFileManagerClass() throws ClassNotFoundException
	  {
		logger.debug("Mac looking for com.apple.eio.FileManager");
	 
	    // NB The following String is intentionally not inlined to prevent ProGuard trying to locate the unknown class.
	    final String appleClass = "com.apple.eio.FileManager";
	    return Class.forName(appleClass);
	  }
	 
	  /**
	   * Checks whether a given executable exists, by means of the "which" command.
	   *
	   * @param cmd the executable to locate
	   * @return true if the executable was found
	   * @throws IOException if Runtime.exec() throws an IOException
	   */
	  private static boolean unixCommandExists(final String cmd) throws IOException
	  {
	    final Process whichProcess = Runtime.getRuntime().exec(new String[]{"which", cmd});
	 
	    boolean finished = false;
	    do {
	      try {
	        whichProcess.waitFor();
	        finished = true;
	      }
	      catch (InterruptedException e) {
	    	  logger.warn("Interrupted waiting for which to complete", e);
	      }
	    } while (!finished);
	 
	    return whichProcess.exitValue() == 0;
	  }
	  /**
	   * Attempt to use java.awt.Desktop by reflection. Does not link directly to Desktop class so that this class can still
	   * be loaded in JRE < 1.6.
	   *
	   * @param file the File to open
	   * @return true if open successful, false if we should fall back to other methods
	   * @throws IOException if Desktop was found, but the open() call failed.
	   */
	  private static boolean openDesktop(final File file) throws IOException
	  {
	    final Class<?> desktopClass = getDesktopClass();
	    if (desktopClass == null) return false;
	 
	    final Object desktopInstance = getDesktopInstance(desktopClass);
	    if (desktopInstance == null) return false;
	 
	    logger.debug("Opening " + file + " using Desktop.open()");
	 
	    try {
	      final Method browseMethod = desktopClass.getDeclaredMethod("open", File.class);
	      browseMethod.invoke(desktopInstance, file);
	      return true;
	    }
	    catch (InvocationTargetException e) {
	      if (e.getCause() instanceof IOException) {
	        throw (IOException) e.getCause();
	      }
	      else if (e.getCause() instanceof IllegalArgumentException) {
	        throw new FileNotFoundException(e.getCause().getLocalizedMessage());
	      }
	      else {
	        logger.debug("Exception in Desktop operation", e);
	        return false;
	      }
	    }
	    catch (Exception e) {
	      logger.debug("Exception in Desktop operation", e);
	      return false;
	    }
	  }

	  /**
	   * Uses shell32.dll to open a file under Windows.
	   *
	   * @param file the File to open
	   * @throws IOException if the open failed
	   */
	  private static void openWindows(final File file) throws IOException
	  {
	    logger.debug("Windows invoking rundll32");
	    Runtime.getRuntime().exec(new String[]{"rundll32", "shell32.dll,ShellExec_RunDLL", file.getAbsolutePath()});
	  }
	 
	  /**
	   * Attempt to use com.apple.eio.FileManager by reflection.
	   *
	   * @param file the File to open
	   * @throws IOException if the open failed
	   */
	  @SuppressWarnings("deprecation")
	  private static void openMac(final File file) throws IOException
	  {
	    // we use openURL() on the file's URL form since openURL supports file:// protocol
	    browseMac(file.getAbsoluteFile().toURL());
	  }

	  
	  /**
	   * Attempt to use com.apple.eio.FileManager by reflection.
	   *
	   * @param url the URL to launch
	   * @throws IOException if the launch failed
	   */
	  private static void browseMac(final URL url) throws IOException
	  {
	    try {
	      final Class<?> fileMgr = getAppleFileManagerClass();
	      final Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
	 
	      logger.debug("Mac invoking");
	      openURL.invoke(null, url.toString());
	    }
	    catch (Exception e) {
	      logger.warn("Couldn't launch Mac URL", e);
	      throw new IOException("Could not launch Mac URL: " + e.getLocalizedMessage());
	    }
	  }
	 

	/** 
	 * {@inheritDoc} This is unsupported by this implemented class. 
	 */
	public void save() {}

}
