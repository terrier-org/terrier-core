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
 * The Original Code is TRECFullTokenizer.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Gianni Amati <gba{a.}fub.it> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> 
 */
package org.terrier.indexing;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.indexing.Tokenizer;
import org.terrier.indexing.tokenisation.TokenStream;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.TagSet;
/**
 * This class is the tokenizer used for indexing TREC topic files. It can be
 * used for tokenizing other topic file formats, provided that the tags to skip
 * and to process are specified accordingly.
 * <p>
 * <b>NB:</b> This class only accepts A-Z a-z and 0-9 as valid character for query terms.
 * If this restriction is too tight, please use TRECFullUTFTokenizer instead.
 * @see org.terrier.utility.TagSet
 * @author Gianni Amati, Vassilis Plachouras
  */
public class TRECFullTokenizer implements Tokenizer {
	protected static final Logger logger = LoggerFactory.getLogger(TRECFullTokenizer.class);
	/** An option to ignore missing closing tags. Used for the query files. */
	protected boolean ignoreMissingClosingTags = false;
	/** last character read */
	public static int lastChar = -1;
	/** A counter for the number of terms. */
	public int number_of_terms = 0;
	/** The end of file from the buffered reader. */
	public boolean EOF;
	/** The end of document. */
	public boolean EOD;
	/** A flag which is set when errors are encountered. */
	public boolean error;
	/** The input reader. */
	public BufferedReader br;
	/** The number of bytes read from the input.*/
	public long counter = 0;
	/**
	 * The stack where the tags are pushed and popped accordingly. 
	 */
	protected static Stack<String> stk = new Stack<String>();
	/** The tag set to use. */
	protected TagSet tagSet;
	
	/** The set of exact tags.*/
	protected TagSet exactTagSet;
	/** The maximum length of a token in the check method. */
	protected final static int tokenMaximumLength = ApplicationSetup.MAX_TERM_LENGTH;
	/** Transform to lowercase or not?. */
	protected final static boolean lowercase = Boolean.parseBoolean(ApplicationSetup.getProperty("lowercase","true"));

