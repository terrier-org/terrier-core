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
 * The Original Code is LexiconBuilder.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.structures.indexing;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Map.Entry;

import org.apache.hadoop.io.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.sorting.HeapSortInt;
import org.terrier.structures.FSOMapFileLexicon;
import org.terrier.structures.FSOMapFileLexiconOutputStream;
import org.terrier.structures.FieldLexiconEntry;
import org.terrier.structures.Index;
import org.terrier.structures.IndexOnDisk;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.LexiconEntry;
import org.terrier.structures.LexiconOutputStream;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.utility.ApplicationSetup;
/**
 * Builds temporary lexicons during indexing a collection and
 * merges them when the indexing of a collection has finished.
 * @author Craig Macdonald &amp; Vassilis Plachouras 
  */
public class LexiconBuilder
{

	/** class to be used as a lexiconoutpustream. set by this and child classes */
	@SuppressWarnings({ "rawtypes" }) //TODO : this is complicated to fix
	protected Class<? extends LexiconOutputStream> lexiconOutputStream = null;

	//protected Class<? extends LexiconMap> LexiconMapClass = null;
	
	protected final String lexiconEntryFactoryValueClass;
	
	/** The logger used for this class */
	protected static final Logger logger = LoggerFactory.getLogger(LexiconBuilder.class);
	
	/** How many documents have been processed so far.*/
	protected int DocCount = 0;

	/** How many terms are in the final lexicon */
	protected int TermCount = 0;
	
	/** The number of documents for which a temporary lexicon is created. 
	 * Corresponds to property <tt>bundle.size</tt>, default value 2000. */
	protected static final int DocumentsPerLexicon = Integer.parseInt(ApplicationSetup.getProperty("bundle.size", "2000"));
	/** The linkedlist in which the temporary lexicon structure names are stored.
	  * These are merged into a single Lexicon by the merge() method. 
	  * LinkedList is best List implementation for this, as all operations
	  * are either append element, or remove first element - making LinkedList
	  * ideal. */
	protected final LinkedList<String> tempLexFiles = new LinkedList<String>();
	
	/** The lexicontree to write the current term stream to */
	protected LexiconMap TempLex;
	
	/** The directory to write the final lexicons to */
	protected String indexPath = null;
	/** The filename of the lexicons. */
	protected String indexPrefix = null;
	
	protected IndexOnDisk index = null;
	
	/** How many temporary lexicons have been generated so far */
	protected int TempLexCount = 0;
	
	/** Should we only merge lexicons in pairs (Terrier 1.0.x scheme)? Set by property <tt>lexicon.builder.merge.2lex.attime</tt> */
	protected static final boolean MERGE2LEXATTIME = Boolean.parseBoolean(ApplicationSetup.getProperty("lexicon.builder.merge.2lex.attime", "false"));

	/** Number of lexicons to merge at once. Set by property <tt>lexicon.builder.merge.lex.max</tt>, defaults to 16 */
	protected static final int MAXLEXMERGE = Integer.parseInt(ApplicationSetup.getProperty("lexicon.builder.merge.lex.max", "16"));
	/** 
	 * Counter of LexiconEntries
	 */
	public interface CollectionStatisticsCounter extends Closeable
	{
		/** 
		 * The current count
		 */
		void count(LexiconEntry value);
	}
	
	/** counts global statistics in the fields case */
	protected static class FieldLexiconCollectionStaticticsCounter 
		implements CollectionStatisticsCounter
	{
		long numberOfTokens = 0;
		int numberOfTerms = 0;
		long numberOfPointers = 0;
		final IndexOnDisk index;
		int numFields;
		final long[] tokensF;
		
		public FieldLexiconCollectionStaticticsCounter(IndexOnDisk _index, int _numFields)
		{
			index = _index;
			numFields = _numFields;
			tokensF = new long[numFields];
		}
		
		public void count(LexiconEntry value)
		{
			numberOfTokens += value.getFrequency();
			numberOfPointers += value.getDocumentFrequency();
			numberOfTerms++;
			int[] fieldFreqs = ((FieldLexiconEntry)value).getFieldFrequencies();
			for(int fi = 0; fi < numFields; fi++)
			{
				tokensF[fi] += (long)fieldFreqs[fi];
			}
		}
	
		public void close()
		{
			if (index != null)
			{
				index.setIndexProperty("num.Terms", ""+numberOfTerms);
				index.setIndexProperty("num.Tokens", ""+numberOfTokens);
				index.setIndexProperty("num.Pointers", ""+numberOfPointers);
				for(int fi = 0; fi < numFields; fi++)
				{
					index.setIndexProperty("num.field."+fi+".Tokens", ""+ tokensF[fi]);
				}
			}
		}
	}
	
