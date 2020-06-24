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
 * The Original Code is CollectionStatistics.java.
 *
 * The Original Code is Copyright (C) 2004-2020 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> 
 */
package org.terrier.structures;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import org.apache.hadoop.io.Writable;

/**
 * This class provides basic statistics for the indexed
 * collection of documents, such as the average length of documents,
 * or the total number of documents in the collection. <br>
 * After indexing, statistics are saved in the PREFIX.log file, along
 * with the classes that should be used for the Lexicon, the DocumentIndex,
 * the DirectIndex and the InvertedIndex. This means that an index knows
 * how it was build and how it should be opened again.
 *
 * @author Gianni Amati, Vassilis Plachouras, Craig Macdonald
 */
@ConcurrentReadable
public class CollectionStatistics implements Serializable,Writable 
{
    private static final long serialVersionUID = 1L;

    /** Number of fields used to index */
    protected int numberOfFields;
    /** Number of tokens in each field */
    protected long[] fieldTokens;
    /** Average length of each field */
    protected double[] avgFieldLengths;
    /** Field names */
    protected String[] fieldNames;
    
    /** Total number of documents in the collection.*/
    protected int numberOfDocuments;
    
    /** Total number of tokens in the collection.*/
    protected long numberOfTokens;
    
    /** 
     * Total number of pointers in the inverted file.
     * This corresponds to the sum of the document frequencies for
     * the terms in the lexicon.
     */
    protected long numberOfPointers;
    
    /**
     * Total number of unique terms in the collection.
     * This corresponds to the number of entries in the lexicon.
     */
    protected int numberOfUniqueTerms;
    
    /**
     * Average length of a document in the collection.
     */
    protected double averageDocumentLength;

    /** Does the index have positions */
    protected boolean hasPositions;
    
    @Deprecated
    public CollectionStatistics(int numDocs, int numTerms, long numTokens, long numPointers, final long[] _fieldTokens, final String[] _fieldNames)
    {
        this(numDocs, numTerms, numTokens, numPointers, _fieldTokens, _fieldNames, false);
    }
    /**
     * Constructs an instance of the class.
     * 
     * @param numDocs the number of documents in the collection.
     * @param numTerms the number of terms in the collection.
     * @param numTokens the number of tokens in the collection.
     * @param numPointers the number of pointers in the inverted file.
     * @param _fieldTokens the number of tokens in each field.
     * @param _fieldNames the field names.
     */
    public CollectionStatistics(int numDocs, int numTerms, long numTokens, long numPointers, final long[] _fieldTokens, final String[] _fieldNames, boolean positions)
    {
        assert _fieldTokens != null && _fieldNames != null && _fieldNames.length ==_fieldTokens.length;

        numberOfDocuments = numDocs;
        numberOfUniqueTerms = numTerms;
        numberOfTokens = numTokens;
        numberOfPointers = numPointers;
        fieldTokens = _fieldTokens;
        fieldNames = _fieldNames;
        
        numberOfFields = _fieldTokens.length;
        avgFieldLengths = new double[numberOfFields];
        hasPositions = positions;
        recalculateAverageLengths();
    }
    
    /**
     * Default constructor.
     */
    public CollectionStatistics()
    {
    }
    
    protected void recalculateAverageLengths()
    {
        if (numberOfDocuments != 0) {
            averageDocumentLength = (double)numberOfTokens / (double)numberOfDocuments;
            for (int fi = 0; fi < numberOfFields; fi++)
                avgFieldLengths[fi] = (double)fieldTokens[fi] / (double)numberOfDocuments;
        } else {
            averageDocumentLength = 0.0D;
            Arrays.fill(avgFieldLengths, 0.0d);
        }
    }
    
    @Override
    public String toString()
    {
        return 
            "Number of documents: " + getNumberOfDocuments()           + "\n" + 
            "Number of terms: "     + getNumberOfUniqueTerms()         + "\n"+ 
            "Number of fields: "    + getNumberOfFields()              + "\n" + 
            "Number of tokens: "    + getNumberOfTokens()              + "\n" +
            "Field names: "         + Arrays.toString(getFieldNames()) + "\n" +
            "Positions:   "         + hasPositions + "\n";
    }

    /** Returns true if the inverted index will have position informat */
    public boolean hasPositions() {
        return hasPositions;
    }
    