	/** Is in tag to process? */
	public boolean inTagToProcess = false;
	/** Is in tag to skip? */
	public boolean inTagToSkip = false;
	/** Is in docno tag? */
	public boolean inDocnoTag = false;
	/**
	 * TConstructs an instance of the TRECFullTokenizer.
	 * The used tags are TagSet.TREC_DOC_TAGS and
	 * TagSet.TREC_EXACT_DOC_TAGS
	 */
	public TRECFullTokenizer() {
		inTagToProcess = false;
		inTagToSkip = false;
		inDocnoTag = false;
		tagSet = new TagSet(TagSet.TREC_DOC_TAGS);
		exactTagSet = new TagSet(TagSet.TREC_EXACT_DOC_TAGS);
		EOD = false;
		EOF = false;
	}
	/**
	 * Constructs an instance of the TRECFullTokenizer, 
	 * given the buffered reader.
	 * The used tags are TagSet.TREC_DOC_TAGS and
	 * TagSet.TREC_EXACT_DOC_TAGS
	 * @param _br java.io.BufferedReader the input stream to tokenize
	 */
	public TRECFullTokenizer(BufferedReader _br) {
		inTagToProcess = false;
		inTagToSkip = false;
		inDocnoTag = false;
		this.br = _br;
		tagSet = new TagSet(TagSet.TREC_DOC_TAGS);
		exactTagSet = new TagSet(TagSet.TREC_EXACT_DOC_TAGS);
		EOD = false;
		EOF = false;
	}
	/**
	 * Constructs an instance of the TRECFullTokenizer with 
	 * non-default tags.
	 * @param _tagSet TagSet the document tags to process.
	 * @param _exactSet TagSet the document tags to process exactly, without
	 *        applying strict checks.
	 */
	public TRECFullTokenizer(TagSet _tagSet, TagSet _exactSet) {
		inTagToProcess = false;
		inTagToSkip = false;
		inDocnoTag = false;
		tagSet = _tagSet;
		exactTagSet = _exactSet;
		EOD = false;
		EOF = false;
	}
	/**
	 * Constructs an instance of the TRECFullTokenizer with 
	 * non-default tags and a given buffered reader.
	 * @param _ts TagSet the document tags to process.
	 * @param _exactSet TagSet the document tags to process exactly, without
	 *        applying strict checks.
	 * @param _br java.io.BufferedReader the input to tokenize.
	 */
	public TRECFullTokenizer(TagSet _ts, TagSet _exactSet, BufferedReader _br) {
		inTagToProcess = false;
		inTagToSkip = false;
		inDocnoTag = false;
		this.br = _br;
		tagSet = _ts;
		exactTagSet = _exactSet;
		EOD = false;
		EOF = false;
	}
	/**
	 * A restricted check function for discarding uncommon, or 'strange' terms.
	 * @param s The term to check.
	 * @return the term if it passed the check, otherwise null.
	 */
	protected String check(String s) {
		//if the s is null
		//or if it is longer than a specified length
		if (s == null)
			return null;
		final int length = s.length();
		if (length == 0 || length > tokenMaximumLength)
			return null;
		if (!stk.empty() && exactTagSet.isTagToProcess(stk.peek()))
			return s;
		final StringReader sr = new StringReader(s);
		int _counter = 0;
		int counterdigit = 0;
		int ch = -1;
		int chNew = -1;
		try {
			while ((chNew = sr.read()) != -1 && _counter <= 2) {
				if (chNew >= 48 && chNew <= 57)
					counterdigit++;
				if (ch == chNew)
					_counter++;
				else
					_counter = 1;
				ch = chNew;
			}
			sr.close();
		} catch (IOException ioe) {  /* we're reading a string, this should never happen */ }
		//if it contains more than 4 consequtive same letters,
		//or more than 4 digits, then discard the term.
		if (_counter > 3 | counterdigit > 4)
			return null;
		return s;
	}
	/**
	 * Closes the buffered reader associated with the tokenizer.
	 */
	public void close() {
		try {
			br.close();
		} catch (IOException ioe) {
			logger.warn("Error while closing the buffered reader in TRECTokenizer", ioe);
		}
	}
	/**
	 * Closes the buffered reader associated with the tokenizer.
	 */
	public void closeBufferedReader() {
		try {
			br.close();
		} catch (IOException ioe) {
			logger.warn("Error while closing the buffered reader in TRECTokenizer", ioe);
		}
	}
	/**
	 * Returns the name of the tag the tokenizer is currently in.
	 * @return the name of the tag the tokenizer is currently in
	 */
	public String currentTag() {
		return stk.peek();
	}
	/**
	 * Indicates whether the tokenizer is in the special document number tag.
	 * @return true if the tokenizer is in the document number tag.
	 */
	public boolean inDocnoTag() {
		return (!stk.isEmpty() && tagSet.isIdTag(stk.peek()));
	}
	/**
	 * Returns true if the given tag is to be processed.
	 * @return true if the tag is to be processed, otherwise false.
	 */
	public boolean inTagToProcess() {
		return (!stk.isEmpty() && tagSet.isTagToProcess(stk.peek()));
	}
	/**
	 * Returns true if the given tag is to be skipped.
	 * @return true if the tag is to be skipped, otherwise false.
	 */
	public boolean inTagToSkip() {
		return (!stk.isEmpty() && tagSet.isTagToSkip(stk.peek()));
	}
	/**
	 * Returns true if the end of document is encountered.
	 * @return true if the end of document is encountered.
	 */
	public boolean isEndOfDocument() {
		return EOD;
	}
	/**
	 * Returns true if the end of file is encountered.
	 * @return true if the end of file is encountered.
	 */
	public boolean isEndOfFile() {
		return EOF;
	}
	/**
	 * Proceed to the next document.
	 */
	public void nextDocument() {
		if (EOD) {
			EOD = false;
		}
	}

	protected final StringBuilder sw = new StringBuilder(tokenMaximumLength);
	protected final StringBuilder tagNameSB = new StringBuilder(10);
	private Tokeniser tokeniser = Tokeniser.getTokeniser();
	private TokenStream currentTokenStream = Tokeniser.EMPTY_STREAM;
	
