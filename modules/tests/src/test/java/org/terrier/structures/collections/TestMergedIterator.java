package org.terrier.structures.collections;
import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class TestMergedIterator {
    @Test
    public void merge() {
        Iterator<Integer> stream1 = Iterators.forArray(4, 6);
        Iterator<Integer> stream3 = Iterators.forArray(1, 3, 5, 7, 9, 11);
        Iterator<Integer> stream2 = Iterators.forArray(2, 8, 10);

        Iterator<Integer> merged = IteratorUtils.merge(Ordering.natural(), stream1, stream2, stream3);

        var theList = Lists.newArrayList(merged);
        assertEquals(11, theList.size());
        assertEquals(1, theList.get(0).intValue());
        assertEquals(11, theList.get(theList.size()-1).intValue());
    }
}