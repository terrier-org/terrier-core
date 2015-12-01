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
 * The Original Code is IncrementalIndex.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 *   Stuart Mackie <s.mackie.1@research.gla.ac.uk>
 */

package org.terrier.realtime.incremental;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.indexing.Document;
import org.terrier.realtime.UpdatableIndex;
import org.terrier.realtime.memory.MemoryIndex;
import org.terrier.structures.Index;
import org.terrier.structures.indexing.DocumentPostingList;
import org.terrier.utility.ApplicationSetup;

/**
 * <p>This is the main Index class for an incremental index. An incremental index is a MultiIndex
 * where one index shard is stored in memory and the rest are stored on disk. Periodically, the
 * memory index is then written do disk, defined as per a FlushPolicy. When the memory index has
 * been flushed to disk, optionally the on-disk portion of the incremental index can then be merged
 * together (based upon a MergePolicy) and/or deleted (based upon a DeletePolicy).</p>
 * 
 * <p><b>Properties</b></p>
 * <ul><li>incremental.flush: the flush policy to use. Four possible values are supported: noflush (default), flushdocs, flushmem, flushtime</li></ul>
 * <ul><li>incremental.merge: the merge policy to use. Three possible values are supported: nomerge (default), single, geometric</li></ul>
 * <ul><li>incremental.delete: the delete policy to use. Two possible values are supported: nodelete (default), deleteFixedSize</li></ul>
 * 
 * @author Richard McCreadie, Stuart Mackie
 * @since 4.0
 */
@SuppressWarnings("resource")
public class IncrementalIndex extends org.terrier.realtime.multi.MultiIndex implements UpdatableIndex {
	protected static final Logger logger = LoggerFactory
			.getLogger(IncrementalIndex.class);

	/*
	 * In-memory index.
	 */
	public MemoryIndex memory;

	/*
	 * Index path,prefix,prefix-n. Fixed by richardm
	 */
	public String path;
	public String prefix;
	public int prefixID;
	
	/*
	 * Flush, merge and delete policy.
	 */
	private boolean flush;
	private IncrementalFlushPolicy flushPolicy;
	private boolean merge;
	private IncrementalMergePolicy mergePolicy;
	private boolean delete;
	private IncrementalDeletePolicy deletePolicy;

	/** The date and time this index last indexed a document **/
	private long lastUpdateTime = System.currentTimeMillis();
	/** A calendar instance so that we can print times **/
	private Calendar currentCalendar = Calendar.getInstance();
	
	/** A lock that stops multiple indexing operations from happening at once **/
    Object indexingLock = new Object();
	
	
	/**
	 * Construct a new incremental index.
	 */
	
	public static IncrementalIndex get(String path, String prefix) {

		int prefixID;
		
		// Check for existing on-disk indices.
		File dir = new File(path);
		File files[] = dir.listFiles();
		Pattern p = Pattern.compile(prefix + "-(\\d+).properties");
		List<Integer> diskIDs = new ArrayList<Integer>();
		for (File file : files) {
			Matcher m = p.matcher(file.getName());
			if (m.matches())
				diskIDs.add(Integer.parseInt(m.group(1)));
		}
		
		Collections.sort(diskIDs);
		int existing = diskIDs.size();
		logger.info("***REALTIME*** IncrementalIndex existing indices: "+existing);
		for (int diskID : diskIDs)
			logger.info(String.valueOf(diskID));

		// No previous on-disk indices.
		if (existing == 0) {
			
				// also check for a normal index that we could treat as an incremental...
				File singleproperties = new File(path+ApplicationSetup.FILE_SEPARATOR+prefix+".properties");
				if (singleproperties.exists()) {
					// Set prefixID to 1.
					prefixID = 1;

					Index[] indexes = new Index[2];
					indexes[0] = Index.createIndex(path, prefix);
					MemoryIndex imi = new MemoryIndex();
					indexes[1] = imi;
					// IncrementalIndex consists of a single InMemoryIndex.
					
					IncrementalIndex ii = new IncrementalIndex(indexes);
					ii.memory = imi;
					ii.setPath(path);
					ii.setPrefix(prefix);
					ii.setPrefixID(prefixID);
					return ii;
				} else {
					// Set prefixID to 1.
					prefixID = 1;

					// IncrementalIndex consists of a single InMemoryIndex.
					MemoryIndex imi;
					IncrementalIndex ii = new IncrementalIndex(
							new Index[] { imi = new MemoryIndex() });
					ii.memory = imi;
					
					ii.setPath(path);
					ii.setPrefix(prefix);
					ii.setPrefixID(prefixID);
					return ii;
				}

		}

		// Found existing on-disk indices.
		else {

			// Set prefixID to n+1, where n is the ID last existing index.
			prefixID = diskIDs.get(existing - 1) + 1;

			// also check for a normal index that we could treat as an incremental...
			File singleproperties = new File(path+ApplicationSetup.FILE_SEPARATOR+prefix+".properties");
			if (singleproperties.exists()) {
				existing++;
				
				// IncrementalIndex consists of the single normal index, all existing on-disk indices,
				// and a single InMemoryIndex.
				Index[] indexes = new Index[existing + 1];
				indexes[0] = Index.createIndex(path, prefix);
				for (int i = 1; i < existing; i++)
					indexes[i] = Index.createIndex(path,
							prefix + "-" + diskIDs.remove(0));
				MemoryIndex mem;
				indexes[existing] = mem = new MemoryIndex();
				IncrementalIndex ii = new IncrementalIndex(indexes);
				ii.memory = mem;
				ii.setPath(path);
				ii.setPrefix(prefix);
				ii.setPrefixID(prefixID);
				return ii;
				
			} else {
				// IncrementalIndex consists of all existing on-disk indices,
				// and a single InMemoryIndex.
				Index[] indexes = new Index[existing + 1];
				for (int i = 0; i < existing; i++)
					indexes[i] = Index.createIndex(path,
							prefix + "-" + diskIDs.remove(0));
				MemoryIndex mem;
				indexes[existing] = mem = new MemoryIndex();
				IncrementalIndex ii = new IncrementalIndex(indexes);
				ii.memory = mem;
				ii.setPath(path);
				ii.setPrefix(prefix);
				ii.setPrefixID(prefixID);
				return ii;
			}
		}
	}

