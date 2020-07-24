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
        System.err.println("Writing " + source.toString() + " (" + source.getCollectionStatistics().getNumberOfDocuments() +  " docs) to disk at " 
            + target.toString() + " fields=" + (fields.length > 0));

        CompressionConfiguration compressionDirectConfig = null;
        CompressionConfiguration compressionInvertedConfig = CompressionFactory.getCompressionConfiguration("inverted", fields, 0,0);

        /*
         * Direct Index and Document Structure
         */
        DocumentIndexBuilder docOut = new DocumentIndexBuilder(target, "document", fields.length > 0);
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
                DocumentIndexEntry dieOld = document.getDocumentEntry(docid);
                Pointer pointer = directIndexBuilder.writePostings(directPosting, dieOld.getNumberOfEntries(), dieOld.getDocumentLength());
                
                DocumentIndexEntry dieNew = compressionDirectConfig.getDocumentIndexEntryFactory().newInstance();
                dieNew.setPointer(pointer);
                dieNew.setDocumentIndexStatistics(dieOld);
                docOut.addEntryToBuffer(dieNew);
                docid++;
            }
            directIndexBuilder.close();
            compressionDirectConfig.getDocumentIndexEntryFactory().writeProperties(target, "document-factory");
            
        } else {
            //NB: we use the *inverted* DocumentIndexEntry implementation
            Iterator<DocumentIndexEntry> docIter = (Iterator<DocumentIndexEntry>) source
				.getIndexStructureInputStream("document");
        
            int docs = source.getCollectionStatistics().getNumberOfDocuments();
            for(int i=0;i<docs;i++){
                DocumentIndexEntry dieOld = docIter.next();
                DocumentIndexEntry dieNew = compressionInvertedConfig.getDocumentIndexEntryFactory().newInstance();
                dieNew.setDocumentIndexStatistics(dieOld);
			    docOut.addEntryToBuffer(dieNew);
            }
            IndexUtil.close(docIter);
            compressionInvertedConfig.getDocumentIndexEntryFactory().writeProperties(target, "document-factory");
        }
        docOut.finishedCollections();
        docOut.close();


        /** metaindex */
        Iterator<String[]> metaIter = (Iterator<String[]>) source.getIndexStructureInputStream("meta");
        CompressingMetaIndexBuilder metaOut = new CompressingMetaIndexBuilder(
                target,
                source.getMetaIndex().getKeys(),
                ArrayUtils.parseCommaDelimitedInts(ApplicationSetup
						.getProperty("indexer.meta.forward.keylens", "")),
				ArrayUtils.parseCommaDelimitedString(ApplicationSetup
						.getProperty("indexer.meta.reverse.keys", "")));
		while(metaIter.hasNext()){
			metaOut.writeDocumentEntry(metaIter.next());
		}
		metaOut.close();

        /*
         * Lexicon and inverted index.
         */
        

        Iterator<Entry<String, LexiconEntry>> lexIN = (Iterator<Entry<String, LexiconEntry>>) source.getIndexStructureInputStream("lexicon");

        FixedSizeWriteableFactory<LexiconEntry> leFactory = compressionInvertedConfig.getLexiconEntryFactory();
        FSOMapFileLexiconOutputStream lexOUT = new FSOMapFileLexiconOutputStream(
            target, 
            "lexicon", 
            new FixedSizeTextFactory(ApplicationSetup.MAX_TERM_LENGTH), 
            leFactory);

        PostingIndexInputStream invIN = (PostingIndexInputStream) source.getIndexStructureInputStream("inverted");
        AbstractPostingOutputStream invOut = compressionInvertedConfig.getPostingOutputStream(
				path + ApplicationSetup.FILE_SEPARATOR + prefix + "." + "inverted" + compressionInvertedConfig.getStructureFileExtension());

        while (lexIN.hasNext()) {

            // Read in-memory lexicon and postings.
            Entry<String, LexiconEntry> term = lexIN.next();
            IterablePosting postings = invIN.next();

            // Write on-disk lexicon and postings.
            Pointer pointer = invOut.writePostings(postings, term.getValue().getNumberOfEntries(), term.getValue().getMaxFrequencyInDocuments());
            LexiconEntry le = leFactory.newInstance();
            le.add(term.getValue());
            le.setTermId(term.getValue().getTermId());
            le.setPointer(pointer);
            lexOUT.writeNextEntry(term.getKey(), le);
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

        // /*
        //  * index
        //  */
        // index.getProperties().put("index.terrier.version", ApplicationSetup.TERRIER_VERSION);
        // index.getProperties().put("index.created", String.valueOf(System.currentTimeMillis()));

        /*
         * num.{Documents,Pointers,Terms,Tokens} max.term.length
         */
        index.getProperties().put("num.Documents", String.valueOf(source.getCollectionStatistics().getNumberOfDocuments()));
        index.getProperties().put("num.Pointers", String.valueOf(source.getCollectionStatistics().getNumberOfPointers()));
        index.getProperties().put("num.Terms", String.valueOf(source.getCollectionStatistics().getNumberOfUniqueTerms()));
        index.getProperties().put("num.Tokens", String.valueOf(source.getCollectionStatistics().getNumberOfTokens()));
        index.getProperties().put("max.term.length", String.valueOf(ApplicationSetup.MAX_TERM_LENGTH));

        /*
         * index.lexicon
         * structureName,className,paramTypes,paramValues
         */

        index.addIndexStructure("lexicon", "org.terrier.structures.FSOMapFileLexicon", new String[] { "java.lang.String", "org.terrier.structures.IndexOnDisk" }, new String[] { "structureName", "index" });
        index.addIndexStructure("lexicon-keyfactory", "org.terrier.structures.seralization.FixedSizeTextFactory", new String[] { "java.lang.String" }, new String[] { "${max.term.length}" });
        compressionConfigInverted.getLexiconEntryFactory().writeProperties(index, "lexicon-valuefactory");

        index.addIndexStructureInputStream("lexicon", "org.terrier.structures.FSOMapFileLexicon$MapFileLexiconIterator", new String[] { "java.lang.String", "org.terrier.structures.IndexOnDisk" }, new String[] { "structureName", "index" });
        index.addIndexStructureInputStream("lexicon-entry", "org.terrier.structures.FSOMapFileLexicon$MapFileLexiconEntryIterator", new String[] { "java.lang.String", "org.terrier.structures.IndexOnDisk" }, new String[] { "structureName", "index" });

        /*
         * index.inverted
         * structureName,className,paramTypes,paramValues
         */

        compressionConfigInverted.writeIndexProperties(index, "lexicon-entry-inputstream");
        if (compressionConfigDirect != null)
    		compressionConfigDirect.writeIndexProperties(index, "document-inputstream");
        
        /*
         * index.meta
         * structureName,className,paramTypes,paramValues
         */

        index.addIndexStructure("meta", "org.terrier.structures.CompressingMetaIndex", new String[] { "org.terrier.structures.IndexOnDisk", "java.lang.String" }, new String[] { "index", "structureName" });
        index.addIndexStructureInputStream("meta", "org.terrier.structures.CompressingMetaIndex$InputStream", new String[] { "org.terrier.structures.IndexOnDisk", "java.lang.String" }, new String[] { "index", "structureName" });
    }
}