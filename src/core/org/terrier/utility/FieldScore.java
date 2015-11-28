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
 * The Original Code is FieldScore.java.
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Douglas Johnson <johnsoda{a.}dcs.gla.ac.uk> (original author)
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk>
 */
package org.terrier.utility;
import java.util.HashSet;
import java.util.Set;
/**
 * A class for modifying the retrieval scores of documents, 
 * according to whether the query terms appear to any of the 
 * fields, or tags specified by the property 
 * <tt>FieldTags.process</tt>. These tags can be either HTML tags 
 * or tags such as DOCHDR, from the documents.<br>
 * If a query term appears in any of the specified tags, then the 
 * document score can be altered according to the values specified in 
 * the property <tt>field.modifiers</tt>. For example, if 
 * <tt>FieldTags.process=TITLE,H1,B</tt> and
 * <tt>field.modifiers=0.10,0.00,0.00</tt>, then if a query term 
 * appears in the title of a document, the document's score will be 
 * increased by a factor of 0.10.
 * 
 * @author Douglas Johnson, Vassilis Plachouras
  */
public class FieldScore {
	/**
	 * A hashset containing the fields which were encountered.
	 */
	private HashSet<String> scoreSet;
	/**
	 * The total number of tags to check for.
	 */
	public static int FIELDS_COUNT;
	/** Indicates whether field information is used.*/
	public static boolean USE_FIELD_INFORMATION;
	
	/**
	 * An integer assigned to each tag, used as 
	 * an internal representation.
	 */
	private static int[] FIELD_SCORES = null;
	/**
	 * The names of the fields to be processed. 
	 * The values are read from the property 
	 * <tt>FieldTags.process</tt>.
	 */
	public static String[] FIELD_NAMES = null;
	/**
	 * The percentage by which the score 
	 * of a document is modified.
	 */
	private static double[] FIELD_MODIFIERS = null;
	
	
	static {
		init();
	}
	
	/** Initialises the FieldTags tagset */
	public static void init()
	{
		TagSet htmlTags = new TagSet(TagSet.FIELD_TAGS);
		String toProcess = htmlTags.getTagsToProcess();
		FIELD_NAMES = toProcess.equals("") ? new String[0] : toProcess.split("\\s*,\\s*");
		FIELDS_COUNT = FIELD_NAMES.length;
		
		if ( FIELDS_COUNT >0
			&&
			! ApplicationSetup.getProperty("field.disabled.in.structures","false").equals("true")
			) 
		{
			USE_FIELD_INFORMATION = false;
		}
		
		if (FIELDS_COUNT > 0) {
			USE_FIELD_INFORMATION = true;
			FIELD_SCORES = new int[FIELDS_COUNT];
			FIELD_SCORES[FIELDS_COUNT - 1] = 1;
			for (int j = FIELDS_COUNT - 2; j >= 0; j--)
				FIELD_SCORES[j] = FIELD_SCORES[j + 1] * 2;
	
			FIELD_MODIFIERS = new double[FIELDS_COUNT];
			int i=0;
			for (String token : ApplicationSetup.getProperty("field.modifiers", "").split("\\s*,\\s*"))
			{
				if (token.length() == 0)
					continue;
				FIELD_MODIFIERS[i] = Double.parseDouble(token);
				i++;
			}
		} else {
			USE_FIELD_INFORMATION = false;
		}
	}
	/**
	 * The default constructor of the class.
	 */
	public FieldScore() {
		if (FIELDS_COUNT>0) 
			scoreSet = new HashSet<String>();
	}
	/**
	 * Computes an field score associated with a 
	 * page. If we check for n different tags, we 
	 * use n bits. Each tag is associated with a 
	 * specific bit. The method returns the binary 
	 * number corresponding to the bits which are 
	 * set to <tt>1</tt>.
	 * @return int a number representing the tags 
	 *         which are in the set
	 */
	public int getFieldScore() {
		int score = 0;
		for (int i = 0; i < FIELDS_COUNT; i++) {
			if (scoreSet.contains(FIELD_NAMES[i]))
				score += FIELD_SCORES[i];
		}
		return score;
	}
	/**
	 * Add a tag in the corresponding score set, 
	 * unless the number of tags to process is zero.
	 * @param tag String the name of the tag to be added.
	 */
	public void insertField(String tag) {
		if (FIELDS_COUNT> 0) 
			scoreSet.add(tag);
	}
	/**
	 * Add tags in the corresponding score set, unless the 
	 * number of tags to process is zero.
	 * @param tags String[] the names of the tags to be added.
	 */
	public void insertFields(String[] tags) {
		for (int i=0; i<tags.length; i++) {
			scoreSet.add(tags[i]);
		}
	}
	/**
	 * Adds tags in the corresponding score set, unless
	 * the number of tags to process is zero.
	 * @param tags HashSet a hashset with the tags to add.
	 */
	public void insertFields(Set<String> tags) {
		if (tags!=null && tags.size()>0)
			scoreSet.addAll(tags);
	}
	
	/**
	 * Computes the modified score for a document with a given 
	 * fieldScore (a bitset where each bit represents a tag) and
	 * its original score. If the number of tags is equal to zero
	 * then we return zero.
	 * @param fieldScore int the bitset where each bit represents 
	 *        whether a specific tag exists.
	 * @param score the original score.
	 * @return the modified score, according to the found field tags.
	 */
	public static double applyFieldScoreModifier(int fieldScore, double score) {
		double tag_score = 0.0d;
		for (int i = 0; i < FIELDS_COUNT; i++) {
			if (fieldScore >= FIELD_SCORES[i]) {
				tag_score += score * FIELD_MODIFIERS[i];
				fieldScore -= FIELD_SCORES[i];
			}
		}
		return tag_score;
	}
	/**
	 * Updates the values of the tag modifiers. If the length of 
	 * the modifiers is different than the length of the already
	 * used modifiers, then the original modifiers are not replaced.
	 * @param modifiers double[] the new values of the modifiers.
	 */
	public static void updateModifiers(double[] modifiers) {
		if (modifiers.length != FIELD_MODIFIERS.length)
			return;
		FIELD_MODIFIERS = modifiers;
	}
	/**
	 * Returns the modifiers scores.
	 * @return the array of the modifiers.
	 */
	public static double[] getModifiers() {
		return FIELD_MODIFIERS;
	}
}
