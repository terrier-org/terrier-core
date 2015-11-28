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
 * The Original Code is AddDirectIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */

package org.terrier.realtime.restructure;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.terrier.structures.BasicLexiconEntry;
import org.terrier.structures.FSOMapFileLexiconOutputStream;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.indexing.LexiconBuilder;
import org.terrier.structures.seralization.FixedSizeTextFactory;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;

/**
 * Facilitates the generation of a single lexicon structure on disk from a multi-index
 * @author Richard McCreadie
 * @since 4.0
 *
 */
public class CreateSingleLexicon {

	public void rewriteLexicon(String path, String prefix) {
		try {
			
			IndexOnDisk oldIndex = Index.createIndex(path, prefix);
			IndexOnDisk newIndex = Index.createNewIndex(path, prefix+"-tmp");
			
			@SuppressWarnings("unchecked")
			Iterator<Entry<String, LexiconEntry>> lexIn = (Iterator<Entry<String, LexiconEntry>>) oldIndex.getIndexStructureInputStream("lexicon");
			
			FSOMapFileLexiconOutputStream lexOut = new FSOMapFileLexiconOutputStream(
					newIndex, "lexicon", new FixedSizeTextFactory(
							ApplicationSetup.MAX_TERM_LENGTH),
					BasicLexiconEntry.Factory.class);
			
			BasicLexiconEntry.Factory lexiconEntryFactory = new BasicLexiconEntry.Factory();
			
			// write new lexicon
			int termID = 0;
			while (lexIn.hasNext()) {

				// get the next term from the old lexicon
				Entry<String, LexiconEntry> lexTermEntry = lexIn.next();

				// make new lexicon entry
				LexiconEntry newLe = lexiconEntryFactory.newInstance();

				// populate its statistics
				newLe.add(lexTermEntry.getValue());
				
				// Change the termID to one that is strictly increasing
				// This is the only real change that we make to the lexicon
				newLe.setTermId(termID);

				// populate its pointer
				newLe.setPointer(lexTermEntry.getValue());

				// write out to disk
				lexOut.writeNextEntry(lexTermEntry.getKey(), newLe);
				
				termID++;
			}
			IndexUtil.close(lexIn);
			IndexUtil.close(lexOut);
			
			newIndex.getProperties().put("max.term.length",
					String.valueOf(ApplicationSetup.MAX_TERM_LENGTH));
			newIndex.addIndexStructure(
					// structureName,className,paramTypes,paramValues
					"lexicon", "org.terrier.structures.FSOMapFileLexicon",
					new String[] { "java.lang.String",
							"org.terrier.structures.IndexOnDisk" }, new String[] {
							"structureName", "index" });
			newIndex.addIndexStructure(
					// structureName,className,paramTypes,paramValues
					"lexicon-keyfactory",
					"org.terrier.structures.seralization.FixedSizeTextFactory",
					new String[] { "java.lang.String" },
					new String[] { "${max.term.length}" });
			newIndex.addIndexStructureInputStream(
					// structureName,className,paramTypes,paramValues
					"lexicon",
					"org.terrier.structures.FSOMapFileLexicon$MapFileLexiconIterator",
					new String[] { "java.lang.String",
							"org.terrier.structures.IndexOnDisk" }, new String[] {
							"structureName", "index" });
			newIndex.addIndexStructureInputStream(
					// structureName,className,paramTypes,paramValues
					"lexicon-entry",
					"org.terrier.structures.FSOMapFileLexicon$MapFileLexiconEntryIterator",
					new String[] { "java.lang.String",
							"org.terrier.structures.IndexOnDisk" }, new String[] {
							"structureName", "index" });
			
			// optimise lexicon
			LexiconBuilder.optimise(newIndex, "lexicon");

			// final write
			newIndex.flush();
			
			// close
			newIndex.close();
			oldIndex.close();
			
			// move over the new lexicon files
			File indexdir = new File(path);
			for (File file : indexdir.listFiles()) {
				if (!file.isDirectory()) {
					if (file.getName().startsWith(prefix)) {
						// one of the files we are interested in
						if (file.getName().startsWith(prefix+"-tmp")) {
							// is one of the new files
							if (!file.getName().endsWith(".properties")) {
								file.renameTo(new File(file.getPath().replace("-tmp", "")));
								//System.err.println("Rename: "+file.getName()+" to "+file.getPath().replace("-tmp", ""));
							} else {
								file.delete();
								//System.err.println("Delete: "+file.getName());
							}
						} else {
							if (file.getName().startsWith(prefix+".")) {
								if (file.getName().contains("lexicon")) {
									file.delete();
									//System.err.println("Delete: "+file.getName());
								}
							}
						}
					}
				}
			}
			
			// update the index properties
			InputStream propertyStream = Files
					.openFileStream(path+ApplicationSetup.FILE_SEPARATOR+prefix+".properties");
			Properties properties =  new Properties();
			properties.load(propertyStream);
			propertyStream.close();
			
			properties.setProperty("index.lexicon.termids","aligned");
			OutputStream out = Files.writeFileStream(path+ApplicationSetup.FILE_SEPARATOR+prefix+".properties");
			properties.store(out, "Index(" + path + "," + prefix + ")");
			out.close();
			
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		CreateSingleLexicon adi = new CreateSingleLexicon();
		adi.rewriteLexicon(args[0], args[1]);
	}
	
}
