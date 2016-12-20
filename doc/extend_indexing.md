Extending Indexing in Terrier
=============================

Unless your data is in files (1 file per document, or in XML or TREC files), you will probably need to create your own collection decoder. Examples of such scenarios could be extracting documents to be indexed from a database. This is done by implementing the [Collection](javadoc/org/terrier/indexing/Collection.html) interface, and setting this to be used with the `trec.collection.class` property.

A Collection implementation returns a [Document](javadoc/org/terrier/indexing/Document.html) object for every document in the corpus. Simple textual contents can be handled by [FileDocument](javadoc/org/terrier/indexing/FileDocument.html), while HTML documents can be handled by [TaggedDocument](javadoc/org/terrier/indexing/TaggedDocument.html). Otherwise, if your documents are of a non-standard format, you’ll need to implement your own Document. The purpose of a Document object is to parse a document's format (e.g. Microsoft Word, HTML), and extract the text that should be indexed – optionally, you can designate the fields that contain the text to be extracted and, if [configured](configure_indexing.md#fields), the indexer will note the fields where each term occurs in a document.

The Document typically provides the extracted text as input to a tokeniser, which identifies multiple tokens, and return them as stream, in their order of occurrence. For languages where tokens are naturally delimited by whitespace characters, Terrier provides [English](javadoc/org/terrier/indexing/tokenisation/EnglishTokeniser.html) and [UTF](javadoc/org/terrier/indexing/tokenisation/UTFTokeniser.html) tokenisers. If your particular corpus has more complicated tokenisation than just whitespace, you can implement the [Tokeniser](javadoc/org/terrier/indexing/tokenisation/Tokeniser.html) interface to suit your needs.

Indexers
--------

As discussed in [Configuring Indexing](configure_indexing.md), Terrier has three different indexing implementations. In the following, we describe how the classical two-pass and single-pass indexing are implemented. Details are on the [Hadoop MapReduce indexing are described elsewhere](hadoop_indexing.md).

### Classical two-pass indexing

