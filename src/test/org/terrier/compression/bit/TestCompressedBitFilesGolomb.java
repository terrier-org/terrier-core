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
 * The Original Code is TestCompressedBitFiles.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.compression.bit;

import static org.junit.Assert.assertEquals;
import gnu.trove.TByteArrayList;
import gnu.trove.TLongArrayList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.terrier.structures.FilePosition;
import org.terrier.utility.io.RandomDataInputMemory;


@SuppressWarnings({"resource"})
@RunWith(Suite.class)
@SuiteClasses({
	TestCompressedBitFilesGolomb.TestCompressedBitFiles_Streams.class,
	TestCompressedBitFilesGolomb.TestCompressedBitFiles_BitByteStreams.class,
	TestCompressedBitFilesGolomb.TestCompressedBitFiles_Specific1.class,
	TestCompressedBitFilesGolomb.TestCompressedBitFiles_Specific3.class,	
	TestCompressedBitFilesGolomb.TestCompressedBitFiles_BitFileBuffered.class,
	TestCompressedBitFilesGolomb.TestCompressedBitFiles_BitFileBufferedSmallBuffer.class,
	TestCompressedBitFilesGolomb.TestCompressedBitFiles_BitFileInMemory.class,
	TestCompressedBitFilesGolomb.TestCompressedBitFiles_BitFileInMemoryLarge.class,
	TestCompressedBitFilesGolomb.TestCompressedBitFiles_BitFileBuffered_RandomDataInputMemory.class
})

/** Ensures that Bit implementations perform as expected */
public class TestCompressedBitFilesGolomb  {


	
	/** Tests come specific cases */
//	public static class TestCompressedBitFiles_Specific2
//	{
//		int initial_bitoffset = 0;
//		ArrayList<int[]> IDS = new ArrayList<int[]>();
//		ArrayList<BitFilePosition> startOffsets = new ArrayList<BitFilePosition>();
//		byte[] bytes;
//		long byteOffset;
//		byte bitOffset;
//		
//		@Before public void writeOut() throws IOException {
//			TIntArrayList ids = new TIntArrayList();
//			BufferedReader br = Files.openFileReader("/users/craigm/src/tr3/linksList");
//			String line = null;
//			
//			
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			NullOutputStream nos = new NullOutputStream();
//			BitOutputStream bo_null = new BitOutputStream(nos);
//			DirectInvertedDocidOnlyOuptutStream dios = new DirectInvertedDocidOnlyOuptutStream(bo_null);
//			BitOutputStream bo = new BitOutputStream(baos);
//			if (initial_bitoffset > 0)
//				bo.writeBinary(initial_bitoffset, 0);
//			//int docid = 0;
//			while((line = br.readLine())!= null)
//			{
//				String[] parts = line.split("\\s+");
//				for(String p : parts)
//					ids.add(Integer.parseInt(p));
//				int[] _tmp = ids.toNativeArray();
//				byteOffset = bo.getByteOffset();
//				bitOffset = bo.getBitOffset();
//				BitFilePosition bp = new FilePosition(byteOffset, bitOffset);
//				startOffsets.add(bp);
//				//System.err.println(_tmp.length + "@{"+byteOffset+","+bitOffset+"}");
//				
//				List<Posting> postingList = new ArrayList<Posting>();
//				IterablePosting ip = new ArrayOfIdsIterablePosting(_tmp);
//				while(ip.next() != IterablePosting.EOL)
//				{
//					postingList.add(ip.asWritablePosting());
//				}
//				BitIndexPointer diosPointer = dios.writePostings(postingList.iterator());
//				
//				IDS.add(_tmp);
//				ids.clear();
//				int previous = -1;
//				for(int i : _tmp)
//				{
//					bo.writeGolomb(i - previous);
//					previous = i;
//				}
//				
//				assertEquals(byteOffset, diosPointer.getOffset());
//				assertEquals(bitOffset, diosPointer.getOffsetBits());
//				assertEquals(_tmp.length, diosPointer.getNumberOfEntries());
//				
//				//System.err.println("startoffset="+ bp.toString() + " dios_pointer="+ diosPointer.toString());
//				
//				//docid++;
//			}		
//			bo.close();
//			bytes = baos.toByteArray();
//		}
//		
//		@Test public void testBitInputStream() throws IOException
//		{
//			testBitIn(new BitInputStream(new ByteArrayInputStream(bytes)));
//		}
//		
////		@Test public void testBitFileBuffered() throws IOException
////		{
////			testBitIn(new BitFileBuffered(new RandomDataInputMemory(bytes)).readReset(0l, (byte)0));
////		}
//		
//		protected void testBitIn(BitIn bi) throws IOException
//		{
//			if (initial_bitoffset > 0)
//				bi.skipBits(initial_bitoffset);			
//			for(int i=0;i<IDS.size();i++)
//			{
//				int[] postings = IDS.get(i);
//				BitFilePosition pos = startOffsets.get(i);
//				assertEquals(pos.getOffset(), bi.getByteOffset());
//				assertEquals(pos.getOffsetBits(), bi.getBitOffset());
//				int id = -1;
//				for(int j=0;j<postings.length;j++)
//				{
//					int target = postings[j];
//					id = bi.readGolomb()+ id;
//					assertEquals("At index "+i,target, id);
//				}
//			}
//		}
//		
//	}
	
