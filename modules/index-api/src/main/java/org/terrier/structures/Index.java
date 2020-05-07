package org.terrier.structures;

import java.io.Closeable;
import org.terrier.querying.IndexRef;
import java.io.IOException;

/** Base class for all Index implementations */
public abstract class Index implements Closeable 
{
    /** Get the collection statistics */
    public abstract CollectionStatistics getCollectionStatistics();

    /** Return the DirectIndex associated with this index */
    public abstract PostingIndex<?> getDirectIndex();

    /** Return the DocumentIndex associated with this index */
    public abstract DocumentIndex getDocumentIndex();
    
    /** Returns a direct IndexRef to this index */
    public abstract IndexRef getIndexRef();

    /** Returns the InvertedIndex to use for this index */
    public abstract PostingIndex<?> getInvertedIndex();

    /** Return the Lexicon associated with this index */
    public abstract Lexicon<String> getLexicon();

    /** Get the Meta Index structure */
    public abstract MetaIndex getMetaIndex();

    /** Return a particular index data structure */
    public Object getIndexStructure(String structureName) 
    {
        if (structureName.equals("direct"))
            return getDirectIndex();
        if (structureName.equals("inverted"))
            return getInvertedIndex();
        if (structureName.equals("lexicon"))
            return getLexicon();
        if (structureName.equals("meta"))
            return getMetaIndex();
        if (structureName.equals("document"))
            return getDocumentIndex();
        if (structureName.equals("collectionstatistics"))
            return getCollectionStatistics();
        return null;
    }

    /** Return a particular index data structure input stream */
    public Object getIndexStructureInputStream(String structureName) 
    {
        return null;
    }
    
    /** Returns true iff the structure exists */
    public boolean hasIndexStructure(String structureName) 
    {
        return getIndexStructure(structureName) != null;
    }

    /** Returns true iff the structure input stream exists */
    public boolean hasIndexStructureInputStream(String structureName) 
    {
        return false;
    }

    /** Returns the first docid in this index **/
    public int getStart() 
    {
        return 0;
    }
    
    /** Returns the last docid in this index **/
    public int getEnd() 
    {
        return this.getCollectionStatistics().getNumberOfDocuments()-1;
    }

    @Override
    public void close() throws IOException 
    {}

    protected static IndexRef makeDirectIndexRef(Index index) 
    {
        return new DirectIndexRef(index);
    }

    public static class DirectIndexRef extends IndexRef
    {
        private static final long serialVersionUID = 1L;
        Index underlyingIndex;
        
        DirectIndexRef(Index i)
        {
            super(i.toString());// THIS IS A HACK
            this.underlyingIndex = i;
        }

        public Index getIndex() {
            return this.underlyingIndex;
        }
    }
    
    /** @Deprecated */ 
    @Deprecated
    public static void setIndexLoadingProfileAsRetrieval(boolean yes) {
        try{
            Index.class.getClassLoader()
                .loadClass("org.terrier.structures.IndexOnDisk")
                .getMethod("setIndexLoadingProfileAsRetrieval", new Class<?>[]{Boolean.TYPE})
                .invoke(null, new Object[]{yes});
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
    
    /** @Deprecated */
    @Deprecated
    public static Index createIndex() {
        /* This method is now deprecated, but we need it still to work.
         * We use reflection, as IndexOnDisk is in a separate module. 
         */
        try{
            return (Index) Index.class.getClassLoader()
                .loadClass("org.terrier.structures.IndexOnDisk")
                .getMethod("createIndex", new Class<?>[0])
                .invoke(null, new Object[0]);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** @Deprecated */
    @Deprecated
    public static Index createIndex(String path, String prefix) {
        /* This method is now deprecated, but we need it still to work.
         * We use reflection, as IndexOnDisk is in a separate module. 
         */
        try{
            return (Index) Index.class.getClassLoader()
                .loadClass("org.terrier.structures.IndexOnDisk")
                .getMethod("createIndex", new Class<?>[]{String.class, String.class})
                .invoke(null, new Object[]{path, prefix});
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}