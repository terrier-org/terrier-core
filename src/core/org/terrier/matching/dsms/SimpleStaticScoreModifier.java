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
 * The Original Code is SimpleStaticScoreModifier.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */
package org.terrier.matching.dsms;

import java.io.BufferedReader;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.matching.MatchingQueryTerms;
import org.terrier.matching.ResultSet;
import org.terrier.structures.Index;
import org.terrier.structures.IndexUtil;
import org.terrier.structures.MetaIndex;
import org.terrier.utility.ApplicationSetup;
import org.terrier.utility.Files;
import org.terrier.utility.HeapSort;
import org.terrier.utility.StaTools;
/** Provides a way to integrate a static (query independent) document (prior) feature 
 * into the document scores. The feature scores are loaded from a file. The filename
 * is specified by the property <tt>ssa.input.file</tt>. It can take several forms,
 * as specified by the property <tt>ssa.input.type</tt>:
 * <ol>
 * <li>"oos" (default) - an ObjectInputStream array of doubles, floats or shorts.</li>
 * <li>"docno2score" - a text file with format "docno value\n"</li>
 * <li>"docno2score_seq" - a text file with format "docno value\n", where docno
 * is sorted in the same order as the docno fields in the MetaIndex.</li>
 * <li>"listofscores" - a text file with format "value\n", for each document.</li>
 * </ol>
 * The static feature score is added to the document score as
 * score(d) += w*prior(d). w is controlled by the property <tt>ssa.w</tt>.
 * <p>
 * <b>Properties:</b>
 * <ul>
 * <li><tt>ssa.input.file</tt> - input file</li>
 * <li><tt>ssa.input.type</tt> - type of input file - one of "oos", "docno2score", "docno2score_seq", "listofscores". Default is oos.</li>
 * <li><tt>ssa.modified.length</tt> - how much of the top-ranked documents to alter. 0 means all documents, defaults to 1000.</li>
 * <li><tt>ssa.w</tt> - combination weight of the feature.</li>
 * <li><tt>ssa.normalise</tt> - if defined, available options are "mean1" or "maxmin". Default is no normalisation.</li> 
 * </ul>
 * @author Craig Macdonald, Vassilis Plachouras
 * @since 3.0
 *
 */
public class SimpleStaticScoreModifier implements DocumentScoreModifier
{
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/** The number of top-ranked documents for which the scores will be modified. */
	protected int modifiedLength;
	/** weight for this feature */
	protected double w;
	/** The array that contains the statically computed scores.*/
	protected double[] staticScores;
	/** check that we have been initialised */
	boolean init = false;

	protected static void makeAverage1(double ar[]) {
		final int count = ar.length;
		double sum = 0;
		for(double v : ar)
			sum += v;
		final double average = sum / (double)count;
		for(int i=0;i<count;i++)
			ar[i] = ar[i] / average;
	}

	/**
	 * init
	 * @param index
	 */
	public void init(Index index) {
		if (init)
			return;
		final long startTime = System.currentTimeMillis();
		
		String type = ApplicationSetup.getProperty("ssa.input.type", "oos");
		if (type.equals("oos"))
		{
			loadOOS();
		}
		else if (type.equals("docno2score"))
		{
			loadDocno2score(index);
		}
		else if (type.equals("listofscores"))
		{
			loadScorefile(index);
		}
		else if (type.equals("docno2score_seq"))
		{
			loadDocno2score_seq(index);
		}
		else
		{
			throw new IllegalArgumentException("Unrecognised value for ssa.input.type " + type);
		}
		normalise(staticScores);
		init = true;
		
		final long endTime = System.currentTimeMillis();
		System.out.println("Loading feature scores took "+ ((endTime -startTime) /1000.d)+" seconds"); 
	}
	
	protected void normalise(double[] scores)
	{		
		final boolean normaliseToMaxMin = ApplicationSetup.getProperty("ssa.normalise", "false").equals("maxmin");
		final boolean normaliseToMean1 = 
			ApplicationSetup.getProperty("ssa.normalise.mean1", null) == null
			? ApplicationSetup.getProperty("ssa.normalise", "null").equals("mean1")
			: Boolean.parseBoolean(ApplicationSetup.getProperty("ssa.normalise.mean1", "false"));
		
		final boolean negate = Boolean.parseBoolean(ApplicationSetup.getProperty("ssa.negate", "false"));
		
		if (normaliseToMean1)
			makeAverage1(staticScores);
		else if (normaliseToMaxMin)
			makeMaxMin(staticScores);
		
		if (negate)
			negate(staticScores);
	}

	protected void loadScorefile(Index index) {
		String inputFile = ApplicationSetup.getProperty("ssa.input.file","");
		try {
			staticScores = new double[index.getCollectionStatistics().getNumberOfDocuments()];
			BufferedReader br = Files.openFileReader(inputFile);
			String line = null;
			int i=0;
			while((line = br.readLine()) != null)
			{
				staticScores[i++] = Double.parseDouble(line);
			}
			br.close();
		} catch (Exception e) {
			logger.error("Problem opening file: \""+inputFile+"\"", e);
		}
	}

