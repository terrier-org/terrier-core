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
 * The Original Code is WritableIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime;

import java.io.IOException;

import org.terrier.structures.Index;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;

/**
 * Interface that defines the methods that an index should have if it has the ability
 * to write itself to disk once it has been loaded/altered.
 * @author Richard McCreadie
 * @since 4.0
 *
 */
public interface WritableIndex {

	/**
	 * Write all of the index structures to disk at the specified location
	 */
	public Index write(String path, String prefix) throws IOException;
	
	/**
	 * Write the index properties to the .properties file.
	 */
	public void collectProperties(Index memory, Index newIndex, CompressionConfiguration compressionConfig);
	
}
