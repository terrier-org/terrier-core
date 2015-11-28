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
 * The Original is in 'POIDocument.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.indexing;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import org.apache.poi.POITextExtractor;
import org.apache.poi.hdgf.extractor.VisioTextExtractor;
import org.apache.poi.hpbf.extractor.PublisherTextExtractor;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.terrier.indexing.tokenisation.Tokeniser;

/** Represents Microsoft Office documents, which are parsed by the Apache POI library 
 * @since 3.6 
 * @author Craig Macdonald
 */
public class POIDocument extends FileDocument {

	
	
	/** Constructs a new MSWordDocument object for the file represented by
	 * 	docStream.
	 */
	public POIDocument(String filename, InputStream docStream, Tokeniser tokeniser)
	{
		super(filename, docStream, tokeniser);
	}
	/** 
	 * Constructs a new MSWordDocument object for the file represented by
	 * 	docStream.
	 * @param docStream
	 * @param docProperties
	 * @param tok
	 */
	public POIDocument(InputStream docStream,
			Map<String, String> docProperties, Tokeniser tok) {
		super(docStream, docProperties, tok);
	}
	
	protected POITextExtractor getExtractor(String filename, InputStream docStream) throws IOException
	{
		//Word .doc: 
		if (filename.endsWith(".doc"))
		{
			return new WordExtractor(docStream);
		}
		//Word .docx:
		if (filename.endsWith(".docx"))
		{
			return new XWPFWordExtractor(new XWPFDocument(docStream));
		}
		//Powertpoint .ppt: 
		if (filename.endsWith(".ppt"))
		{
			return new PowerPointExtractor(docStream);
		}
		//Powertpoint .pptx: 
		if (filename.endsWith(".pptx"))
		{
			return new XSLFPowerPointExtractor(new XMLSlideShow(docStream));
		}
		//Publisher .pub: 
		if (filename.endsWith(".pub"))
		{
			return new PublisherTextExtractor(docStream);
		}
		//Excel: .xls:
		if (filename.endsWith(".xls"))
		{
			return new ExcelExtractor(new POIFSFileSystem(docStream));
		}
		//Excel: .xlsx:
		if (filename.endsWith(".xlsx"))
		{
			return new org.apache.poi.xssf.extractor.XSSFExcelExtractor(new XSSFWorkbook(docStream));
		}
		//Visio: .vsd:
		if (filename.endsWith(".vsd"))
		{
			return new VisioTextExtractor(docStream);
		}
		return null;
	}
	
	/** Converts the docStream InputStream parameter into a Reader which contains
	 *  plain text, and from which terms can be obtained. 
	 *  On failure, returns null and sets EOD to true, so no terms can be read from
	 *  this object.
	 */
	protected Reader getReader(InputStream docStream)
	{
		try{	
			POITextExtractor pte = getExtractor(super.filename, docStream);
			final String text = pte.getText();
			return new StringReader(text);
		} catch (Exception e) {
			logger.warn("WARNING: Problem converting POI doc " + super.filename,e);
			EOD = true;
			return null;
		}
	}
	
}
