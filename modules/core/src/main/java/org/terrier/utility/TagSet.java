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
 * The Original Code is TagSet.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.utility;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * A class that models a set of tags to process (white list),
 * a set of tags to skip (black list), a tag that is used as a
 * document delimiter, and a tag the contents of which are 
 * used as a unique identifier. The text within any tag encountered 
 * within the scope of a tag from the white list, is processed
 * by default, unless it is explicitly black listed. 
 * <br>
 * For example, in order to index all the text within
 * the DOC tag of a document from a typical TREC collection, 
 * without indexing the contents of the DOCHDR tag,
 * we could define in the properties file the following properties:
 * <br>
 * <tt>TrecDocTags.doctag=DOC</tt><br>
 * <tt>TrecDocTags.idtag=DOCNO</tt><br>
 * <tt>TrecDocTags.process=</tt><br>
 * <tt>TrecDocTags.skip=DOCHDR</tt><br>
 * <tt>TrecDocTags.casesensitive=false</tt><br>
 * <br>
 * In the source code, we would create an instance of
 * the class as follows:
 * <br>
 * <tt>TagSet TrecIndexToProcess = new TagSet("TrecDocTags");</tt>
 * <br>
 * All the tags are converted to uppercase, in order to check 
 * whether they belong to the specified set of tags.
 * 
 * @author Vassilis Plachouras, Craig Macdonald
  */
public class TagSet {
	/**
	 * A prefix for an empty set of tags, that is a set of tags
	 * that are not defined in the properties file.
	 */
	public final static String EMPTY_TAGS = "";
	
	/**
	 * The prefix for the TREC document tags. The corresponding
	 * properties in the setup file should start with <tt>TrecDocTags</tt>.
	 */
	public final static String TREC_DOC_TAGS = "TrecDocTags";
	
	/**
	 * The prefix for the TREC document exact tags. The corresponding
	 * properties in the setup file should start with <tt>TrecExactDocTags</tt>.
	 */
	public final static String TREC_EXACT_DOC_TAGS = "TrecExactDocTags";
	
	/**
	 * The prefix for the TREC topic tags. The corresponding
	 * properties in the setup file should start with <tt>TrecQueryTags</tt>.
	 */
	public final static String TREC_QUERY_TAGS = "TrecQueryTags";
	
	/**
	 * The prefix for the TREC property tags. The corresponding
	 * properties in the setup file should start with <tt>TrecPropertyTags</tt>.
	 */
	public final static String TREC_PROPERTY_TAGS = "TrecPropertyTags";
	
	/**
	 * The prefix for the tags to consider as fields, during indexing. 
	 * The corresponding properties in the setup file should start with
	 * <tt>FieldTags</tt>.
	 */
	public final static String FIELD_TAGS = "FieldTags";
	
	/**
	 * The set of tags to process.
	 */
	protected Set<String> whiteList = new HashSet<>();
	/** Size of whiteList hashset */
	protected final int whiteListSize;
	
	protected List<String> whiteListTags = new ArrayList<>();
	
	/**
	 * The set of tags to skip.
	 */
	protected Set<String> blackList = new HashSet<>();
	/** Size of whiteList hashset */
	protected final int blackListSize;

	protected List<String> blackListTags = new ArrayList<>();
	
	
	/**
	 * The tag that is used as a unique identifier.
	 */
	protected final String idTag;
	
	/**
	 * The tag that is used for denoting the beginning of a
	 * document.
	 */
	protected final String docTag;
	/** is this TagSet case sensitive. Defaults to true for all sets except TrecDocTags */
	protected final boolean caseSensitive;

	public static TagSetFactory factory() {
		return new TagSetFactory();
	}

	public static class TagSetFactory {

		String docTag = null;
		String idTag = null;
		List<String> whitelist = Collections.emptyList();
		List<String> blacklist = Collections.emptyList();
		boolean caseSensitive = false;

		public TagSetFactory setDocTag(String tag) {
			this.docTag = tag;
			return this;
		}

		public TagSetFactory setIdTag(String tag) {
			this.idTag = tag;
			return this;
		}

		public TagSetFactory setWhitelist(List<String> tags) {
			this.whitelist = tags;
			return this;
		}

		public TagSetFactory setWhitelist(String... tags) {
			this.whitelist = Arrays.asList(tags);
			return this;
		}

		public TagSetFactory setBlacklist(List<String> tags) {
			this.blacklist = tags;
			return this;
		}

		public TagSetFactory setBlacklist(String... tags) {
			this.blacklist = Arrays.asList(tags);
			return this;
		}

		public TagSetFactory setCaseSensitive(boolean yes) {
			this.caseSensitive = yes;
			return this;
		}

		public TagSet build() {
			return new TagSet(docTag, caseSensitive, idTag, whitelist, blacklist);
		}
	}

	//private constructor - the the factory class instead
	private TagSet(String docTag, boolean caseSensitive, String idTag, List<String> whitelist, List<String> blacklist) {
		this.caseSensitive = caseSensitive;
		if (this.caseSensitive) {
			this.docTag = docTag;
			this.idTag = idTag;
			this.whiteListTags = whitelist;
			this.blackListTags = blacklist;
			this.whiteList = new HashSet<>(whitelist);
			this.blackList = new HashSet<>(blacklist);			
		} else {
			this.idTag = StringTools.toUpperCase(idTag);
			this.docTag = StringTools.toUpperCase(docTag);
			this.whiteListTags = whitelist.stream().map(t -> StringTools.toUpperCase(t)).collect(Collectors.toList());
			this.blackListTags = blacklist.stream().map(t -> StringTools.toUpperCase(t)).collect(Collectors.toList());
			this.blackList = blacklist.stream().map(t -> StringTools.toUpperCase(t)).collect(Collectors.toSet());
			this.whiteList = whitelist.stream().map(t -> StringTools.toUpperCase(t)).collect(Collectors.toSet());
		}
		this.whiteListSize = whitelist.size();
		this.blackListSize = blackList.size();
		if (this.idTag.length() > 0)
			this.whiteList.add(this.idTag);
		if (this.docTag.length() > 0)
			this.whiteList.add(this.docTag);
	}
	
