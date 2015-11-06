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
 * The Original Code is MemBitSetBuffer.java.
 *
 * The Original Code is Copyright (C) 2004-2014 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.realtime.compression;

import java.io.IOException;
import java.util.BitSet;

import org.apache.commons.lang.StringUtils;
import org.terrier.compression.bit.BitIn;
import org.terrier.compression.bit.BitOut;

/**
 * Buffer backed by a {@link java.util.BitSet}.
 * Supports gamma and unary encoding of strictly positive integers.
 * 
 * @since 4.0
 */
public class MemBitSetBuffer implements BitOut, BitIn {

    private BitSet buffer;
    private int    pointer;
    private int    position;

    /** Constructor (64 bits). */
    public MemBitSetBuffer() {
        buffer = new BitSet();
        pointer = 1;
        position = 1;
    }

    /** Copy constructor. */
    public MemBitSetBuffer(MemBitSetBuffer copy) {
        this.buffer = (BitSet) copy.buffer.clone();
        this.pointer = copy.pointer;
        this.position = 1;
    }

    /**
     * Unary encoding.
     * Write n-1 1s, then 0.
     */
    public int writeUnary(int n) {
        buffer.set(pointer, pointer + (n - 1), true);
        pointer += n - 1;
        buffer.set(pointer++, false);
        return n;
    }

    /**
     * Gamma encoding.
     * Take n in binary.
     * Pad left nbits-1 0s.
     */
    public int writeGamma(int n) {
        String binary = Integer.toBinaryString(n);
        String gamma = StringUtils.repeat("0", binary.length() - 1) + binary;
        for (int bit = 0; bit < gamma.length(); bit++)
            buffer.set(pointer++, gamma.charAt(bit) == '0' ? false : true);
        return gamma.length();
    }

    /**
     * Unary decoding.
     * Count 1s until 0 seen.
     * Return 1s + 1.
     */
    public int readUnary() {
        int ones = 0;
        while (buffer.get(position) == true) {
            ones++;
            position++;
        }
        position++;
        return ones + 1;
    }

    /**
     * Gamma decoding.
     * Count 0s until 1 seen.
     * From 1 read 0s bits as binary int.
     */
    public int readGamma() {
        int zeros = 0;
        while (buffer.get(position) == false) {
            zeros++;
            position++;
        }
        String binary = "";
        for (int bit = 0; bit <= zeros; bit++)
            binary += buffer.get(position++) == false ? "0" : "1";
        return Integer.parseInt(binary, 2);
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
