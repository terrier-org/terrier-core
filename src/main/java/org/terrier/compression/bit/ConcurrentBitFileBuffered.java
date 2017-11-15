package org.terrier.compression.bit;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import org.terrier.compression.bit.BitFileBuffered;
import org.terrier.compression.bit.BitIn;
import org.terrier.utility.io.RandomDataInput;

public class ConcurrentBitFileBuffered extends BitFileBuffered {

	public static ConcurrentBitFileBuffered of(BitFileBuffered old) {
		return new ConcurrentBitFileBuffered(old.file);
	}
	
	public ConcurrentBitFileBuffered(File _file, int bufSize) {
		super(_file, bufSize);
	}

	public ConcurrentBitFileBuffered(File _file) {
		super(_file);
	}

	public ConcurrentBitFileBuffered(RandomDataInput f) {
		super(f);
	}

	public ConcurrentBitFileBuffered(String filename, int bufSize) {
		super(filename, bufSize);
	}

	public ConcurrentBitFileBuffered(String filename) {
		super(filename);
	}
	
	@Override
	public BitIn readReset(long startByteOffset, byte startBitOffset, long endByteOffset, byte endBitOffset) {
		final long range = endByteOffset - startByteOffset + (long)1;
		return new ConcurrentBitInBuffered(file,startByteOffset,startBitOffset, range < buffer_size ? (int)range : buffer_size);
	}
	
	
	@Override
	public BitIn readReset(long startByteOffset, byte startBitOffset) 
	{
		final long actualBufferSize = (startByteOffset + buffer_size) > fileSize 
			? (fileSize - startByteOffset) 
			: buffer_size;
		return new ConcurrentBitInBuffered(file,startByteOffset,startBitOffset, (int)actualBufferSize);
	}
	
	protected static class ConcurrentBitInBuffered extends BitInBuffered {
		
		public ConcurrentBitInBuffered(RandomDataInput file, long startByteOffset, byte _bitOffset, int _bufLength)
		{
			super();
			this.offset = startByteOffset;
			this.bitOffset= _bitOffset;
			this.parentFile = file;
			this.size = _bufLength;
			try{
				synchronized (parentFile) {
					parentFile.seek(startByteOffset);
					inBuffer = new byte[size];
					parentFile.readFully(inBuffer);
					readByteOffset = 0;
					byteRead = inBuffer[readByteOffset];
				}				
			}catch(IOException ioe){
				logger.error("Input/Output exception while reading from a random access file. Stack trace follows", ioe);
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
					synchronized (parentFile) {
						parentFile.seek(offset);
						try{
							parentFile.readFully(inBuffer);
						} catch (EOFException eofe) { /* ignore this */}
					}					
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
					synchronized (parentFile) {
						parentFile.seek(offset); // we skip the first bytes of the next block
						readByteOffset = 0;
						try{
							parentFile.readFully(inBuffer);
						} catch (EOFException eofe) { /* ignore this */}
					}					
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
				synchronized (parentFile) {
					parentFile.seek(offset); // we skip the first bytes of the next block
					readByteOffset = 0;
					try{
						parentFile.readFully(inBuffer);
					} catch (EOFException eofe) { /* ignore this */}
				}				
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
