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
 * The Original Code is Tokenizer.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> 
 */
package org.terrier.indexing;
import java.io.BufferedReader;
/**
 * The specification of the interface implemented
 * by tokeniser classes.
 * @author Gianni Amati, Vassilis Plachouras
 */
public interface Tokenizer {
	/**
	 * Returns the identifier of the tag the tokenizer is into.
	 * @return the name of the tag the tokenizer is processing
	 */	
	String currentTag();
	/**
	 * Returns the next token from the input stream used.
	 * @return the next token, or null if the end of file is encountered.*/
	String nextToken();
	/**
	 * Indicates whether we are in a special document number tag.
	 * @return true if the tokenizer is in a document number tag.
	 */
	boolean inDocnoTag();
	/**
	 * Indicates whether we are in a tag to process.
	 * @return true if we are in a tag to process.
	 */
	boolean inTagToProcess();
	/** 
	 * Indicates whether we are in a tag to skip
	 * @return true if we are in a tag to skip
	 */
	boolean inTagToSkip();
	
	/**
	 * Returns true if the end of document is encountered.
	 * @return true if the end of document is encountered.
	 */
	boolean isEndOfDocument();
	/**
	 * Returns true if the end of file is encountered.
	 * @return true if the end of document is encountered.
	 */
	boolean isEndOfFile();
	
	/**
	 * Proceed to process the next document.
	 */
	void nextDocument();
	
	/**
	 * Returns the byte offset in the current indexed file.
	 */
	long getByteOffset();
	
	/**
	 * Sets the input of the tokenizer
	 * @param input BufferedReader the input stream to tokenize
	 */
	void setInput(BufferedReader input);
}
