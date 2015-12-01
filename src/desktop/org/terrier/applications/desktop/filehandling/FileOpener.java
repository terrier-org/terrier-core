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
 * The Original Code is FileOpener.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.applications.desktop.filehandling;
/**
 * This interface is used for encapsulatign the platform-dependent
 * process of opening a file with an arbitrary extension.
 * @author Vassilis Plachouras
  */
public interface FileOpener {
	/**
	 * Opens the file with the given name, using either a 
	 * pre-defined, or a user-defined application. This 
	 * interface should be implemented by classes that should
	 * work for Linux or Windows.
	 * @param filename the name of the file to open.
	 */
	void open(String filename) throws Exception;
	/** Perform any saving to disk of data the implementor may requires. */
	void save();
	/** Perform any loading in of data the implementor may require. */
	void load();
}
