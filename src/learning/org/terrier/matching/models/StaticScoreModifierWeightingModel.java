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
 * The Original Code is StaticScoreModifierWeightingModel.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 */

package org.terrier.matching.models;

import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntFloatHashMap;

import java.io.BufferedReader;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terrier.structures.postings.Posting;
import org.terrier.utility.Files;
import org.terrier.utility.StaTools;
import org.terrier.utility.TerrierTimer;

/** Base abstract class for query independent features loaded from file.
 * For types of file are supported:
 * <ul>
 * <li>oos/ois: an {@link java.io.ObjectOutputStream} file of float[] or double[] array</li>
 * <li>docid2score: a text file containing [docid] [score]</li>
 * <li>listofscores: a text file containing one score per line</li>
 * <li>tmap: a {@link TIntDoubleHashMap} saved in an {@link java.io.ObjectOutputStream} file</li>
 * </ul>
 * @since 4.0 
 * @author Craig Macdonald
 */
public abstract class StaticScoreModifierWeightingModel extends WeightingModel {

	private static final long serialVersionUID = 1L;
	private static final Pattern SPLIT_SPACE = Pattern.compile("\\s+");

	static final Logger logger = LoggerFactory.getLogger(StaticScoreModifierWeightingModel.class);
	
	double[] staticScores;
	float[] FstaticScores;
	final boolean asFloat;
	final boolean map;
	TIntDoubleHashMap staticMap;
	TIntFloatHashMap FstaticMap;
	String source;
	
	public final double getScoreD(int docid)
	{
		return map
			? staticMap.get(docid)
			: staticScores[docid];
	}

	public final float getScoreF(int docid)
	{
		return map
			? FstaticMap.get(docid)
			: FstaticScores[docid];
	}
	
	public final String getSource()
	{
		return source;
	}
	
	
	StaticScoreModifierWeightingModel(final double[] scores)
	{
		map = false;
		asFloat = false;
		staticScores = scores;
	}
	
	StaticScoreModifierWeightingModel(final TIntDoubleHashMap scores)
	{
		map = false;
		asFloat = false;
		staticMap = scores;
	}
	
	StaticScoreModifierWeightingModel(String[] params)
	{
		if (params[0].equals("OOS")||params[0].equals("OIS"))
		{
			map = false; staticMap = null; FstaticMap = null;
			if (params.length > 2 && params[2].equals("float"))
			{
				asFloat = true;
				loadOOS(params[1]);
			}
			else
			{
				asFloat = false;
				loadOOS(params[1]);
			}
			source = params[1];
		}
		else if (params[0].equals("docid2score"))
		{
			asFloat = false;
			map = false; staticMap = null; FstaticMap = null;
			if (params.length == 4)
				loadDocid2score(Integer.parseInt(params[1]), params[2], Integer.parseInt(params[3]));
			else
				loadDocid2score(Integer.parseInt(params[1]), params[2], 2);
			source = params[2];
		}
		else if (params[0].equals("listofscores")||params[0].equals("listofscore"))
		{
			asFloat = false;
			map = false; staticMap = null; FstaticMap = null;
			if (params.length == 4)
				loadScorefile(Integer.parseInt(params[1]), params[2], Integer.parseInt(params[3]));
			else
				loadScorefile(Integer.parseInt(params[1]), params[2], 1);
			source = params[2];
		}
		else if (params[0].equals("tmap")) //TODO add float tmap support
		{
			asFloat = false;
			map = true;
			FstaticMap = null;
			loadOOS_Map(params[1]);
			source = "tmap";
		}
		else
		{
			throw new UnsupportedOperationException("Unknown feature file type: " + params[0]);
		}
		if (! map)
			if (asFloat)
				standardNormalisation(FstaticScores);
			else
				StaTools.standardNormalisation(staticScores);
	}
	
	@Override
	public abstract double score(Posting p);

	
	@Override
	public String getInfo() {
		return null;
	}

	@Override
	public double score(double tf, double docLength) {
		throw new UnsupportedOperationException();
	}
	
	protected void loadDocid2score(int numDocs, String inputFile, final int column) {
		logger.info("Opening docid2score feature file " + inputFile);
		try {
			String line = null;
			BufferedReader br = Files.openFileReader(inputFile);
			staticScores = new double[numDocs];
			while((line = br.readLine())!= null)
			{
				final String[] parts = SPLIT_SPACE.split(line, column+1);
				staticScores[Integer.parseInt(parts[0])] = Double.parseDouble(parts[column-1]);	
			}			
			printStats(staticScores);
			br.close();
		} catch (Exception e) {
			System.err.println("Problem opening file: \""+inputFile+"\" : "+e);
			e.printStackTrace();
		}
	}
	
