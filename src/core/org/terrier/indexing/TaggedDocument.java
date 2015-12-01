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
 * The Original Code is TaggedDocument.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>  
 */
package org.terrier.indexing;
import gnu.trove.TObjectIntHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.indexing.tokenisation.EnglishTokeniser;
import org.terrier.indexing.tokenisation.TokenStream;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.TagSet;

/**
 * Models a tagged document (e.g., an HTML or TREC document). In particular,
 * {@link #getNextTerm() getNextTerm()} returns the next token in the current
 * chunk of text, according to the specified tokeniser. 
 * 
 * This class uses the following properties:
 * <ul>
 * <li><tt>tokeniser</tt>, the tokeniser class to be used (defaults to EnglishTokeniser);</li>
 * <li><tt>max.term.length</tt>, the maximum length in characters of a term (defaults to 20);</li>
 * <li><tt>lowercase</tt>, whether characters are transformed to lowercase (defaults to <tt>true</tt>).</li>
 * <li><tt>TaggedDocument.abstracts</tt> - names of the abstracts to be saved for query-biased summarisation. 
 * Defaults to empty. Example: <tt>TaggedDocument.abstracts=title,abstract</tt></li>
 * <li><tt>TaggedDocument.abstracts.tags</tt> - names of tags to save text from for the purposes of 
 * query-biased summarisation. Example: <tt>TaggedDocument.abstracts=title,body</tt>. ELSE is special
 * tag name, which means anything not consumed by other tags.</li>
 * <li><tt>TaggedDocument.abstracts.lengths</tt> - max lengths of the asbtrcts. Defaults to empty. 
 * Example: <tt>TaggedDocument.abstracts.lengths=100,2048</tt></li>
 * <li><tt>TaggedDocument.abstracts.tags.casesensitive</li> - should the names of tags be case-sensitive? Defaults to false.</li>
 * </ul>
 * @author Craig Macdonald, Vassilis Plachouras, Richard McCreadie, Rodrygo Santos
 * @since 3.5
 */
public class TaggedDocument implements Document {
	protected static final Logger logger = LoggerFactory.getLogger(TaggedDocument.class);
	/** The maximum length of a token in the check method. */
	protected final static int tokenMaximumLength = ApplicationSetup.MAX_TERM_LENGTH;
	
	/** Change to lowercase? */
	protected final static boolean lowercase = Boolean.parseBoolean(ApplicationSetup.getProperty("lowercase", "true"));
	
	/** A temporary String array*/
	protected final String[] stringArray = new String[1];
	
	/** The input reader. */
	protected Reader br;
	
	/** End of Document. Set by the last couple of lines in getNextTerm() */
	protected boolean EOD = false;
	
	/** The number of bytes read from the input.*/
	protected long counter = 0;	
	
	/** Saves the last read character between consecutive calls of getNextTerm().*/
	protected int lastChar = -1;
	
	/** Indicates whether an error has occurred.*/
	protected boolean error;
	
	/**	The tags to process or skip.*/
	protected TagSet _tags = null; 
	
	/** 
	 * The tags to process exactly. For these tags,
	 * the check() method is not applied.
	 */
	protected TagSet _exact = null; 
	
	/** The tags to consider as fields. */
	protected TagSet _fields = null; 
	
	/** The stack where the tags are pushed and popped accordingly. */
	protected Stack<String> stk = new Stack<String>();
	
	/** Indicates whether we are in a tag to process. */
	protected boolean inTagToProcess = false;
	/** Indicates whether we are in a tag to skip. */
	protected boolean inTagToSkip = false;
	
	/** The hash set where the tags, considered as fields, are inserted. */
	protected Set<String> htmlStk = new HashSet<String>();
	/** Specifies whether the tokeniser is in a field tag to process. */
	protected boolean inHtmlTagToProcess = false;

	protected Map<String, String> properties = null;

	protected Tokeniser tokeniser;
	protected TokenStream currentTokenStream;
	