	protected static class NullCollectionStatisticsCounter implements CollectionStatisticsCounter
	{

		public void count(LexiconEntry value) {
			
		}

		public void close() throws IOException {
			
		}
		
	}
	
	/** counts global statistics in the non-fields case */
	public static class BasicLexiconCollectionStaticticsCounter 
		implements CollectionStatisticsCounter
	{
		String midfix = "";
		long numberOfTokens = 0;
		int numberOfTerms = 0;
		long numberOfPointers = 0;
		final Index index;
		/**
		 * constructor
		 * @param _index
		 */
		public BasicLexiconCollectionStaticticsCounter(Index _index)
		{
			index = _index;
		}
		/**
		 * constructor
		 * @param _index
		 * @param subset_name
		 */
		public BasicLexiconCollectionStaticticsCounter(Index _index, String subset_name)
		{
			index = _index;
			midfix = '.'+subset_name;
		}
				
		/** 
		 * {@inheritDoc} 
		 */
		public void count(LexiconEntry value)
		{
			numberOfTokens += value.getFrequency();
			numberOfPointers += value.getDocumentFrequency();
			numberOfTerms++;
		}
		/** 
		 * {@inheritDoc} 
		 */
		public void close()
		{
			if (index != null)
			{
				index.setIndexProperty("num"+midfix+".Terms", ""+numberOfTerms);
				index.setIndexProperty("num"+midfix+".Tokens", ""+numberOfTokens);
				index.setIndexProperty("num"+midfix+".Pointers", ""+numberOfPointers);
			}
		}
	}
	
	protected static LexiconMap instantiate(Class<? extends LexiconMap> LexiconMapClass)
	{
		LexiconMap TempLex = null;
		try{ TempLex = (LexiconMap) LexiconMapClass.newInstance(); } catch (Exception e) {logger.error("Error when creating new LexiconMap", e);}
		return TempLex;
	}
	
	protected String defaultStructureName;
	protected FixedSizeWriteableFactory<LexiconEntry> valueFactory;
	
	/**
	 * constructor
	 * @param i
	 * @param _structureName
	 */
	public LexiconBuilder(IndexOnDisk i, String _structureName) {
		this(i, _structureName, 
				instantiate(LexiconMap.class), "org.terrier.structures.BasicLexiconEntry", "", "");
	}
	/**
	 * constructor
	 * @param i
	 * @param _structureName
	 * @param _LexiconMapClass
	 * @param _lexiconEntryClass
	 */
	public LexiconBuilder(IndexOnDisk i, String _structureName, 
			Class <? extends LexiconMap> _LexiconMapClass,
			String _lexiconEntryClass)
	{
		this(i, _structureName, instantiate(_LexiconMapClass), _lexiconEntryClass, "", "");
	}
	
	
	/**
	 * constructor
	 * @param i
	 * @param _structureName
	 * @param lexiconMap
	 * @param _lexiconEntryClass
	 */
	public LexiconBuilder(IndexOnDisk i, String _structureName, 
				LexiconMap lexiconMap,
				String _lexiconEntryClass)
	{
		this(i, _structureName, lexiconMap, _lexiconEntryClass, "", "");
	}
				
	
	/**
	 * constructor
	 * @param i
	 * @param _structureName
	 * @param lexiconMap
	 * @param _lexiconEntryClass
	 * @param valueFactoryParamTypes
	 * @param valueFactoryParamValues
	 */
	@SuppressWarnings("unchecked")
	public LexiconBuilder(IndexOnDisk i, String _structureName, 
				LexiconMap lexiconMap,
				String _lexiconEntryClass, String valueFactoryParamTypes, String valueFactoryParamValues)
	{
		this.index = i;
		this.indexPath = index.getPath();
		this.indexPrefix = index.getPrefix();
		this.defaultStructureName = _structureName;
		this.TempLex = lexiconMap;
		//TemporaryLexiconDirectory = indexPath + ApplicationSetup.FILE_SEPARATOR + indexPrefix + "_";
		//LexiconMapClass = lexiconMap;	
		lexiconEntryFactoryValueClass = _lexiconEntryClass;
		
	
		this.index.addIndexStructure(
				defaultStructureName+"-keyfactory", 
				"org.terrier.structures.seralization.FixedSizeTextFactory",
				"java.lang.String",
				"${max.term.length}"
				);
		if (this.index.getIndexProperty("max.term.length", null) == null)
			this.index.setIndexProperty("max.term.length", ApplicationSetup.getProperty("max.term.length", ""+20));
		this.index.addIndexStructure(defaultStructureName+"-valuefactory", lexiconEntryFactoryValueClass+"$Factory", valueFactoryParamTypes, valueFactoryParamValues);
		valueFactory = (FixedSizeWriteableFactory<LexiconEntry>)this.index.getIndexStructure(defaultStructureName+"-valuefactory");
		lexiconOutputStream = LexiconOutputStream.class;
	}

