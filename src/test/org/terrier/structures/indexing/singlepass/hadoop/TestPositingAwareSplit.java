/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.uk
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
 * The Original Code is TestPositingAwareSplit.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 */
package org.terrier.structures.indexing.singlepass.hadoop;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.junit.Test;

/** Check that PostitionAwareSplit works as expected.
 * @since 3.0
 * @author Craig Macdonald
 */
@SuppressWarnings("deprecation")
public class TestPositingAwareSplit {

	private byte[] toBytes(Writable w) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		w.write(dos);
		return baos.toByteArray();
	}
	
	
	@Test public void testBasic() throws Exception
	{
		final String path = "/tmp.txt";
		final long start = 0;
		final long length = 100;
		final String[] hosts = new String[]{"localhost"};
		final int index = 5;
		FileSplit f1 = new FileSplit(new Path(path), start, length, hosts);
		PositionAwareSplit<FileSplit> pf1 = new PositionAwareSplit<FileSplit>(f1, index);
		final byte[] b = toBytes(pf1);
		PositionAwareSplit<FileSplit> pf2 = new PositionAwareSplit<FileSplit>();
		pf2.readFields(new DataInputStream(new ByteArrayInputStream(b)));
		assertEquals(index, pf2.getSplitIndex());
		assertEquals(length, pf2.getLength());
		assertEquals(f1.getPath(), pf2.getSplit().getPath());
		assertEquals(start, pf2.getSplit().getStart());
		assertEquals(length, pf2.getSplit().getLength());
		//dont compare hosts, as FileSplit doesnt serialize these
	}
}
