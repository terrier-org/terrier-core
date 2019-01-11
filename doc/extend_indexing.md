Extending Indexing in Terrier
=============================

If your data is in files (1 file per document, or in XML or TREC files), you should be able to index your data using one of the provided collection decoder, such as [SimpleFileCollection](javadoc/org/terrier/indexing/SimpleFileCollection.html) or [TRECCollection](javadoc/org/terrier/indexing/TRECCollection.html). Otherwise, in scenarios such as extracting documents to be indexed from a database, you will need to write your own Collection decoder. This is done by implementing the [Collection](javadoc/org/terrier/indexing/Collection.html) interface, and setting this to be used with the `trec.collection.class` property. [MultiFileCollection](javadoc/org/terrier/indexing/MultiFileCollection.html) is a useful base class for implementing readers for TREC-like corpora with multiple documents stored in each file. Due to its ability to fetch HTTP URLs, SimpleFileCollection can be used to download webpages also.

A Collection implementation returns a [Document](javadoc/org/terrier/indexing/Document.html) object for every document in the corpus. Simple textual contents can be handled by [FileDocument](javadoc/org/terrier/indexing/FileDocument.html), while HTML documents can be handled by [TaggedDocument](javadoc/org/terrier/indexing/TaggedDocument.html). Otherwise, if your documents are of a non-standard format, you'll need to implement your own Document. The purpose of a Document object is to parse a document's format (e.g. Microsoft Word, HTML), and extract the text that should be indexed – optionally, you can designate the fields that contain the text to be extracted and, if [configured](configure_indexing.md#fields), the indexer will note the fields where each term occurs in a document.

The Document typically provides the extracted text as input to a tokeniser, which identifies multiple tokens, and return them as stream, in their order of occurrence. For languages where tokens are naturally delimited by whitespace characters, Terrier provides [English](javadoc/org/terrier/indexing/tokenisation/EnglishTokeniser.html) and [UTF](javadoc/org/terrier/indexing/tokenisation/UTFTokeniser.html) tokenisers. If your particular corpus has more complicated tokenisation than just whitespace, you can implement the [Tokeniser](javadoc/org/terrier/indexing/tokenisation/Tokeniser.html) interface to suit your needs.

Index Data Structures
---------------------

As discussed in [Configuring Indexing](configure_indexing.md), Terrier has different indexing implementations. In the following, we describe how the generic indexing infrastructure. Details on the implementation of the classical two-pass and single-pass indexing can be at (indexer details)[indexer_details.md]. Details are on the [Hadoop MapReduce indexing are described elsewhere](hadoop_indexing.md). In-memory and incremental indices are described under [real-time indexing](realtime_indices.md).

Each indexer creates several data structures, and creates a differing [Index](javadoc/org/terrier/structures/Index.html) implementation (summarised in the table below):

-   *direct* ([PostingIndex](javadoc/org/terrier/structures/PostingIndex.html)) : a compressed file, where we store the terms contained in each document. The direct index is used for automatic query expansion. Accessed using an IterablePosting. Optionally contains position and field information.

-  *document* ([DocumentIndex](javadoc/org/terrier/structures/DocumentIndex.html)) : a fixed-length entry file, where we store information about documents, such as the number of indexed tokens (document length), the identifier of a document, and the offset of its corresponding entry in the direct index. The direct index provides the [Pointer](javadoc/org/terrier/structures/Pointer.html) necessary for accessing the direct index. Created by the [DocumentIndexBuilder](javadoc/org/terrier/structures/indexing/DocumentIndexBuilder.html).

-  *lexicon* ([Lexicon](javadoc/org/terrier/structures/Lexicon.html)) : a fixed-length entry file, where we store information about the vocabulary of the indexed collection. The lexicon provides the [Pointer](javadoc/org/terrier/structures/Pointer.html) necessary for accessing the inverted index.

- *inverted* ([PostingIndex](javadoc/org/terrier/structures/PostingIndex.html)) : a compressed file, where we store the docids of the documents containing a given term. Accessed using an IterablePosting. Optionally contains position and field information.

- *meta* ([MetaIndex](javadoc/org/terrier/structures/MetaIndex.html)) : stores metadata about each document.

|Structure|Classical| Single-pass | MapReduce | Memory |
|------------|---|---|---|---|
|direct|✔|x|x|optional|
|document|✔|✔|✔|✔|
|lexicon|✔|✔|✔|✔|
|inverted|✔|✔|✔|✔|
|meta|✔|✔|✔|✔|
|(Index Type)|IndexOnDisk|IndexOnDisk|IndexOnDisk|MemoryIndex|

> *Hint:*
> If a direct file is required after using an indexer that does not create one, the [Inverted2DirectIndexBuilder](javadoc/org/terrier/structures/indexing/singlepass/Inverted2DirectIndexBuilder.html) can be used to create one.

Each indexer iterates through the documents of the collection, using a [Tokeniser](javadoc/org/terrier/indexing/tokenisation/Tokeniser.html) to identify terms to index. Each term found is sent through the [TermPipeline](javadoc/org/terrier/terms/TermPipeline.html). The TermPipeline transforms the terms, and can remove terms that should not be indexed. The TermPipeline chain in use is `termpipelines=Stopwords,PorterStemmer`, which removes terms from the document using the [Stopwords](javadoc/org/terrier/terms/Stopwords.html) object, and then applies Porter’s Stemming algorithm for English to the terms ([PorterStemmer](javadoc/org/terrier/terms/PorterStemmer.html)). If you wanted to use a different stemmer, this is the point at which it should be implemented.

Once terms have been processed through the TermPipeline, they are aggregated by the [DocumentPostingList](javadoc/org/terrier/structures/indexing/DocumentPostingList.html). Each DocumentPostingList is then processed to update temporary data structures.

There are two variants of each indexer: one providing basic functionality (storing term frequencies only), and one additionally storing position information (i.e where each word occurs at in each document) within the direct and inverted index structures. This allows querying to use term positions information - for example phrasal search (`""`) and proximity search (`""~10`). For more details about the querying process, you may refer to [querying with Terrier](extend_retrieval.md) and the description of the [query language](querylanguage.md).


Block Delimiter Terms
---------------------

Block indexing can be configured to consider bounded instead of fixed-size blocks. Basically, a list of pre-defined terms must be specified as special-purpose block delimiters. By using sentence boundaries as block delimiters, for instance, one can have blocks to represent sentences. BlockIndexer, BlockSinglePassIndexer, and Hadoop\_BlockSinglePassIndexer all implement this feature.

Bounded block indexing can be used by configuring the following properties:

-   `block.delimiters.enabled` - Whether delimited blocks should be used instead of fixed-size blocks. Defaults to false.

-   `block.delimiters` - Comma-separated list of terms that cause the block counter to be incremented.

-   `block.delimiters.index.terms` - Whether delimiters should be themselves indexed as normal terms. Defaults to false.

-   `block.delimiters.index.doclength` - Whether indexed delimiters should contribute to document length statistics. Defaults to false; if set to true, this property only has effect if `block.delimiters.index.terms` is enabled.

-   `termpipeline.skip` - Comma-separated list of tokens to be skipped by the configured term pipelines. In practice, this should be set to the value of block.delimiters in order to prevent the specified delimiters from being stemmed or removed during indexing.

Compression
-----------

Terrier uses highly compressed data structures as much as possible. In particular, the inverted and direct index structures are encoded using bit-level compression, namely Elias Gamma and Elias Unary encoding of integers (namely term ids, docids and frequencies). The underlying compression is provided by the org.terrier.compression.bit package. Since version 4.0, alternative integer-focussed compression schemes have been supported. These are applied by rewriting the inverted (or direct) index data structures with a new format. See the [compression documentation](compression.md) for more information.

For document metadata, the default [MetaIndex](javadoc/org/terrier/structures/MetaIndex.html), namely [CompressingMetaIndex](javadoc/org/terrier/structures/CompressingMetaIndex.html) uses Zip compression to minimise the number of bytes necessary for every document.


------------------------------------------------------------------------


> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
