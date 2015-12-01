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
 * The Original Code is QueryParserException.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author) 
 */


package org.terrier.querying.parser;

/** Thrown by the Manager when it cannot parse the query */
public class QueryParserException extends Exception
{
	private static final long serialVersionUID = 1L;
	/** 
	 * Constructs an instance of the QueryParserException.
	 * @param message
	 * @param cause
	 */
	public QueryParserException(String message, Throwable cause)
	{
		super(message, cause);
	}
	/**
	 * Constructs an instance of the QueryParserException.
	 * @param message
	 */
	public QueryParserException(String message)
	{
		super(message);
	}
}