	/**
	 * Returns the next token from the current chunk of text, extracted from the
	 * document into a TokenStream.
	 * 
	 * @return String the next token of the document, or null if the 
	 *		 token was discarded during tokenisation.
	 */
	public String nextToken() {
		// consumes the current token stream
		if (currentTokenStream.hasNext()) {
			return currentTokenStream.next();
		}
		
		// if the current token stream is exhausted, construct a new one
		
		//the string to return as a result at the end of this method.
		String s = null;
		//StringBuilder sw = null;
		String tagName = null;
		boolean endOfTagName;
		//are we in a body of a tag?
		boolean btag = true;
		int ch = 0;
		//while not the end of document, or the end of file, or we are in a tag
		while (btag && ch != -1 && !EOD) {
			//initialise the stringbuffer with the maximum length of a term (heuristic)
			//sw = new StringBuilder(tokenMaximumLength);
			boolean tag_close = false;
			boolean tag_open = false;
			error = false;
			try {
				if (lastChar == '<' || lastChar == '&') {
					ch = lastChar;
					lastChar = -1;
				}
				//If not EOF and ch.isNotALetter and ch.isNotADigit and
				//ch.isNot '<' and ch.isNot '&'
				//CONSUME: whitespace
				while (ch != -1 && (( ch != '<' && ch != '&') && Character.isWhitespace((char)ch)))
				{
					ch = br.read();
					counter++;
					//if ch is '>' (end of tag), then there is an error.
					if (ch == '>')
						error = true;
				}
				
				//IDENTIFIES: start of opening or closing tags
				if (ch == '<') {
					ch = br.read();
					counter++;
					//if it is a closing tag, set tag_f true
					if (ch == '/') {
						ch = br.read();
						counter++;
						tag_close = true;
					} else if (ch == '!') { //else if it is a comment, that is <!
						counter++;
						ch = br.read();
						if (ch == '[')
						{
							counter++;
							//CDATA block, read until another [
							while ((ch = br.read()) != '['  && ch != -1) {
								counter++;
							}
						}
						else
						{	//it is a comment	
							//read until you encounter a '<', or a '>', or the end of file
							while ((ch = br.read()) != '>' && ch != '<' && ch != -1) {
								counter++;
							} 
							counter++;
						}
					} else {
						tag_open = true; //otherwise, it is an opening tag
					}
				}
				
				if (ch == '&' ) {
					//read until an opening or the end of a tag is encountered, or the 
					//end of file, or a space, or a semicolon,
					//which means the end of the escape sequence &xxx;
					while ((ch = br.read()) != '>' && 
							ch != '<' && 
							ch != ' ' && 
							ch != ';' &&
							ch != -1) {
						counter++;
					} 
					counter++;
					 
				}
				
				//if the body of a tag is encountered
				if ((btag = (tag_close || tag_open))) {
					endOfTagName = false;
					//read until the end of file, or the start, or the end 
					//of a tag, and save the content of the tag
					while (ch != -1 && ch != '<' && ch != '>') {
						if (! endOfTagName)
							tagNameSB.append((char)ch);
						ch = br.read();
						counter++;
						if (! endOfTagName && Character.isWhitespace((char)ch)) {
							endOfTagName = true;
							tagName = tagNameSB.toString();
							//System.err.println("Found tag  " + tagName + (tag_open ? "open" : "close") );
						}
					}
					//ch = br.read();counter++;
					if (! endOfTagName)
					{
						tagName = tagNameSB.toString();
						//System.err.println("Found tag " + tagName+ (tag_open ? "open" : "close"));
						tagNameSB.setLength(0);
					}
				} else { //otherwise, if we are not in the body of a tag
					//read text to tokenise
					if (((char)ch) == '>') {
						counter++;
						ch = br.read();
					}
					while (ch != -1 && ch != '<' && ch != '&')
					{
						sw.append((char)ch);
						ch = br.read();
						counter++;
					}
//					while (ch != -1
//							&& (//ch=='&' || 
//								((ch >= 'A') && (ch <= 'Z'))
//							 || ((ch >= 'a') && (ch <= 'z'))
//							 || ((ch >= '0') && (ch <= '9')))) {
//						sw.append((char)ch);
//						ch = br.read();
//						counter++;
//					}
				}
				lastChar = ch;
				s = sw.toString();
				sw.setLength(0);
				if (tag_open) {
					//System.err.println("processing open " + tagName);
					if ((tagSet.isTagToProcess(tagName) || tagSet.isTagToSkip(tagName)) && !tagName.equals("")) {
						stk.push(tagName.toUpperCase());
						if (tagSet.isTagToProcess(tagName)) {
							inTagToProcess = true;
							inTagToSkip = false;
						} else {
							inTagToSkip = true;
							inTagToProcess = false;
							continue;
						}
					}
				}
				if (tag_close) {
					//System.err.println("processing close " + tagName);
					if ((tagSet.isTagToProcess(tagName) || tagSet.isTagToSkip(tagName)) && !tagName.equals("")) {
						processEndOfTag(tagName.toUpperCase());
						String stackTop = null;
						if (!stk.isEmpty()) {
							stackTop = stk.peek();
							if (tagSet.isTagToProcess(stackTop)) {
								inTagToProcess = true;
								inTagToSkip = false;
							} else {
								inTagToProcess = false;
								inTagToSkip = true;
								continue;
							}
						} else {
							inTagToProcess = false;
							inTagToSkip = false;
						}
					}
				}
				
			} catch (IOException ioe) {
				logger.warn("Input/Output exception while reading tokens", ioe);
				return null;
			}
		}
		if (ch == -1) {
			EOF = true;
			EOD = true;	
		}
		boolean hasWhitelist = tagSet.hasWhitelist();
		if (!btag && 
				(!hasWhitelist || (hasWhitelist && inTagToProcess )) && 
				!inTagToSkip) 
		{
			if (!stk.empty() && tagSet.isIdTag(stk.peek()))
				return s;
			if (!stk.empty() && exactTagSet.isTagToProcess(stk.peek()))
				return lowercase ? s.toLowerCase() : s;
			//}
			currentTokenStream = tokeniser.tokenise(new StringReader(s));
			if (currentTokenStream.hasNext())
				return currentTokenStream.next();
//			if (lowercase)
//				return check(s.toLowerCase());
//			return(check(s));
		}
		return null;
	}
	