	/** The names of the abstracts to be saved (comma separated list) **/
	protected final String[] abstractnames = ArrayUtils.parseCommaDelimitedString(ApplicationSetup.getProperty("TaggedDocument.abstracts", "").toLowerCase());
	/** The fields that the named abstracts come from (comma separated list) **/
	protected final String[] abstracttags = ArrayUtils.parseCommaDelimitedString(ApplicationSetup.getProperty("TaggedDocument.abstracts.tags", ""));
	/** The maximum length of each named abstract (comma separated list) **/
	protected final int[] abstractlengths = ArrayUtils.parseDelimitedInts(ApplicationSetup.getProperty("TaggedDocument.abstracts.lengths", ""), ",");
	protected final boolean abstractTagsCaseSensitive = Boolean.parseBoolean(ApplicationSetup.getProperty("TaggedDocument.abstracts.tags.casesensitive", "false"));
	/** number of abstract types */
	protected final int abstractCount = abstractnames.length;
	/** builders for each abstract */
	protected final StringBuilder[] abstracts = new StringBuilder[abstractCount];
	/** A mapping for quick lookup of abstract tag names */
	protected final TObjectIntHashMap<String> abstractName2Index;
	/** Flag to check that determines whether to short-cut the abstract generation method */
	protected final boolean considerAbstracts;
	
	/** else field index **/
	protected int elseAbstractSpecialTag = -1;
	
	
	/**
	 * Constructs an instance of the class from the given input stream.
	 * @param docStream
	 * @param docProperties
	 * @param _tokeniser
	 */
	public TaggedDocument(InputStream docStream, Map<String, String> docProperties, Tokeniser _tokeniser)
	{
		this(docStream,docProperties,_tokeniser,null,null,null);
	}
	
	/**
	 * Constructs an instance of the class from the given input stream.
	 * @param docStream
	 * @param docProperties
	 * @param _tokeniser
	 * @param doctags
	 * @param exactdoctags
	 * @param fieldtags
	 */
	public TaggedDocument(InputStream docStream, Map<String, String> docProperties, Tokeniser _tokeniser, String doctags, String exactdoctags, String fieldtags)
	{
		String charset = docProperties.get("charset");
		try{
			// Java's decoding of InputStream bytes into characters is
			// inefficient for one character at a time.
			// So we wrap it in BufferedReader which decodes bunches of
			// characters each time.			
			this.br = new BufferedReader(charset != null
				? new InputStreamReader(docStream, charset)
				: new InputStreamReader(docStream));					
		} catch (UnsupportedEncodingException uee) {
			logger.warn("Desired encoding ("+charset+") unsupported. Resorting to platform default.", uee);
			this.br = new BufferedReader(new InputStreamReader(docStream));
		}
		this.properties = docProperties;
		
		if (doctags!=null) this._tags = new TagSet(doctags);
		else this._tags = new TagSet(TagSet.TREC_DOC_TAGS);
		
		if (exactdoctags!=null) this._exact = new TagSet(exactdoctags);
		else this._exact = new TagSet(TagSet.TREC_EXACT_DOC_TAGS);
		
		if (fieldtags!=null) this._fields = new TagSet(fieldtags);
		else  this._fields = new TagSet(TagSet.FIELD_TAGS);
		this.tokeniser = _tokeniser;
		this.currentTokenStream = Tokeniser.EMPTY_STREAM;
		for(int i=0;i<abstractCount;i++)
		{
			abstracts[i] = new StringBuilder(abstractlengths[i]);
			if (! abstractTagsCaseSensitive)
				 abstracttags[i] = abstracttags[i].toUpperCase();
			if (abstracttags[i].toUpperCase().equals("ELSE"))
				elseAbstractSpecialTag = i;
		}
		
		if (abstracttags.length>0) {
			considerAbstracts=true;
			abstractName2Index = new TObjectIntHashMap<String>();
			int aIndex = 0;
			for (String abstractName : abstracttags) {
				abstractName2Index.put(abstractName, aIndex);
				aIndex++;
			}
		} else {
			considerAbstracts = false;
			abstractName2Index = null;
		}
	}
	
