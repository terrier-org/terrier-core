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
 * The Original Code is LemireFastPFORVBCodec.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *  Matteo Catena
 *  Craig Macdonald <craig.macdonald@glasgow.ac.uk> 
 */

package org.terrier.compression.integer.codec;

/** FastPFOR, falling back to VariableByte 
 * 
 *  @author Matteo Catena and Craig Macdonald
 *  @since 4.0
 * */
public class LemireFastPFORVBCodec extends LemireCodec {

	public LemireFastPFORVBCodec() throws Exception {
		super(new String[]{"Composition", "FastPFOR", "VariableByte"});
	}

}
