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
 * The Original Code is MetaIndexBuilder.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.indexing;

import java.io.IOException;
import java.util.Map;
/** Abstract class for writing document metadata. Metadata means textual data associated
 * with a document, e.g. an external document identifier (e.g. docnos), a URL, or the title
 * or abstracts of a document.
 * <p>
 * Lookups in the resulting <{@link org.terrier.structures.MetaIndex} are supported in two manners - either by docid, or for specified key
 * types, by value. In the latter scenario, metadata values are assumed to be unique.
 * <p>
 * Typical usage during indexing:
 * <pre>
 * MetaIndexBuilder metaBuilder = ...
 * while(collection.nextDocument())
 * {
 * 	Document d = collection.getDocument();
 *  metaBuilder.writeDocumentEntry(d.getAllProperties());
 * }
 * </pre>
 * @since 3.0
 * @author Craig Macdonald 
 */
public abstract class MetaIndexBuilder implements java.io.Closeable{
	/** Write out metadata for current document, extracted from specified map 
	 * Typically, the MetaIndexBuilder will know which keys from data that
	 * it is interested in. */
	public abstract void writeDocumentEntry(Map<String, String> data) throws IOException;
	/** Write out metadata for current document. Values for all keys are specified. */
	public abstract void writeDocumentEntry(String[] data) throws IOException;
}
