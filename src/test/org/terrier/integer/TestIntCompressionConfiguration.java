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
 * The Original Code is TestIntCompressionConfiguration.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craig.macdonald@glasgow.ac.uk>
 */

package org.terrier.integer;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.terrier.indexing.TestCompressionConfig;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;
import org.terrier.structures.integer.IntegerCodecCompressionConfiguration;
import org.terrier.utility.ApplicationSetup;

public class TestIntCompressionConfiguration extends TestCompressionConfig {

	@Override
	protected CompressionConfiguration getConfig(String structure, String[] fieldNames,
			int hasBlocks, int maxBlocks) {
		return new IntegerCodecCompressionConfiguration(structure, fieldNames, hasBlocks, maxBlocks);
	}

	
	protected void toTest(String ids, String tfs) throws IOException
	{
		System.err.println("ids="+ids + " tfs="+tfs);
		ApplicationSetup.setProperty("compression.direct.integer.ids.codec", ids);
		ApplicationSetup.setProperty("compression.direct.integer.tfs.codec", tfs);
		ApplicationSetup.setProperty("compression.inverted.integer.ids.codec", ids);
		ApplicationSetup.setProperty("compression.inverted.integer.tfs.codec", tfs);
		super.testSimple();
	}
//	
//	protected void toTest(Class<? extends IntegerCodecFactory> ids, Class<? extends IntegerCodecFactory> tfs) throws IOException
//	{
//		System.err.println("ids="+ids.getName() + " tfs="+tfs.getName());
//		//ApplicationSetup.getProperty("indexing."+prefix+".lemire.integercodec-class"
//		ApplicationSetup.setProperty("compression.integer.ids.codec.factory", ids.getName());
//		ApplicationSetup.setProperty("compression.integer.tfs.codec.factory", tfs.getName());
//		super.testSimple();
//	}
	
	@Test public void testAll() throws IOException
	{
		String[] factories = new String[]{
				"LemireCodec(Composition,FastPFOR,VariableByte)",
				"LemireFastPFORVBCodec",
				"LemireNewPFDVBCodec",
				"LemireOptPFDVBCodec",
				"LemireFORVBCodec",
				"LemireSimple16Codec",
				"KamikazePForDeltaVBCodec",
				"GammaCodec",
				"UnaryCodec",
				"VIntCodec"
		};
		for(String a : factories)
		{
			for(String b : factories)
			{
				toTest(a,b);
			}
		}
		//Class<? extends IntegerCodecFactory>[] c = new Class[]{LemireCodecFactory.class, GammaCodecFactory.class, VIntCodecFactory.class, UnaryCodecFactory.class,SimpleLemireCodecFactoryVB.class, SimpleLemireCodecFactoryS16.class};
		//for(Class<? extends IntegerCodecFactory> a : c)
		//	for (Class<? extends IntegerCodecFactory> b: c)
		//		toTest(a,b);	
	}
	
//	
//	@Test public void testLemireLemire() throws IOException
//	{
//		toTest(LemireCodecFactory.class,LemireCodecFactory.class);	
//	}
//	
//	
//	@Test public void testGammaLemire() throws IOException
//	{
//		toTest(GammaCodecFactory.class,LemireCodecFactory.class);	
//	}
//	
//	@Test public void testLemireGamma() throws IOException
//	{
//		toTest(LemireCodecFactory.class,GammaCodecFactory.class);	
//	}
//	
//	@Test public void testVIntVint() throws IOException
//	{
//		toTest(VIntCodecFactory.class,VIntCodecFactory.class);	
//	}
//	
//	@Test public void testGammaGamma() throws IOException
//	{
//		toTest(GammaCodecFactory.class,GammaCodecFactory.class);	
//	}
//	
//	@Test public void testUnaryUnary() throws IOException
//	{
//		toTest(UnaryCodecFactory.class,UnaryCodecFactory.class);	
//	}
//	
//	@Test public void testSimpleVB() throws IOException
//	{
//		toTest(SimpleLemireCodecFactoryVB.class,SimpleLemireCodecFactoryVB.class);	
//	}
//	
//	@Test public void testSimple16() throws IOException
//	{
//		toTest(SimpleLemireCodecFactoryS16.class,SimpleLemireCodecFactoryS16.class);	
//	}
	
	@Ignore @Override @Test public void testSimple()
	{
		
	}
}
