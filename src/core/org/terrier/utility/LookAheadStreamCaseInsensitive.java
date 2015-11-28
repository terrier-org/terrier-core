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
 * The Original Code is LookAheadStreamCaseInsensitive.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.utility;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
/** Version of LookAheadStream that is case-insensitive. This version assumes that the characters
  * that form the end-of-stream markers are single bytes, from the default character set. Use with care. 
 * @author Craig Macdonald
 * @since 2.1
 */
public class LookAheadStreamCaseInsensitive extends LookAheadStream
{
	/** Create a LookAheadStream that is case insensitive. The default character set
	  * is used to parse the marker into bytes.
	  * @param parent The InputStream to wrap
	  * @param endMarker the marker at which to give EOF
	  */
	public LookAheadStreamCaseInsensitive(InputStream parent, String endMarker) {
		super(parent, endMarker.toUpperCase());
	}
	
	/** Create a LookAheadStream that is case insensitive. The default character set
	  * is used to parse the marker into bytes.
	  * @param parent The InputStream to wrap
	  * @param endMarker the marker at which to give EOF
	  * @param encoding name for encoding
	  */
	public LookAheadStreamCaseInsensitive(InputStream parent, String endMarker, String encoding) throws UnsupportedEncodingException {
		super(parent, endMarker.toUpperCase(), encoding);
	}
	
    /**
     * Read a character from the parent stream, first checking that
     * it doesn't form part of the end marker.
     * @return int the code of the read character, or -1 if the end of
     *       the stream has been reached.
     * @throws IOException if there is any error while reading from the stream.
     */
	@Override
    public int read() throws IOException {
        if (EOF)
            return -1;
        if (BufLen > 0) {
            BufLen--;
            return Buffer[BufIndex++];
        }
        int c = -1;
        boolean keepReading = true;
        while (keepReading) {
            if ((c = ParentStream.read()) == -1)
            {
                EOF = true;
                return -1;
            }
            char cc = Character.toUpperCase((char)c);
            if (((int)cc) == EndMarker[BufLen]) {
                Buffer[BufLen++] = c;
                if (BufLen == MarkerLen) {
                    EOF = true;
                    return -1;
                }
            } else {
                Buffer[BufLen++] = c;
                BufIndex = 0;
                //keepReading = false;
                break;
            }
        }
        BufLen--;
        return Buffer[BufIndex++];
    }
}