	public static class TestCompressedBitFiles_Specific1
	{
		//int initial_bitoffset = 1;
		int initial_bitoffset = 4;
		int[] IDS = new int[]{
			//170071, 170252, 171338, 171694, 171869, 172403, 173109, 187999, 188632, 195940, 196297, 197560, 207292, 207645, 211041, 212136, 215169, 215714, 311055, 369393, 403591, 434066, 434242, 434419, 434593, 434773, 434947, 435125, 435305, 435483, 435661, 435749, 436005, 436176, 436359, 436561, 436704, 436885, 437191, 437250, 8369819, 8548046,
			1255, 1431, 1606, 1781, 1952, 2167, 2346, 2527, 2706, 2884, 3068, 7432, 8527, 10021, 14986, 277146, 582195, 582714, 879259, 7847927, 7848266,
		};
		int b = 594129;
		byte[] bytes;
		long byteOffset;
		byte bitOffset;
		@Before public void writeOut() throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BitOutputStream bo = new BitOutputStream(baos);
			bo.writeBinary(initial_bitoffset, 0);
			int previous = -1;
			for(int i : IDS)
			{
				bo.writeGolomb(i - previous, b);
				previous = i;
			}
			byteOffset = bo.getByteOffset();
			bitOffset = bo.getBitOffset();
			System.err.println(IDS.length + "@{"+byteOffset+","+bitOffset+"}");
			//bo.writeUnary(10);
			bo.close();
			bytes = baos.toByteArray();
		}

		
		protected void testBitIn(BitIn bi) throws IOException
		{
			int id=-1;
			bi.skipBits(initial_bitoffset);
			for(int i : IDS)
			{
				id = bi.readGolomb(b) + id;
				assertEquals(i, id);
			}
			assertEquals(byteOffset, bi.getByteOffset());
			assertEquals(bitOffset, bi.getBitOffset());
		}
		
		@Test public void testBitInputStream() throws IOException
		{
			testBitIn(new BitInputStream(new ByteArrayInputStream(bytes)));
		}
		
