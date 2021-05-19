package org.terrier.structures.concurrent;
import java.io.IOException;
import org.terrier.structures.ConcurrentReadable;
import org.terrier.structures.MetaIndex;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@ConcurrentReadable
public class ConcurrentDecodingMetaIndex implements MetaIndex {

    ExecutorService es;
    MetaIndex parent;

    public ConcurrentDecodingMetaIndex(MetaIndex _parent) {
        es = Executors.newCachedThreadPool();
        parent = _parent;
    }

    public Future<String[]> getItemsFuture(final String[] Keys, final int docid ) {
        return es.submit(() -> { return parent.getItems(Keys, docid);  } );
    }

    public Future<String> getItemFuture(final String key, final int docid ) {
        return es.submit(() -> { return parent.getItem(key, docid);  } );
    }

    public String[][] getItems(final String Keys[], int[] docids) throws IOException {

        try{
            Future<String[]>[] results = new Future[docids.length];
            for(int i=0;i<docids.length;i++) {
                results[i] =  getItemsFuture(Keys, docids[i]);           
            }
            String rtr[][] = new String[docids.length][];
            for(int i=0;i<docids.length;i++) {
                rtr[i] = results[i].get();
            }
            return rtr;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public String[] getItems(final String Key, int[] docids) throws IOException {
        try{
            Future<String>[] results = new Future[docids.length];
            for(int i=0;i<docids.length;i++) {
                results[i] =  getItemFuture(Key, docids[i]);           
            }
            String rtr[] = new String[docids.length];
            for(int i=0;i<docids.length;i++) {
                rtr[i] = results[i].get();
            }
            return rtr;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public String getItem(String key, int docid) throws IOException {
        return parent.getItem(key, docid);
    }

    public String[] getItems(String keys[], int docid) throws IOException {
        return parent.getItems(keys, docid);
    }

    public String[] getKeys() {
        return parent.getKeys();
    }

    public String[] getAllItems(int docid) throws IOException {
        return getItems(parent.getKeys(), docid);
    }

    public int getDocument(String key, String value) throws IOException {
        return parent.getDocument(key, value);
    }

    public int size() {
        return parent.size();
    }

    public void close() throws IOException {
        es.shutdownNow();
        parent.close();
    }

}