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
 * The Original Code is IndexUtil.java
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
package org.terrier.structures;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.gson.Gson;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.hadoop.io.Writable;
import org.terrier.applications.CLITool;
import org.terrier.applications.CLITool.CLIParsedCLITool;
import org.terrier.querying.IndexRef;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.ArrayUtils;
import org.terrier.utility.Files;

/**
 * Class with handy utilities for use on an Index.
 * 
 * @since 3.0
 */
public class IndexUtil {

	public static class Command extends CLIParsedCLITool {

		@Override
		public String sourcepackage() {
			return CLITool.PLATFORM_MODULE;
		}

		@Override
		public String commandname() {
			return "indexutil";
		}

		@Override
		public String helpsummary() {
			return "utilities for displaying the content of an index";
		}

		@Override
		@SuppressWarnings({ "unchecked", "deprecation" })
		// public int run(String[] args) throws Exception {
		public int run(CommandLine line) throws Exception {
			String[] args = line.getArgs();

			IndexRef iRef = IndexRef.of(ApplicationSetup.TERRIER_INDEX_PATH, ApplicationSetup.TERRIER_INDEX_PREFIX);
			if (line.hasOption("I")) {
				String indexLocation = line.getOptionValue("I");
				iRef = IndexRef.of(indexLocation);
			}

			Index.setIndexLoadingProfileAsRetrieval(false);

			// load the index
			final Index index = IndexFactory.of(iRef);
			if (index == null) {
				System.err.println("Index for ref "+iRef+" not found: " + Index.getLastIndexLoadError());
				return 2;
			}

			// command loop
			if (line.hasOption("printpostingfile")) {
				String structureName = "inverted";
				if (line.hasOption("s"))
					structureName = line.getOptionValue("s"); 
				PostingIndexInputStream bpiis = (PostingIndexInputStream) index
						.getIndexStructureInputStream(structureName);
				bpiis.print();
				bpiis.close();
			} else if (line.hasOption("printterm")) {
				IndexUtil.forceStructure(index, "document", new DocumentIndex() {
					@Override
					public int getNumberOfDocuments() {
						return index.getCollectionStatistics().getNumberOfDocuments();
					}

					@Override
					public int getDocumentLength(int docid) throws IOException {
						return 0;
					}

					@Override
					public DocumentIndexEntry getDocumentEntry(int docid) throws IOException {
						return null;
					}
				});
				String structureName = "lexicon";
				if (line.hasOption("s"))
					structureName = line.getOptionValue("s"); 
				Lexicon<String> lex = (Lexicon<String>) index.getIndexStructure(structureName);
				PostingIndex<?> inv = (PostingIndex<?>) index.getInvertedIndex();
				LexiconEntry le = lex.getLexiconEntry(args[1]);
				IterablePosting ip = inv.getPostings(le);
				while (ip.next() != IterablePosting.EOL) {
					System.out.print(ip.toString());
					System.out.println(" ");
				}
				ip.close();
				lex.close();
				close(inv);
			} else if (line.hasOption("--printPosting")) {
				IndexUtil.forceStructure(index, "document", new DocumentIndex() {

					@Override
					public int getNumberOfDocuments() {
						return index.getCollectionStatistics().getNumberOfDocuments();
					}

					@Override
					public int getDocumentLength(int docid) throws IOException {
						return 0;
					}

					@Override
					public DocumentIndexEntry getDocumentEntry(int docid) throws IOException {
						return null;
					}
				});
				Lexicon<String> lex = index.getLexicon();
				PostingIndex<Pointer> inv = (PostingIndex<Pointer>) index.getInvertedIndex();
				LexiconEntry le = lex.getLexiconEntry(args[1]);
				IterablePosting ip = inv.getPostings(le);
				int targetId = Integer.parseInt(args[2]);
				int foundId = ip.next(targetId);
				if (foundId == targetId) {
					System.out.println(ip.toString());
				} else {
					System.err.println(
							"Docid " + targetId + " not found for term " + args[1] + " (nearest was " + foundId + ")");
				}
				ip.close();
				lex.close();
				close(inv);
			} else if (line.hasOption("--printbitentry")) {
				String structureName = "inverted";
				if (line.hasOption("s"))
					structureName = line.getOptionValue("s"); 
				List<BitIndexPointer> pointerList = (List<BitIndexPointer>) index.getIndexStructure(args[2]);
				PostingIndex<?> bpi = (PostingIndex<?>) index.getIndexStructure(structureName);
				// for every docid on cmdline
				for (String arg: args) {
					BitIndexPointer pointer = pointerList.get(Integer.parseInt(arg));
					if (pointer.getNumberOfEntries() == 0)
						continue;
					System.out.print(arg + " ");
					IterablePosting ip = bpi.getPostings(pointer);
					while (ip.next() != IterablePosting.EOL) {
						System.out.print(ip.toString());
						System.out.print(" ");
					}
					System.out.println();
				}
			} else if (line.hasOption("--printlex")) {
				String structureName = "lexicon";
				if (line.hasOption("s"))
					structureName = line.getOptionValue("s"); 
				LexiconUtil.printLexicon(index, structureName);
			} else if (line.hasOption("--printdocument")) {
				String structureName = "document";
				if (line.hasOption("s"))
					structureName = line.getOptionValue("s"); 
				printDocumentIndex(index, structureName);
			} else if (line.hasOption("--printlist")) {
				String structureName = "document";
				if (line.hasOption("s"))
					structureName = line.getOptionValue("s"); 
				Iterator<? extends Writable> in = (Iterator<? extends Writable>) index
						.getIndexStructureInputStream(structureName);
				while (in.hasNext()) {
					System.out.println(in.next().toString());
				}
				IndexUtil.close(in);
			} else if (line.hasOption("--printlistentry")) {
				String structureName = "document";
				if (line.hasOption("s"))
					structureName = line.getOptionValue("s"); 
				List<? extends Writable> list = (List<? extends Writable>) index.getIndexStructure(structureName);
				for (int argC = 2; argC < args.length; argC++) {
					System.out.println(list.get(Integer.parseInt(args[argC])).toString());
				}
				IndexUtil.close(list);
			} else if (line.hasOption("--printmeta")) {
				boolean json = line.hasOption("j");
				String structureName = "meta";
				if (line.hasOption("s"))
					structureName = line.getOptionValue("s"); 

				if (json)
					printMetaIndexJson(index, structureName);
				else
					printMetaIndex(index, structureName);
			} else {
				System.err.println(MAIN_USAGE);
			}
			index.close();
			return 0;
		}

