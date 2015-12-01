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
 * The Original Code is PDFDocument.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.indexing;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.util.PDFTextStripper;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.utility.Files;
/** 
 * Implements a Document object for reading PDF documents, using <a href="http://pdfbox.apache.org/">Apache PDFBox</a>.
 * @author Craig Macdonald
 */
public class PDFDocument extends FileDocument
{
	static boolean USE_PDF_TITLE = false;
	protected static final Logger logger = LoggerFactory.getLogger(PDFDocument.class);
	/**
	 * Constructs a new PDFDocument, which will convert the docStream
	 * which represents the file to a Document object from which an Indexer
	 * can retrieve a stream of terms.
	 * @param docStream InputStream the input stream that represents the
	 *        the document's file. 
	 */
	public PDFDocument(String filename, InputStream docStream, Tokeniser tokeniser)
	{
		super(filename, docStream, tokeniser);
	}
	/**
	 * Constructs a new PDFDocument
	 * @param docStream
	 * @param docProperties
	 * @param tok
	 */
	public PDFDocument(InputStream docStream,
			Map<String, String> docProperties, Tokeniser tok) {
		super(docStream, docProperties, tok);
	}
	/** 
	 * Constructs a new PDFDocument
	 * @param docReader
	 * @param docProperties
	 * @param tok
	 */
	public PDFDocument(Reader docReader, Map<String, String> docProperties,
			Tokeniser tok) {
		super(docReader, docProperties, tok);
	}
	/** 
	 * Constructs a new PDFDocument
	 * @param filename
	 * @param docReader
	 * @param tok
	 */
	public PDFDocument(String filename, Reader docReader, Tokeniser tok) {
		super(filename, docReader, tok);
	}
	/** 
	 * Returns the reader of text, which is suitable for parsing terms out of,
	 * and which is created by converting the file represented by 
	 * parameter docStream. This method involves running the stream 
	 * through the PDFParser etc provided in the org.pdfbox library.
	 * On error, it returns null, and sets EOD to true, so no terms 
	 * can be read from this document.
	 * @param is the input stream that represents the document's file.
	 * @return Reader a reader that is fed to an indexer.
	 */
	protected Reader getReader(InputStream is)
	{
		
		if ((Files.length(filename)/1048576)>300) {
			logger.info("Skipping document "+filename+" because it's size exceeds 300Mb");
			return new StringReader("");
		}
		
		PDDocument pdfDocument = null;
		Reader rtr = null;
        try
        {
            pdfDocument = PDDocument.load( is );

            if( pdfDocument.isEncrypted() )
            {
                //Just try using the default password and move on
                pdfDocument.decrypt( "" );
            }

            //create a writer where to append the text content.
            StringWriter writer = new StringWriter();
			PDFTextStripper stripper = new PDFTextStripper();
            stripper.writeText( pdfDocument, writer );

			String contents = writer.getBuffer().toString();
			int spaceCount = StringUtils.countMatches(contents, " ");
			for(char badChar : new char[]{
				'\u00A0',
				'\u2029',
				'#'})
			{
				final int count = StringUtils.countMatches(contents, ""+badChar);
				if (count > spaceCount / 2)
				{
					contents = contents.replace(badChar, ' ');
					spaceCount += count;
				}
			}
			rtr = new StringReader(contents);
		
			PDDocumentInformation info = pdfDocument.getDocumentInformation();
            if(info != null && USE_PDF_TITLE) 
            {	
				setProperty("title", info.getTitle() );
			}
			else
			{
				setProperty("title", new java.io.File(super.filename).getName() );
			}
		}
		catch( CryptographyException e )
        {
            throw new RuntimeException( "Error decrypting PDF document: " + e );
        }
        catch( InvalidPasswordException e )
        {
            //they didn't suppply a password and the default of "" was wrong.
            throw new RuntimeException( 
                "Error: The PDF document is encrypted and will not be indexed." );
        }
		catch (Exception e) {
			throw new RuntimeException("Error extracting PDF document",  e);	
		}
        finally
        {
            if( pdfDocument != null )
            {
				try{
                	pdfDocument.close();
				} catch (IOException ioe){}
            }
        }
		return rtr;
	}
}
