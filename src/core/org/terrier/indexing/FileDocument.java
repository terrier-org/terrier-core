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
 * The Original Code is FileDocument.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.indexing;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.indexing.tokenisation.TokenStream;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.ApplicationSetup;
/** 
 * Models a document which corresponds to one file. The first FileDocument.abstract.length characters
 * can be saved as an abstract.
 * @author Craig Macdonald, Vassilis Plachouras, Richard McCreadie, Rodrygo Santos
 */
public class FileDocument implements Document {
	protected static final Logger logger = LoggerFactory.getLogger(FileDocument.class);
	/** The maximum number of digits that are allowed in valid terms. */
	/** The input reader. */
	protected Reader br;
	/** End of Document. Set by the last couple of lines in getNextTerm() */
	protected boolean EOD = false;
	
	/** The number of bytes read from the input.*/
	//public long counter = 0;
	
	protected Map<String,String> fileProperties;

	/** The name of the file represented by this document. */
	protected String filename;
	
	protected TokenStream tokenStream;
	
	protected FileDocument() {}
	
	/** The names of the abstracts to be saved (comma separated list) **/
	protected final String abstractname = ApplicationSetup.getProperty("FileDocument.abstract", "");
	/** The maximum length of each named abstract (comma separated list) **/
	protected final int abstractlength = Integer.parseInt(ApplicationSetup.getProperty("FileDocument.abstract.length", "0"));
	/** The number of characters currently written **/
	protected int abstractwritten = 0;
	/** The current abstract text **/
	StringBuilder abstractText = new StringBuilder();

	protected static Map<String,String> makeFilenameProperties(String filename)
	{
		Map<String,String> docProperties = new HashMap<String,String>();
		docProperties.put("filename", filename);
		return docProperties;
	}
	/**
	 * create a document for a file
	 * @param _filename
	 * @param docReader
	 * @param tok
	 */
	public FileDocument(String _filename, Reader docReader, Tokeniser tok) {
		this(docReader, makeFilenameProperties(_filename), tok);
	}
	/**
	 * create a document for a file
	 * @param _filename
	 * @param docStream
	 * @param tok
	 */
	public FileDocument(String _filename, InputStream docStream, Tokeniser tok) {
		this(docStream, makeFilenameProperties(_filename), tok);
	}
	/**
	 * create a document for a file
	 * @param docReader
	 * @param docProperties
	 * @param tok
	 */
	public FileDocument(Reader docReader, Map<String,String> docProperties, Tokeniser tok) {
		this.br = docReader;
		this.fileProperties = docProperties;
		this.fileProperties.put("parser", this.getClass().getName());
		this.filename = docProperties.get("filename");
		try{
			//do we have abstract enabled?
			if (abstractname.length() != 0)
				tokenStream = tok.tokenise(new ReaderWrapper(this.br));
			else 
				tokenStream = tok.tokenise(this.br);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
	
	/** 
	 * Constructs an instance of the FileDocument from the 
	 * given input stream.
	 * @param docStream the input stream that reads the file.
	 */
	public FileDocument(InputStream docStream, Map<String,String> docProperties, Tokeniser tok) {
		this.fileProperties = docProperties;
		this.filename = docProperties.get("filename");
		this.br = getReader(docStream);
		this.fileProperties.put("parser", this.getClass().getName());
		try{
			//do we have abstract enabled?
			if (abstractname.length() != 0)
				tokenStream = tok.tokenise(new ReaderWrapper(this.br));
			else 
				tokenStream = tok.tokenise(this.br);				
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}
	
	/**
	 * A wrapper around the token stream used to lift the terms from the stream
	 * for storage in the abstract
	 * @author Richard McCreadie
	 * @since 3.5
	 */
	public class ReaderWrapper extends Reader {
		
		Reader underlyingStream;

		/**
		 * create a wraper for token stream
		 * @param stream
		 */
		public ReaderWrapper(Reader stream) {
			underlyingStream = stream;
		}
		
		@Override
		public int read() throws IOException {
			final int readChar = underlyingStream.read();
			if (abstractwritten<abstractlength) {
				abstractText.append(((char)readChar));
				abstractwritten++;
			}
			if (readChar==-1)
			{
				setProperty(abstractname, abstractText.toString());
			}
			return readChar;
		}

		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			final int readChar = underlyingStream.read(cbuf,off,len);
			if (abstractwritten<abstractlength) {
				abstractText.append(cbuf, off, len);
				abstractwritten++;
			}
			if (readChar==-1)
			{	
				setProperty(abstractname, abstractText.toString());
			}
			return readChar;
		}

		@Override
		public void close() throws IOException {
			underlyingStream.close();
		}
			
	}
	
	

	/** Returns the underlying buffered reader, so that client code can tokenise the
	 * document itself, and deal with it how it likes. */
    public Reader getReader()
    {
        return this.br;
    }


	/** 
	 * Returns a buffered reader that encapsulates the
	 * given input stream.
	 * @param docStream an input stream that we want to 
	 *        access as a buffered reader.
	 * @return the buffered reader that encapsulates the 
	 *         given input stream.
	 */
	protected Reader getReader(InputStream docStream) {
		return new BufferedReader(new InputStreamReader(docStream));
	}
	
	/**Gets the next term from the Document */
	public String getNextTerm()
	{
		return tokenStream.next();
	}
	/**
	 * Returns null because there is no support for fields with
	 * file documents.
	 * @return null.
	 */
	public Set<String> getFields() {
		return Collections.emptySet();
	}
	/** 
	 * Indicates whether the end of a document has been reached.
	 * @return boolean true if the end of a document has been reached, 
	 *         otherwise, it returns false.
	 */
	public boolean endOfDocument() {
		return ! tokenStream.hasNext();
	}
	/** 
	 * Get a document property
	 */	
	public String getProperty(String name){
		return fileProperties.get(name.toLowerCase());
	}
	/** 
	 * Set a document property
	 */
	public void setProperty(String name, String value)
	{
		fileProperties.put(name.toLowerCase(),value);
	}
	/** 
	 * {@inheritDoc} 
	 */
	public Map<String,String> getAllProperties(){
		return fileProperties;
	}
}