There are two variants of two-pass indexing: the BlockIndexer provides the same functionality as BasicIndexer, but uses a larger DirectIndex and InvertedIndex for storing the positions that each word occurs at in each document. This allows querying to use term positions information - for example Phrasal search (`""``) and proximity search (`""~10`). For more details about the querying process, you may refer to [querying with Terrier](extend_retrieval.md) and the description of the [query language](querylanguage.md).

The indexer iterates through the documents of the collection, using a [Tokeniser](javadoc/org/terrier/indexing/tokenisation/Tokeniser.html) to identify terms to index. Each term found is sent through the [TermPipeline](javadoc/org/terrier/terms/TermPipeline.html). The TermPipeline transforms the terms, and can remove terms that should not be indexed. The TermPipeline chain in use is `termpipelines=Stopwords,PorterStemmer`, which removes terms from the document using the [Stopwords](javadoc/org/terrier/terms/Stopwords.html) object, and then applies Porter’s Stemming algorithm for English to the terms ([PorterStemmer](javadoc/org/terrier/terms/PorterStemmer.html)). If you wanted to use a different stemmer, this is the point at which it should be implemented.

Once terms have been processed through the TermPipeline, they are aggregated by the [DocumentPostingList](javadoc/org/terrier/structures/indexing/DocumentPostingList.html) and the [LexiconMap](javadoc/org/terrier/structures/indexing/LexiconMap.html), to create the following data structures:

-   [DirectIndex](javadoc/org/terrier/structures/bit/DirectIndex.html) : a compressed file, where we store the terms contained in each document. The direct index is used for automatic query expansion.

-   [DocumentIndex](javadoc/org/terrier/structures/DocumentIndex.html) : a fixed-length entry file, where we store information about documents, such as the number of indexed tokens (document length), the identifier of a document, and the offset of its corresponding entry in the direct index. Created by the [DocumentIndexBuilder](javadoc/org/terrier/structures/indexing/DocumentIndexBuilder.html).

-   [Lexicon](javadoc/org/terrier/structures/Lexicon.html) : a fixed-length entry file, where we store information about the vocabulary of the indexed collection. Built as a series of temporary Lexicons by the [LexiconBuilder](javadoc/org/terrier/structures/indexing/LexiconBuilder.html)s, which are then merged at the end of the DirectIndex build phase.

As the indexer iterates through the documents of the collection, it appends the direct and document indexes. For saving the vocabulary information, the indexer creates temporary lexicons for parts of the collection, which are merged once all the documents have been processed.

Once the direct index, the document index and the lexicon have been created, the inverted index is created, by the [InvertedIndexBuilder](javadoc/org/terrier/structures/indexing/classical/InvertedIndexBuilder.html), which inverts the direct index.

### Single-pass indexing

Since v2.0, Terrier has had a single-pass indexing architecture. In this architecture, indexing is performed to build up in-memory posting lists ([MemoryPostings](javadoc/org/terrier/structures/indexing/singlepass/MemoryPostings.html) containing [Posting](javadoc/org/terrier/structures/indexing/singlepass/Posting.html) objects), which are written to disk as "runs" by the [RunWriter](javadoc/org/terrier/structures/indexing/singlepass/RunWriter.html) when most of the available memory is consumed.

Once the collection has been parsed, all runs are merged by the [RunsMerger](javadoc/org/terrier/structures/indexing/singlepass/RunsMerger.html), which uses a [SimplePostingInRun](javadoc/org/terrier/structures/indexing/singlepass/SimplePostingInRun.html) to represent each posting list when iterating through the contents of each run.

If a direct file is required, the [Inverted2DirectIndexBuilder](javadoc/org/terrier/structures/indexing/singlepass/Inverted2DirectIndexBuilder.html) can be used to create one.

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

Terrier uses highly compressed data structures as much as possible. In particular, the inverted and direct index structures are encoded using bit-level compression, namely Elias Gamma and Elias Unary encoding of integers (namely term ids, docids and frequencies). The underlying compression is provided by the org.terrier.compression package. For document metadata, the default [MetaIndex](javadoc/org/terrier/structures/MetaIndex.html), namely [CompressingMetaIndex](javadoc/org/terrier/structures/CompressingMetaIndex.html) uses Zip compression to minimise the number of bytes necessary for every document.

Changing Indexing
-----------------

To replace the default indexing structures in Terrier with others is very easy, as the data.properties file contains information about which classes should be used to load the five main data structures of the Index: [DocumentIndex](javadoc/org/terrier/structures/DocumentIndex.html), [MetaIndex](javadoc/org/terrier/structures/MetaIndex.html), [DirectIndex](javadoc/org/terrier/structures/bit/DirectIndex.html), [Lexicon](javadoc/org/terrier/structures/Lexicon.html) and [InvertedIndex](javadoc/org/terrier/structures/bit/InvertedIndex.html). A more detailed summary of the standard index structures is given at [Index Structures (Terrier wiki)](http://ir.dcs.gla.ac.uk/wiki/Terrier/IndexStructures). To implement a replacement index data structure, it may sufficient to subclass a builder, and then subclass the appropriate Indexer class to ensure it is used.

Adding other data structures to a Terrier index is also straightforward. The abstract [Index](javadoc/org/terrier/structures/Index.html) class defines methods such as [addIndexStructure(String, String)](javadoc/org/terrier/structures/Index.html#addIndexStructure(java.lang.String,%20java.lang.String)) which allow a class to be associated with a structure name (e.g. org.terrier.structures.InvertedIndex is associated to the “inverted” structure. You can retrieve your structure by casting the result of [getIndexStructure(String)](javadoc/org/terrier/structures/Index.html#getIndexStructure(java.lang.String)). For structures with more complicated constructors, other addIndexStructure methods are provided. Finally, your application can check that the desired structure exists using [hasIndexStructure(String)](javadoc/org/terrier/structures/Index.html#hasIndexStructure(java.lang.String)).

Terrier indices specify the random-access and in-order structure classes for each of the main structure types: direct, inverted, lexicon and document. When generating new data structures, it is good practice to provide in-order (stream) classes as well as random-access classes to your data structures, should other developers wish to access these index structures at another indexing stage. For instance, for the `inverted` structure, Terrier provides the [InvertedIndex](javadoc/org/terrier/structures/bit/InvertedIndex.html) and [InvertedIndexInputStream](javadoc/org/terrier/structures/bit/InvertedIndexInputStream.html) classes.

Many of Terrier's index structures can be accessed from Hadoop MapReduce. In particular, we provide [BitPostingIndexInputFormat](javadoc/org/terrier/structures/indexing/singlepass/hadoop/BitPostingIndexInputFormat.html) which allows an inverted index or direct index to be split across many map tasks, with an orthogonal [CompressingMetaIndexInputFormat](javadoc/org/terrier/structures/CompressingMetaIndex.CompressingMetaIndexInputFormat.html) for the MetaIndex.



------------------------------------------------------------------------


> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2017 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