	/** Returns the number of terms in the final lexicon. Only updated once finishDirectIndexBuild() has executed */
	public int getFinalNumberOfTerms()
	{
		return TermCount;
	}

	/** If the application code generated lexicons itself, use this method to add them to the merge list 
	  * Otherwise dont touch this method.
	  * @param structureName Fully path to a lexicon to merge
	  * @deprecated */
	public void addTemporaryLexicon(String structureName) {
		tempLexFiles.addLast(structureName);
		//filename = ApplicationSetup.makeAbsolute(filename, TemporaryLexiconDirectory);
	}

	/** Writes the current contents of TempLex temporary lexicon binary tree down to
	  * a temporary disk lexicon.
	  */
	protected void writeTemporaryLexicon()
	{
		try{
			//TempLexDirCount = TempLexCount / TempLexPerDir;
			//if (! Files.exists(TemporaryLexiconDirectory + TempLexDirCount)) {
			//	String tmpDir = TemporaryLexiconDirectory + TempLexDirCount;
			//	Files.mkdir(tmpDir);
			//	Files.deleteOnExit(tmpDir);//it's fine to mark the temporary *directory* for deletion
			//}
			//String tmpLexName = TemporaryLexiconDirectory + TempLexDirCount + ApplicationSetup.FILE_SEPARATOR + TempLexCount;
			//LexiconOutputStream<String> los = getLexOutputStream(TempLexDirCount+""+TempLexCount);
			final String tmpLexName = this.defaultStructureName+"-tmp"+ TempLexCount;
			LexiconOutputStream<String> los = getLexOutputStream(tmpLexName);
			TempLex.storeToStream(los);
			los.close();
			/* An alternative but deprecated method to store the temporary lexicons is: 
			 * TempLex.storeToFile(tmpLexName); */
			//tempLexFiles.addLast(TempLexDirCount+""+TempLexCount);
			tempLexFiles.addLast(tmpLexName);
		}catch(IOException ioe){
			logger.error("Indexing failed to write a lexicon to disk : ", ioe);
		}		
	}

	/** Add a single term to the lexicon being built 
	  * @param term The String term
	  * @param tf the frequency of the term */	
	public void addTerm(String term, int tf)
	{
		TempLex.insert(term,tf);
	}

	/** adds the terms of a document to the temporary lexicon in memory.
	  * @param terms DocumentPostingList the terms of the document to add to the temporary lexicon */
	public void addDocumentTerms(DocumentPostingList terms)
	{
		TempLex.insert(terms);
		DocCount++;
		if((DocCount % DocumentsPerLexicon) == 0)
		{
			if (logger.isDebugEnabled())
				logger.debug("flushing lexicon");
			writeTemporaryLexicon();
			TempLexCount++;
			TempLex.clear();
			//try{ TempLex = (LexiconMap)LexiconMapClass.newInstance(); } catch (Exception e) {logger.error(e);}
		}
	}

