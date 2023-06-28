package org.terrier.structures.indexing;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import org.terrier.utility.*;
import org.terrier.structures.*;
import org.terrier.structures.seralization.FixedSizeWriteableFactory;
import org.terrier.structures.seralization.FixedSizeTextFactory;
import org.terrier.structures.postings.IterablePosting;
import org.terrier.structures.indexing.CompressionFactory.CompressionConfiguration;

/** Utility class for writing an existing index to disk */
public class DiskIndexWriter {

    String path, prefix;
    boolean blocks = false;
    String[] fields = new String[0];
    boolean direct = false;

    public DiskIndexWriter(String path, String prefix) {
        this.path = path;
        this.prefix = prefix;
    }

    public DiskIndexWriter setFields(String[] fields) {
        this.fields = fields;
        return this;
    }

    public DiskIndexWriter setBlocks(boolean blocks)
    {
        this.blocks = blocks;
        return this;
    }

    public DiskIndexWriter withBlocks() {
        this.blocks = true;
        return this;
    }

    public DiskIndexWriter setDirect(boolean direct)
    {
        this.direct = direct;
        return this;
    }

    public DiskIndexWriter withDirect() {
        this.direct = true;
        return this;
    }

    @SuppressWarnings("unchecked")
    public IndexOnDisk write(Index source) throws IOException {
        IndexOnDisk target = IndexOnDisk.createNewIndex(path, prefix);
        CompressionConfiguration compressionDirectConfig = null;
        

        /*
         * Direct Index and Document Structure
         */
        DocumentIndexBuilder docOut = new DocumentIndexBuilder(target, "document");
        if (direct)
        {
            compressionDirectConfig = CompressionFactory.getCompressionConfiguration("direct", fields, 0, 0);
        
            
            AbstractPostingOutputStream directIndexBuilder = compressionDirectConfig.getPostingOutputStream(
                    path + ApplicationSetup.FILE_SEPARATOR + prefix + "." + "direct" + compressionDirectConfig.getStructureFileExtension());
            
            
            PostingIndexInputStream postingIterator = (PostingIndexInputStream) source.getIndexStructureInputStream("direct");
            DocumentIndex document = source.getDocumentIndex();

            int docid =0;
            while (postingIterator.hasNext()) {
                IterablePosting directPosting = postingIterator.next();
                BitIndexPointer pointer = directIndexBuilder.writePostings(directPosting);
                DocumentIndexEntry die = document.getDocumentEntry(docid);
                die.setBitIndexPointer(pointer);
                docOut.addEntryToBuffer(die);
                docid++;
            }
            directIndexBuilder.close();
            
        } else {
            Iterator<DocumentIndexEntry> docIter = (Iterator<DocumentIndexEntry>) source
				.getIndexStructureInputStream("document");
        
            int docs = source.getCollectionStatistics().getNumberOfDocuments();
            for(int i=0;i<docs;i++){
                DocumentIndexEntry die = docIter.next();
			    docOut.addEntryToBuffer(die);
            }
            IndexUtil.close(docIter);
        }
        docOut.finishedCollections();
        docOut.close();


        /** metaindex */
        Iterator<String[]> metaIter = (Iterator<String[]>) source.getIndexStructureInputStream("meta");
        final String metaBuilderName = ApplicationSetup.getProperty("indexer.meta.builder", ZstdMetaIndexBuilder.class.getName());
		MetaIndexBuilder metaOut = MetaIndexBuilder.create(
            metaBuilderName, 
            target, 
            source.getMetaIndex().getKeys(), 
            ArrayUtils.parseCommaDelimitedInts(ApplicationSetup.getProperty("indexer.meta.forward.keylens", "")), 
            ArrayUtils.parseCommaDelimitedString(ApplicationSetup.getProperty("indexer.meta.reverse.keys", "")));
		while(metaIter.hasNext()){
			metaOut.writeDocumentEntry(metaIter.next());
		}
		metaOut.close();

        /*
         * Lexicon and inverted index.
         */
        CompressionConfiguration compressionInvertedConfig = CompressionFactory.getCompressionConfiguration("inverted", fields, 0,0);

        Iterator<Entry<String, LexiconEntry>> lexIN = (Iterator<Entry<String, LexiconEntry>>) source.getIndexStructureInputStream("lexicon");

        FSOMapFileLexiconOutputStream lexOUT = new FSOMapFileLexiconOutputStream(
            target, 
            "lexicon", 
            new FixedSizeTextFactory(ApplicationSetup.MAX_TERM_LENGTH), 
            fields.length > 0    
                ? FieldLexiconEntry.Factory.class
                : BasicLexiconEntry.Factory.class);
                
        FixedSizeWriteableFactory<LexiconEntry> leFactory = fields.length > 0
            ? new FieldLexiconEntry.Factory(fields.length)
            : new BasicLexiconEntry.Factory();

        PostingIndexInputStream invIN = (PostingIndexInputStream) source.getIndexStructureInputStream("inverted");
        AbstractPostingOutputStream invOut = compressionInvertedConfig.getPostingOutputStream(
				path + ApplicationSetup.FILE_SEPARATOR + prefix + "." + "inverted" + compressionInvertedConfig.getStructureFileExtension());

        int tid = 0;
        while (lexIN.hasNext()) {

            // Read in-memory lexicon and postings.
            Entry<String, LexiconEntry> term = lexIN.next();
            IterablePosting postings = invIN.next();

            // Write on-disk lexicon and postings.
            BitIndexPointer pointer = invOut.writePostings(postings);
            LexiconEntry le = leFactory.newInstance();
            //set maxtf to 0, so that it can be updated
            le.setMaxFrequencyInDocuments(0);
            le.add(term.getValue());
            // we only need to preserve termids if we are creating a direct index
            if (direct)
                le.setTermId(term.getValue().getTermId());
            else
                le.setTermId(tid);
            le.setPointer(pointer);
            lexOUT.writeNextEntry(term.getKey(), le);
            tid++;
        }

        IndexUtil.close(lexIN);
        IndexUtil.close(lexOUT);
        IndexUtil.close(invIN);
        IndexUtil.close(invOut);

        /*
         * Tidy up.
         */
        target.flush();
        collectProperties(source,target, compressionInvertedConfig, compressionDirectConfig);
        LexiconBuilder.optimise(target, "lexicon");
        target.flush();

        return target;
    }