	/** 
	 * Constructs an instance of the class from the given reader object.
	 * @param docReader Reader the stream from the collection that ends at the 
	 *		end of the current document.
	 */
	public TaggedDocument(Reader docReader, Map<String, String> docProperties, Tokeniser _tokeniser)
	{
		this.br = docReader;
		properties = docProperties;	
		this._tags = new TagSet(TagSet.TREC_DOC_TAGS);
		this._exact = new TagSet(TagSet.TREC_EXACT_DOC_TAGS);
		this._fields = new TagSet(TagSet.FIELD_TAGS);
		this.tokeniser = _tokeniser;
		this.currentTokenStream = Tokeniser.EMPTY_STREAM;
		
		if (abstracttags.length>0) {
			considerAbstracts=true;
			abstractName2Index = new TObjectIntHashMap<String>();
			int aIndex = 0;
			for (String abstractName : abstracttags) {
				abstractName2Index.put(abstractName, aIndex);
				aIndex++;
			}
		} else {
			considerAbstracts = false;
			abstractName2Index = null;
		}
	}

	/** Returns the underlying buffered reader, so that client code can tokenise the
	  * document itself, and deal with it how it likes. */
	public Reader getReader()
	{
		return this.br;
	}

	
	protected final StringBuilder sw = new StringBuilder(tokenMaximumLength);
	protected final StringBuilder tagNameSB = new StringBuilder(10);
	
