Indexing Implementation Details
================

### Classical two-pass indexing

This subsection describes the algorithm underlying classical indexing implemented by BasicIndexer and BlockIndexer. For single-pass indexing, see the next subsection.

DocumentPostingList, which are parsed, aggregated representation of documents are passed to the indexer. As the indexer iterates through the documents of the collection, it appends to the direct and document indexes. For saving the vocabulary information, the indexer creates temporary lexicons for parts of the collection, which are merged once all the documents have been processed. These are built using [LexiconBuilder](javadoc/org/terrier/structures/indexing/LexiconBuilder.html)s, which are then merged at the end of the d direct index build phase.

The LexiconMap is flushed to disk every `bundle.size` documents. If memory during indexing is a concern, then reduce this property to less than its default 2500. However, more temporary lexicons will be created. The rate at which the temporary lexicons are merged is controlled by the `lexicon.builder.merge.lex.max` property, though we have found 16 to be a good compromise.

Once all documents in the index have been created, the InvertedIndex is created by the [InvertedIndexBuilder](javadoc/org/terrier/structures/indexing/classical/InvertedIndexBuilder.html). As the entire DirectIndex cannot be inverted in memory, the InvertedIndexBuilder takes several iterations, selecting a few terms, scanning the direct index for them, and then writing out their postings to the inverted index. If it takes too many terms at once, Terrier can run out of memory. Reduce the property `invertedfile.processpointers` from its default 20,000,000 and rerun (default is only 2,000,000 for block indexing, which is more memory intensive). See the [InvertedIndexBuilder](javadoc/org/terrier/structures/indexing/classical/InvertedIndexBuilder.html) for more information about the inversion and term selection strategies.

### Single-pass indexing

Essentially, instead of building a direct file from the collection, term posting lists are held in memory, and written to disk as a 'run' when memory is exhausted. These are then merged to form the lexicon and the inverted file. Note that no direct index is created - indeed, the single-pass indexing is much faster than classical two-pass indexing when the direct index is not required. If the direct index is required, then this can be built from the inverted index using the [Inverted2DirectIndexBuilder](javadoc/org/terrier/structures/indexing/singlepass/Inverted2DirectIndexBuilder.html).

The single-pass indexer can be used by using the `batchindexing -j` commandline to TrecTerrier.

The majority of the properties configuring the single-pass indexer are related to memory consumption, and how it decides that memory has been exhausted. Firstly, the indexer will commit a run to disk when free memory falls below the threshold set by `memory.reserved` (100MB for 64bit JVMs, 50MB for 32bit). To ensure that this doesn’t happen too soon, 85% of the possible heap must be allocated (controlled by the property `memory.heap.usage`). This check occurs every 20 documents (`docs.checks`).

Single-pass indexing is significantly quicker than two-pass indexing. However, there are some configuration points to be aware of. In particular, it makes much use of the memory to reduce disk IO. For Java 6+, we recommend adding the `-XX:-UseGCOverheadLimit` to the command line. Moreover, for very large indices, many files have to be opened during merging, possibly exhausting the maximum number of allowed open files. Refer to your operating system documentation to increase this limit

In this architecture, indexing is performed to build up in-memory posting lists ([MemoryPostings](javadoc/org/terrier/structures/indexing/singlepass/MemoryPostings.html) containing [Posting](javadoc/org/terrier/structures/indexing/singlepass/Posting.html) objects), which are written to disk as "runs" by the [RunWriter](javadoc/org/terrier/structures/indexing/singlepass/RunWriter.html) when most of the available memory is consumed.

Once the collection has been parsed, all runs are merged by the [RunsMerger](javadoc/org/terrier/structures/indexing/singlepass/RunsMerger.html), which uses a [SimplePostingInRun](javadoc/org/terrier/structures/indexing/singlepass/SimplePostingInRun.html) to represent each posting list when iterating through the contents of each run.

### Advanced properties

Both indexers discussed above will start a new index occasionally. This can be controlled using the properties discussed below:

-   `indexing.max.docs.per.builder` - Maximum number of documents in an index before a new index is created and merged later.

-   `indexing.builder.boundary.docnos` - Comma-delimited list of docnos of documents that force the index being created to be completed, and a new index to be commenced. An alternative to `indexing.max.docs.per.builder`.


### Changing Indexing

To replace the default indexing structures in Terrier with others is very easy, as the data.properties file contains information about which classes should be used to load the five main data structures of the Index: [DocumentIndex](javadoc/org/terrier/structures/DocumentIndex.html), [MetaIndex](javadoc/org/terrier/structures/MetaIndex.html), [Lexicon](javadoc/org/terrier/structures/Lexicon.html) and the direct and inverted index structures [PostingIndex](javadoc/org/terrier/structures/PostingIndex.html). A more detailed summary of the standard index structures is given at [Index Structures (Terrier wiki)](http://ir.dcs.gla.ac.uk/wiki/Terrier/IndexStructures). To implement a replacement index data structure, it may sufficient to subclass a builder, and then subclass the appropriate Indexer class to ensure it is used.

Adding other data structures to a Terrier index is also relatively straightforward. The abstract [Index](javadoc/org/terrier/structures/Index.html) class defines methods such as [addIndexStructure(String, String)](javadoc/org/terrier/structures/Index.html#addIndexStructure(java.lang.String,%20java.lang.String)) which allow a class to be associated with a structure name (e.g. org.terrier.structures.PostingIndex is associated to the "inverted" and "direct" structures. You can retrieve your structure by casting the result of [getIndexStructure(String)](javadoc/org/terrier/structures/Index.html#getIndexStructure(java.lang.String)). For structures with more complicated constructors, other addIndexStructure methods are provided. Finally, your application can check that the desired structure exists using [hasIndexStructure(String)](javadoc/org/terrier/structures/Index.html#hasIndexStructure(java.lang.String)).


Terrier indices specify the random-access and in-order structure classes for each of the main structure types: direct, inverted, lexicon and document. When generating new data structures, it is good practice to provide in-order (stream) classes as well as random-access classes to your data structures, should other developers wish to access these index structures at another indexing stage. For instance, for the "inverted" and "direct" structures, Terrier provides the [PostingIndex](javadoc/org/terrier/structures/bit/InvertedIndex.html) and [PostingIndexInputStream](javadoc/org/terrier/structures/bit/InvertedIndexInputStream.html) classes.

Many of Terrier's index structures can be accessed from Hadoop MapReduce. In particular, we provide [BitPostingIndexInputFormat](javadoc/org/terrier/structures/indexing/singlepass/hadoop/BitPostingIndexInputFormat.html) which allows an inverted index or direct index to be split across many map tasks, with an orthogonal [CompressingMetaIndexInputFormat](javadoc/org/terrier/structures/CompressingMetaIndex.CompressingMetaIndexInputFormat.html) for the MetaIndex.
