/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is WARC018Collection.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.indexing;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.FixedSizeInputStream;

/**
 * This object is used to parse WARC format web crawls, 0.18. 
 * The precise {@link Document} class to be used can be specified with the
 * <tt>trec.document.class</tt> property.
 * 
 * <p>
 * <b>Properties</b>
 * <ul>
 * <li><tt>trec.document.class</tt> the {@link Document} class to parse individual documents (defaults to {@link TaggedDocument}).</li>
 * <li><tt>warc018collection.force.utf8</tt> - should UTF8 encoding be assumed throughout. Defaults to false.</li>
 * <li><tt>warc018collection.header.docno</tt> - what header has the thing to be used as docno? Defaults to warc-trec-id.</li>
 * <li><tt>warc018collection.header.url</tt> - what header has the thing to be used as url? Defaults to warc-target-url.</li>
 * </ul>
 * @author Craig Macdonald
 */
public class WARC018Collection extends MultiDocumentFileCollection implements Collection
{
	/** the length of the blob containing the document data */
	protected long currentDocumentBlobLength = 0;
	
	/** what header for the docno document metadata */
	protected final String warc_docno_header = ApplicationSetup.getProperty("warc018collection.header.docno","warc-trec-id").toLowerCase();
	/** what header for the url document metadata */
	protected final String warc_url_header = ApplicationSetup.getProperty("warc018collection.header.url", "warc-target-uri").toLowerCase();
	/** what header for the crawldate document metadata */
	protected final String warc_crawldate_header = ApplicationSetup.getProperty("warc018collection.header.crawldate", "date").toLowerCase();
	/** how to parse WARC date formats */
	final static SimpleDateFormat dateWARC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	
    
    public WARC018Collection() {
		super();
	}


	public WARC018Collection(InputStream input) {
		super(input);
	}


	public WARC018Collection(String CollectionSpecFilename) {
		super(CollectionSpecFilename);
	}


	/** Move the collection to the start of the next document. */
	public boolean nextDocument() {
		DocProperties = new HashMap<String,String>(15);
		try{
		warcrecord: while(true)
		{
			String line = readLine();
			//logger.debug("Checking "+line + " for the magic warc header");
			//look for a warc line
			if (line.startsWith("WARC/0.18"))
			{
				//logger.debug("Found warc header");
				int headerSize = parseHeaders(true);
				//logger.debug("Parsed WARC headers in "+ headerSize + " bytes");
				final long warc_response_length = Long.parseLong(DocProperties.get("content-length"));
				//logger.debug("length of http message is "+warc_response_length);
				if (! DocProperties.get("warc-type").equals("response"))
				{
					is.skip(warc_response_length);
					//-49
					continue warcrecord;
				}
				headerSize = parseHeaders(false);
				//logger.debug("Parsed HTTP headers in "+ headerSize + " bytes");
				DocProperties.put("docno", DocProperties.get(warc_docno_header));
				DocProperties.put("url", DocProperties.get(warc_url_header));
				DocProperties.put("crawldate", parseDate(DocProperties.get(warc_crawldate_header)));
				if (logger.isDebugEnabled())
					logger.debug("Now working on document "+ DocProperties.get("docno"));
	
				extractCharset();
				documentsInThisFile++;
				currentDocumentBlobLength = warc_response_length - headerSize; //-16
				return true;
			}
			if (eof)
			{
				if (documentsInThisFile == 0)
				{
					logger.warn(this.getClass().getSimpleName() + " found no documents in " + FilesToProcess.get(FileNumber-1) + ". "
						+"Perhaps trec.collection.class is wrongly set or decompression failed.");
				}
				if (! openNextFile())
					return false;
			}
		}
		} catch (IOException ioe) {
			logger.error("IOException while reading WARC format collection file" + ioe);
		}
		return false;
	}    
	
	/** 
	 * This is unsupported by this Collection implementation, and
	 * any calls will throw UnsupportedOperationException
	 * @throws UnsupportedOperationException on all invocations */
//	@Override 
//	public void remove()
//	{
//		throw new UnsupportedOperationException("Iterator.remove() not supported");
//	}
	
	
	/** Get the String document identifier of the current document. */
	public String getDocid()
	{
		return DocProperties.get("docno");
	}
	
	
	

	/** Get the document object representing the current document. */
	@Override
	public Document getDocument()
	{
		FixedSizeInputStream fsis = new FixedSizeInputStream(is, currentDocumentBlobLength);
		fsis.suppressClose();
		Document rtr; 
		try {
			rtr = documentClass.getConstructor(InputStream.class, Map.class, Tokeniser.class).newInstance(fsis, DocProperties, tokeniser);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return rtr;
//		String charset = DocProperties.get("charset");
//		Reader r;
//		if (charset == null)
//		{
//			r = new InputStreamReader(fsis);
//		}
//		else
//		{
//			try{
//				charset = StringTools.normaliseEncoding(charset);
//				logger.debug("Using "+ charset + " to decode "+ DocProperties.get("docno"));
//				r = new InputStreamReader(fsis, charset);
//			} catch (java.io.UnsupportedEncodingException uee) {
//				logger.warn("Encoding "+charset+ " is unrecognised, resorting to system default");
//                r = new InputStreamReader(fsis);
//			} catch (Exception e) {
//				logger.warn("Problem reading documents, perhaps encoding "+charset+ " is unrecognised, trying to read with system default encoding", e);
//				r = new InputStreamReader(fsis);
//			}
//		}	
//		return new TaggedDocument(r, DocProperties, tokeniser);
	}

	protected int parseHeaders(final boolean requireContentLength) throws IOException
	{
		int headerSize = 0;
		boolean foundContentLength = false;
		while(true)
		{
			final String followLine = readLine();
			final int len = followLine.length();
			headerSize += readLineByteCount;
			if (len == 0)
			{
				if ( (! requireContentLength) || (requireContentLength && foundContentLength))
					break;
			}
			final int colonIndex = followLine.indexOf(':');
			if (colonIndex < 0)
			{
				continue;
			}
			final String key = followLine.substring(0,colonIndex).trim().toLowerCase();
			final String value = followLine.substring(colonIndex+1, len).trim();
			DocProperties.put(key, value);
			if (key.equals("content-length"))
				foundContentLength = true;
		}
		if (requireContentLength)
			assert foundContentLength;
		//System.err.println("Header length was " + headerSize);
		return headerSize;
	}

	
	int readLineByteCount;
	
	/** read a line from the currently open InputStream is */
	protected String readLine() throws IOException
	{
		final StringBuilder s = new StringBuilder();
		int c = 0;char ch; char ch2;
		readLineByteCount = 0;
		while(true)
		{
			c = is.read(); readLineByteCount++;
			if (c == -1)
			{
				eof = true;
				break;
			}
			ch = (char)c;
			if (ch == '\r')
			{
				c = is.read(); readLineByteCount++;
				if (c== -1)
				{
					s.append(ch);
					eof = true;
					break;
				}
				ch2 = (char)c;
				if (ch2 == '\n')
					break;
				s.append(ch); s.append(ch2);
			}
			else if (ch == '\n')
			{
				break;
			}
			else
			{
				s.append(ch);
			}
		}
		return s.toString();
	}

	final static String parseDate(String date)
	{
		if (date == null)
			return "";
		try{
			synchronized (dateWARC) {
				return Long.toString(dateWARC.parse(date).getTime() / 1000l);
			}
		} catch (ParseException pe ) {
			return "";
		}
	}

}
