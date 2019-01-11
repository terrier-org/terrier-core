
Real-time Indexing with Terrier
===============================

Introduction
------------

In addition to traditional on-disk indices, Terrier provides both memory-only and hybrid memory+disk index structures that can be updated dynamically with new documents over time. Since Terrier 4.0, the top level [Index](javadoc/org/terrier/structures/Index.html) class became abstract such that different types of indices can be supported. The pre-Terrier 4.0 index functionality is contained within the [IndexOnDisk](javadoc/org/terrier/structures/IndexOnDisk.html) class, while new index types were added to enable search systems that can be updated in real-time without a lengthy batch indexing process.

Index Interfaces
----------------

To support real-time indexing, two new interfaces have been defined, namely [UpdatableIndex](javadoc/org/terrier/realtime/UpdatableIndex.html) and [WritableIndex](javadoc/org/terrier/realtime/WritableIndex.html). An index class that implements WritableIndex supports the dynamic addition of new documents via a indexDocument() method. When indexDocument() is called, that document will be added to the index immediately and will be searchable once the indexDocument() returns. The WritableIndex interface represents an index that can be written to disk. In particular, a class that implements WritableIndex will implement a write() method that will convert each of the index structures into equivalent on-disk structures and will be written out to a specified path and with a named prefix. An index written in this way can then be later loaded as an IndexOnDisk index.

Real-time Index Types
---------------------

There are two real-time index types supported since Terrier 4.0:

-   [MemoryIndex](javadoc/org/terrier/realtime/memory/MemoryIndex.html): Represents an index that is held wholly in memory. MemoryIndex is both an UpdatableIndex and a WritableIndex. MemoryIndex is designed to provide a fast updatable index structure for relatively small numbers of documents.

-   [IncrementalIndex](javadoc/org/terrier/realtime/incremental/IncrementalIndex.html): A hybrid index structure that combines a MemoryIndex with zero or more IndexOnDisk indices, facilitating the updating of a large index that could not be stored in memory alone. An incremental index is a [MultiIndex](javadoc/org/terrier/realtime/multi/MultiIndex.html), where one index shard is stored in memory and the rest are stored on disk. Periodically, the memory index is then written to disk, defined as per a FlushPolicy. When the memory index has been flushed to disk, optionally the on-disk portion of the incremental index can then be merged together (based upon a MergePolicy) and/or deleted (based upon a DeletePolicy). Incremental index uses the following properties:

 - `incremental.flush`: the flush policy to use. Four possible values are supported: noflush (default), flushdocs, flushmem, flushtime

 - `incremental.merge`: the merge policy to use. Three possible values are supported: nomerge (default), single, geometric

 - `incremental.delete`: the delete policy to use. Two possible values are supported: nodelete (default), deleteFixedSize

Usage
-----

Below we give some examples for using the real-time Terrier index structures.

-   We provide a Website search demo that illustrates a use of MemoryIndex to search over webpages as they are crawled. For more information about this demo see [Real-time Indexing and Search of Websites](website_search.md).

-   Any custom Java application can make use of an updatable index using MemoryIndex or IncrementalIndex, a code sample that illustrates indexing of a document and then search for that document is provided below:

```java

    // define an example document and query
    String docContent = "Real-time indexing and retrieval is easy to use in Terrier";
    String queryString = "Indexing";

    // create a new index
    MemoryIndex memIndex = new MemoryIndex();

    // get the default tokeniser to break the document down into words
    Tokeniser tokeniser = Tokeniser.Tokeniser.getTokeniser();

    // create a Terrier document from the content string
    Reader contentReader = new StringReader(docContent);
    Map documentProperties = new HashMap();
    FileDocument document = new FileDocument(contentReader, documentProperties, tokeniser);

    // index the document
    memIndex.indexDocument(document);

    // the document is now available for searching

    // create a search manager (runs the search process over an index)
    Manager queryingManager = ManagerFactory.from(memIndex.getIndexRef());

    // a search request represents the search to be carried out
    SearchRequest srq = queryingManager.newSearchRequest("query", sb.toString());

    // define a matching model, in this case use the classical BM25 retrieval model
    srq.setControl(SearchRequest.CONTROL_WMODEL,"BM25");

    // run a Terrier search
    queryingManager.runSearchRequest(srq);

    ScoredDocList results = srq.getResults();
```

------------------------
> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved. 

 