	/** Force a temporary lexicon to be flushed */
	public void flush()
	{
		if (logger.isDebugEnabled())
			logger.debug("flushing lexicon");
		writeTemporaryLexicon();
		TempLexCount++;
		TempLex.clear();
	}
	
	/**
	 * Processing the lexicon after finished creating the
	 * inverted index.
	 */
	public void finishedInvertedIndexBuild() {
		optimiseLexicon();
	}
	
	
	
	/** 
	 * Processing the lexicon after finished creating the 
	 * direct and document indexes.
	 */
	public void finishedDirectIndexBuild()
	{
		if (logger.isDebugEnabled())
			logger.debug("flushing lexicon to disk after the direct index completed");
		//only write a temporary lexicon if there are any items in it
		if (TempLex.getNumberOfNodes() > 0)
			writeTemporaryLexicon();
		TempLex = null;

		//merges the temporary lexicons
		if (tempLexFiles.size() > 0)
		{
			//Set<String> tempDirectories = new HashSet<String>();
			//for(String tmpLex : tempLexFiles)
			//{
			//	tempDirectories.add(Files.getParent(tmpLex));
			//}
			try{
				merge(tempLexFiles);
				
				//creates the offsets and hash file
				optimiseLexicon();
			} catch(IOException ioe){
				logger.error("Indexing failed to merge temporary lexicons to disk : ", ioe);
			}
			//for (String tmpDir : tempDirectories)
			//{
			//	Files.delete(tmpDir);
			//}
		}	
		else
			logger.warn("No temporary lexicons to merge, skipping");
	}
	
