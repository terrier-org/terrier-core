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
 * The Original Code is TRECWebCollection.java
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
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.CharEncoding;
import org.terrier.utility.StringTools;

/** Version of TRECCollection which can parse
 * standard form DOCHDR tags in TREC Web corpoa. 
 * A standard format DOCHDR tag from WT2G is shown below.
 * <pre>
 * &lt;DOCHDR&gt;
 * http://www.city.geneva.ny.us:80/index.htm 192.108.245.124 19970121041510 text/html 2407
 * HTTP/1.0 200 OK
 * Date: Tue, 21 Jan 1997 04:14:08 GMT
 * Server: Apache/1.1.1
 * Content-type: text/html
 * Content-length: 2236
 * Last-modified: Fri, 18 Oct 1996 17:33:56 GMT
 * &lt;/DOCHDR&gt;
 * </pre>
 * TRECWebCollection parses each HTTP header as Document property.
 * In addition, the URL, IP address, date and length are parsed
 * from the DOCHDR tags. In particular, the following Document properies
 * are set, depending on the format of the DOCHDR tag:
 * <ul>
 * <li>url (all corpora)</li>
 * <li>ip (WT2G, WT10G only)</li>
 * <li>docbytelength (WT2G, WT10G, Blog06, Blogs08 only)</li>
 * <li>contenttype (WT2G, WT10G only, but usually identified in the HTTP headers)</li>
 * <li>crawldate (WT2G, WT10G only)</li>
 * </ul> 
 * <p>
 * <b>Supported TREC Collections:</b><br>
 * There are some variations in the format of the DOCHDR tags in the various
 * TREC web corpora, in particular the first line of the tag. The following corpora are supported.
 * <ul>
 * <li>WT2G, WT10G: URL IP Crawldate content-type docbytelength</li>
 * <li>Blogs06,Blogs08: URL invalidIP invalidCrawldate docbytelength</li>
 * <li>GOV,GOV2,W3C,CERC: URL</li>
 * </ul>
 * For indexing the more recent TREC ClueWeb09 corpus, see {@link WARC018Collection}.
 * @author Craig Macdonald
 * @since 3.5
 */
public class TRECWebCollection extends TRECCollection {
	/** 
	 * Constructs an instance of the TRECWebCollection.
	 */
	public TRECWebCollection() {
		super();
	}
	/**
	 * Constructs an instance of the TRECWebCollection, given an InputStream.
	 * @param input
	 */
	public TRECWebCollection(InputStream input) {
		super(input);
	}
	/** 
	 * Constructs an instance of the TRECWebCollection.
	 * @param CollectionSpecFilename
	 * @param TagSet
	 * @param BlacklistSpecFilename
	 * @param ignored
	 */
	public TRECWebCollection(String CollectionSpecFilename, String TagSet,
			String BlacklistSpecFilename, String ignored) {
		super(CollectionSpecFilename, TagSet, BlacklistSpecFilename, ignored);
	}

	final static char[] DOCHDR_START = "<DOCHDR>".toCharArray();
	final static char[] DOCHDR_END = "</DOCHDR>".toCharArray();
	final static int DOCHDR_START_LENGTH = DOCHDR_START.length;
	final static Pattern CHARSET_PATTERN = Pattern.compile("charset=(\\S+)");
	final static SimpleDateFormat dateWT2G = new SimpleDateFormat("yyyyMMddHHmmss");
	static{
		dateWT2G.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	//final static SimpleDateFormat dateBlogs06 = new SimpleDateFormat("yyyyMddHHmm");
		
	//2008 9 30 3 0031
	//1997 01 14 12 01 21
	final static String parseDate(String date)
	{
		try{
			synchronized (dateWT2G) {
				return Long.toString(dateWT2G.parse(date).getTime() / 1000l);
			}
		} catch (Exception e) {
			//System.err.println("date=" + date + "exception="+e);
			return "";
		}
	}
	
	@Override
	protected void afterPropertyTags() throws IOException {
		StringBuilder hdr = super.getTag(DOCHDR_START_LENGTH, DOCHDR_START, DOCHDR_END);
		if (hdr == null) {
			logger.info("No header found for document " + super.ThisDocID);
			return;
		}
		final String[] lines = hdr.toString().split("\\n+");
		boolean first = false;
		for(int i=0;i<lines.length;i++)
		{
			if (lines[i].length() == 0)
				continue;

			if (! first)
			{   //first line is a special case
				first = true;
				final String[] parts = lines[i].split("\\s+");
				switch (parts.length)
				{
				//GOV,GOV2,W3C,CERC
				case 1: 
					DocProperties.put("url", parts[0]); 
					break;
				//Blog06, Blogs08
				case 4: 
					DocProperties.put("url", parts[0]);
					DocProperties.put("docbytelength", parts[3]);
					break;
				//WT2G, WT10G
				case 5:
					DocProperties.put("url", parts[0]);
					DocProperties.put("ip", parts[1]);
					DocProperties.put("crawldate", parseDate(parts[2]));
					DocProperties.put("content-type", parts[3]);
					DocProperties.put("docbytelength", parts[4]);
				}
			}
			else
			{
				int Colon;
				if ((Colon = lines[i].indexOf(':') ) > 1)
				{
					/*
						Content-Type: text/html
						becomes
						content-type => text/html
						contenttype => text/html
					*/
					
					DocProperties.put(
						lines[i].substring(0,Colon).trim().toLowerCase(),
						lines[i].substring(Colon+2).trim());
					DocProperties.put(
						lines[i].substring(0,Colon).trim().toLowerCase().replaceAll("-",""),
						lines[i].substring(Colon+2).trim());
				}
			}
		}//for
		String cType = DocProperties.get("contenttype");
		if (cType == null)
			return;	
		cType = cType.toLowerCase();
		if (cType.contains("charset"))
		{
			final Matcher m = CHARSET_PATTERN.matcher(cType);
			if (m.find() && m.groupCount() > 0)
			{
				try{
					String charset = m.group(1);
					charset = StringTools.normaliseEncoding(charset);
					if (CharEncoding.isSupported(charset))
						DocProperties.put("charset", charset);
				} catch (IllegalStateException ise) {/* this shouldnt happen if m.groupCount > 0, but it does */ }
			}
		}
	}
}
