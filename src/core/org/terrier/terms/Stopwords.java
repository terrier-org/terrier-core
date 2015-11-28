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
 * The Original Code is StopWords.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.terms;
import java.io.BufferedReader;
import org.terrier.utility.Files;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.THashSet;

import org.terrier.utility.ApplicationSetup;
/** 
 * Implements stopword removal, as a TermPipeline object. Stopword list to load can be
 * passed in the constructor or loaded from the <tt>stopwords.filename</tt> property.
 * Note that this TermPipeline uses the system default encoding for the stopword list.
 * <b>Properties</b><br />
 * <ul><li><tt>stopwords.filename</tt> - the stopword list to load. More than one stopword list can be specified, by comma-separating
 * the filenames.</li>
 * <li><tt>stopwords.intern.terms</tt> - optimisation of Java for indexing: Stopwords terms are likely to appear extremely frequently
 * in a Collection, <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/lang/String.html#intern()">interning</a> them in Java will
 * save on GC costs during indexing.</li>
 * <li><tt>stopwords.encoding</tt> - encoding of the file containing the stopwords, if not set defaults to <tt>trec.encoding</tt>,
 * and if that is not set, onto the default system encoding.</li></ul>
 * @author Craig Macdonald <craigm{a.}dcs.gla.ac.uk> 
  */
public class Stopwords implements TermPipeline
{
	/** The logger used */
	private static Logger logger = LoggerFactory.getLogger(Stopwords.class);
	protected final static boolean INTERN_STOPWORDS = Boolean.parseBoolean(
		ApplicationSetup.getProperty("stopwords.intern.terms", "false"));
	/** The next component in the term pipeline. */
	protected final TermPipeline next;

	/** The hashset that contains all the stop words.*/
	protected final THashSet<String> stopWords = new THashSet<String>();
	/** 
	 * Makes a new stopword termpipeline object. The stopwords 
	 * file is loaded from the application setup file, 
	 * under the property <tt>stopwords.filename</tt>.
	 * @param _next TermPipeline the next component in the term pipeline.
	 */
	public Stopwords(final TermPipeline _next)
	{
		this(_next, ApplicationSetup.getProperty("stopwords.filename", "stopword-list.txt"));
	}

	/** Makes a new stopword term pipeline object. The stopwords file(s)
	  * are loaded from the filename parameter. If the filename is not absolute, it is assumed
	  * to be in TERRIER_SHARE. StopwordsFile is split on \s*,\s* if a comma is found in 
	  * StopwordsFile parameter.
	  * @param _next TermPipeline the next component in the term pipeline
	  * @param StopwordsFile The filename(s) of the file to use as the stopwords list. Split on comma,
	  * and passed to the (TermPipeline,String[]) constructor.
	  */	
	public Stopwords(final TermPipeline _next, final String StopwordsFile)
	{
		this.next = _next;
		if (StopwordsFile.indexOf(',') >= 0)
			loadStopwordsList(StopwordsFile.split("\\s*,\\s*"));
		else
			loadStopwordsList(StopwordsFile);
	}

	/** Makes a new stopword term pipeline object. The stopwords file(s)
	  * are loaded from the filenames array parameter. The non-existance of
	  * any file is not enough to stop the system. If a filename is  not absolute, it is
	  * is assumed to be in TERRIER_SHARE.
	  * @param _next TermPipeline the next component in the term pipeline
	  * @param StopwordsFiles Array of filenames of stopword lists.
	  * @since 1.1.0
	  */
	public Stopwords(final TermPipeline _next, final String StopwordsFiles[])
	{
		this.next = _next;
		loadStopwordsList(StopwordsFiles);
	}

	/** Loads the specified stopwords files. Calls loadStopwordsList(String).
	  * @param StopwordsFiles Array of filenames of stopword lists.
	  * @since 1.1.0
	  */
	public void loadStopwordsList(final String StopwordsFiles[])
	{
		for(int i=0;i<StopwordsFiles.length;i++)
		{
			loadStopwordsList(StopwordsFiles[i]);
		}
	}

	/** Loads the specified stopwords file. Used internally by Stopwords(TermPipeline, String[]).
	  * If a stopword list filename is not absolute, then ApplicationSetup.TERRIER_SHARE is appended.
	  * @param stopwordsFilename The filename of the file to use as the stopwords list. */
	public void loadStopwordsList(String stopwordsFilename)
	{
		//get the absolute filename
		stopwordsFilename = ApplicationSetup.makeAbsolute(stopwordsFilename, ApplicationSetup.TERRIER_SHARE);
		//determine encoding to use when reading the stopwords files
		String stopwordsEncoding = 
			ApplicationSetup.getProperty("stopwords.encoding", 
				ApplicationSetup.getProperty("trec.encoding", null));
		try {
			//use sys default encoding if none specified
			BufferedReader br = stopwordsEncoding != null
				? Files.openFileReader(stopwordsFilename, stopwordsEncoding)
				: Files.openFileReader(stopwordsFilename);
			String word;
			while ((word = br.readLine()) != null)
			{
				word = word.trim();
				if (word.length() > 0)
				{
					if (INTERN_STOPWORDS)
						word = word.intern();
					stopWords.add(word);
				}
			}
			br.close();
		} catch (IOException ioe) {
			logger.error("Errror: Input/Output Exception while reading stopword list ("+stopwordsFilename+") :  Stack trace follows.",ioe);
			
		}
		if (stopWords.size() == 0)
            logger.error("Error: Empty stopwords file was used ("+stopwordsFilename+")");
	}


	/** Clear all stopwords from this stopword list object. 
	  * @since 1.1.0 */
	public void clear()
	{
		stopWords.clear();	
	}

	/** Returns true is term t is a stopword */
	public boolean isStopword(final String t)
	{
		return stopWords.contains(t);
	}

	
	/** 
	 * Checks to see if term t is a stopword. If so, then the TermPipeline
	 * is exited. Otherwise, the term is passed on to the next TermPipeline
	 * object. This is the TermPipeline implementation part of this object.
	 * @param t The term to be checked.
	 */
	public void processTerm(final String t)
	{
		if (stopWords.contains(t))
			return;
		next.processTerm(t);
	}
	
	/** {@inheritDoc} */
	public boolean reset() {
		return next.reset();
	}
}