	/**
	 * Merges the intermediate lexicon files created during the indexing.
	 * @param filesToMerge java.util.LinkedList the list containing the 
	 *		filenames of the temporary files.
	 * @throws IOException an input/output exception is throws 
	 *		 if a problem is encountered.
	 */
	@SuppressWarnings("unchecked")
	public void merge(LinkedList<String> filesToMerge) throws IOException {
		//now the merging of the files in the filesToMerge vector 
		//must take place. 
		//Several strategies exist here: 
		// a. number to merge is 0 - error condition?
		// b. number ito merge is 1 - none to merge, just rename it
		// c. merge 2 at a time in pairs (default to 1.0.2)
		// d. merge N at once (N is a constant)
		// e. merge all at once.


		final int mergeNMaxLexicon = MAXLEXMERGE;
		final int StartFileCount = filesToMerge.size();
		logger.info(StartFileCount+ " lexicons to merge");
		if (StartFileCount == 0)
		{
			logger.warn("Tried to merge 0 lexicons. That's funnny. Is everything ok?");
			return;
		}
		if (StartFileCount == 1)
		{
			FSOMapFileLexicon.renameMapFileLexicon(filesToMerge.removeFirst(), index.getPath(), index.getPrefix(), 
					defaultStructureName, index.getPath(), index.getPrefix());
		}
		else if (MERGE2LEXATTIME)
		{
			//more than 1 lexicon to merge, but configured only to merge 2 at a time
			if (logger.isDebugEnabled())
				logger.debug("begin merging "+ StartFileCount +" temporary lexicons, in pairs...");
			long startTime = System.currentTimeMillis();
			int progressiveNumber = 0;
			String newMergedFile = null;
			while (filesToMerge.size() > 1) {
				String fileToMerge1 = (String) filesToMerge.removeFirst();
				String fileToMerge2 = (String) filesToMerge.removeFirst();
				
				//give the proper name to the final merged lexicon
				if (filesToMerge.size() == 0) 
					newMergedFile = defaultStructureName;
				else 
					newMergedFile = defaultStructureName + "-mergetmp"+ String.valueOf(progressiveNumber++);
	
				//The opening of the files needs to break into more steps, so that
				//all the open streams are closed after the completion of the 
				//operation, and eventually the intermediate files are deleted.

				Iterator<Map.Entry<String,LexiconEntry>> lis1 = getLexInputStream(fileToMerge1);
				Iterator<Map.Entry<String,LexiconEntry>> lis2 = getLexInputStream(fileToMerge2);
				LexiconOutputStream<String> los = getLexOutputStream(newMergedFile);
	
				if (logger.isDebugEnabled())
					logger.debug(
						"merging "
							+ fileToMerge1
							+ " with "
							+ fileToMerge2
							+ " to "
							+ newMergedFile);
				
				mergeTwoLexicons(lis1, lis2, los);
			
				//delete the two files just merged
				FSOMapFileLexicon.deleteMapFileLexicon(fileToMerge1, indexPath, indexPrefix);
				FSOMapFileLexicon.deleteMapFileLexicon(fileToMerge2, indexPath, indexPrefix);
				filesToMerge.addLast(newMergedFile);
			}
			long endTime = System.currentTimeMillis();
			if (logger.isDebugEnabled())
				logger.debug("end of merging...("+((endTime-startTime)/1000.0D)+" seconds)");
		}
		else if (mergeNMaxLexicon > 0 && StartFileCount > mergeNMaxLexicon)
		{
			if (logger.isDebugEnabled())
				logger.debug("begin merging "+ StartFileCount +" files in batches of upto "+mergeNMaxLexicon+"...");
			long startTime = System.currentTimeMillis();
			int progressiveNumber = 0;
	

			while (filesToMerge.size() > 1)
			{
				final int numLexicons = Math.min(filesToMerge.size(), mergeNMaxLexicon);
				if (logger.isDebugEnabled())
					 logger.debug("merging "+ numLexicons + " temporary lexicons");
				final String inputLexiconFileNames[] = new String[numLexicons];
				final Iterator<Map.Entry<String,LexiconEntry>>[] lis = (Iterator<Map.Entry<String,LexiconEntry>>[])new Iterator[numLexicons];

				for(int i=0;i<numLexicons;i++)
				{
					inputLexiconFileNames[i] =  filesToMerge.removeFirst();
					lis[i] = getLexInputStream(inputLexiconFileNames[i]);
				}

				String newMergedFile = null;
				//give the proper name to the final merged lexicon
				if (filesToMerge.size() == 0)
					newMergedFile = defaultStructureName;
				else
					newMergedFile = defaultStructureName + "-mergetmp"+ String.valueOf(progressiveNumber++);

				final LexiconOutputStream<String> los = getLexOutputStream(newMergedFile);
				mergeNLexicons(lis, los);
				for(String inputLexiconFileName : inputLexiconFileNames)
				{
					FSOMapFileLexicon.deleteMapFileLexicon(inputLexiconFileName, ((IndexOnDisk) index).getPath(), ((IndexOnDisk) index).getPrefix());
				}
				filesToMerge.addLast(newMergedFile);
			}
			long endTime = System.currentTimeMillis();
			if (logger.isDebugEnabled())
				logger.debug("end of merging...("+((endTime-startTime)/1000.0D)+" seconds)");
		} else {
			//merge all lexicons at once, regardless of how many exist
			 if (logger.isDebugEnabled())
				logger.debug("begin merging "+ StartFileCount +" temporary lexicons at once...");
			long startTime = System.currentTimeMillis();
			final String inputLexiconFileNames[] = new String[StartFileCount];
			final Iterator<Map.Entry<String,LexiconEntry>>[] lis = 
				(Iterator<Map.Entry<String,LexiconEntry>>[]) new Iterator[StartFileCount];
			
			for(int i=0;i<StartFileCount;i++)
			{
				inputLexiconFileNames[i] = filesToMerge.removeFirst();
				lis[i] = getLexInputStream(inputLexiconFileNames[i]);
				//logger.debug(i+" "+inputLexiconFileNames[i]);
			}
			final LexiconOutputStream<String> los = getLexOutputStream(defaultStructureName);
			mergeNLexicons(lis, los);
			for(int i=0;i<StartFileCount;i++)
			{
				FSOMapFileLexicon.deleteMapFileLexicon(inputLexiconFileNames[i], ((IndexOnDisk) index).getPath(), ((IndexOnDisk) index).getPrefix());
			}
			long endTime = System.currentTimeMillis();
			if (logger.isDebugEnabled())
				logger.debug("end of merging...("+((endTime-startTime)/1000.0D)+" seconds)");
		}
		FSOMapFileLexiconOutputStream.addLexiconToIndex(this.index, defaultStructureName, lexiconEntryFactoryValueClass+"$Factory");
	}
	
