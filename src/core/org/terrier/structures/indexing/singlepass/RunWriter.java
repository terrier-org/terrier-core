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
 * The Original Code is RunWriter.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Roi Blanco (rblanc{at}@udc.es)
 *   Craig Macdonald (craigm{at}dcs.gla.ac.uk)
 */
package org.terrier.structures.indexing.singlepass;


import java.io.DataOutputStream;
import java.io.IOException;

import org.terrier.compression.bit.BitOutputStream;
import org.terrier.compression.bit.MemorySBOS;
import org.terrier.utility.Files;
/**
 * This class writes a run to disk. The data written depends on the specific subclass.
 * This one, writes the Nt, TF and the <docid, tf> sequence.
 * It also writes the max frequency of a term in the run (useful for allocating memory during the merging phase).
 * @author Roi Blanco
 */
public class RunWriter {
	/** Underlying {@link org.terrier.compression.bit.BitOutputStream} to write the compressed objects */
	protected final BitOutputStream bos;
	/** Underlying {@link java.io.DataOutputStream} to write the term Strings */
	protected final DataOutputStream stringDos;
	/** Debug String representation of this RunWriter */
	protected String info;
	
	protected RunWriter()
	{
		bos = null;
		stringDos = null;
		info = null;
	}
	
	/** other constructor for use by subclasses */
	protected RunWriter(BitOutputStream _bos, DataOutputStream _stringDos) throws IOException
	{
		this.bos = _bos;
		this.stringDos = _stringDos;
		this.info = "RunWriter(Streams)";
	}
	
	/**
	 * Instanciates a RunWriter, given the filenames to write.
	 * @param fileName name of the file to write the posting lists data. 
	 * @param termsFile name of the file to write the terms.
	 * @throws IOException if an I/O error occurs.
	 */
	public RunWriter(String fileName, String termsFile) throws IOException{
		bos = new BitOutputStream(fileName);
		stringDos = new DataOutputStream( Files.writeFileStream(termsFile));
		this.info = "RunWriter("+fileName+")";
	}
	
	/** Returns true if this RunWriter needs writeTerm() to be called sorted by term */
	public boolean writeSorted()
	{
		return true;
	}
	
	/**
	 * Writes the headers of the run.
	 * @param maxSize max size of a posting.
	 * @param size number of postings in the run.
	 * @throws IOException if an I/O error occurs.
	 */
	public void beginWrite(int maxSize, int size) throws IOException{			
		bos.writeGamma(maxSize);
		bos.writeGamma(size);	
	}
	
	/**
	 * Writes the information for a given term.
	 * @param term the term to write.
	 * @param post the Posting with the data of the term.
	 * @throws IOException if an I/O error occurs.
	 */
	public void writeTerm(final String term, final Posting post) throws IOException{		
		stringDos.writeUTF(term);
		bos.writeGamma(post.getDocF());
		bos.writeGamma(post.getTF());
		//System.err.println("Writing "+term + " TF="+post.getTF()+ " Nt="+post.getDocF());
		final MemorySBOS Docs = post.getDocs();
		Docs.pad();
		/* when reading, ie RunReader and it's children classes
		 * an align call is required here. */
		bos.append(Docs.getMOS().getBuffer(), Docs.getMOS().getPos());
	}
		
	/**
	 * Closes the output streams.
	 * @throws IOException if an I/O error occurs.
	 */
	public void finishWrite() throws IOException{
		bos.close();
		stringDos.close();
	}
	/** 
	 * {@inheritDoc} 
	 */
	public String toString()
	{
		return info;
	}
	
}
