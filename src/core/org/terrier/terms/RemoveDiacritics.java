/*
 * Terrier - Terabyte Retriever
 * Webpage: http://terrier.org
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.uk
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
 * The Original Code is RemoveDiacritics.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Saúl Vargas <Saul.Vargas{a.}glasgow.ac.uk> (original author)
 *   
 */
package org.terrier.terms;

import java.text.Normalizer;
import java.text.Normalizer.Form;

/**
 * Removes diacritics in letters.
 *
 * @author Saúl Vargas (Saul.Vargas@glasgow.ac.uk)
 */
public class RemoveDiacritics implements TermPipeline {

    protected final TermPipeline next;

    public RemoveDiacritics(TermPipeline next) {
        this.next = next;
    }

    @Override
    public void processTerm(String t) {

        if (t == null) {
            return;
        }

        next.processTerm(Normalizer.normalize(t, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", ""));
    }

    @Override
    public boolean reset() {
        return next.reset();
    }
}
