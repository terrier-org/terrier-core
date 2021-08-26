package org.terrier.matching;
import org.terrier.structures.Index;
public class TestDAATFullMatching extends TestMatching
{
    @Override
    protected Matching makeMatching(Index i)
    {
        return new org.terrier.matching.daat.Full(i);
    }

    @Override
    protected Class<? extends Matching> getMatchingClass() {
        return org.terrier.matching.daat.Full.class;
    }
}