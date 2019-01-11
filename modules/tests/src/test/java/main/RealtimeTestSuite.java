
package main;

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
 * The Original Code is TerrierDefaultTestSuite.java
 *
 * The Original Code is Copyright (C) 2004-2019 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.terrier.realtime.incremental.TestIncremental;
import org.terrier.realtime.memory.TestMemoryIndex;
import org.terrier.realtime.memory.TestMemoryIndexer;
import org.terrier.realtime.memory.TestMemoryInvertedIndex;
import org.terrier.realtime.memory.TestMemoryLexicon;
import org.terrier.realtime.memory.TestMemoryMetaIndex;
import org.terrier.realtime.memory.fields.TestMemoryFieldsIndex;
import org.terrier.realtime.multi.TestMultiIndex;
/** This class defines the active JUnit test classes for Terrier
 * @since 3.0
 * @author Craig Macdonald */
@RunWith(Suite.class)
@SuiteClasses({

        // memory and incremental index tests
        TestMemoryFieldsIndex.class,
        TestMemoryIndexer.class,
        TestMemoryInvertedIndex.class,
        TestMemoryIndex.class,
        TestMemoryLexicon.class,
        TestMemoryMetaIndex.class,
        TestMultiIndex.class,
        TestIncremental.class
})
public class RealtimeTestSuite{}