	/*
	 * Private constructor.
	 */
	protected IncrementalIndex(Index[] indices) {

		// Pass indices up to MultiIndex.
		super(indices);

		// Get policy from terrier.properties.
		String policy;

		// Flush policy.
		policy = ApplicationSetup.getProperty("incremental.flush", "noflush");
		flushPolicy = IncrementalFlushPolicy.get(policy, super.indices, this);
		flush = flushPolicy.flushPolicy();

		// Merge policy.
		policy = ApplicationSetup.getProperty("incremental.merge", "nomerge");
		mergePolicy = IncrementalMergePolicy.get(policy, super.indices, this);
		merge = mergePolicy.mergePolicy();
		
		// Delete Policy
		policy = ApplicationSetup.getProperty("incremental.delete", "nodelete");
		deletePolicy = IncrementalDeletePolicy.get(policy);

		logger.info("***REALTIME*** IncrementalIndex (NEW)");
	}

	/**
	 * Update the index with a new document.
	 */
	public void indexDocument(Document doc) throws Exception {

		synchronized(indexingLock) {
		
		// Don't index null documents.
		if (doc == null)
			return;

		// Index document.
		memory.indexDocument(doc);

		// Check flush.
		if (flush && flushPolicy.flushCheck() == true)
			flush();
		
		}

	}

	/**
	 * Update the index with a new document.
	 */
	public void indexDocument(Map<String, String> docProperties,
			DocumentPostingList docContents) throws Exception {

		synchronized(indexingLock) {
		
		// Don't index null documents.
		if (docContents == null || docProperties == null)
			return;

		// Index document.
		memory.indexDocument(docProperties, docContents);

		// Check flush.
		if (flush && flushPolicy.flushCheck() == true)
			flush();

		}
	}

	/** {@inheritDoc} */
	public void flush() throws IOException {

		// Create new (empty) in-memory index.
		synchronized (super.indices) {
			super.indices.add(memory = new MemoryIndex());
		}

		// Flush old (full) in-memory index to disk.
		flushPolicy.run();

		// Run delete policy to remove old indices if any
		if (delete && deletePolicy.deletePolicy() == true) {
			deletePolicy.runPolicy(indices);
		}
		
		// Check merge.
		if (merge && mergePolicy.mergeCheck() == true)
			((Runnable) mergePolicy).run();
		

	}

	/** {@inheritDoc} */
	public void close() throws IOException {
		if (flush && flushPolicy.flushCheck() == true)
			flush();
	}
	
	/** This method prints out the last time this index was updated as a String in GMT format **/
	@SuppressWarnings("deprecation")
	public String getTimeOfLastUpdate() {
		currentCalendar.setTimeInMillis(lastUpdateTime);
		return currentCalendar.getTime().toGMTString();
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public int getPrefixID() {
		return prefixID;
	}

	public void setPrefixID(int prefixID) {
		this.prefixID = prefixID;
	}

	@Override
	public boolean removeDocument(int docid) {
		return false;
	}

	@Override
	public boolean addToDocument(int docid, Document doc) throws Exception {
		return false;
	}

	@Override
	public boolean addToDocument(int docid, DocumentPostingList docContents)
			throws Exception {
		return false;
	}
	
	
}