	protected void loadScorefile(int numDocs, String inputFile, final int column) {
		logger.info("Opening listofscores feature file " + inputFile);
		try {
			staticScores = new double[numDocs];
			BufferedReader br = Files.openFileReader(inputFile);
			String line = null;
			int i=0;
			TerrierTimer tt = new TerrierTimer("Loading score file", numDocs);
			tt.start();
			while((line = br.readLine()) != null)
			{
				String[] parts = SPLIT_SPACE.split(line, column+1);
				staticScores[i++] = Double.parseDouble(parts[column -1]);
				tt.increment();
			}
			tt.finished();
			br.close();
		} catch (Exception e) {
			logger.error("Problem opening file: \""+inputFile+"\"", e);
		}
	}
	
	protected void loadOOS(String inputFile) {
		logger.info("Opening OOS feature file " + inputFile);
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
	
	private void loadOOS_Map(String inputFile) {
		logger.info("Opening OOS feature file from map " + inputFile);
		try {
			java.io.ObjectInputStream ois = new java.io.ObjectInputStream(Files.openFileStream(inputFile));
			Object o = ois.readObject();
			if (o instanceof TIntDoubleHashMap)
				staticMap = (TIntDoubleHashMap) o;
			else
				throw new ClassCastException("Inputfile contained " + o.getClass().getName() + " expected double[], short[] or float[]");
			//printStats(staticScores);
			ois.close();
		} catch (Exception e) {
			logger.error("Problem opening file: \""+inputFile+"\"", e);
		}		
	}
	
	protected void loadfloatOOS(String inputFile) {
		logger.info("Opening OOS feature file " + inputFile);
		try {
			java.io.ObjectInputStream ois = new java.io.ObjectInputStream(Files.openFileStream(inputFile));
			Object o = ois.readObject();
			if (o instanceof float[])
				FstaticScores = (float[]) o;
			else if (o instanceof double[])
				FstaticScores = castToFloatArr((double[]) o);
			else if (o instanceof short[])
				FstaticScores = castToFloatArr((short[]) o);
			else
				throw new ClassCastException("Inputfile contained " + o.getClass().getName() + " expected double[], short[] or float[]");
			printStats(staticScores);
			ois.close();
		} catch (Exception e) {
			logger.error("Problem opening file: \""+inputFile+"\"", e);
		}		
	}
	
	
	protected static void printStats(double ar[]) {
		double sum = 0;
		final int l = ar.length;
		for(int i=0;i<l;i++)
			sum += ar[i];
		System.err.println("Sum of array of length "+l+" is "+ sum+ " average "+ (sum/(double)l));
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
	
	protected static float[] castToFloatArr(double[] f) {
		final int l = f.length;
		final float rtr[] = new float[l];
		for(int i=0;i<l;i++)
			rtr[i] = (float)f[i];
		return rtr;
	}

	protected static float[] castToFloatArr(short[] f) {
		final int l = f.length;
		final float rtr[] = new float[l];
		for(int i=0;i<l;i++)
			rtr[i] = (float)f[i];
		return rtr;
	}
	
	/** Normalises the data in the specified array to be in range [0,1], with
	 * 0 as the minimum, and 1 as the maximum. RETURNS THE SAME ARRAY OBJECT
	 *  - i.e. changes are made in place.
	 * @param data
	 */
	public static float[] standardNormalisation(final float[] data)
	{
		final int l = data.length;
		if (l==0)
			return data;
		final float min = min(data);
		final float max = max(data);
		if (max == 0 && min == 0)
			return data;
		final float product =  (max != min) ? 1.0f/ ( max - min) : 1.f/max;
		for(int i=0;i<l;i++)
		{
			data[i] = (data[i] - min) * product;
		}
		return data;
	}
	
	 /** Return the min of the specified array
     * @param a the array
     * @return the minimum value in the arrays */
    public static final float min(final float[] a)
    {
    	float min = a[0];
    	for(float i : a)
    		if (i < min)
    			min = i;
    	return min;
    }
	
    /** Return the max of the specified array
     * @param a the array
     * @return the maximum value in the arrays */
    public static final float max(final float[] a)
    {
    	float max = a[0];
    	for(float i : a)
    		if (i > max)
    			max = i;
    	return max;
    }
	
}
