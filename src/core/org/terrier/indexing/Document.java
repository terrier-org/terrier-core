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
 * The Original Code is Document.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.indexing;
import java.util.Set;
import java.io.Reader;
import java.util.Map;
/** 
 * This interface encapsulates the concept of a document during indexing.
 * Implementors of this interface as responsible for parsing and tokenising
 * a document (eg parse the HTML tags, output the text terms found).
 * @author Craig Macdonald, Vassilis Plachouras
 */
public interface Document
{
	/** 
	 * Gets the next term of the document. 
	 * <B>NB:</B>Null string returned from getNextTerm() should
	 * be ignored. They do not signify the lack of any more terms.
	 * endOfDocument() should be used to check that.
	 * @return String the next term of the document. Null returns should be
	 * ignored.         
	 */
	String getNextTerm();

	/** 
	 * Returns a list of the fields the current term appears in.
	 * @return HashSet a set of the terms that the current term appears in. 
	 */
	Set<String> getFields();
	
	/** 
	 * Returns true when the end of the document has been reached, and there
	 * are no other terms to be retrieved from it.
	 * @return boolean true if there are no more terms in the document, otherwise
	 *         it returns false.
     */
	boolean endOfDocument();

	/** Returns a Reader object so client code can tokenise the document
	 * or deal with the document itself. Examples might be extracting URLs,
	 * language detection. */
	Reader getReader();

	/** Allows access to a named property of the Document. Examples might be URL, filename etc. 
	  * @param name Name of the property. It is suggested, but not required that this name
	  * should not be case insensitive.
	  * @since 1.1.0 */
	String getProperty(String name);
	/** Returns the underlying map of all the properties defined by this Document. 
	  * @since 1.1.0 */
	Map<String,String> getAllProperties();

}