		@Test public void testBitFileBuffered() throws IOException
		{
			testBitIn(new BitFileBuffered(new RandomDataInputMemory(bytes)).readReset(0l, (byte)0));
		}
	}
	
	
	public static abstract class TestCompressedBitFiles_Basic
	{
		final int[] testNumbers = new int[]{1, 20, 2, 40, 30, 2, 9, 1, 100, 200, 40};
		int b = 28;
		
		public TestCompressedBitFiles_Basic(){}
		
		protected abstract BitOut getBitOut() throws Exception;
		protected abstract BitIn getBitIn() throws Exception;
		
		@Test public void testBit() throws Exception
		{
			BitOut out = getBitOut();
			
			FilePosition [] startOffsets = new FilePosition[testNumbers.length];
			
			for(int i=0;i<testNumbers.length;i++)
			{
				startOffsets[i] = new FilePosition(out.getByteOffset(), out.getBitOffset());
				if (i %2 == 0)
					out.writeGolomb(testNumbers[i], b);
				else
					out.writeUnary(testNumbers[i]);
				//System.err.println(i+ " wrote " + testNumbers[i] + " started @" + startOffsets[i] + " finished @{" + out.getByteOffset()  + "," + out.getBitOffset() + "}" );
			}
			//System.err.println("For "+testNumbers.length+"numbers, file length is {"  + out.getByteOffset()  + "," + out.getBitOffset() + "}" );
			out.close();
			
			BitIn in = getBitIn();
			for(int i=0;i<testNumbers.length;i++)
			{
				//System.err.println("i="+i + " start offset is " + in.getByteOffset() + "," + in.getBitOffset());
				assertEquals(startOffsets[i].getOffset(), in.getByteOffset());
				assertEquals(startOffsets[i].getOffsetBits(), in.getBitOffset());
				int number = (i %2 == 0) ? in.readGolomb(b) : in.readUnary();
				assertEquals( ( (i %2 == 0) ? "readGolomb()" : "readUnary()") + " at position "+i, testNumbers[i], number);
				//System.err.println("i="+ i + " end offset is " + in.getByteOffset() + "," + in.getBitOffset());
			}
			in.close();
			//System.err.println("compressed form has size " + baos.size() + " bytes");
			
			//now check the skipping (bytes & bits) works as expected
			//for each number, open the file, skip forward to the start offset
			//that the number was written at
			for(int j=0;j<testNumbers.length;j++)
			{
				in = new DebuggingBitIn( getBitIn() );
				System.err.println(j);
				in.skipBytes(startOffsets[j].getOffset());
				//skipping bytes resets the bitoffset
				assertEquals((byte)0, in.getBitOffset());
				in.skipBits(startOffsets[j].getOffsetBits());
				assertEquals(startOffsets[j].getOffset(), in.getByteOffset());
				assertEquals(startOffsets[j].getOffsetBits(), in.getBitOffset());
				for(int i=j;i<testNumbers.length;i++)
				{
					//System.err.println("i="+i + " start offset is " + in.getByteOffset() + "," + in.getBitOffset());
					assertEquals(startOffsets[i].getOffset(), in.getByteOffset());
					assertEquals(startOffsets[i].getOffsetBits(), in.getBitOffset());
					int number = (i %2 == 0) ? in.readGolomb(b) : in.readUnary();
					assertEquals(testNumbers[i], number);
					//System.err.println("i="+ i + " end offset is " + in.getByteOffset() + "," + in.getBitOffset());
				}
				in.close();
			}
			
			//now check the skipping (bits) works as expected
			//for each number, open the file, skip forward to the start offset
			//that the number was written at
			for(int j=0;j<testNumbers.length;j++)
			{
				in = getBitIn();
				System.err.println(j);
				
				in.skipBits((int)startOffsets[j].getOffset() * 8 + startOffsets[j].getOffsetBits());
				assertEquals(startOffsets[j].getOffset(), in.getByteOffset());
				assertEquals(startOffsets[j].getOffsetBits(), in.getBitOffset());
				for(int i=j;i<testNumbers.length;i++)
				{
					//System.err.println("i="+i + " start offset is " + in.getByteOffset() + "," + in.getBitOffset());
					assertEquals(startOffsets[i].getOffset(), in.getByteOffset());
					assertEquals(startOffsets[i].getOffsetBits(), in.getBitOffset());
					int number = (i %2 == 0) ? in.readGolomb(b) : in.readUnary();
					assertEquals(testNumbers[i], number);
					//System.err.println("i="+ i + " end offset is " + in.getByteOffset() + "," + in.getBitOffset());
				}
				in.close();
			}
		}
	}
	
	public static class TestCompressedBitFiles_Streams extends TestCompressedBitFiles_Basic
	{
		public TestCompressedBitFiles_Streams(){}
		ByteArrayOutputStream baos;
		protected BitOut getBitOut() throws Exception
		{
			baos = new ByteArrayOutputStream();
			return new BitOutputStream(baos);
		}
		
		protected BitIn getBitIn() throws Exception
		{
			return new BitInputStream(new ByteArrayInputStream(baos.toByteArray()));
		}
	}
	
	public static class TestCompressedBitFiles_BitByteStreams extends TestCompressedBitFiles_Basic
	{
		public TestCompressedBitFiles_BitByteStreams(){}
		ByteArrayOutputStream baos;
		protected BitOut getBitOut() throws Exception
		{
			baos = new ByteArrayOutputStream();
			return new BitByteOutputStream(baos);
		}
		
		protected BitIn getBitIn() throws Exception
		{
			return new BitInputStream(new ByteArrayInputStream(baos.toByteArray()));
		}
	}
	
	public static abstract class TestCompressedBitFiles_OnFile extends TestCompressedBitFiles_Basic
	{
		@Rule
	    public TemporaryFolder tmpfolder = new TemporaryFolder();

		String filename = null;
		public TestCompressedBitFiles_OnFile() {}
		
		protected BitOut getBitOut() throws Exception
		{
			return new BitOutputStream(filename = tmpfolder.newFile("test.bf").toString());
		}
		
	}
	
	public static class TestCompressedBitFiles_BitFileBuffered extends TestCompressedBitFiles_OnFile
	{
		public TestCompressedBitFiles_BitFileBuffered(){}
		
		protected BitOut getBitOut() throws Exception
		{
			return new BitOutputStream(filename = tmpfolder.newFile("test.bf").toString());
		}
		
		protected BitIn getBitIn() throws Exception
		{
			return new BitFileBuffered(filename).readReset((long)0, (byte)0, new File(filename).length()-1, (byte)7);
		}
	}
	
	public static class TestCompressedBitFiles_BitFileBufferedSmallBuffer extends TestCompressedBitFiles_OnFile
	{
		public TestCompressedBitFiles_BitFileBufferedSmallBuffer(){}
		
		protected BitOut getBitOut() throws Exception
		{
			return new BitOutputStream(filename = tmpfolder.newFile("test.bf").toString());
		}
		
		protected BitIn getBitIn() throws Exception
		{
			return new BitFileBuffered(new File(filename), 10).readReset((long)0, (byte)0, new File(filename).length()-1, (byte)7);
		}
	}
	
	public static class TestCompressedBitFiles_BitFileInMemory extends TestCompressedBitFiles_OnFile
	{
		public TestCompressedBitFiles_BitFileInMemory(){}
				
		protected BitIn getBitIn() throws Exception
		{
			return new BitFileInMemory(filename).readReset((long)0, (byte)0, new File(filename).length()-1, (byte)7);
		}
	}

	public static class TestCompressedBitFiles_BitFileInMemoryLarge extends TestCompressedBitFiles_OnFile
	{
		public TestCompressedBitFiles_BitFileInMemoryLarge(){}
				
		protected BitIn getBitIn() throws Exception
		{
			return new BitFileInMemoryLarge(filename).readReset((long)0, (byte)0, new File(filename).length()-1, (byte)7);
		}
	}
	
	public static class TestCompressedBitFiles_BitFileBuffered_RandomDataInputMemory extends TestCompressedBitFiles_OnFile
	{
		public TestCompressedBitFiles_BitFileBuffered_RandomDataInputMemory(){}
				
		protected BitIn getBitIn() throws Exception
		{
			return new BitFileBuffered(new RandomDataInputMemory(filename)).readReset((long)0, (byte)0, new File(filename).length()-1, (byte)7);
		}
	}
	
	public static class TestCompressedBitFiles_Specific3
	{
		//int initial_bitoffset = 1;
		int initial_bitoffset = 0;
		int[][] IDS = new int[][]{
			new int[]{100,200,300,400}, new int[]{ 0, 1, 2, 4, 8}, new int[]{ 0, 8, 10}
		};
		int b = 59;
		byte[] bytes;
		TLongArrayList byteOffsets = new TLongArrayList();
		TByteArrayList bitOffsets = new TByteArrayList();

		@Before public void writeOut() throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BitOutputStream bo = new BitOutputStream(baos);
			bo.writeBinary(initial_bitoffset, 0);
			for(int[] ids: IDS)
			{
				int previous = -1;
				byteOffsets.add(bo.getByteOffset());
				bitOffsets.add(bo.getBitOffset());
				//long byteOffset = bo.getByteOffset();
				//byte bitOffset = bo.getBitOffset();
				//System.err.println(ids.length + "@{"+byteOffset+","+bitOffset+"}");
				for(int i : ids)
				{
					bo.writeGolomb(i - previous, b);
					previous = i;
				}
			}			
			bo.close();
			bytes = baos.toByteArray();
		}

		
		protected void testBitIn(BitIn bi) throws IOException
		{
			int index = 0;
			bi.skipBits(initial_bitoffset);
			for(int[] ids : IDS)
			{
				assertEquals(byteOffsets.get(index), bi.getByteOffset());
				assertEquals(bitOffsets.get(index), bi.getBitOffset());
				int id=-1;
				for (int i : ids)
				{
					id = bi.readGolomb(b) + id;
					assertEquals(i, id);
				}
				index++;
			}			
		}
		
		protected void testBitInSkip(int offset, BitIn bi) throws IOException
		{
			bi.skipBits(initial_bitoffset);
			
			bi.skipBytes(byteOffsets.get(offset));
			bi.skipBits(bitOffsets.get(offset));
			
			for(int index=offset;index<IDS.length;index++)
			{
				int[] ids = IDS[index];
				assertEquals(byteOffsets.get(index), bi.getByteOffset());
				assertEquals(bitOffsets.get(index), bi.getBitOffset());
				int id=-1;
				for (int i : ids)
				{
					id = bi.readGolomb(b) + id;
					assertEquals(i, id);
				}
			}			
		}
		
		
		@Test public void testBitInputStream() throws IOException
		{
			testBitIn(new BitInputStream(new ByteArrayInputStream(bytes)));
			testBitInSkip(1, new BitInputStream(new ByteArrayInputStream(bytes)));
			testBitInSkip(2, new BitInputStream(new ByteArrayInputStream(bytes)));
		}
		
		@Test public void testBitFileBuffered() throws IOException
		{
			testBitIn(new BitFileBuffered(new RandomDataInputMemory(bytes)).readReset(0l, (byte)0));
			testBitInSkip(1, new BitFileBuffered(new RandomDataInputMemory(bytes)).readReset(0l, (byte)0));
			testBitInSkip(2, new BitFileBuffered(new RandomDataInputMemory(bytes)).readReset(0l, (byte)0));
		}
	}
	
}


