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
 * The Original Code is ConcurrentBitFileBuffered.java.
 *
 * The Original Code is Copyright (C) 2017-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Craig Macdonald
 */
package org.terrier.compression.bit;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.io.RandomAccessFile;

import org.terrier.compression.bit.BitFileBuffered;
import org.terrier.compression.bit.BitIn;
import org.terrier.utility.io.RandomDataInput;

public class BitFileChannel extends BitFileBuffered {

	public static BitFileChannel of(BitFileBuffered old) {
		return new BitFileChannel(old.file);
	}

	FileChannel _channel;
	
	public BitFileChannel(File _file, int bufSize) {
		super(_file, bufSize);
		_channel = ((RandomAccessFile)file).getChannel();
	}

	public BitFileChannel(File _file) {
		super(_file);
		_channel = ((RandomAccessFile)file).getChannel();
	}

	public BitFileChannel(RandomDataInput f) {
		super(f);
		_channel = ((RandomAccessFile)file).getChannel();
	}

	public BitFileChannel(String filename, int bufSize) {
		super(filename, bufSize);
		_channel = ((RandomAccessFile)file).getChannel();
	}

	public BitFileChannel(String filename) {
		super(filename);
		_channel = ((RandomAccessFile)file).getChannel();
	}
	
	@Override
	public BitIn readReset(long startByteOffset, byte startBitOffset, long endByteOffset, byte endBitOffset) {
		final long range = endByteOffset - startByteOffset + (long)1;
		return new FileChannelBitInBuffered(_channel,startByteOffset,startBitOffset, range < buffer_size ? (int)range : buffer_size);
	}
	
	
	@Override
	public BitIn readReset(long startByteOffset, byte startBitOffset) 
	{
		final long actualBufferSize = (startByteOffset + buffer_size) > fileSize 
			? (fileSize - startByteOffset) 
			: buffer_size;
		assert actualBufferSize > 0;
		return new FileChannelBitInBuffered(_channel,
			startByteOffset,
			startBitOffset, 
			(int)actualBufferSize);
	}
	
	protected static class FileChannelBitInBuffered extends BitInBuffered {
		
		protected FileChannel channel; 
		protected ByteBuffer buf;
		public FileChannelBitInBuffered(FileChannel file, long startByteOffset, byte _bitOffset, int _bufLength)
		{
			super();
			this.offset = startByteOffset;
			this.bitOffset= _bitOffset;
			this.channel = file;
			this.size = _bufLength;
			//System.out.println("allocating buffer of " + size);
			inBuffer = new byte[size];
			this.buf = ByteBuffer.wrap(inBuffer);
			//System.out.println("buffer has limit " + buf.limit());
			
			try{
				readFully(channel, buf, offset);
				readByteOffset = 0;
				byteRead = inBuffer[readByteOffset];
			}catch(IOException ioe){
				logger.error("Input/Output exception while reading from a random access file. Stack trace follows", ioe);
			}
		}

		static void readFully(FileChannel file, ByteBuffer buf, long offset) throws IOException {
			buf.clear();
			int n = buf.remaining();
			assert n > 0;

			int toRead = n;
			while (n > 0) {
				int amt = file.read(buf, offset);
				if (amt == -1) {
					int read = toRead - n;
					throw new EOFException("reached end of stream after reading "
							+ read + " bytes; " + toRead + " bytes expected");
				} else {
					n -= amt;
					offset += amt;
				}
			}
		}
		
		@Override
		protected void incrByte()
		{
			try{		
				readByteOffset++;
				offset++;
				if(readByteOffset == size)
				{					
					readByteOffset=0;
					readFully(channel, buf, offset);
				}
				byteRead = inBuffer[readByteOffset];
			}catch(IOException ioe){
				logger.error("Input/Output exception while reading from a random access file. Stack trace follows", ioe);
			}
		}	
	
		@Override
		protected void incrByte(int i)
		{
			try{
				//System.out.println("skypping");
				offset += i;
				readByteOffset+=i;
				if( readByteOffset >= size ) // we go to the next block  -- we skip only the begin of the block
				{
					readByteOffset=0;
					readFully(channel, buf, offset);
				}
				byteRead = inBuffer[readByteOffset];
			}catch(IOException ioe){
				logger.error("Input/Output exception while reading from a random access file. Stack trace follows", ioe);
			}
		}
		
		@Override
		public void skipBytes(long len) throws IOException
		{
			if (readByteOffset + len >= inBuffer.length)
			{
				offset += len;
				readByteOffset=0;
				readFully(channel, buf, offset);				
				byteRead = inBuffer[readByteOffset];
			}
			else
			{
				offset += len;
				readByteOffset += len;
				bitOffset = 0;
				byteRead = inBuffer[readByteOffset];
			}			
		}
	}

}