	protected LexiconEntry newLexiconEntry(int termid)
	{
		LexiconEntry rtr = valueFactory.newInstance();
		rtr.setTermId(termid);
		return rtr;
	}
	
	@SuppressWarnings("unchecked")
	protected void mergeNLexicons(Iterator<Map.Entry<String,LexiconEntry>>[] lis, LexiconOutputStream<String> los) throws IOException
	{
		final int numLexicons = lis.length;
		boolean hasMore[] = new boolean[numLexicons];
		Map.Entry<String,LexiconEntry>[] currentEntries = new Map.Entry[numLexicons];
		
		Arrays.fill(hasMore, false);
		PriorityQueue<String> terms = new PriorityQueue<String>(numLexicons);
		for(int i=0;i<numLexicons;i++)
		{
			hasMore[i] = lis[i].hasNext();
			if (hasMore[i])
			{
				currentEntries[i] = lis[i].next();
				terms.add(currentEntries[i].getKey());
			}
			else
			{
				currentEntries[i] = null;
			}
				
		}
		String targetTerm= null;
		int targetTermId  = -1;
		LexiconEntry nextEntryToWrite = null;
		while(terms.size() > 0)
		{
			//what term are we working on
			targetTerm = terms.poll();
			//logger.debug("Current term is "+targetTerm + "length="+targetTerm.length());
			//for each input lexicon
			for(int i=0;i<numLexicons;i++)
			{
				//does this lexicon contain the term
				//logger.debug("Checking lexicon "+i+" for "+targetTerm+"="+lis[i].getTerm());
				if(hasMore[i] && currentEntries[i].getKey().equals(targetTerm))
				{
					if (targetTermId == -1)
					{	//obtain the termid for this term from the first lexicon that has the term
						nextEntryToWrite = newLexiconEntry(targetTermId = currentEntries[i].getValue().getTermId());
					}
					else if (targetTermId != currentEntries[i].getValue().getTermId())
					{	//check the termids match for this term
						logger.error("Term "+targetTerm+" had two termids ("+targetTermId+","+currentEntries[i].getValue().getTermId()+")");
					}
					//logger.debug("Term "+targetTerm + " found in "+i + "termid="+ lis[i].getTermId());
					nextEntryToWrite.add(currentEntries[i].getValue());
					hasMore[i] = lis[i].hasNext();
					
					if (hasMore[i])
					{
						currentEntries[i] = lis[i].next();
						terms.add(currentEntries[i].getKey());
					}
					else
					{
						currentEntries[i] = null;
					}
					break;
				}
			}
			if (terms.size()>0 && !terms.peek().equals(targetTerm))
			{
				if (targetTermId == -1)
				{
					logger.error("Term "+ targetTerm + " not found in any lexicons");
				}
				//end of this term, so we can write the lexicon entry
				los.writeNextEntry(targetTerm, nextEntryToWrite);
				nextEntryToWrite = null; targetTermId = -1; targetTerm = null;
			}
		}
		if (targetTermId != -1)
			los.writeNextEntry(targetTerm, nextEntryToWrite);
		los.close();
		for(int i=0;i<numLexicons;i++)
		{
			IndexUtil.close(lis[i]);
		}
	}
		

