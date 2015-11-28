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
 * The Original Code is FieldPosting.java.
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

/** Class holding the information for a posting list read
 * from a previously written run at disk. Used in the merging phase of the Single pass inversion method.
 * This class knows how to append itself to a {@link org.terrier.compression.bit.BitOutputStream} and it
 * represents a posting with field information <code>(tf, df, [docid, idf, field_f, field_f, field_f])</code>
 * @author Roi Blanco and Craig Macdonald
 *
 */
public class FieldPosting extends Posting{
	/**
	 * Writes the first document in the posting list.
	 * @param doc the document identifier.
	 * @param frequency the frequency of the term in the document.
	 * @param fieldFrequencies tf in each field for the term in the document.
	 * @throws IOException if an I/O error ocurrs.
	 */	
	public int writeFirstDoc(final int doc, final int frequency, final int[] fieldFrequencies) throws IOException{
		writeFirstDoc(doc, frequency);
		for(int field_f : fieldFrequencies)
		{
			//System.err.println("f" + "=" + field_f);
			docIds.writeUnary(field_f+1);
		}
		return docIds.getSize();
	}
	
	/**
	 * Inserts a new document in the posting list. Document insertions must be done
	 * in order.  
	 * @param doc the document identifier.
	 * @param freq the frequency of the term in the document.
	 * @param fieldFrequencies the frequency of the term in the document.
	 * @return the number of bytes consumed in the buffer
	 * @throws IOException if and I/O error occurs.
	 */
	public int insert(final int doc, final int freq, final int[] fieldFrequencies) throws IOException{		
	  final int bytes = docIds.getSize();
	  insert(doc, freq);
	  for(int field_f : fieldFrequencies)
	  {
		  //System.err.println("f" + "=" + field_f);
			docIds.writeUnary(field_f+1);
	  }
	  return docIds.getSize() - bytes;
	}
}