		@Override
		protected Options getOptions() {
			Options opts = super.getOptions();
			opts.addOption(Option.builder("printmeta").build());
			opts.addOption(Option.builder("printlex").build());
			opts.addOption(Option.builder("printlist").build());
			opts.addOption(Option.builder("printlistentry").build());
			opts.addOption(Option.builder("printbitentry").build());
			opts.addOption(Option.builder("printposting").build());
			opts.addOption(Option.builder("printpostingfile").build());
			opts.addOption(Option.builder("printterm").build());
			opts.addOption(Option.builder("s").longOpt("structure").build());
			opts.addOption(Option.builder("j").longOpt("json").build());

			return opts;
		}
		
	}
	
	
	
	
	private final static String MAIN_USAGE = "Usage: " + IndexUtil.class.getName() + " {--printbitfile|--printlex|--printdocument|--printlist|--printmeta} structureName";
	/** Has some handy utilities for printing various index structures to System.out.
	 * <ul>
	 * <li>--printbitfile - print the bit file with the specified name</li>
	 * <li>--printbitentry - print one entry in a bit posting index.</li>
	 * <li>--printlex - print the entire lexicon</li>
	 * <li>--printdocument - print the document index</li>
	 * <li>--printlist - print the named list (e.g. document index)</li>
	 * <li>--printmeta - print the meta index</li>
	 * </ul>
	 */
	public static void main(String[] args) {
		CLITool.run(Command.class, args);
	}
	