	/** Merge the two LexiconInputStreams into the given LexiconOutputStream
	  * @param lis1 First lexicon to be merged
	  * @param lis2 Second lexicon to be merged
	  * @param los Lexion to be merged to
	  */
	protected void mergeTwoLexicons(
			Iterator<Map.Entry<String,LexiconEntry>> lis1,
			Iterator<Map.Entry<String,LexiconEntry>> lis2,
			LexiconOutputStream<String> los) throws IOException
	{

		//We always take the first two entries of
		//the vector, merge them, store the new lexicon in the directory
		//of the first of the two merged lexicons, and put the filename
		//of the new lexicon file at the back of the vector. The first
		//two entries that were merged are removed from the vector. The
		//use of the vector is similar to a FIFO queue in this case.

		boolean hasMore1 = true;
		boolean hasMore2 = true;
		int termID1 = 0;
		int termID2 = 0;

	
		hasMore1 = lis1.hasNext();
		hasMore2 = lis2.hasNext();
		String sTerm1 = null;
		String sTerm2 = null;
		Map.Entry<String, LexiconEntry> lee1 = null;
		Map.Entry<String, LexiconEntry> lee2 = null;
		if (hasMore1) {
			lee1 = lis1.next();
			termID1 = lee1.getValue().getTermId();
			sTerm1 = lee1.getKey();
		}
		if (hasMore2) {
			lee2 = lis2.next();
			termID2 = lee2.getValue().getTermId();
			sTerm2 = lee2.getKey();
		}
		while (hasMore1 && hasMore2) {
			int compareString = 0;
			if (termID1 != termID2)
			{
				compareString = sTerm1.compareTo(sTerm2);
				if (compareString == 0)//, but termids don't match
				{
					logger.error("Term "+sTerm1+" had two termids ("+
						termID1+","+termID2+")");
				}
			}
			
			if (compareString <0) {
				los.writeNextEntry(sTerm1, lee1.getValue());
				hasMore1 = lis1.hasNext();
				if (hasMore1) {
					lee1 = lis1.next();
					termID1 = lee1.getValue().getTermId();
					sTerm1 = lee1.getKey();
				}
			} else if (compareString >0) {
				los.writeNextEntry(sTerm2, lee2.getValue());
				hasMore2 = lis2.hasNext();
				if (hasMore2) {
					lee2 = lis2.next();
					termID2 = lee2.getValue().getTermId();
					sTerm2 = lee2.getKey();
				}
			} else /*if (compareString == 0)*/ {
				lee1.getValue().add(lee2.getValue());
				los.writeNextEntry(
					sTerm1, 
					lee1.getValue()
				);
				hasMore1 = lis1.hasNext();
				hasMore2 = lis2.hasNext();
				if (hasMore1) {
					lee1 = lis1.next();
					termID1 = lee1.getValue().getTermId();
					sTerm1 = lee1.getKey();
				}
				if (hasMore2) {
					lee2 = lis2.next();
					termID2 = lee2.getValue().getTermId();
					sTerm2 = lee2.getKey();
				}
			}
		}
		if (hasMore1) {
			
			while (hasMore1) {
				los.writeNextEntry(sTerm1, lee1.getValue());
				hasMore1 = lis1.hasNext();
				if (hasMore1) {
					lee1 = lis1.next();
					termID1 = lee1.getValue().getTermId();
					sTerm1 = lee1.getKey();
				}
			}

		} else if (hasMore2) {
			
			while (hasMore2) {
				los.writeNextEntry(sTerm2, lee2.getValue());
				hasMore2 = lis2.hasNext();
				if (hasMore2) {
					lee2 = lis2.next();
					termID2 = lee2.getValue().getTermId();
					sTerm2 = lee2.getKey();
				}
			}
			
		}
		IndexUtil.close(lis1);
		IndexUtil.close(lis2);
		//close output file streams
		los.close();
	}
	
	
	/** Creates a lexicon index for the specified index
	  * @param index IndexOnDisk to make the lexicon index for
	  * @deprecated use optimise instead
	  */	
	public static void createLexiconIndex(IndexOnDisk index) throws IOException
	{
		optimise(index, "lexicon");
	}


	
	/** Creates a lexicon hash for the specified index
	 * @param index IndexOnDisk to make the LexiconHash the lexicoin
	 * @deprecated use optimise instead
	 */
	public static void createLexiconHash(final IndexOnDisk index) throws IOException
	{
		optimise(index, "lexicon");
	}
	/** optimise the lexicon */
	public void optimiseLexicon()
	{
		optimise(index, defaultStructureName);
	}
	
