package org.terrier.structures.postings;
import org.terrier.structures.Pointer;
import org.terrier.structures.EntryStatistics;
import java.io.IOException;

public class Postings {
 
    public static IterablePosting or(IterablePosting[] ips)  throws IOException {
        return ORIterablePosting.mergePostings(ips);
    }

    public static IterablePosting and(IterablePosting[] ips, Pointer[] p) throws IOException {
        return new ANDIterablePosting(ips, p);
    }

    public static IterablePosting and(IterablePosting[] ips, EntryStatistics[] p) throws IOException {
        return new ANDIterablePosting(ips, p);
    }

    public static IterablePosting chain(IterablePosting[] ips, boolean blocks, boolean fields) {
        return ChainIterablePosting.of(ips, blocks, fields);
    }

    public static IterablePosting shiftIds(IterablePosting ip, int delta, boolean blocks, boolean fields) {
        return ShiftIdIterablePosting.of(ip, delta, blocks, fields);
    }
}