    protected void collectProperties(Index source, IndexOnDisk index, CompressionConfiguration compressionConfigInverted, CompressionConfiguration compressionConfigDirect) {

        /*
         * index
         */
        index.getProperties().put("index.terrier.version", ApplicationSetup.TERRIER_VERSION);
        index.getProperties().put("index.created", String.valueOf(System.currentTimeMillis()));

        /*
         * num.{Documents,Pointers,Terms,Tokens} max.term.length
         */
        index.getProperties().put("num.Documents", String.valueOf(source.getCollectionStatistics().getNumberOfDocuments()));
        index.getProperties().put("num.Pointers", String.valueOf(source.getCollectionStatistics().getNumberOfPostings()));
        index.getProperties().put("num.Terms", String.valueOf(source.getCollectionStatistics().getNumberOfUniqueTerms()));
        index.getProperties().put("num.Tokens", String.valueOf(source.getCollectionStatistics().getNumberOfTokens()));
        index.getProperties().put("max.term.length", String.valueOf(ApplicationSetup.MAX_TERM_LENGTH));

        /*
         * index.lexicon
         * structureName,className,paramTypes,paramValues
         */

        index.addIndexStructure("lexicon", "org.terrier.structures.FSOMapFileLexicon", new String[] { "java.lang.String", "org.terrier.structures.IndexOnDisk" }, new String[] { "structureName", "index" });
        index.addIndexStructure("lexicon-keyfactory", "org.terrier.structures.seralization.FixedSizeTextFactory", new String[] { "java.lang.String" }, new String[] { "${max.term.length}" });
        if (fields.length > 0)
            index.addIndexStructure("lexicon-valuefactory", "org.terrier.structures.FieldLexiconEntry$Factory", new String[] { "java.lang.String" }, new String[] { "${index.inverted.fields.count}" });
        else
            index.addIndexStructure("lexicon-valuefactory", "org.terrier.structures.BasicLexiconEntry$Factory", new String[0], new String[0]);

        index.addIndexStructureInputStream("lexicon", "org.terrier.structures.FSOMapFileLexicon$MapFileLexiconIterator", new String[] { "java.lang.String", "org.terrier.structures.IndexOnDisk" }, new String[] { "structureName", "index" });
        index.addIndexStructureInputStream("lexicon-entry", "org.terrier.structures.FSOMapFileLexicon$MapFileLexiconEntryIterator", new String[] { "java.lang.String", "org.terrier.structures.IndexOnDisk" }, new String[] { "structureName", "index" });

        /*
         * index.document
         * structureName,className,paramTypes,paramValues
         */
        if (fields.length > 0)
        {
            index.addIndexStructure("document", "org.terrier.structures.FSAFieldDocumentIndex", new String[] { "org.terrier.structures.IndexOnDisk", "java.lang.String" }, new String[] { "index", "structureName" });
            index.addIndexStructure("document-factory", "org.terrier.structures.FieldDocumentIndexEntry$Factory", new String[] { "java.lang.String" }, new String[] { "${index.inverted.fields.count}" });
        }
        else
        {
            index.addIndexStructure("document", "org.terrier.structures.FSADocumentIndex", new String[] { "org.terrier.structures.IndexOnDisk", "java.lang.String" }, new String[] { "index", "structureName" });
            index.addIndexStructure("document-factory", "org.terrier.structures.BasicDocumentIndexEntry$Factory", new String[0], new String[0]);
        }
        index.addIndexStructureInputStream("document", "org.terrier.structures.FSADocumentIndex$FSADocumentIndexIterator", new String[] { "org.terrier.structures.IndexOnDisk", "java.lang.String" }, new String[] { "index", "structureName" });
        /*
         * index.inverted
         * structureName,className,paramTypes,paramValues
         */

        compressionConfigInverted.writeIndexProperties(index, "lexicon-entry-inputstream");
        if (compressionConfigDirect != null)
    		compressionConfigDirect.writeIndexProperties(index, "document-inputstream");
        
        index.getProperties().put("index.inverted.fields.count", String.valueOf(fields.length));
        index.getProperties().put("index.inverted.fields.names", String.join(",",fields));
        
        
        /*for (Object o : index.getProperties().keySet()) {
        	System.err.println(o.toString()+" "+index.getProperties().getProperty((String)o));
        }*/
    }
}