	/**
	 * Constructs the tag set for the given prefix,
	 * by reading the corresponding properties from
	 * the properties file.
	 * @param prefix the common prefix of the properties to read.
	 */
	public TagSet(String prefix) {
		caseSensitive = Boolean.parseBoolean(
            ApplicationSetup.getProperty(prefix+".casesensitive",
				prefix.equals(TREC_DOC_TAGS) ? "true" : "false"));
		
		String whiteListTags;
		String blackListTags;
		if (prefix.length() > 0)
		{
			String _whiteListTags = ApplicationSetup.getProperty(prefix+".process","");
			String _blackListTags = ApplicationSetup.getProperty(prefix+".skip","");
			if (!caseSensitive)
			{
				whiteListTags = StringTools.toUpperCase(_whiteListTags);
				blackListTags =  StringTools.toUpperCase(_blackListTags);
			}
			else
			{
				whiteListTags = _whiteListTags;
				blackListTags = _blackListTags;
			}
			for (String t: whiteListTags.split("\\s*,\\s*"))
				if (t.length() > 0){
					whiteList.add(t);
					this.whiteListTags.add(t);
				}
					
			for (String t : blackListTags.split("\\s*,\\s*"))
				if (t.length() > 0)
				{
					if (whiteList.contains(t))
						throw new IllegalArgumentException(prefix+".process" + " and " + prefix+".skip" + " cannot both contain tag " + t);
					blackList.add(t);
					this.blackListTags.add(t);
				}
			String _idTag = ApplicationSetup.getProperty(prefix+".idtag","");
			String _docTag = ApplicationSetup.getProperty(prefix+".doctag","");
			if (!caseSensitive)
			{
				idTag =  StringTools.toUpperCase(_idTag);
				docTag = StringTools.toUpperCase(_docTag);
			} else {
				idTag = _idTag;
				docTag = _docTag;
			}			
		}
		else
		{
			whiteListTags = null;
			blackListTags = null;
			idTag = null;
			docTag = null;
		}
		whiteListSize = whiteList.size();
		blackListSize = blackList.size();

		/*if the exist, the id and doc tags do not have to be included in the whitelist, as 
		they are automatically added here*/
		if (idTag != null && idTag.length() > 0)
			whiteList.add(idTag);
		if (idTag != null && docTag.length() > 0)	
			whiteList.add(docTag);
	}

	/** Returns true if whiteListSize &gt; 0.
	 *  @return Returns true if whiteListSize &gt; 0
	 */
	public boolean hasWhitelist()
	{
		return whiteListSize > 0;
	}
	
	/**
	 * Checks whether the tag should be processed. 
	 * @param tag String the tag to check.
	 * @return boolean true if the tag should be processed
	 */
	public boolean isTagToProcess(String tag) {
		return whiteListSize > 0
			? whiteList.contains(caseSensitive ? tag : StringTools.toUpperCase(tag))
			: false;
	}
	
	/**
	 * Checks whether a tag should be skipped. You should
	 * use isTagToProcess as it checks the whitelist and blacklist.
	 * @param tag the tag to check.
	 * @return true if the tag is an identifier tag,
	 *         otherwise it returns false.
	 */
	public boolean isTagToSkip(String tag) {
		return blackListSize > 0
			? blackList.contains(caseSensitive ? tag : StringTools.toUpperCase(tag))
			: false;
	}
	
	/**
	 * Checks whether the given tag is a 
	 * unique identifier tag, that is the document
	 * number of a document, of the identifier of a
	 * topic.
	 * @param tag String the tag to check.
	 * @return boolean true if the tag is an identifier tag,
	 *         otherwise it returns false.
	 */
	public boolean isIdTag(String tag) {
		return idTag.equals(
			caseSensitive ? tag : StringTools.toUpperCase(tag));
	}
	/**
	 * Checks whether the given tag indicates
	 * the limits of a document.
	 * @param tag String the tag to check.
	 * @return boolean true if the tag is a document
	 *         delimiter tag, otherwise it returns false.
	 */
	public boolean isDocTag(String tag) {
		return docTag.equals(
			caseSensitive ? tag : StringTools.toUpperCase(tag));
	}

	/** Returns true if this tag set has been specified as case-sensitive */
	public boolean isCaseSensitive()
	{
		return caseSensitive;
	}
	
	/**
	 * Returns a comma separated list of tags to process
	 * @return String the tags to process
	 */
	public String[] getTagsToProcess() {
		return whiteListTags.toArray(new String[whiteListTags.size()]);
	}
	
	/**
	 * Returns a comma separated list of tags to skip
	 * @return String the tags to skip
	 */
	public String[] getTagsToSkip() {
		return blackListTags.toArray(new String[blackListTags.size()]);
	}
	
	/**
	 * Return the id tag.
	 * @return String the id tag
	 */
	public String getIdTag() {
		return idTag;
	}
	
	/**
	 * Return the document delimiter tag.
	 * @return String the document delimiter tag
	 */
	public String getDocTag() {
		return docTag;
	}
	
}
