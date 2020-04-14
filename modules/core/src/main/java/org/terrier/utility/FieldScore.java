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
 * The Original Code is FieldScore.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Douglas Johnson <johnsoda{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.utility;
import java.util.HashSet;
import java.util.Set;
/**
 * A class for modifying the retrieval scores of documents, 
 * according to whether the query terms appear to any of the 
 * fields, or tags specified by the property 
 * <tt>FieldTags.process</tt>. These tags can be either HTML tags 
 * or tags such as DOCHDR, from the documents.<br>
 * If a query term appears in any of the specified tags, then the 
 * document score can be altered according to the values specified in 
 * the property <tt>field.modifiers</tt>. For example, if 
 * <tt>FieldTags.process=TITLE,H1,B</tt> and
 * <tt>field.modifiers=0.10,0.00,0.00</tt>, then if a query term 
 * appears in the title of a document, the document's score will be 
 * increased by a factor of 0.10.
 * 
 * @author Douglas Johnson, Vassilis Plachouras
  */
public class FieldScore {
	/**
	 * The total number of tags to check for.
	 */
	public static int FIELDS_COUNT;
	/** Indicates whether field information is used.*/
	public static boolean USE_FIELD_INFORMATION;
	
	/**
	 * The names of the fields to be processed. 
	 * The values are read from the property 
	 * <tt>FieldTags.process</tt>.
	 */
	public static String[] FIELD_NAMES = null;
	
	static {
		init();
	}
	
	/** Initialises the FieldTags tagset */
	public static void init()
	{
		TagSet htmlTags = new TagSet(TagSet.FIELD_TAGS);
		FIELD_NAMES = htmlTags.getTagsToProcess();
		FIELDS_COUNT = FIELD_NAMES.length;
		USE_FIELD_INFORMATION = FIELDS_COUNT > 0; 
	}
}