	/**
	 * Returns the next token from the current chunk of text, extracted from the
	 * document into a TokenStream.
	 * 
	 * @return String the next token of the document, or null if the 
	 *		 token was discarded during tokenisation.
	 */
	public String getNextTerm() {

		String upperCaseTagName = null;
		
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
				//while ((ch < 1 && ch != '<' && ch != '&') || Character.isWhitespace((char)ch))
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
							upperCaseTagName = tagName.toUpperCase();
							//System.err.println("Found tag  " + tagName + (tag_open ? " open" : " close") );
							tagNameSB.setLength(0);
						}
					}
					//ch = br.read();counter++;
					if (! endOfTagName)
					{
						tagName = tagNameSB.toString();
						upperCaseTagName = tagName.toUpperCase();
						//System.err.println("Found tag " + tagName+ (tag_open ? " open" : " close"));
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
					if ((_tags.isTagToProcess(tagName) || _tags.isTagToSkip(tagName)) && !tagName.equals("")) {
						stk.push(upperCaseTagName);
						if (_tags.isTagToProcess(tagName)) {
							inTagToProcess = true;
							inTagToSkip = false;
						} else {
							inTagToSkip = true;
							inTagToProcess = false;
							continue;
						}
					}
					if (_fields.isTagToProcess(tagName) && !tagName.equals("")) {
						htmlStk.add(upperCaseTagName);
						inHtmlTagToProcess = true;
					}
				}
				if (tag_close) {
					//System.err.println("processing close " + tagName);
					if ((_tags.isTagToProcess(tagName) || _tags.isTagToSkip(tagName)) && !tagName.equals("")) {
						processEndOfTag(upperCaseTagName);
						String stackTop = null;
						if (!stk.isEmpty()) {
							stackTop = stk.peek();
							if (_tags.isTagToProcess(stackTop)) {
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
					if (_fields.isTagToProcess(tagName) && !tagName.equals("")) {
						htmlStk.remove(upperCaseTagName);
					}
				}
				
			} catch (IOException ioe) {
				logger.warn("Input/Output exception during reading tokens. Document "+ this.getProperty("docno"), ioe);
				return null;
			}
		}
		if (ch == -1) {
			processEndOfDocument();			
		}
		boolean hasWhitelist = _tags.hasWhitelist();
		if (!btag && 
				(!hasWhitelist || (hasWhitelist && inTagToProcess )) && 
				!inTagToSkip) 
		{
			if (!stk.empty() && _exact.isTagToProcess(stk.peek()))
				return lowercase ? s.toLowerCase() : s;
			//}
			if (considerAbstracts) {
				if (abstractTagsCaseSensitive) saveToAbstract(s,tagName);
				else saveToAbstract(s,upperCaseTagName);
			}
			currentTokenStream = tokeniser.tokenise(new StringReader(s));
			if (currentTokenStream.hasNext())
				return currentTokenStream.next();
//			if (lowercase)
//				return check(s.toLowerCase());
//			return(check(s));
		}
		return null;
	}
	
	protected void processEndOfDocument()
	{
		EOD = true;
		for(int abstractId=0;abstractId<abstractCount;abstractId++)
		{
			setProperty(abstractnames[abstractId], abstracts[abstractId].toString().trim());
		}
	}
	
	/**
	 * This method takes the text parsed from a tag and then saves it to the
	 * abstract(s). This method contains the logic to decide whether indeed the
	 * text or some subset of it should be saved. The default behaviour checks each
	 * abstract named in TaggedDocument.absracts, if for an abstract we are in the
	 * correct field (specified in TaggedDocument.abstracts.tags) and then it
	 * saves up to maximum character length specified in TaggedDocument.abstracts.lengths.
	 * 
	 * The 'ELSE' abstract tag is a special case that will be filled with any tag that 
	 * is not added to an existing abstract. 
	 * 
	 * TaggedDocument should be sub-classed and this method overwritten if you want
	 * to save abstracts in a different manner, e.g. saving the first paragraph.
	 * @param text - the text to be saved
	 * @param tag - the tag that this text came from
	 */
	protected void saveToAbstract(String text, String tag) {
		if (tag == null) return;
		
		if (abstractName2Index.containsKey(tag)) {
			int i = abstractName2Index.get(tag);
			final int maxAbstractLength = abstractlengths[i];
			final int currentAbstractLength = abstracts[i].length();
			final int textLength = text.length();
			if (currentAbstractLength<maxAbstractLength) 
			{					
				if (currentAbstractLength + textLength < maxAbstractLength)
				{
					abstracts[i].append(' ');
					abstracts[i].append(text);
				}
				else
				{
					abstracts[i].append(' ');
					abstracts[i].append(text.substring(0, maxAbstractLength - currentAbstractLength));
				}
			}
		} else {
			if (elseAbstractSpecialTag != -1) {
				final int maxAbstractLength = abstractlengths[elseAbstractSpecialTag];
				final int currentAbstractLength = abstracts[elseAbstractSpecialTag].length();
				final int textLength = text.length();
				if (currentAbstractLength<maxAbstractLength) 
				{					
					if (currentAbstractLength + textLength < maxAbstractLength)
					{
						abstracts[elseAbstractSpecialTag].append(' ');
						abstracts[elseAbstractSpecialTag].append(text);
					}
					else
					{
						abstracts[elseAbstractSpecialTag].append(' ');
						abstracts[elseAbstractSpecialTag].append(text.substring(0, maxAbstractLength - currentAbstractLength));
					}
				}
			}
		}
	}
	
	/** 
	 * Returns the fields in which the current term appears in.
	 * @return HashSet a hashset containing the fields that the current
	 *		 term appears in.
	 */
	public Set<String> getFields() {
		return htmlStk;
	}
	/**
	 * Indicates whether the tokenizer has reached the end of the 
	 * current document.
	 * @return boolean true if the end of the current document has
	 *		 been reached, otherwise returns false.
	 */
	public boolean endOfDocument() {
		return EOD && ! currentTokenStream.hasNext();
	}
	
	/**
	 * The encountered tag, which must be a final tag is matched with the tag on
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
			int _counter = 0;
			int x = stk.search(tag);
			while (!stk.empty() & _counter < x) {
				_counter++;
				stk.pop();
			}
		}
	}
	
	/** The maximum number of digits that are allowed in valid terms. */
	protected static final int maxNumOfDigitsPerTerm = 4;
	
	/** 
	 * The maximum number of consecutive same letters or digits that are 
	 * allowed in valid terms.
	 */
	protected static final int maxNumOfSameConseqLettersPerTerm = 3;
	
	/**
	 * Checks whether a term is shorter than the maximum allowed length,
	 * and whether a term does not have many numerical digits or many 
	 * consecutive same digits or letters.
	 * @param s String the term to check if it is valid. 
	 * @return String the term if it is valid, otherwise it returns null.
	 */
	public static String check(String s) {
		//if the s is null
		//or if it is longer than a specified length
		if (s == null)
			return null;
		s = s.trim();
		final int length = s.length();
		if (length == 0 || length > tokenMaximumLength)
			return null;
		
		int counter = 0;
		int counterdigit = 0;
		int ch = -1;
		int chNew = -1;
		for(int i=0;i<length;i++)
		{
			chNew = s.charAt(i);
			if (chNew >= 48 && chNew <= 57)
				counterdigit++;
			if (ch == chNew)
				counter++;
			else
				counter = 1;
			ch = chNew;
			/* if it contains more than 3 consequtive same 
			 * letters (or digits), or more than 4 digits, 
			 * then discard the term. 
			 */
			if (counter > maxNumOfSameConseqLettersPerTerm ||
				counterdigit > maxNumOfDigitsPerTerm)
				return null;
		}
		return s;
	}

	/** Allows access to a named property of the Document. Examples might be URL, filename etc.
	  * @param name Name of the property. It is suggested, but not required that this name
	  * should not be case insensitive.
	  * @since 1.1.0 */
	public String getProperty(String name)
	{
		return properties.get(name.toLowerCase());
	}
	
	/** Allows a named property to be added to the Document. Examples might be URL, filename etc.
	  * @param name Name of the property. It is suggested, but not required that this name
	  * should not be case insensitive.
	  * @param value The value of the property
	  * @since 1.1.0 */
	public void setProperty(String name, String value)
	{
		properties.put(name.toLowerCase(),value);
	}

    /** Returns the underlying map of all the properties defined by this Document.
	  * @since 1.1.0 */	
	public Map<String,String> getAllProperties()
	{
		return properties;
	}

	/**
	 * Static method which dumps a document to System.out
	 * @param args A filename to parse
	 */
	public static void main(String args[])
	{
		if (args.length == 0)
		{
			logger.error("ERROR: Please specify a test file on the command line");
			return;
		}
		Document d = generateDocumentFromFile(args[0]);
		if (d !=  null)
			dumpDocument(d);
	}

	/** instantiates a TREC document from a file */
	public static Document generateDocumentFromFile(final String filename)
	{
		BufferedReader b = null;
		try{
			b = new BufferedReader(new FileReader(filename));
		} catch (IOException ioe) {
			logger.error("ERROR: Problem opening TRECDocument test file : "+ ioe);
			logger.error("Exiting ...");
			ioe.printStackTrace();
		}
		return new TaggedDocument(b, null, new EnglishTokeniser());
	}
	
	/**
	 * Dumps a document to stdout
	 * @param d a Document object
	 */
	public static void dumpDocument(final Document d)
	{
		int terms = 0;
		while(! d.endOfDocument() )
		{
			String t = d.getNextTerm();
			if (t == null)
				continue;
			terms++;
			System.out.print("term: "+ t);
			System.out.print("; fields = {");
			Set<String> fields = d.getFields();
			java.util.Iterator<String> f = fields.iterator();
			if (f.hasNext())
				System.out.print((f.next()));
			while(f.hasNext())
			{
				System.out.print(","+(f.next()));
			}
			System.out.println("}");
		}
		System.out.println("terms: "+terms);
	}

}
