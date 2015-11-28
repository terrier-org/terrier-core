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
 * The Original Code is WARC09Collection.java
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
import java.util.TimeZone;

import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.FixedSizeInputStream;

/**
 * This object is used to parse WARC format web crawls, version 0.9. 
 * The precise {@link Document} class to be used can be specified with the
 * <tt>trec.document.class</tt> property. The following links denote the pages
 * that were used to construct the format of this object:
 * 
 * http://www.yr-bcn.es/webspam/datasets/uk2006-pages/excerpt.txt
 * http://archive-access.sourceforge.net/warc/warc_file_format.html
 * http://crawler.archive.org/apidocs/index.html?org/archive/io/arc/ARCWriter.html
 * http://crawler.archive.org/apidocs/org/archive/io/GzippedInputStream.html
 * 
 * <p>
 * <b>Properties</b>
 * <ul>
 * <li><tt>trec.document.class</tt> the {@link Document} class to parse individual documents (defaults to {@link TaggedDocument}).</li>
 * </ul>
 * </p>
 * 
 * @author Craig Macdonald
 */
public class WARC09Collection extends MultiDocumentFileCollection
{
	protected long currentDocumentBlobLength = 0;
	/** properties for the current document */
	
	protected String currentDocno;
	

	
	final static SimpleDateFormat dateWARC = new SimpleDateFormat("yyyyMMddHHmmss");
	static{
		dateWARC.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	

	public WARC09Collection() {
		super();
	}

	public WARC09Collection(InputStream input) {
		super(input);
	}

	public WARC09Collection(String CollectionSpecFilename) {
		super(CollectionSpecFilename);
	}

	

	/** Get the document object representing the current document. */
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
	}

	/** Move the collection to the start of the next document. */
	public boolean nextDocument()
	{
		DocProperties = new HashMap<String,String>(15);
		try{
		warcrecord: while(true)
		{
			String line = readLine();
			//look for a warc line
			if (line.toLowerCase().startsWith("warc/"))
			{
				final String[] parts = line.split("\\s+");
				final long length = Long.parseLong(parts[1]);
				int headerSize = line.length() + 2;	
				if (! parts[2].equals("response"))
				{
					//it's not a downloaded document
					is.skip(length - headerSize);
					continue warcrecord;
				}
				/* now let's parse the rest of the WARC header 
				 * format: warc-id data-length record-type subject-uri		   creation-date record-id	content-type 
				 * example: warc/0.9 10757 response http://www.mattenltd.co.uk/ 20060920234350 message/http uuid:c6f7927d-aaea-4e53-b121-c4a594218d8a
				 */
				DocProperties.put("warc-id", parts[0]);
				DocProperties.put("url", parts[3]);
				DocProperties.put("creationdate", parts[4]);
				DocProperties.put("crawldate", parseDate(parts[4]));
				DocProperties.put("uuid", parts[6].replaceAll("^uuid:", ""));
				//System.out.println("parts array="+java.util.Arrays.toString(parts));	
				int blankCount = 0;
				do {
					final String followLine = readLine();
					final int len = followLine.length();
					if (len == 0)
					{
						headerSize+=2;
						blankCount++;
					}
					else
					{
						headerSize+= len +2;
						final int colonIndex = followLine.indexOf(':');
						if (colonIndex < 0)
							continue;
						DocProperties.put(followLine.substring(colonIndex-1).trim(), followLine.substring(colonIndex, len ).trim());
					}
				}while(blankCount != 2);

				extractCharset();

				//TODO: check for empty documents, redirects?
				documentsInThisFile++;
				currentDocno = FileNumber + "-" + documentsInThisFile;
				DocProperties.put("docno", currentDocno);	
				currentDocumentBlobLength = length - headerSize;
				//logger.debug("Document "+ currentDocno + " blobsize="+currentDocumentBlobLength);
				return true;
			}
			if (eof)
			{
				if (documentsInThisFile == 0)
				{
					logger.warn(this.getClass().getSimpleName() + " found no documents in " + FilesToProcess.get(FileNumber-1) + ". "
						+"Perhaps trec.collection.class is wrongly set, or decompression failed.");
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

	
	/** read a line from the currently open InputStream is */
	protected String readLine() throws IOException
	{
		final StringBuilder s = new StringBuilder();
		int c = 0;char ch; char ch2;
		while(true)
		{
			c = is.read();
			if (c == -1)
			{
				//logger.debug("readLine setting eof @1");
				eof = true;
				break;
			}
			ch = (char)c;
			if (ch == '\r')
			{
				c = is.read();
				if (c== -1)
				{
					s.append(ch);
					//logger.debug("readLine setting eof @2");
					eof = true;
					break;
				}
				ch2 = (char)c;
				if (ch2 == '\n')
					break;
				s.append(ch); s.append(ch2);
			}
			else
			{
				s.append(ch);
			}
		}
		//logger.debug("readLine: "+ s.toString());
		return s.toString();
	}

	/** Resets the Collection iterator to the start of the collection. */
	public void reset()
	{}
	
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
