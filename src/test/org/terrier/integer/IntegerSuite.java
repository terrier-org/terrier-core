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
 * The Original Code is IntegerSuite.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */

package org.terrier.integer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.terrier.integer.structure.TestIntegerCoding;
import org.terrier.integer.structure.TestIntegerCoding2;
import org.terrier.integer.structure.TestNext;
import org.terrier.integer.tests.BasicShak;
import org.terrier.integer.tests.BasicShakFastPFORRecompress;
import org.terrier.integer.tests.BasicShakNullRecompress;
import org.terrier.integer.tests.BasicShakSmallChunk;
import org.terrier.integer.tests.BlockShak;
import org.terrier.integer.tests.BlockShakSmallMaxBlockSize;


/** This class defines the active JUnit test classes for Terrier
 * @since 3.0
 * @author Craig Macdonald */
@RunWith(Suite.class)
@SuiteClasses({
	
	//.integer
	TestByteInByteOut.class,
	TestIntCompressionConfiguration.class,
	TestIntegerCoding.class,
	TestIntegerCoding2.class,
	TestNext.class,
	BasicShak.class,
	BasicShakFastPFORRecompress.class,
	BasicShakNullRecompress.class,
	BasicShakSmallChunk.class,
	BlockShak.class,
	BlockShakSmallMaxBlockSize.class	
	
})
public class IntegerSuite {}
