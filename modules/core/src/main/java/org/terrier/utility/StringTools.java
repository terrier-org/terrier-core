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
 * The Original Code is StringTools.java.
 *
 * The Original Code is Copyright (C) 2005-2018 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}.dcs.gla.ac.uk/>
 */
package org.terrier.utility;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.lang.StringEscapeUtils;
/**
 * This class implements useful string functions
 */
public class StringTools {

	/** Defines escape encodings that are supported. Most are implemented using
	 * org.apache.commons.lang3.StringEscapeUtils. See methods escape() and unescape() in StringTools.
	 * 
	 * @author Craig Macdonald
	 * @since 3.0
	 */
	public static enum ESCAPE { 
		/** Perform no escaping on the String */
		NONE, 
		/** Escape any characters unsuitable for HTML in the String */
		HTML, 
		/** Escape any characters unsuitable for XML in the String */
		XML, 
		/** Escape any characters unsuitable for Java source code in the String */
		JAVA, 
		/** Escape any characters unsuitable for Javascript source code in the String */
		JAVASCRIPT, 
		/** Escape any characters unsuitable for a URL within the String */
		URL; 
		
		/** Parse a string into an ESCAPE value. Allowed values are html, xml, java, javascript, url */
		public static ESCAPE parse(String s)
		{
			s = s.toLowerCase();
			if (s.equals("html"))
				return HTML;
			if (s.equals("xml"))
				return XML;
			if (s.equals("java"))
				return JAVA;
			if (s.equals("javascript"))
				return JAVASCRIPT;
			if (s.equals("url"))
				return URL;
			return NONE;
		}
		
	};
	
	/** Escape a String according to the defined escape sequence requested */
	public static String escape(ESCAPE e, String s)
	{
		try{
			switch(e) {
			case NONE: return s;
			case HTML: return StringEscapeUtils.escapeHtml(s);
			case XML: return StringEscapeUtils.escapeXml(s);
			case JAVA: return StringEscapeUtils.escapeJava(s);
			case JAVASCRIPT: return StringEscapeUtils.escapeJavaScript(s);
			case URL: return URLEncoder.encode(s, "UTF-8");
			}
		} catch (UnsupportedEncodingException uee) {
			//UTF-8 should never be unsupported			
		}
		//cannot reach here
		return null;
	}
	
	/** Unescape a String according to the defined escape sequence requested */
	public static String unescape(ESCAPE e, String s)
	{
		try{
			switch(e) {
			case NONE: return s;
			case HTML: return StringEscapeUtils.unescapeHtml(s);
			case XML: return StringEscapeUtils.unescapeXml(s);
			case JAVA: return StringEscapeUtils.unescapeJava(s);
			case JAVASCRIPT: return StringEscapeUtils.unescapeJavaScript(s);
			case URL: return URLDecoder.decode(s, "UTF-8");
			}
		} catch (UnsupportedEncodingException uee) {
			//UTF-8 should never be unsupported			
		}
		//cannot reach here
		return null;
	}
	
	/** Returns how long String s is in bytes, if encoded in UTF-8
	  * @param s The string to be measured.
	  * @return The number of bytes s is when encoded in UTF-8
	  */
	public static int utf8_length(String s)
	{
		try{
			return s.getBytes("UTF-8").length;	
		}catch(UnsupportedEncodingException uce){
			//this should never happen, as UTF-8 is always supported
			return s.length();
		}
	}

	/** Normalises several common encodings found, for instance in HTTP or HTML headers,
	  * into the compatible Java encoding */
	public static String normaliseEncoding(String encodingName)
	{
		if(encodingName == null) return null;
		
		if (encodingName.equalsIgnoreCase("ISO-LATIN-1"))
			return "ISO-8859-1";

		if(encodingName.toLowerCase().startsWith("x-mac-")){
			String tmp = encodingName.substring(6);
			String first = tmp.substring(0, 1);
			tmp = tmp.substring(1);
            //e.g. convert 'x-mac-roman' to 'x-MacRoman'
			encodingName = "x-Mac"+first.toUpperCase()+tmp;
        	return encodingName;
		}
		encodingName = encodingName.toUpperCase();
		//from EuroGOVCollection
        if (encodingName.startsWith("WINDOWS"))
        {
            if (encodingName.indexOf("_") > 0)
            {
                encodingName = encodingName.replaceFirst("^WINDOWS_","WINDOWS-");
            }
            else if (encodingName.indexOf("-") == -1)
            {
                encodingName = encodingName.replaceFirst("^WINDOWS", "WINDOWS-");
            }
        }
        else if (encodingName.startsWith("WIN"))
        {
            encodingName = encodingName.replaceFirst("^WIN(-|_)?","WINDOWS-");
        }
        else if (encodingName.equals("UTF-8LIAS"))
        {
        		encodingName = "UTF-8";
        }
		return encodingName;
	}
	
	/*
	 *  Licensed to the Apache Software Foundation (ASF) under one
	 *  or more contributor license agreements.  See the NOTICE file
	 *  distributed with this work for additional information
	 *  regarding copyright ownership.  The ASF licenses this file
	 *  to you under the Apache License, Version 2.0 (the
	 *  "License"); you may not use this file except in compliance
	 *  with the License.  You may obtain a copy of the License at
	 *  
	 *    http://www.apache.org/licenses/LICENSE-2.0
	 *  
	 *  Unless required by applicable law or agreed to in writing,
	 *  software distributed under the License is distributed on an
	 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
	 *  KIND, either express or implied.  See the License for the
	 *  specific language governing permissions and limitations
	 *  under the License. 
	 *  
	 */

	
	/** lowerCase = 'a' .. 'z', '0'..'9', '-' */
	private static final char[] LOWER_CASE = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '-', 0, 0, '0', '1', '2', '3',
			'4', '5', '6', '7', '8', '9', 0, 0, 0, 0, 0, 0, 0, 'a', 'b', 'c',
			'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
			'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 0, 0, 0, 0, 0, 0,
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
			'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0 };

	/** lowerCase = 'a' .. 'z', '0'..'9', '-' */
	private static final char[] UPPER_CASE = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '-', 0, 0, '0', '1', '2', '3',
			'4', '5', '6', '7', '8', '9', 0, 0, 0, 0, 0, 0, 0, 'A', 'B', 'C',
			'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
			'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 0, 0, 0, 0, 0, 0,
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
			'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0 };

	/**
	 * Rewrote the toLowercase method to improve performances. In Ldap,
	 * attributesType are supposed to use ASCII chars : 'a'-'z', 'A'-'Z',
	 * '0'-'9', '.' and '-' only.
	 * 
	 * @param value
	 *            The String to lowercase
	 * @return The lowercase string
	 */
	public static final String toLowerCase(String value) {
		char[] chars = value.toCharArray();

		for (int i = 0; i < chars.length; i++) {
			final char k = chars[i];
			final char c = k < 192 ? LOWER_CASE[k] : k;
			chars[i] = c != 0 ? c : chars[i];
		}

		return new String(chars);
	}

	public static final String toUpperCase(String value) {
		char[] chars = value.toCharArray();

		for (int i = 0; i < chars.length; i++) {
			final char k = chars[i];
			char c = k < 192 ? UPPER_CASE[chars[i]] : k;
			chars[i] = c != 0 ? c : chars[i];
		}

		return new String(chars);
	}
}