	/** Optimises the lexicon, eg lexid file */
	public static void optimise(final IndexOnDisk index, final String structureName)
	{
		try {
			logger.info("Optimising structure "+structureName);
			CollectionStatisticsCounter counter;
			if (structureName.contains("lexicon"))
			{
				int fieldCount = index.getIntIndexProperty("index.inverted.fields.count", 0);
				if (fieldCount > 0)
				{
					logger.info(structureName + " has " + fieldCount + " fields");
					counter = new FieldLexiconCollectionStaticticsCounter(index, fieldCount);
				}
				else
				{
					counter = new BasicLexiconCollectionStaticticsCounter(index);
				}
			}
			else
			{
				//other uses of lexicons shouldnt overwrite the tokens in the index
				counter = new NullCollectionStatisticsCounter();
			}
			FSOMapFileLexicon.optimise(structureName, index, counter);
			counter.close();
			index.flush();
		} catch(IOException ioe) {
			logger.error("IOException while creating optimising lexicon called " + structureName, ioe);
		}
	}


	/** Re-assigned the termids within the named lexicon structure to be ascending with 
	 * descending term frequency, i.e. the terms with termid 0 will have the highest frequency.
	 * @param index
	 * @param structureName
	 * @param numEntries
	 * @throws IOException
	 */
	public static void reAssignTermIds(IndexOnDisk index, String structureName, int numEntries) throws IOException
	{
		int[] entryIndex = new int[numEntries];
		int[] TF = new int[numEntries];
		Iterator<Map.Entry<String,LexiconEntry>> le = getLexInputStream(index, structureName);
		int i=0;
		while(le.hasNext())
		{
			entryIndex[i] = i;
			TF[i] = le.next().getValue().getFrequency();
			i++;
		}
		HeapSortInt.descendingHeapSort(TF, entryIndex);
		int[] newTermId = new int[numEntries];
		for(i=0;i<numEntries;i++)
		{
			newTermId[entryIndex[i]] = i;
		}
		i=0;
		
		IndexUtil.renameIndexStructure(index, structureName, structureName+ "-old");
		le = getLexInputStream(index, structureName + "-old");
		LexiconOutputStream<String> leOut = getLexOutputStream(index, structureName);
		while(le.hasNext())
		{
			Entry<String, LexiconEntry> lee = le.next();
			lee.getValue().setTermId(newTermId[i]);
			leOut.writeNextEntry(lee.getKey(), lee.getValue());
			i++;
		}
		leOut.close();
		optimise(index, structureName);
	}
	
	@SuppressWarnings("unchecked")
	private static Iterator<Entry<String, LexiconEntry>> getLexInputStream(
			IndexOnDisk index, String structureName)  throws IOException
	{
		return new FSOMapFileLexicon.MapFileLexiconIterator(structureName, index.getPath(), index.getPrefix(), 
				(FixedSizeWriteableFactory<Text>)index.getIndexStructure(structureName+"-keyfactory"), 
				(FixedSizeWriteableFactory<LexiconEntry>)index.getIndexStructure(structureName+"-valuefactory"));
	}
	/** return the lexicon input stream for the current index at the specified filename */	
	@SuppressWarnings("unchecked")
	protected Iterator<Map.Entry<String,LexiconEntry>> getLexInputStream(String structureName) throws IOException
	{
		return new FSOMapFileLexicon.MapFileLexiconIterator(structureName, ((IndexOnDisk) index).getPath(), ((IndexOnDisk) index).getPrefix(), 
				(FixedSizeWriteableFactory<Text>)index.getIndexStructure(defaultStructureName+"-keyfactory"), 
				(FixedSizeWriteableFactory<LexiconEntry>)index.getIndexStructure(defaultStructureName+"-valuefactory"));
	}

	/** return the lexicon outputstream for the current index at the specified filename */
	@SuppressWarnings("unchecked")
	protected LexiconOutputStream<String> getLexOutputStream(String structureName) throws IOException
	{
		return new FSOMapFileLexiconOutputStream(
				index.getPath(), index.getPrefix(), 
				structureName, 
				(FixedSizeWriteableFactory<Text>)index.getIndexStructure(defaultStructureName+"-keyfactory"));
	}
	
	/** return the lexicon outputstream for the specified index at the specified filename */
	@SuppressWarnings("unchecked")
	private static LexiconOutputStream<String> getLexOutputStream(IndexOnDisk index, String structureName) throws IOException
	{
		return new FSOMapFileLexiconOutputStream(
				index.getPath(), index.getPrefix(), 
				structureName, 
				(FixedSizeWriteableFactory<Text>)index.getIndexStructure(structureName+"-keyfactory"));
	}

}
