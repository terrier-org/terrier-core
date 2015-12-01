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
 * The Original Code is BasicShak.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */

package org.terrier.integer.tests;

import org.terrier.compression.integer.codec.LemireNewPFDVBCodec;
import org.terrier.compression.integer.codec.UnaryCodec;
import org.terrier.structures.integer.IntegerCodecCompressionConfiguration;
import org.terrier.tests.BasicShakespeareEndToEndTest;

public class BasicShak extends BasicShakespeareEndToEndTest {

	public BasicShak()
	{
		indexingOptions.add("-Dindexing.direct.compression.configuration="+IntegerCodecCompressionConfiguration.class.getName());
		indexingOptions.add("-Dindexing.inverted.compression.configuration="+IntegerCodecCompressionConfiguration.class.getName());
		indexingOptions.add("-Dcompression.integer.chunk.size=1024");
		indexingOptions.add("-Dcompression.direct.integer.ids.codec="+ LemireNewPFDVBCodec.class.getName());
		indexingOptions.add("-Dcompression.direct.integer.tfs.codec="+UnaryCodec.class.getName());
		indexingOptions.add("-Dcompression.inverted.integer.ids.codec="+LemireNewPFDVBCodec.class.getName());
		indexingOptions.add("-Dcompression.inverted.integer.tfs.codec="+UnaryCodec.class.getName());
	}
	
}
