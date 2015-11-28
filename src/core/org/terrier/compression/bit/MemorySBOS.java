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
 * The Original Code is BitIn.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Roi Blanco
 */

package org.terrier.compression.bit;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This class extends the BitByteOutputStream, so it provides the compression writing functions, but
 * uses a MemoryOutputStream as an underlying OutputStream, so it is needed to be flushed to disk separately.
 * It uses BitByteOutputStream and not BitOutputStream because the MemoryOutputStream class has already its own buffer 
 * (to keep everything in memory), and extra buffering is inadequate.
 * @author Roi Blanco
 *
 */
public class MemorySBOS extends BitByteOutputStream {
	/** The underlying MemoryOutputStream */
	protected MemoryOutputStream mos; 
	/**
	 * Constructor for the class. Instanciates the BitByteOutputStream and MemoryOutputStream classes. 
	 * @throws IOException if an I/O error occurs.
	 */
	public MemorySBOS() throws IOException{
		super();
		mos = new MemoryOutputStream();
		dos = new DataOutputStream(mos);
	}
	
	/**
	 * Writes the underlying MemoryOutputStream in String format.
	 */
	public String toString(){
		return mos.toString();
	}	
		
	/**
	 * @return The underlying MemoryOutputStream.
	 */
	public MemoryOutputStream getMOS(){
		return mos;
	}

	/** Return the amount of memory bytes consumed by the underlying buffer */
	public int getSize() {
		return mos.getSize();
	}	
}