    /**
     * Returns the documents' average length.
     * 
     * @return the average length of the documents in the collection.
     */
    public double getAverageDocumentLength() 
    {
        return averageDocumentLength;
    }
    
    /**
     * Returns the total number of documents in the collection.
     * 
     * @return the total number of documents in the collection.
     */
    public int getNumberOfDocuments() 
    {
        return numberOfDocuments;
    }
    
    /**
     * Returns the total number of pointers in the collection.
     * 
     * @return the total number of pointers in the collection.
     */
    public long getNumberOfPointers() 
    {
        return numberOfPointers;
    }
    
    /**
     * Returns the total number of tokens in the collection.
     * @return the total number of tokens in the collection.
     */
    public long getNumberOfTokens() 
    {
        return numberOfTokens;
    }
    
    /**
     * Returns the total number of unique terms in the lexicon.
     * 
     * @return the total number of unique terms in the lexicon.
     */
    public int getNumberOfUniqueTerms() 
    {
        return numberOfUniqueTerms;
    }
    
    /** 
     * Returns the number of fields being used to index. 
     * 
     * @return the number of fields being used to index.
     */
    public int getNumberOfFields()
    {
        return numberOfFields;
    }
    
    /** 
     * Returns the length of each field in tokens. 
     * 
     * @return the length of each field in tokens.
     */
    public long[] getFieldTokens()
    {
        return fieldTokens;
    }
    
    /** 
     * Returns the average length of each field in tokens.
     * 
     * @return the average length of each field in tokens.
     */
    public double[] getAverageFieldLengths()
    {
        return avgFieldLengths;
    }

    /** 
     * Returns the field names.
     * 
     * @return the field names.
     */
    public String[] getFieldNames()
    {
        return fieldNames;
    }
    
    /** 
     * Increment the collection statistics with the provided collection statistics. 
     * 
     * @param cs the collection statistics to use to increment.
     * */
    public void addStatistics(final CollectionStatistics cs)
    {
        numberOfDocuments += cs.getNumberOfDocuments();
        numberOfPointers += cs.getNumberOfPointers();
        numberOfTokens += cs.getNumberOfTokens();
        numberOfUniqueTerms = Math.max(cs.getNumberOfUniqueTerms(), numberOfUniqueTerms);
        final long[] otherFieldTokens = cs.getFieldTokens();
        for (int fi = 0; fi < numberOfFields; fi++)
            fieldTokens[fi] += otherFieldTokens[fi];
        recalculateAverageLengths();
    }

    @Override
    public void readFields(DataInput in) throws IOException 
    {
        numberOfDocuments = in.readInt();
        numberOfUniqueTerms = in.readInt();
        numberOfTokens = in.readLong();
        numberOfPointers = in.readLong();
        numberOfFields = in.readInt();
        fieldTokens = new long[numberOfFields];
        avgFieldLengths = new double[numberOfFields];
        fieldNames = new String[numberOfFields];
        for (int fi = 0; fi < numberOfFields; fi++) {
            fieldTokens[fi] = in.readLong();
            fieldNames[fi] = in.readUTF();
        }
        hasPositions = in.readBoolean();
        recalculateAverageLengths();
    }

    public void readFieldsV5(DataInput in) throws IOException 
    {
        numberOfDocuments = in.readInt();
        numberOfUniqueTerms = in.readInt();
        numberOfTokens = in.readLong();
        numberOfPointers = in.readLong();
        numberOfFields = in.readInt();
        fieldTokens = new long[numberOfFields];
        avgFieldLengths = new double[numberOfFields];
        fieldNames = new String[numberOfFields];
        for (int fi = 0; fi < numberOfFields; fi++) {
            fieldTokens[fi] = in.readLong();
            fieldNames[fi] = in.readUTF();
        }
        recalculateAverageLengths();
    }

    @Override
    public void write(DataOutput out) throws IOException 
    {
        out.writeInt(numberOfDocuments);
        out.writeInt(numberOfUniqueTerms);
        out.writeLong(numberOfTokens);
        out.writeLong(numberOfPointers);
        out.writeInt(numberOfFields);
        for (int fi = 0; fi < numberOfFields; fi++) {
            out.writeLong(fieldTokens[fi]);
            out.writeUTF(fieldNames[fi]);
        }
        out.writeBoolean(hasPositions);
    }
}