	/** Force the specified object into the structure cache of the specified object, 
	 * as the given structure name
	 * @param index Index to operate on
	 * @param structureName which structure name to use
	 * @param structure which object to put into the structure cache
	 */
	public static void forceStructure(Index index, String structureName, Object structure)
	{
		((IndexOnDisk) index).structureCache.put(structureName, structure);
	}
	
	/** Forces a structure to be reloaded, by removing it from the index's structure cache */
	public static void forceReloadStructure(Index index, String structureName)
	{
		((IndexOnDisk) index).structureCache.remove(structureName);
	}
	
	public static IndexOnDisk reOpenIndex(Index index) throws IOException
	{
		return reOpenIndex((IndexOnDisk) index);
	}
	
	/** Reopen an existing index */
	public static IndexOnDisk reOpenIndex(IndexOnDisk index) throws IOException
	{
		IndexOnDisk rtr = null;
		String path = index.getPath();
		String prefix = index.getPrefix();
		index.close();
		rtr = Index.createIndex(path, prefix);
		return rtr;
	}
	
	/** Returns a list of the structures in the given index */
	public static String[] getStructures(Index index)
	{
		List<String> rtr = new ArrayList<String>();
		for(Object o : index.getProperties().keySet())
		{
			String key = (String)o;
			if (key.matches("index\\..+\\.class"))
			{
				key = key.replaceAll("index.", "");
				key = key.replaceFirst(".class", "");
				rtr.add(key);
			}
		}
		return rtr.toArray(new String[0]);
	}
	
	/** Move an index from one location to another */
	public static void renameIndex(String srcPath, String srcPrefix, String dstPath, String dstPrefix)
		throws IOException
	{
		final String actualPrefix = srcPrefix +'.';
		for (String filename : Files.list(srcPath))
		{
			if (filename.startsWith(actualPrefix))
			{
				final String newFilename = filename.replaceFirst(srcPrefix, dstPrefix);
				if (! Files.rename(srcPath + "/" + filename, dstPath+"/"+ newFilename))
				{
					final String srcExists = Files.exists(srcPath + "/" + filename) ? "exists" : "notexists";
					final String destExists = Files.exists(dstPath+"/"+ newFilename) ? "exists" : "notexists";
					throw new IOException("Rename of index structure file '"+srcPath + "/" + filename+"' ("+srcExists+") to " + 
							"'"+ dstPath+"/"+ newFilename +"' ("+destExists+") failed - likely that source file is still open. " +
							"Possible indexing bug?");
				}
			}
		}
	}
	
	/** Delete an existing index */
	public static void deleteIndex(String path, String prefix)
		throws IOException
	{
		final String actualPrefix = prefix +'.';
		String[] files = Files.list(path);
		if (files == null)
			return;
		for (String filename : files)
		{
			if (filename.startsWith(actualPrefix))
			{
				Files.delete(path + "/" + filename);
			}
		}
	}
	
	/** Print the contents of the document index */
	@SuppressWarnings("unchecked")
	public static void printDocumentIndex(Index index, String structureName) throws IOException
	{
		Iterator<DocumentIndexEntry> iterator = (Iterator<DocumentIndexEntry>)index.getIndexStructureInputStream(structureName);
		int docid =0;
		while(iterator.hasNext())
		{
			DocumentIndexEntry die = iterator.next();
			System.out.println(docid +": " + die.toString());
			docid++;
		}
		close(iterator);
	}
	
