package org.terrier.matching;
import org.terrier.structures.Index;
public class TestTAATFullMatching extends TestMatching
{
    @Override
    protected Matching makeMatching(Index i)
    {
        return new org.terrier.matching.taat.Full(i);
    }

    @Override
    protected Class<? extends Matching> getMatchingClass() {
        return org.terrier.matching.taat.Full.class;
    }		
}