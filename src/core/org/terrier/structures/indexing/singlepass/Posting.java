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
 * The Original Code is Posting.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Roi Blanco (rblanc{at}@udc.es)
 *   Craig Macdonald (craigm{at}dcs.gla.ac.uk)
 */

package org.terrier.structures.indexing.singlepass;

import java.io.IOException;

import org.terrier.compression.bit.MemorySBOS;

/**
 * Class representing a simple posting list in memory.
 * It keeps the information for <code>TF, Nt</code>, and the sequence <code>[doc, tf]</code>
 * @author Roi Blanco
 *
 */
public class Posting {
	
	/** The term frequency */
	protected int TF;
	/** The document frequency */
	protected int Nt;
	/** The compressed in-memory object holding the sequence doc_id, idf*/	
	protected MemorySBOS docIds;
	/** Last document inserted in the posting */
	protected int lastInt = 0;
		
	/**
	 * Writes the first document in the posting list.
	 * @param docId the document identifier.
	 * @param freq the frequency of the term in the document.
	 * @return the number of bytes consumed in the buffer
	 * @throws IOException if an I/O error ocurrs.
	 */	
	public int writeFirstDoc(int docId, int freq) throws IOException{		
		docIds = new MemorySBOS();
		TF = freq;			
		Nt = 1;
		//System.err.println("Writing docid="+ (docId+1) + " f=" + freq);
		docIds.writeGamma(docId + 1);
		docIds.writeGamma(freq);
		lastInt = docId;
		return docIds.getSize();
	}
	
	/**
	 * Inserts a new document in the posting list. Document insertions must be done
	 * in order.  
	 * @param doc the document identifier.
	 * @param freq the frequency of the term in the document.
	 * @return the number of bytes consumed in the buffer
	 * @throws IOException if and I/O error occurs.
	 */
	public int insert(int doc, int freq) throws IOException
	{		
		final int bytes = docIds.getSize();
		Nt++;
		TF += freq;
		docIds.writeGamma(doc - lastInt);
		docIds.writeGamma(freq);
		lastInt = doc;					
		return docIds.getSize() - bytes;
	}

	/**
	 * @return the term frequency of the term in the run
	 */
	public int getTF(){
		return TF;
	}
	
	/**
	 * @return the document data compressed object.
	 */
	public MemorySBOS getDocs(){
	    return docIds;
	}	
	
	/**
	 * Sets the term frequency in the run.
	 * @param tf the term frequency.
	 */
	public void setTF(int tf){
		this.TF = tf;
	}
	
	/**
	 * Sets the document data compressed object.
	 * @param docs
	 */
	public void setDocs(MemorySBOS docs){
		this.docIds = docs;
	}

	/**
	 * @return the document frequency - the number of documents this term occurs in
	 */
	public int getDocF() {
		return Nt;
	}

	/**
	 * Set the document frequency the number of documents this term occurs in.
	 * @param docF the document frequency.
	 */
	public void setDocF(int docF) {
		this.Nt = docF;
	}

	/** Returns the size of the underlying buffer representing this posting set. */
	public int getSize() {
		return docIds.getSize();
	}
}
