/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.uk
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
 * The Original Code is WrappedIOException.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   
 */
package org.terrier.utility.io;

import java.io.IOException;

/** A class for IOException. Use this when you have an error, but 
 * are being forced by an existing API to return an IOException.
 * @author Craig Macdonald
  * @since 2.2
 */
public class WrappedIOException extends IOException {
	private static final long serialVersionUID = 1L;

	/** Make a WrappedIOException with the specified message */
	public WrappedIOException(final String message) {
		super(message);
	}
	
	/** Make a WrappedIOException with the specified cause */
	public WrappedIOException(final Throwable e) {
		super();
		this.initCause(e);
	}
	
	/** Make a WrappedIOException with the specified message and cause */
	public WrappedIOException(final String message, final Throwable e)
	{
		super(message);
		this.initCause(e);
	}
}