	/** Delete the named structure from the specified index.
	 * Deletes files as well.
	 * @param index - index to operate on
	 * @param structureName name of structure to delete
	 * @return true if structure was found and deleted, false otherwise
	 */
	public static boolean deleteStructure(Index index, String structureName) throws IOException
	{
		boolean found = false;
		List<String> toRemove = new ArrayList<String>();
		for(Object o : index.getProperties().keySet())
		{
			String key = (String)o;
			if (key.startsWith("index."+structureName + "."))
			{
				toRemove.add(key);
				found = true;
			}			
		}
		for(String key : toRemove)
			index.getProperties().remove(key);
		
		for(String file : Files.list(((IndexOnDisk) index).getPath()))
		{
			if (file.startsWith(((IndexOnDisk) index).getPrefix() + "." + structureName + "."))
			{
				Files.delete(((IndexOnDisk) index).getPath() + "/" + file);
			}
		}
		return found;
	}
	
	/** Checks the underlying structurecache of the specificed index to see if the 
	 * named index structure is there.
	 * @param index index to examine
	 * @param structureName what structure
	 * @return true if the structure cache contains the item
	 */
	public static boolean isStructureOpen(IndexOnDisk index, String structureName) {
		return index.structureCache.containsKey(structureName);
	}
	
	/** Copies an index structure from one index to another.
	 * @param sourceIndex
	 * @param destIndex
	 * @param sourceStructureName
	 * @param destinationStructureName
	 * @throws IOException if an IO problem occurs
	 */
	public static boolean copyStructure(Index sourceIndex, Index destIndex, String sourceStructureName, String destinationStructureName) throws IOException
	{
		boolean found = false;
		
		/* if source and destination index as the same, then a ConcurrentModificationException
		 * will occur if we try to alter the Properties table while the iteration is taking place.
		 * Hence, to prevent this, we create a temporary Properties, put new properties to that, 
		 * then apply all new properties back on the source(dest) index */
		
		final boolean sameIndex = sourceIndex == destIndex;
		// use temporary index as destination if sameIndex
		Properties destProperties = sameIndex ? new Properties() : destIndex.properties;
		for(Object o : sourceIndex.getProperties().keySet())
		{
			String key = (String)o;
			if (key.startsWith("index."+sourceStructureName + "."))
			{
				destProperties.setProperty(
						key.replaceFirst("^index\\."+sourceStructureName + "\\.", 
						"index." + destinationStructureName + "."), sourceIndex.getProperties().getProperty(key));
				found = true;
				destIndex.dirtyProperties = true;
			}			
		}
		//copy new properties to real index
		if (sameIndex)
		{
			for(Object o : destProperties.keySet())
			{
				String key = (String)o;
				sourceIndex.setIndexProperty((String)o, destProperties.getProperty(key, null));
			}
		}
		//copy 
		for(String file : Files.list(((IndexOnDisk) sourceIndex).getPath()))
		{
			if (file.startsWith(((IndexOnDisk) sourceIndex).getPrefix() + "." + sourceStructureName + "."))
			{
				Files.copyFile(
					((IndexOnDisk) sourceIndex).getPath() + "/" + file, 
					((IndexOnDisk) destIndex).getPath() + "/" + file.replaceFirst(
						((IndexOnDisk) sourceIndex).getPrefix() + "\\." + sourceStructureName, 
						((IndexOnDisk) destIndex).getPrefix() + "." + sourceStructureName));
			}
		}
		return found;
	}
	
	/** Print the contents of the meta index */
	@SuppressWarnings("unchecked")
	public static void printMetaIndex(Index index, String structureName) throws IOException
	{
		Iterator<String[]> inputStream = (Iterator<String[]>)index.getIndexStructureInputStream(structureName);
		while(inputStream.hasNext())
		{
			System.out.println(ArrayUtils.join(inputStream.next(), ", "));
		}
		IndexUtil.close(inputStream);
	}

