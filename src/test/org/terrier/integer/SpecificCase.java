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
 * The Original Code is SpecificCase.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */

package org.terrier.integer;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import org.junit.Test;
import org.terrier.compression.integer.ByteIn;
import org.terrier.compression.integer.ByteInputStream;
import org.terrier.compression.integer.codec.util.BitInCodec;

public class SpecificCase {

	@Test public void testUnarySpecific() throws Exception
	{
		byte[] b = new byte[]{28, 16, -92, 37, -98, -63, -65, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2};
		ByteIn bytes = new ByteInputStream(new DataInputStream(new ByteArrayInputStream(b)));
		BitInCodec bit = new BitInCodec();
		bit.setup(bytes, b.length);
		for(int i=0;i<1571;i++)
		{
			int num = bit.readUnary();
			assertTrue(num >= 0);
			System.err.println(String.valueOf(i) + " = " + num);
		}
		bit.close();
	}
	
}
