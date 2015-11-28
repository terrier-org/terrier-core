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
 * The Original Code is RandomDataInputMemory.java
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.utility.io;
 
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
 
import org.terrier.utility.Files;
 
/** Implements a RandomDataInput backed by a byte[] rather than a file.
 * @since 3.0
 * @author Craig Macdonald 
 */
public class RandomDataInputMemory extends DataInputStream implements RandomDataInput, Cloneable  {
     
    static int MAX_INDIVIDUAL_BUFFER_SIZE = Integer.MAX_VALUE - 8;
    //static int MAX_INDIVIDUAL_BUFFER_SIZE = 8;
    // 1024 * 1024; //
    static interface Seekable extends Closeable
    {
        void seek(long _pos);       
        long getFilePointer();              
        long length();
    }
     
    /** seekable implementation which uses multiple byte arrays */
    private static class MultiSeeakableByteArrayInputStream extends InputStream implements Seekable
    {   
        private static long MAX_INDIVIDUAL_BUFFER_SIZE = RandomDataInputMemory.MAX_INDIVIDUAL_BUFFER_SIZE;
        /** the global position of the file pointer (measured in bytes) */
        long pos = 0;
        /** the length of the input file (measured in bytes) */
        long length;
        /** the raw file contents */
        byte[][] data;
        private int part_id=0;
        private int part_off=0;
        private byte[] current_sector;
        /** the parts of the file size */
        final long individual_buffer_size = MAX_INDIVIDUAL_BUFFER_SIZE;
         
        public MultiSeeakableByteArrayInputStream(byte[][] _data, long _pos, long _length)
        {
            super();
            data = _data;
            pos = _pos;
            length = _length;
             
            part_id = (int)(this.pos / individual_buffer_size);
            part_off = (int)(this.pos % individual_buffer_size);
            current_sector = data[part_id];            
        }
         
        public MultiSeeakableByteArrayInputStream(DataInputStream in, long length) throws IOException
        {
            super();
            assert(length > 0);
 
            this.length = length;
            int parts = (int) (this.length / this.individual_buffer_size);
            if (this.length % this.individual_buffer_size != 0)
                parts++;
            this.data = new byte[parts][];
 
            long remainingLength = length;
            for (int i = 0; i < parts; ++i) {
                int bytesToRead = (int) Math.min(this.individual_buffer_size, remainingLength);
                System.err.println("Reading from disk to memory " + bytesToRead + " bytes");
                data[i] = new byte[bytesToRead];
                in.readFully(data[i]);
                remainingLength -= bytesToRead;
                System.err.println("array " + i + " length = " + bytesToRead + " bytes");
            }
             
            current_sector = data[0];
             
            in.close();
        }
 
        public final void seek(long _pos)
        {
            assert(pos >= 0);
             
            this.pos = _pos;
             
            part_id = (int)(this.pos / individual_buffer_size);
            part_off = (int)(this.pos % individual_buffer_size);
            current_sector = data[part_id];
        }
         
        public final long getFilePointer()
        {
            return this.pos;
        }
         
        public final long length() {
            return this.length;
        }
         
        @Override
        public final int read() throws IOException
        {
            if (pos >= length)
                return -1;
             
            if (part_off >= individual_buffer_size) {
                 
                current_sector = data[++part_id];
                part_off=0;
            }
             
            byte b = current_sector[part_off];
            pos++;
            part_off++;
             
            return b & 0xff;
        }
         
        @Override
        public final int read(final byte[] b) throws IOException
        {
            return read(b, 0, b.length);
        }
         
        @Override
        public final int read(final byte[] b, final int off, final int len) throws IOException
        {
            if (pos >= length) return -1;
                         
            if (part_off >= individual_buffer_size) {
                 
                current_sector = data[++part_id];
                part_off=0;
            }
             
            int read = Math.min(current_sector.length - part_off, len);
            System.arraycopy(current_sector, part_off, b, off, read);
            pos += read;
            part_off += read;
         
            return read;
        }       
 
        @Override
        public final long skip(long n) throws IOException
        {
            seek(pos + n);
            return n;
        }
 
        @Override
        public void close() throws IOException { }
         
    }
     
    /** class which allows seeking over a ByteArrayInputStream */
    private static class SeeakableByteArrayInputStream extends ByteArrayInputStream implements Seekable
    {
        public SeeakableByteArrayInputStream(byte[] buf) 
        {
            super(buf);
        }
         
        public void seek(long _pos)
        {
            super.pos = (int)_pos;
        }
         
        public long getFilePointer()
        {
            return (long)super.pos;
        }
         
        public long length() {
            return (long)count;
        }
         
        public byte[] getBuffer()
        {
            return super.buf;
        }
    }
     
    /** input stream to use */
    protected Seekable buf;
     
    /** decide which seekable implementatino to use.
     * @param in a DataInputStream to read
     * @param length how many bytes to expect from in
     * @return an InputStream which also implements Seekable
     * @throws IOException if an IO problem occurs
     */
    private static InputStream getSeekable(DataInputStream in, long length) throws IOException
    {
        if (length < MAX_INDIVIDUAL_BUFFER_SIZE)
        {
            byte[] buf = new byte[(int)length];
            in.readFully(buf);
            in.close();
            return new SeeakableByteArrayInputStream(buf);
        } 
        return new MultiSeeakableByteArrayInputStream(in, length);
    }
     
    protected RandomDataInputMemory(InputStream seekable) throws IOException
    {
        super(seekable);
        buf = (Seekable)super.in;
    }
     
    /** Construct a new RandomDataInputMemory object, backed by the specified file */
    public RandomDataInputMemory(String filename) throws IOException {
        this(new DataInputStream(Files.openFileStream(filename)), Files.length(filename));
    }
     
    /** Construct a new RandomDataInputMemory object, backed by the specified buffer */
    public RandomDataInputMemory(DataInputStream in, long length) throws IOException {
        super(getSeekable(in, length));
        buf = (Seekable)super.in;
    }
     
    /** Construct a new RandomDataInputMemory object, backed by the specified buffer */
    public RandomDataInputMemory(byte[] b) {
        super(new SeeakableByteArrayInputStream(b));
        buf = (Seekable)super.in;
    }
     
    /** {@inheritDoc} */
    public long getFilePointer() throws IOException {
        return buf.getFilePointer();
    }
 
    /** {@inheritDoc} */
    public long length() throws IOException {
        return buf.length();
    }
 
    /** {@inheritDoc} */
    public void seek(long _pos) throws IOException {
        buf.seek(_pos);
    }
 
    /** {@inheritDoc} */
    public void close() throws IOException {
        buf.close();
    }
     
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        InputStream in = null;
        if (buf instanceof MultiSeeakableByteArrayInputStream)
        {
            in = new MultiSeeakableByteArrayInputStream( 
                    ((MultiSeeakableByteArrayInputStream)buf).data, 
                    ((MultiSeeakableByteArrayInputStream)buf).pos,
                    ((MultiSeeakableByteArrayInputStream)buf).length);
        }
        else if (buf instanceof SeeakableByteArrayInputStream) 
        {
            in = new SeeakableByteArrayInputStream(((SeeakableByteArrayInputStream)buf).getBuffer());
        }           
        try{
            return new RandomDataInputMemory(in);
        } catch (IOException ioe) {
            throw new CloneNotSupportedException(ioe.getMessage());
        }
    }
 
}
