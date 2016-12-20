Indexing Implementation Details
================

### Classical two-pass indexing

This subsection describes the algorithm underlying classical indexing implemented by BasicIndexer and BlockIndexer. For single-pass indexing, see the next subsection.

The LexiconMap is flushed to disk every `bundle.size` documents. If memory during indexing is a concern, then reduce this property to less than its default 2500. However, more temporary lexicons will be created. The rate at which the temporary lexicons are merged is controlled by the `lexicon.builder.merge.lex.max` property, though we have found 16 to be a good compromise.

Once all documents in the index have been created, the InvertedIndex is created by the [InvertedIndexBuilder](javadoc/org/terrier/structures/indexing/classical/InvertedIndexBuilder.html). As the entire DirectIndex cannot be inverted in memory, the InvertedIndexBuilder takes several iterations, selecting a few terms, scanning the direct index for them, and then writing out their postings to the inverted index. If it takes too many terms at once, Terrier can run out of memory. Reduce the property `invertedfile.processpointers` from its default 20,000,000 and rerun (default is only 2,000,000 for block indexing, which is more memory intensive). See the [InvertedIndexBuilder](javadoc/org/terrier/structures/indexing/classical/InvertedIndexBuilder.html) for more information about the inversion and term selection strategies.

### Single-pass indexing

Essentially, instead of building a direct file from the collection, term posting lists are held in memory, and written to disk as a 'run' when memory is exhausted. These are then merged to form the lexicon and the inverted file. Note that no direct index is created - indeed, the single-pass indexing is much faster than classical two-pass indexing when the direct index is not required. If the direct index is required, then this can be built from the inverted index using the [Inverted2DirectIndexBuilder](javadoc/org/terrier/structures/indexing/singlepass/Inverted2DirectIndexBuilder.html).

The single-pass indexer can be used by using the `-i -j` command line argument to TrecTerrier.

The majority of the properties configuring the single-pass indexer are related to memory consumption, and how it decides that memory has been exhausted. Firstly, the indexer will commit a run to disk when free memory falls below the threshold set by `memory.reserved` (50MB). To ensure that this doesn’t happen too soon, 85% of the possible heap must be allocated (controlled by the property `memory.heap.usage`). This check occurs every 20 documents (`docs.checks`).

Single-pass indexing is significantly quicker than two-pass indexing. However, there are some configuration points to be aware of. In particular, it makes much use of the memory to reduce disk IO. For Java 6+, we recommend adding the `-XX:-UseGCOverheadLimit` to the command line. Moreover, for very large indices, many files have to be opened during merging, possibly exhausting the maximum number of allowed open files. Refer to your operating system documentation to increase this limit

### Advanced properties

Both indexers discussed above will start a new index occasionally. This can be controlled using the properties discussed below:

-   `indexing.max.docs.per.builder` - Maximum number of documents in an index before a new index is created and merged later.

-   `indexing.builder.boundary.docnos` - Comma-delimited list of docnos of documents that force the index being created to be completed, and a new index to be commenced. An alternative to `indexing.max.docs.per.builder`.
