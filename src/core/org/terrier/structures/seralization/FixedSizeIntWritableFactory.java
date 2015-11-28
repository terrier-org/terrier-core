/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
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
 * The Original Code is FixedSizeIntWritableFactory.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures.seralization;
import org.apache.hadoop.io.IntWritable;
/** Makes an IntWritable, which has a fixed Writable size of 4 bytes.
 * @since 3.0
 * @author Craig Macdonald
 */
public class FixedSizeIntWritableFactory implements FixedSizeWriteableFactory<IntWritable>
{
	/** Return size of IntWritable, namely 4 bytes */
    public int getSize()
	{
		return 4;	
	}

    /** Makes a new IntWritable */
	public IntWritable newInstance() { 
		return new IntWritable();
	}
}