	/**
	 * The encounterd tag, which must be a final tag is matched with the tag on
	 * the stack. If they are not the same, then the consistency is restored by
	 * popping the tags in the stack, the observed tag included. If the stack
	 * becomes empty after that, then the end of document EOD is set to true.
	 * 
	 * @param tag The closing tag to be tested against the content of the stack.
	 */
	protected void processEndOfTag(String tag) {
		//if there are no tags in the stack, return
		if (stk.empty())
			return;
		//if the given tag is on the top of the stack then pop it
		if (tag.equals(stk.peek()))
			stk.pop();
		else { //else report an error, and find the tag.
			if (!ignoreMissingClosingTags) {
				logger.warn("<" + stk.peek()
						+ "> has no closing tag");
				logger.warn("<" + tag + "> not expected");
			}
			int _counter = 0;
			int x = stk.search(tag);
			while (!stk.empty() & _counter < x) {
				_counter++;
				stk.pop();
			}
		}
		//if the stack is empty, this signifies the end of a document.
		if (stk.empty())
			EOD = true;
	}
	/**
	 * Sets the value of the ignoreMissingClosingTags.
	 * @param toIgnore boolean to ignore or not the missing closing tags
	 */
	public void setIgnoreMissingClosingTags(boolean toIgnore) {
		ignoreMissingClosingTags = toIgnore;
	}
	/**
	 * Returns the number of bytes read from the current file.
	 * @return long the byte offset
	 */
	public long getByteOffset() {
		return counter;
	}
	
	/**
	 * Sets the input of the tokenizer.
	 * @param _br BufferedReader the input stream
	 */
	public void setInput(BufferedReader _br) {
		br = _br;
	}
}
