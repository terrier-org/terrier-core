Configuring Indexing in Terrier
===============================

Indexing Overview
-----------------

The indexing process in Terrier is described [here](http://terrier.org/docs/current/basicComponents.md)

Firstly, a [Collection](javadoc/org/terrier/indexing/TRECCollection.html) object extracts the raw content of each individual document (from a collection of documents) and hands it in to a [Document](javadoc/org/terrier/indexing/Document.html) object. The Document object then removes any unwanted content (e.g., from a particular document tag) and gives the resulting text to a [Tokeniser](javadoc/org/terrier/indexing/tokenisation/Tokeniser.html) object. Finally, the Tokeniser converts the text into a stream of tokens that represent the content of the document.

By default, Terrier uses [TRECCollection](javadoc/org/terrier/indexing/TRECCollection.html), which parses corpora in TREC format. In particular, in TREC-formatted files, there are many documents delimited by `<DOC></DOC>` tags, as in the following example:

    <DOC>
    <DOCNO> doc1 </DOCNO>
    Content of the document does here
    </DOC>
    <DOC>
    ...

For corpora in other formats, you will need to change the Collection object being used, by setting the property `trec.collection.class`, or using the `-C` command line option of the `batchindexing` command. Here are some options:

-   [TRECCollection](javadoc/org/terrier/indexing/TRECCollection.html) - Parses TREC formatted corpora, delimited by the `<DOC></DOC>` tags.

-   [TRECWebCollection](javadoc/org/terrier/indexing/TRECWebCollection.html) - As TRECCollection, but additionally parses DOCHDR tags, which contain the URL of each document. TREC Web and Blog corpora such as WT2G, WT10G, .GOV, .GOV2, Blogs06 and Blogs08 are supported.

-   [WARC09Collection](javadoc/org/terrier/indexing/WARC09Collection.html) - Parses corpora in WARC version 0.9 format, such as UK-2006.

-   [WARC018Collection](javadoc/org/terrier/indexing/WARC018Collection.html) - Parses corpora in WARC version 0.18 format, such as ClueWeb09.

-   [SimpleFileCollection](javadoc/org/terrier/indexing/SimpleFileCollection.html) - Parses HTML, Microsoft Word/Excel/Powerpoint, PDF, text documents, etc., one document per file. For a guide on how to use this class, see the [collection of files guide](http://ir.dcs.gla.ac.uk/wiki/Terrier/CollectionOfFiles) on the Terrier wiki.

-   [SimpleXMLCollection](javadoc/org/terrier/indexing/SimpleXMLCollection.html) - Like TRECCollection, but where the input is valid XML.

-   [SimpleMedlineXMLCollection](javadoc/org/terrier/indexing/SimpleMedlineXMLCollection.html) - Special version of SimpleXMLCollection for modern Medline documents.

Except for the special-purpose collections (SimpleFileCollection, SimpleXMLCollection, and SimpleMedlineXMLCollection), all other Collection implementations allow for different [Document](javadoc/org/terrier/indexing/Document.html) implementations to be used, by specifying the `trec.document.class` property. By default, these collections use [TaggedDocument](javadoc/org/terrier/indexing/TaggedDocument.html). The available Document implementations are:

-   [TaggedDocument](javadoc/org/terrier/indexing/TaggedDocument.html) - Models a tagged document (e.g., an HTML or TREC document). Note that from Terrier 4.0, this class replaced HTMLDocument and TRECDocument.

-   [FileDocument](javadoc/org/terrier/indexing/FileDocument.html) - Models a document which corresponds to a single, plain-text file.

-   [PDFDocument](javadoc/org/terrier/indexing/PDFDocument.html), [MSExcelDocument](javadoc/org/terrier/indexing/MSExcelDocument.html), and [MSWordDocument](javadoc/org/terrier/indexing/MSWordDocument.html) - Model PDF, MS Excel (.xls), MS Powerpoint (.ppt), and MS Word (.doc) documents, respectively.

Finally, all Document implementations can specify their own [Tokeniser](javadoc/org/terrier/indexing/tokenisation/Tokeniser.html) implementation. By default, Terrier uses the [EnglishTokeniser](javadoc/org/terrier/indexing/tokenisation/EnglishTokeniser.html). When [indexing non-English corpora](languages.html), a different Tokeniser implementation can be specified by the `tokeniser` property.

### Basic indexing setup

For now, we’ll stick to TRECCollection, which can be used for all TREC corporas from Disks 1&2 until Blogs08, including WT2G, .GOV, .GOV2, etc. TRECCollection can be further configured.

-   Set `TrecDocTags.doctag` to denote the marker tag for document boundaries (usually `DOC`).

-   `TrecDocTags.idtag` denotes the tag that contains the `DOCNO` of the document.

-   `TrecDocTags.skip` denotes tags that should not be parsed in this collection (for instance, the `DOCHDR` tags of TREC Web collections).

Note that the specified tags are case-sensitive, but this can be relaxed by setting the `TrecDocTags.casesensitive` property to false. Furthermore, TRECCollection also supports the addition of the contents of tags to the meta index. This is useful if you wish to present these during retrieval (e.g. the URL of the document, or the date). To use this, the tags in the TREC collection file need to be in a fixed order, beginning with the `DOC` and `DOCNO` tags, followed by the tags to be added to the meta index and specified by the `TrecDocTags.propertytags` property. Any tags occurring after the property tags will be indexed as if they contain text (unless excluded by `TrecDocTags.skip`). The name of the entries in the meta index must be the same as the tag names. Moreover, as with any entries added to the meta index, these entries must be specified in the `indexer.meta.forward.keys` property and the maximum length of each tag must be given in the `indexer.meta.forward.keylens` property.

#### Fields

Terrier has the ability to record the frequency with which terms occur in various fields of documents. The required fields are specified by the `FieldTags.process` property. For example, to note when a term occurs in the TITLE or H1 HTML tags of a document, set `FieldTags.process=TITLE,H1`. FieldTags are case-insensitive. There is a special field called ELSE, which contains all terms not in any other specified field.

The indexer iterates through the documents of the collection and sends each term found through the [TermPipeline](javadoc/org/terrier/terms/TermPipeline.html). The TermPipeline transforms the terms, and can remove terms that should not be indexed. The TermPipeline chain in use is `termpipelines=Stopwords,PorterStemmer`, which removes terms from the document using the [Stopwords](javadoc/org/terrier/terms/Stopwords.html) object, and then applies Porter's Stemming algorithm for English to the terms ([PorterStemmer](javadoc/org/terrier/terms/PorterStemmer.html)). If you want to use a different stemmer, this is the point at which it should be called.

The term pipeline can also be configured at indexing time to skip various tokens. Set a comma-delimited list of tokens to skip in the property `termpipelines.skip`. The same property works at retrieval time also.

The indexers are more complicated. Each class can be configured by several properties.

-   `indexing.max.tokens` - The maximum number of tokens the indexer will attempt to index in a document. If 0, then all tokens will be indexed (default).

-   `ignore.empty.documents` - whether to assign document Ids to empty documents. Defaults to true.

For the [BlockIndexer](javadoc/org/terrier/structures/indexing/classical/BlockIndexer.html):

-   `block.indexing` - Whether block indexing should be enabled. Defaults to false.

-   `blocks.size` - How many terms should be in one block. If you want to use phrasal search, this needs to be 1 (default).

-   `blocks.max` - Maximum number of blocks in a document. After this number of blocks, all subsequent terms will be in the same block. Defaults to 100,000.

Once terms have been processed through the TermPipeline, they are aggregated by the [DocumentPostingList](javadoc/org/terrier/structures/indexing/DocumentPostingList.html) and the [LexiconMap](javadoc/org/terrier/structures/indexing/LexiconMap.html). These have a few properties:

-   `max.term.length` - Maximum length of one term, in characters.

Document metadata is recorded in a [MetaIndex](javadoc/org/terrier/structures/MetaIndex.html) structure. For instance, such metadata could include the DOCNO and URL of each document, which the system can use to represent the document during retrieval. The MetaIndex can be configured to take note of various document attributes during indexing. The available attributes depend on those provided by the [Document](javadoc/org/terrier/indexing/Document.html) implementation. MetaIndex can be configured using the following properties:

-   `indexer.meta.forward.keys` - Comma-delimited list of document attributes to store in the MetaIndex. e.g. `indexer.meta.forward.keys=docno` or `indexer.meta.forward.keys=url,title`. If this property is set the following property needs also to be set.

-   `indexer.meta.forward.keylens` - Comma-delimited list of the maximum length of the attributes to be stored in the MetaIndex. Defaults to 20. The number of key lengths here should be identical to the number keys in indexer.meta.forward.keys.

-   `indexer.meta.reverse.keys` - Comma-delimited list of document attributes that *uniquely* denote a document. These mean that given a document attribute value, a single document can be identified.

Note that for presenting results to a user, additional indexing configuration is required. See [Web-based Terrier](terrier_http.md) for more information.

### Choice of Indexers

Terrier supports three types of indexing: *classical two-pass*, and *single-pass*. All three methods create an identical inverted index, that produces identical retrieval effectiveness. However, they differ on other characteristics, namely their support for query expansion, and the scalability and efficiency when indexing large corpora. The choice of indexing method is likely to be driven by your need for query expansion, and the scale of the data you are working with. In particular, only classical two-pass indexing directly creates a direct index, which is used for query expansion. However, classical two-pass indexing doesn't scale to large corpora (maximum practical is about 25 million documents). Single pass indexing is faster, but doesn't create a direct index. If you do create an index that doesn't have a direct index, you can create one later using the `inverted2direct` command of Terrier.

### Classical two-pass indexing

Classical indexing works by creating a direct index, and then inverting that data structure to create an inverted index. For details on the implementation of classical indexing, see the [indexing implementation](indexer_details.md) documentation.

### Single-pass indexing

Single-pass indexing is implemented by the classes [BasicSinglePassIndexer](javadoc/org/terrier/structures/indexing/singlepass/BasicSinglePassIndexer.html) and [BlockSinglePassIndexer](javadoc/org/terrier/structures/indexing/singlepass/BasicSinglePassIndexer.html). Essentially, instead of building a direct file from the collection, term posting lists are held in memory, and written to disk when memory is exhausted. The final step merged the temporary files to form the lexicon and the inverted file. Notably, single-pass indexing does not build a direct index. However, a direct index can be build later using the `inverted2direct` command of Terrier.

For details on the implementation of single-pass indexing, see the [indexing implementation](indexer_details.md) documentation.

### Threaded indexing

Starting from version 4.2, Terrier has *experimental* support for indexing using multiple threads. This can be enabled using `-p` option to `batchindexing`. Both single-pass and classical indexing are supported by threaded indexing.  The number of threads used is equal to the number of CPU cores in the machine, minus one, or can be specified by an optional argument to `-p`.

### Real-time indexing

Terrier also supports the real-time indexing of document collections using MemoryIndex and IncrementalIndex structures, allowing for new documents to be added to the index at later points in time. For more details, please see [Real-time Index Structures](realtime_indices.md).

Compression
-----------

By default, Terrier uses Elias-Gamma and Elias-Unary algorithms for ensuring a highly compressed direct and inverted indices, however since version 4.0 Terrier has support for a variety of state-of-the-art compression schemes including PForDelta. For more information about configuring the compression used for indexing, see the [documentation on compression](compression.md).

More about Block Indexing
-------------------------

### What are blocks?

A block is a unit of text in a document. When you index using blocks, you tell Terrier to save positional information with each term. Depending on how Terrier has been configured, a block can be of size 1 or larger. Size 1 means that the exact position of each term can be determined. For size &gt; 1, the block id is incremented after every N terms. You can configure the size of a block using the property `blocks.size`.

### How do I use blocks?

You can enable block indexing by setting the property `block.indexing` to `true` in your terrier.properties file. The `batchindexing` command also supports a `-b` option. When set, these ensure that the Indexer used for indexing is the BlockIndexer, not the BasicIndexer (or BlockSinglePassIndexer instead of BasicSinglePassIndexer). When loading an index, Terrier will detect that the index has block information saved and use the appropriate classes for reading the index files.

You can use the positional information when doing retrieval. For instance, you can search for documents matching a phrase, e.g. `"Terabyte retriever"`, or where the words occur near each other, e.g. `"indexing blocks"~20`.

### What changes when I use block indexing?

When you enable the property `block.indexing`, TrecTerrier will use the BlockIndexer, not the BasicIndexer (if you have specified single-pass indexing, it is the BlockSinglePassIndexer, not the BasicSinglePassIndexer that is used). The created index data structures will contain the positions for each posting, and can be read by, and when accessed through `PostingIndex.getPostings()` will implement [BlockPosting](javadoc/org/terrier/structures/postings/BlockPosting.html) in addition to [IterablePosting](javadoc/org/terrier/structures/postings/IterablePosting.html).

------------------------------------------------------------------------

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