	@SuppressWarnings("unchecked")
	public static void printMetaIndexJson(Index index, String structureName) throws IOException
	{
		//this is expensive
		final String[] keys = index.getMetaIndex().getKeys();
		final int K = keys.length;
		Iterator<String[]> inputStream = (Iterator<String[]>)index.getIndexStructureInputStream(structureName);
		while(inputStream.hasNext())
		{
			System.out.print("{");
			String[] values = inputStream.next();
			for (int i=0;i<K;i++)
			{
				System.out.print(" " + keys[i] + ": " + new Gson().toJson(values[i]) );
			}
			System.out.println("}");
			
		}
		IndexUtil.close(inputStream);
	}
	
	public static int getFieldId(Index index, String structureName, String fieldName) {
		if (fieldName == null)
			return -1;
		String[] fieldNames = index.getCollectionStatistics().getFieldNames();
		int i=-1;
		for(String f : fieldNames)
		{
			i++;
			if (f.equals(fieldName))
				return i;
		}
		return -1;			
	}
	
	/** Rename a structure within a given index. 
	 * @return Returns true iff a structure was successfully renamed.
	 */
	public static boolean renameIndexStructure(Index index, String sourceStructureName, String destinationStructureName) throws IOException
	{
		final String actualSourcePrefix = ((IndexOnDisk) index).getPrefix() +'.' + sourceStructureName+".";
		final String actualDestinationPrefix = ((IndexOnDisk) index).getPrefix() +'.' + destinationStructureName + ".";
		final String path = ((IndexOnDisk) index).getPath();
		for (String filename : Files.list(((IndexOnDisk) index).getPath()))
		{
			if (filename.startsWith(actualSourcePrefix))
			{
				//System.err.println("Renaming "+ index.getPath() + '/' + filename + " to " + index.getPath() + '/' + filename.replaceFirst(actualSourcePrefix, actualDestinationPrefix));
				Files.delete(path + '/' + filename.replaceFirst(actualSourcePrefix, actualDestinationPrefix));
				Files.rename(
					path + '/' + filename, 
					path + '/' + filename.replaceFirst(actualSourcePrefix, actualDestinationPrefix));
			}
		}
		//boolean found = false;
		Properties p = index.getProperties();
		Set<String> toRemove = new HashSet<String>();
		Map<String,String> toAdd = new HashMap<String,String>();
		for(Object o : p.keySet())
		{
			String key = (String)o;
			if (key.startsWith("index."+sourceStructureName + "."))
			{
				toAdd.put(
						key.replaceFirst("index."+sourceStructureName + "\\.", 
								"index." + destinationStructureName + "."), 
						p.getProperty(key));
				toRemove.add(key);
				//System.err.println("new key is " + key.replaceFirst("index."+sourceStructureName + "\\.", 
				//		"index." + destinationStructureName + "."));
			}
			if (key.startsWith("index."+sourceStructureName + "-inputstream."))
			{
				toAdd.put(
						key.replaceFirst("index."+sourceStructureName + "-inputstream\\.", 
								"index." + destinationStructureName + "-inputstream."), 
						p.getProperty(key));
				toRemove.add(key);		
			}
			
		}
		boolean OK = false;
		for(String k : toRemove)
		{
			//System.err.println("Removing property " + k);
			p.remove(k);
		}
		for(Map.Entry<String,String> e : toAdd.entrySet())
		{
			//System.err.println("Setting property " + e.getKey());
			p.setProperty(e.getKey(), e.getValue());
			OK = true;
		}
		index.dirtyProperties = true;
		index.flush();
		return OK;
	}
	
	/** Configures an object with the index, if the object implements IndexConfigurable */
	public static void configure(Index index, Object o)
	{
		if (o instanceof IndexConfigurable)
		{
			((IndexConfigurable)o).setIndex(index);
		}
	}
	
	/** Check to see if an object is closeable, and if so, close it. Propagate
	 * any exception thrown.
	 * @param o object to check for being closeable.
	 * @throws IOException if exception thrown while closing.
	 */
	public static void close(Object o) throws IOException
	{
		if (o instanceof Closeable)
			((Closeable)o).close();
	}

}
