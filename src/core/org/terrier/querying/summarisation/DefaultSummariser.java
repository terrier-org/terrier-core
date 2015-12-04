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
 * The Original is in 'DefaultSummariser.java'
 *
 * The Original Code is Copyright (C) 2004-2015 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
 *   Richard McCreadie <richard.mccreadie@glasgow.ac.uk>
 */
package org.terrier.querying.summarisation;

import java.util.regex.Pattern;

/** Summariser that was originally in the Decorate class, and formed
 * the Terrier release from 3.0
 * @since 3.6
 * @author Vassilis Plachouras, Craig Macdonald, Eric Sutherland
 */
public class DefaultSummariser extends Summariser {

	//the regular expression for splitting the text into sentences
    static final Pattern sentencePattern = Pattern.compile("\\.\\s+|!\\s+|\\|\\s+|\\?\\s+");
   
    //the regular expression for removing common endings from words - similar to very light stemming
    static final Pattern removeEndings = Pattern.compile("ing$|es$|s$");
	
	@Override
	public String generateSummary(String extract, String[] _qTerms) {
        int tmpSentence;
        double tmpScore;
        String[] sentences = sentencePattern.split(extract, 50); //use 50 sentences at most
        double[] sentenceScores = new double[sentences.length];
        int frsSentence = -1;
        int sndSentence = -1;
        int top1Sentence = 0;
        int top2Sentence = 0;
        double max1Score = -1;
        double max2Score = 0;
        final int qTermsLength = _qTerms.length;
        for (int i=0; i<qTermsLength; i++) {
            _qTerms[i] = removeEndings.matcher(_qTerms[i]).replaceAll("");
        }
        String lowerCaseSentence;
        int sentenceLength;
        final int sentencesLength = sentences.length;

        for (int i=0; i<sentencesLength; i++) {
   
            lowerCaseSentence = sentences[i].toLowerCase();
            sentenceLength=sentences[i].length();
            if (sentenceLength < 20 || sentenceLength > 250) {
                for (int j=0; j<qTermsLength; j++) {
                    if (lowerCaseSentence.indexOf(_qTerms[j])>=0) {
                        sentenceScores[i]+=1.0d + sentenceLength / (20.0d + sentenceLength);
                    }
                }
            } else {
                for (int j=0; j<qTermsLength; j++) {
                    if (lowerCaseSentence.indexOf(_qTerms[j])>=0) {
                        sentenceScores[i]+=_qTerms[j].length() + sentenceLength / (1.0d + sentenceLength);
                    }
                }
            }
            
            //do your best to get at least a second sentence for the snippet, 
            //after having found the first one
            if (frsSentence > -1 && sndSentence == -1 && sentenceLength > 5) {
                sndSentence = i;
            }

            //do your best to get at least one sentence for the snippet
            if (frsSentence == -1 && sentenceLength > 5) {
                frsSentence = i;
            }

            if (max2Score < sentenceScores[i]) {
                max2Score = sentenceScores[i];
                top2Sentence = i;
                //logger.debug("top 2 sentence is " + i);
                if (max2Score > max1Score) {
                    tmpScore = max1Score; max1Score = max2Score; max2Score = tmpScore;
                    tmpSentence = top1Sentence; top1Sentence = top2Sentence; top2Sentence = tmpSentence;
                }
            }

        }
        
        int lastIndexOfSpace = -1;
        String sentence="";
        String secondSentence="";
        String snippet = "";
        if (max1Score == -1) {
            if (frsSentence>=0) {
                sentence = sentences[frsSentence];
                if (sentence.length() > 100) {
                    lastIndexOfSpace = sentence.substring(0, 100).lastIndexOf(" ");
                    sentence = sentence.substring(0, lastIndexOfSpace > 0 ? lastIndexOfSpace : 100);
                }
            }
            if (sndSentence>=0) {
                secondSentence = sentences[sndSentence];
                if (secondSentence.length() > 100) {
                    lastIndexOfSpace = secondSentence.substring(0, 100).lastIndexOf(" ");
                    secondSentence = secondSentence.substring(0, lastIndexOfSpace>0 ? lastIndexOfSpace : 100);
                }
            }

            if (frsSentence >=0 && sndSentence >= 0)
                snippet = sentence.trim() + "..." + secondSentence.trim();
            else if (frsSentence >= 0 && sndSentence<0)
                snippet = sentence.trim();

        } else if (sentences[top1Sentence].length()<100 && top1Sentence!=top2Sentence) {
            sentence = sentences[top1Sentence];
            if (sentence.length() > 100) {
                lastIndexOfSpace = sentence.substring(0, 100).lastIndexOf(" ");
                sentence = sentence.substring(0, lastIndexOfSpace > 0 ? lastIndexOfSpace : 100);
            }

            secondSentence = sentences[top2Sentence];
            if (secondSentence.length() > 100) {
                lastIndexOfSpace = secondSentence.substring(0, 100).lastIndexOf(" ");
                secondSentence = secondSentence.substring(0, lastIndexOfSpace>0 ? lastIndexOfSpace : 100);
            }
            snippet = sentence.trim() + "..." + secondSentence.trim();
        } else {
            sentence = sentences[top1Sentence];
            if (sentence.length()>200) {
                lastIndexOfSpace = sentence.substring(0, 200).lastIndexOf(" ");
                sentence = sentence.substring(0, lastIndexOfSpace > 0 ? lastIndexOfSpace : 200);
            }
            snippet = sentence.trim();
        }
        return snippet;
	}

}