	protected void loadOOS() {
		String inputFile = ApplicationSetup.getProperty("ssa.input.file","");
		try {
			java.io.ObjectInputStream ois = new java.io.ObjectInputStream(Files.openFileStream(inputFile));
			Object o = ois.readObject();
			if (o instanceof double[])
				staticScores = (double[]) o;
			else if (o instanceof float[])
				staticScores = castToDoubleArr((float[]) o);
			else if (o instanceof short[])
				staticScores = castToDoubleArr((short[]) o);
			else
				throw new ClassCastException("Inputfile contained " + o.getClass().getName() + " expected double[], short[] or float[]");
			printStats(staticScores);
			ois.close();
		} catch (Exception e) {
			logger.error("Problem opening file: \""+inputFile+"\"", e);
		}		
	}

	protected static void makeMaxMin(final double[] staticScores2) {
		StaTools.standardNormalisation(staticScores2);
	}

	@SuppressWarnings("unchecked")
	protected void loadDocno2score_seq(Index index) {
		String inputFile = ApplicationSetup.getProperty("ssa.input.file","");
		try {
			BufferedReader br = Files.openFileReader(inputFile);
			staticScores = new double[index.getCollectionStatistics().getNumberOfDocuments()];
			Iterator<String[]> metaIn = (Iterator<String[]>) index.getIndexStructureInputStream("meta");
			
			int docid = 0;
			boolean hasMoreMeta = metaIn.hasNext();
			int docnoMetaIndex = 0;
			String line = br.readLine();
			String[] meta = metaIn.next();
			boolean hasMoreFile = line != null;
			
			while(hasMoreFile && hasMoreMeta)
			{
				String meta_docno = meta[docnoMetaIndex];
				final String[] parts = line.split("\\s+");
				int cmp = meta_docno.compareTo(parts[0]);
				if (cmp < 0)
				{
					meta = metaIn.next();
					docid++;
					hasMoreMeta = metaIn.hasNext();
				}
				else if (cmp > 0)
				{
					line = br.readLine();
					hasMoreFile = line != null;
				}
				else
				{
					staticScores[docid] = Double.parseDouble(parts[1]);
				}
			}
			br.close();
			IndexUtil.close(metaIn);
			printStats(staticScores);
			
		} catch (Exception e) {
			logger.error("Problem opening file: \""+inputFile+"\"", e);
		}
	}

	protected void loadDocno2score(Index index) {
		String inputFile = ApplicationSetup.getProperty("ssa.input.file","");
		try {
			String line = null;
			BufferedReader br = Files.openFileReader(inputFile);
			staticScores = new double[index.getCollectionStatistics().getNumberOfDocuments()];
			MetaIndex m = index.getMetaIndex();
			while((line = br.readLine())!= null)
			{
				final String[] parts = line.split("\\s+");
				final int docid = m.getDocument("docno", parts[0]);
				if (docid < 0)
				{
					logger.warn("Docno " + parts[0] + " not found" );
					continue;
				}
				staticScores[docid] = Double.parseDouble(parts[1]);
			}			
			printStats(staticScores);
			br.close();
		} catch (Exception e) {
			System.err.println("Problem opening file: \""+inputFile+"\" : "+e);
			e.printStackTrace();
		}
	}

	protected static void negate(double[] staticScores2) {
		final int l = staticScores2.length;
		for(int i=0;i<l;i++)
			staticScores2[i] = -1.0d * staticScores2[i];
	}

	protected static void printStats(double ar[]) {
		double sum = 0;
		final int l = ar.length;
		for(int i=0;i<l;i++)
			sum += ar[i];
		System.err.println("Sum of array is "+ sum+ " average "+ (sum/(double)l));
	}

	protected static double[] castToDoubleArr(float[] f) {
		final int l = f.length;
		final double rtr[] = new double[l];
		for(int i=0;i<l;i++)
			rtr[i] = (double)f[i];
		return rtr;
	}

	protected static double[] castToDoubleArr(short[] f) {
		final int l = f.length;
		final double rtr[] = new double[l];
		for(int i=0;i<l;i++)
			rtr[i] = (double)f[i];
		return rtr;
	}
	/** Constructs an instance of SimpleStaticScoreModifier.*/
	public SimpleStaticScoreModifier() {
		super();
	}

	protected void initialise_parameters() {
		modifiedLength = Integer.parseInt(ApplicationSetup.getProperty("ssa.modified.length","1000"));
		w = Double.parseDouble(ApplicationSetup.getProperty("ssa.w","1"));
	}
	/** {@inheritDoc}*/@Override
	public boolean modifyScores(Index index, MatchingQueryTerms queryTerms, ResultSet set) {
		init(index);
		initialise_parameters();
		int minimum = modifiedLength;
		//if the minimum number of documents is more than the
		//number of documents in the results, aw.length, then
		//set minimum = aw.length
		
		if (minimum > set.getResultSize() || minimum == 0)
			minimum = set.getResultSize();

		logger.info("Applying "+ this.getClass().getSimpleName() + " to " + minimum + " retrieved documents");

		int[] docids = set.getDocids();
		double[] scores = set.getScores();
		int start = 0;
		int end = minimum;
		int altered = 0;
		 
		for(int i=start;i<end;i++)
		{
			if (scores[i] != 0.0d)
			{
				scores[i] += w * staticScores[docids[i]];
				altered++;
			}
		}
		
		logger.info("Altered " + altered + " doc scores");
		HeapSort.descendingHeapSort(scores, docids, set.getOccurrences(), set.getResultSize());
		return true;
	}
	/** {@inheritDoc}*/@Override
	public String getName() {
		return this.getClass().getSimpleName() + "_w"+ w;
	}
	/** {@inheritDoc}*/@Override
	public Object clone() {
		//object has no intended state
		return this;
	}

}