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
 * The Original Code is ByteOutputStream.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 */

package org.terrier.compression.integer;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.io.WritableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.compression.bit.BitOut;
import org.terrier.utility.Files;

/**
 * Bytewise counterpart of {@link BitOut}
 * 
 * @author Matteo Catena
 * @since 4.0
 */
public class ByteOutputStream implements ByteOut {
    /** the logger for this class */
    protected static final Logger logger = LoggerFactory.getLogger(ByteOutputStream.class);	
	
	private long byteOffset;

	private DataOutputStream dos;
	
	/**
	 * Initialises the variables in the stream. Used internally.
	 * @param file 
	 * @throws IOException 
	 */
	private void init(OutputStream file) throws IOException{
		
		dos = new DataOutputStream(file);
		byteOffset = 0;
	}	
	
	/** sleep for the specified ms */ 
	private static void sleep(long millis)
	{
		try {Thread.sleep(millis); } catch (Exception e) {/* ignore */}
	}	
	
	public ByteOutputStream(String filename) throws IOException {
		
		OutputStream file;
		try{
			file = Files.writeFileStream(filename);
			init(file);
		} catch(FileNotFoundException fnfe) {
			final String dir = Files.getParent(filename);
			logger.warn("Could not open new ByteOutputStream because it alleged file could not be found.", fnfe);
			if (logger.isDebugEnabled())
				logger.debug("File.canRead()="+Files.canWrite(filename)+ "Dir.exists()=" +Files.exists(dir) 
					+ " Dir.canWrite()="+Files.canWrite(dir) +" Dir.contentsSize="+Files.list(dir).length);
			sleep(1000);
			if (logger.isDebugEnabled())
				logger.debug("File.canRead()="+Files.canWrite(filename)+ "Dir.exists()=" +Files.exists(dir) 
					+ " Dir.canWrite()="+Files.canWrite(dir)+" Dir.contentsSize="+Files.list(dir).length);
			logger.warn("Retrying to write BitOutputStream.");
			init(Files.writeFileStream(filename));
			logger.info("Previous warning can be ignored, BitOutputStream "+filename+" has opened successfully");
		}
		
	}		
	
	public ByteOutputStream(OutputStream os) throws IOException {
		
		init(os);
	}	
	
	
	@Override
	public void close() throws IOException {
		
		dos.close();
	}
		
	@Override
	public long getByteOffset() {

		return byteOffset;
	}

	@Override
	public int writeVInt(int x) throws IOException {
		 
		int bytes = WritableUtils.getVIntSize(x);		
		WritableUtils.writeVInt(dos, x);
		byteOffset += bytes;
		
		return bytes;
	}

	@Override
	public int write(byte[] arr, int off, int len) throws IOException {
			
		dos.write(arr, off, len);
		byteOffset += len;
		
		return len;
	}

	@Override
	public int writeVLong(long x) throws IOException {
		
		int bytes = WritableUtils.getVIntSize(x);		
		WritableUtils.writeVLong(dos, x);
		byteOffset += bytes;
		
		return bytes;
	}

	@Override
	public int getVSize(long x) {

		return WritableUtils.getVIntSize(x);
	}
}