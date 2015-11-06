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
 * The Original Code is MemByteBuffer.java.
 *
 * The Original Code is Copyright (C) 2004-2014 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.realtime.compression;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.terrier.compression.bit.BitIn;
import org.terrier.compression.bit.BitOut;

/*
 * TODO: ByteBuffer - work in progress.
 */

/**
 * Buffer backed by a {@link java.nio.ByteBuffer}.
 * Supports gamma and unary encoding of strictly positive integers.
 * 
 * @since 4.0
 */
public class MemByteBuffer implements BitOut, BitIn {

    private ByteBuffer buffer;
    private int        pointer;
    private int        in, out;
    private int        inptr, outptr;

    /** Constructor (1 byte). */
    public MemByteBuffer() {
        buffer = ByteBuffer.allocate(4);
        pointer = 0;
        in = out = 0;
        inptr = outptr = 0;
    }

    /** Copy constructor. */
    public MemByteBuffer(MemByteBuffer copy) {
        this.buffer = buffer.duplicate();
        pointer = 0;
    }

    /* Grow buffer. */
    private void grow() {
        System.err.printf("ByteBuffer grow()\n");
        ByteBuffer tmp = ByteBuffer.allocate(buffer.capacity() * 2);
        System.err.printf("From %d to %d\n", buffer.capacity(), tmp.capacity());
        tmp.put(buffer.array());
        buffer = tmp;
    }

    /* Write four bytes to buffer. */
    private void writeInt(int i) {
        if (buffer.remaining() >= 4) {
            buffer.put((byte) (i >> 24));
            buffer.put((byte) (i >> 16));
            buffer.put((byte) (i >> 8));
            buffer.put((byte) (i));
        } else {
            grow();
            writeInt(i);
        }
    }

    /* Read four bytes from buffer. */
    private int readInt() {

        // Check for empty buffer and EOF.
        if (buffer.position() == 0 || pointer >= buffer.position())
            return -1;

        int i = 0;
        i |= buffer.get(pointer++) & 0xFF;
        i <<= 8;
        i |= buffer.get(pointer++) & 0xFF;
        i <<= 8;
        i |= buffer.get(pointer++) & 0xFF;
        i <<= 8;
        i |= buffer.get(pointer++) & 0xFF;
        return i;
    }

    /**
     * Unary encoding.
     * Write n-1 0s, then 1.
     */
    public int writeUnary(int n) {

        System.err.printf("\n\nwriteUnary(%d)\n", n);
        System.err.printf("%d ints in the buffer\n", Integer.bitCount(in));

        // Check buffer size.
        int bits = 32 - inptr; // Bits available.
        System.err.printf("%d bits available (need %d).\n", bits, n);

        if (bits == 0) { // Flush buffer.
            System.err.printf("Buffer full - flushing to ByteBuffer.\n");
            System.err.printf("ByteBuffer - %d.\n", buffer.remaining());
            writeInt(in);
            System.err.printf("ByteBuffer - %d.\n", buffer.remaining());
            in = inptr = 0;
        }

        if (inptr + n <= 32) { // Space for n.
            System.err.printf("Space for n.\n");
            // Left shift by n.
            in <<= n;
            // OR with "00000000000000000000000000000001".
            in |= 0x00000001;
            // Bump pointer.
            inptr += n;
            // Return n bits written (n).
            return n;
        } else { // No space for n.
            System.err.printf("No space for n.\n");
            System.err.printf("Writing %d bits.\n", bits);
            in <<= bits;
            System.err.printf("Buffer full - flushing to ByteBuffer.\n");
            System.err.printf("ByteBuffer - %d.\n", buffer.remaining());
            writeInt(in);
            System.err.printf("ByteBuffer - %d.\n", buffer.remaining());
            in = inptr = 0;
            System.err.printf("Writing %d remaining bits.\n", n - bits);
            writeUnary(n - bits);
            return n;
        }
    }

    /** Gamma encoding. */
    public int writeGamma(int n) {
        writeInt(n);
        return n;
    }

    /** Unary decoding. */
    public int readUnary() {
        System.err.printf("\n\nreadUnary()\n");

        // Initialise the buffer.
        if (pointer == 0) {
            System.err.printf("Init buffer.\n");
            out = readInt();
            if (out == -1)
                return -1;
        }

        // outptr.
        int bits = 32 - outptr;
        int ints = Integer.bitCount(out);
        System.err.printf("outptr: %d\n", outptr);
        System.err.printf("%d ints in buffer (and %d bits to go)\n", ints, bits);

        if (ints == 0 && bits > 0) {
            int n;
            out = readInt();
            if (out == -1) {
                n = -1;
                return -1;
            }
            outptr = 0;
            n = bits + readUnary();
            System.err.printf("got: %d\n", n);
            return n;
        }

        System.err.printf("%d zeros then a 1 = %d.\n", Integer.numberOfLeadingZeros(out), Integer.numberOfLeadingZeros(out) + 1);

        int n = Integer.numberOfLeadingZeros(out) + 1;
        out <<= n;
        outptr += n;
        return n;

        //        // Initialise the buffer.
        //       
        //        
        //        if (ints < 1 && outptr == 0) {
        //            System.err.printf("Buffer empty: loading from ByteBuffer.\n");
        //            out = readInt();
        //            if (out == -1)
        //                return -1;
        //            ints = Integer.bitCount(out);
        //            System.err.printf("%d ints in buffer\n", ints);
        //        }

        // Decode ints in buffer.

    }

    /** Gamma decoding. */
    public int readGamma() {
        return readInt();
    }

    /*
     * Only gamma and unary compression implemented...
     */

    /** Not Implemented. */
    public int readBinary(int len) throws IOException {
        return 0;
    }

    /** Not Implemented. */
    public int readMinimalBinary(int b) throws IOException {
        return 0;
    }

    /** Not Implemented. */
    public int readMinimalBinaryZero(int b) throws IOException {
        return 0;
    }

    /** Not Implemented. */
    public int readGolomb(int b) throws IOException {
        return 0;
    }

    /** Not Implemented. */
    public int readSkewedGolomb(int b) throws IOException {
        return 0;
    }

    /** Not Implemented. */
    public int readDelta() throws IOException {
        return 0;
    }

    /** Not Implemented. */
    public void readInterpolativeCoding(int[] data, int offset, int len, int lo, int hi) throws IOException {
    }

    /** Not Implemented. */
    public void skipBits(int len) throws IOException {
    }

    /** Not Implemented. */
    public void skipBytes(long len) throws IOException {
    }

    /** Not Implemented. */
    public void align() throws IOException {
    }

    /** Not Implemented. */
    public long getByteOffset() {
        return 0;
    }

    /** Not Implemented. */
    public byte getBitOffset() {
        return 0;
    }

    /** Not Implemented. */
    public int writeBinary(int len, int x) throws IOException {
        return 0;
    }

    /** Not Implemented. */
    public int writeInterpolativeCode(int[] data, int offset, int len, int lo, int hi) throws IOException {
        return 0;
    }

    /** Not Implemented. */
    public int writeSkewedGolomb(int x, int b) throws IOException {
        return 0;
    }

    /** Not Implemented. */
    public int writeGolomb(int x, int b) throws IOException {
        return 0;
    }

    /** Not Implemented. */
    public int writeMinimalBinary(int x, int b) throws IOException {
        return 0;
    }

    /** Not Implemented. */
    public int writeDelta(int x) throws IOException {
        return 0;
    }

    /** Not Implemented. */
    public int writeInt(int x, int len) throws IOException {
        return 0;
    }

    /** Not Implemented. */
    public void close() throws IOException {
    }
}
