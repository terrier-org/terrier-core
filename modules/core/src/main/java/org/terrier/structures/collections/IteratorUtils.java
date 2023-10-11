package org.terrier.structures.collections;

import java.util.function.BiFunction;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;


public class IteratorUtils {

    public static <T> Iterator<Map.Entry<T,Integer>> addOffset(final Iterator<T> iter, int offset) {
        return new Iterator<Map.Entry<T,Integer>>() {
            
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Map.Entry<T,Integer> next() {
                return new MapEntry<T,Integer>(iter.next(), offset);
            }
        };
    }

    public static <X,Y> Iterator<Map.Entry<X,Y>> zip(final Iterator<X> iterX, final Iterator<Y> iterY) {
        return new Iterator<Map.Entry<X,Y>>() {
            
            @Override
            public boolean hasNext() {
                return iterX.hasNext();
            }

            @Override
            public Map.Entry<X,Y> next() {
                return new MapEntry(iterX.next(), iterY.next());
            }
        };
    }

    @SafeVarargs
    public static <T> Iterator<T> merge(Comparator<? super T> ordering, Iterator<T>... iterators) {
        return merge(ordering, iterators, 0, iterators.length);
    }

    @SafeVarargs
    public static <T> Iterator<T> merge(Comparator<? super T> ordering, BiFunction<T,T,T> merger, Iterator<T>... iterators) {
        return merge(ordering, merger, iterators, 0, iterators.length);
    }

    private static <T> Iterator<T> merge(Comparator<? super T> ordering, Iterator<T>[] iterators, int offset, int length) {
        if (length == 0) {
            return Collections.emptyIterator();
        }
        if (length == 1) {
            return iterators[offset];
        }
        return new MergedIterator<>(ordering,
            merge(ordering, iterators, offset, length / 2),
            merge(ordering, iterators, offset + length / 2, length - (length / 2)));
    }

    private static <T> Iterator<T> merge(Comparator<? super T> ordering,  BiFunction<T,T,T> merger, Iterator<T>[] iterators, int offset, int length) {
        if (length == 0) {
            return Collections.emptyIterator();
        }
        if (length == 1) {
            return iterators[offset];
        }
        return new MergingMergedIterator<>(ordering, merger,
            merge(ordering, merger, iterators, offset, length / 2),
            merge(ordering, merger, iterators, offset + length / 2, length - (length / 2)));
    }

    static class MergedIterator<T> implements Iterator<T> {
        final Comparator<? super T> comparator;
        final PeekingIterator<T> first;
        final PeekingIterator<T> second;

        private MergedIterator(Comparator<? super T> comparator, Iterator<T> first, Iterator<T> second) {
            this.comparator = comparator;
            this.first = Iterators.peekingIterator(first);
            this.second = Iterators.peekingIterator(second);
        }

        @Override
        public boolean hasNext() {
            return first.hasNext() || second.hasNext();
        }

        @Override
        public T next() {
            if (!first.hasNext()) {
                if (!second.hasNext()) {
                    throw new NoSuchElementException();
                }
                return second.next();
            }

            return !second.hasNext() || (comparator.compare(first.peek(), second.peek()) <= 0) ? first.next() : second.next();
        }
    }

    static class MergingMergedIterator<T> extends MergedIterator<T> {
        BiFunction<T,T,T> merger;

        private MergingMergedIterator(
            Comparator<? super T> comparator, BiFunction<T,T,T> merger, Iterator<T> first, Iterator<T> second) {
            super(comparator, first, second);
            this.merger = merger;
        }

        @Override
        public T next() {
            if (!first.hasNext()) {
                if (!second.hasNext()) {
                    throw new NoSuchElementException();
                }
                return second.next();
            }
            if (! second.hasNext()) {
                return first.next();
            }
            final int cmp = comparator.compare(first.peek(), second.peek());
            if (cmp == 0)
                return merger.apply(first.next(), second.next()); //move both interators on
            else if (cmp > 0)
                return second.next();
            return first.next();
        }
    }


}
