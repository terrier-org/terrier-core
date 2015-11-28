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
 * The Original is in 'WARC10Collection.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.indexing;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.regex.Matcher;

import org.apache.commons.lang.CharEncoding;
import org.terrier.utility.StringTools;

/**
* This object is used to parse WARC format web crawls, version 0.10. 
* Uses properties from WARC018Collection.
* 
* @author Craig Macdonald
*/
public class WARC10Collection extends WARC018Collection  {

	public WARC10Collection() {
		super();
	}

	public WARC10Collection(InputStream input) {
		super(input);
	}

	public WARC10Collection(String CollectionSpecFilename) {
		super(CollectionSpecFilename);
	}
	
	protected void processRedirect(String source, String target) {
		
	}
	

	/** Move the collection to the start of the next document. */
	public boolean nextDocument()
	{
		DocProperties = new HashMap<String,String>(15);
		try{
		warcrecord: while(true)
		{
			String line = readLine();
			//logger.debug("Checking "+line + " for the magic warc header, found = " + line.startsWith("WARC/1.0"));
			//look for a warc line
			if (line.startsWith("WARC/1.0"))
			{
				//logger.debug("Found warc header");
				int headerSize = parseHeaders(true);
				//logger.debug("Parsed WARC headers in "+ headerSize + " bytes");
				final long warc_response_length = Long.parseLong(DocProperties.get("content-length"));
				//logger.debug("length of following message is "+warc_response_length);
				if (! DocProperties.get("warc-type").equals("response"))
				{
					//System.err.println("Skipping warc-type of " + DocProperties.get("warc-type"));
					is.skip(warc_response_length);
					continue warcrecord;
				}
				
				headerSize = 0;
				//ignore all content before the HTTP line, e.g. clueweb12-1216wb-96-28178
				String statusLine = readLine();
				headerSize += readLineByteCount;
				while(! statusLine.startsWith("HTTP/"))
				{
					statusLine = readLine();
					headerSize += readLineByteCount;
				}				
				final String[] statusParts = statusLine.split(" ", 3);
				int code;
				try {
					code = Integer.parseInt(statusParts[1]);
				} catch (NumberFormatException nfe) {
					throw new IOException("Invalid status line '"+statusLine+"' for " + DocProperties.get(warc_docno_header));
				}
				headerSize += parseHeaders(false);
				if (code == 301 || code == 302)
				{
					processRedirect(DocProperties.get(warc_url_header), DocProperties.get("location"));
					//there is no need to skip, as there should only be headers
					//is.skip(warc_response_length - headerSize);
					continue warcrecord;					
				} else if (code != 200) {
					assert false : "Unsupported status code: " + code;
				}
				
				//logger.debug("Parsed HTTP headers in "+ headerSize + " bytes");
				DocProperties.put("docno", DocProperties.get(warc_docno_header));
				DocProperties.put("url", DocProperties.get(warc_url_header));
				DocProperties.put("crawldate", parseDate(DocProperties.get(warc_crawldate_header)));
				if (logger.isDebugEnabled())
					logger.debug("Now working on document "+ DocProperties.get("docno"));
				
				DocProperties.put("charset", desiredEncoding);
				//obtain the character set of the document and put in the charset property
				String cType = DocProperties.get("content-type");
				if (cType != null)
				{
					cType = cType.toLowerCase();
					if (cType.contains("charset"))
	   				{
						final Matcher m = charsetMatchPattern.matcher(cType);
						if (m.find() && m.groupCount() > 0) {
							String charset = StringTools.normaliseEncoding(m.group(1));							
							if (CharEncoding.isSupported(charset))
								DocProperties.put("charset", charset);
						}
					}
				}
				//force UTF-8 for english documents - webpage isnt clear:
				//http://boston.lti.cs.cmu.edu/Data/clueweb09/dataset.html#encodings
				if (forceUTF8)
					DocProperties.put("charset", "utf-8");
				documentsInThisFile++;
				currentDocumentBlobLength = Math.max(0, warc_response_length - headerSize );
				//document clueweb12-0000tw-00-00010 has no content, causes a negative currentDocumentBlobLength
				
				//assert currentDocumentBlobLength > 0 : 
				//	"document "+DocProperties.get(warc_docno_header)+" must have size: "
				//	+"warc_response_length - headerSize - 26 = " 
				//	+ warc_response_length + " - " + headerSize + " -26 " + " = " + currentDocumentBlobLength;
				
				return true;
			}
			if (eof)
			{
				if (documentsInThisFile == 0)
				{
					String sourcemsg = FilesToProcess.size() > 0 ? FilesToProcess.get(FileNumber-1) : "input stream";
					logger.warn(this.getClass().getSimpleName() + " found no documents in " 
						+ sourcemsg + ". "
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
	
}
