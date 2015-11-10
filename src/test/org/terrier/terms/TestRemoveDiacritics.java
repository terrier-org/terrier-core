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
 * The Original Code is TestRemoveDiacritics.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Saúl Vargas <Saul.Vargas{a.}glasgow.ac.uk> (original author)
 *   
 */
package org.terrier.terms;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Test for RemoveDiacritics.
 *
 * @author Saúl Vargas (Saul.Vargas@glasgow.ac.uk)
 */
public class TestRemoveDiacritics {
    
    public TestRemoveDiacritics() {
    }
    
    @Test
    public void test() {
        String[][] pairs = new String[][]{
            new String[]{"proboscídeo", "proboscideo"}, // Spanish
            new String[]{"jalapeño", "jalapeno"}, // Spanish (not actually a diacritic, but commonly processed as such)
            new String[]{"éclair", "eclair"}, // French
            new String[]{"România", "Romania"}, // Romanian
            new String[]{"Erdős", "Erdos"}, // Hungarian
            new String[]{"Việt", "Viet"}, // Vietnamese
            new String[]{"Dvořák", "Dvorak"}, // Czech
            new String[]{"McDonald's", "McDonald's"}, // English example with no diacritics
        };

        TermPipelineAccessor tpa = new org.terrier.terms.BaseTermPipelineAccessor(new String[]{"RemoveDiacritics"});

        for (String[] pair : pairs) {
            pair[0] = tpa.pipelineTerm(pair[0]);
            Assert.assertEquals(pair[0], pair[1]);
        }
    }
}
