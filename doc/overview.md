

Terrier Features
================

Below, you can find a succinct list of features offered by Terrier.

### General

-   Indexing support for common desktop file formats, and for commonly used TREC research collections (e.g. TREC CDs 1-5, WT2G, WT10G, GOV, GOV2, Blogs06, Blog08, ClueWeb09, ClueWeb12).

-   Many document weighting models, such as many parameter-free Divergence from Randomness weighting models, Okapi BM25 and language modelling.

-   ***NEW!*** Supervised (machine learned) ranking models are supported via learning to rank.

-   Conventional query language supported, including phrases, and terms occurring in tags.

-   Handling full-text indexing of large-scale document collections, in a centralised architecture to at least 50 million documents, and using the Hadoop MapReduce distributed indexing scheme for even larger collections.

-   ***NEW!*** Incremental indexing and retrieval capabilities to support real-time search

-   Modular and open indexing and querying APIs, to allow easy extension for your own applications and research.

-   Active Information Retrieval research fed into the Open Source platform.

-   Open Source (Mozilla Public Licence).

-   Written in cross-platform Java - works on Windows, Mac OS X, Linux and Unix.

-   Large user-base over 10 years of public release.

### Indexing

-   Out-of-the box indexing of tagged document collections, such as the TREC test collections.

-   Out-of-the box indexing for documents of various formats, such as HTML, PDF, or Microsoft Word, Excel and PowerPoint files.

-   Out-of-the box support for distributed indexing in a Hadoop MapReduce setting.

-   Indexing of field information, such as the frequency of a term in a TITLE or H1 HTML tag.

-   Indexing of position information on a word, or a block (e.g. a window of terms within a distance) level.

-   Support for various encodings of documents (UTF), to facilitate multi-lingual retrieval.

-   Support for changing the tokenisation being used.

-   ***NEW!*** Updatable indices to support real-time search

-   Indexing support for query-biased summarisation.

-   Support for fetching files to index by HTTP, allowing intranets to be easily searched.

-   ***NEW!*** Highly compressed index disk data structures with built-in pluggable compression algorithms.

-   Highly compressed direct file for efficient query expansion.

-   Alternative faster single-pass and MapReduce based indexing.

-   Various stemming techniques supported, including the Snowball stemmer for European languages.

### Retrieval

-   Provides desktop, command-line and Web based querying interfaces.

-   Provides standard querying facilities, as well as Query Expansion (pseudo-relevance feedback).

-   Can be applied in interactive applications, such as the included [Desktop Search](terrier_desktop.md), or in a batch setting for research and experimentation.

-   Provides many standard document weighting models, including up to 126 Divergence From Randomness (DFR) document ranking models, and other models such as Okapi BM25, language modelling and TF-IDF. Two new 2nd generation DFR weighting model, JsKLs and XSqrA\_M, are also included, which provide robust performance on a range of test collections without the need for any parameter tuning or training.

-   Advanced [query language](querylanguage.md) that supports synonyms, +/- operators, phrase and proximity search, and fields.

-   ***NEW!*** Learning-to-rank support enables out-of-the-box supervised ranking models.

-   Provides a number of parameter-free DFR term weighting models for automatic query expansion, in addition to Rocchio's query expansion.

-   Flexible processing of terms through a pipeline of components, such as stopword removers and stemmers.

### Experimentation

-   Handles all currently available TREC test collections - see [TREC Experimentation Examples](trec_examples.md) for examples and known settings.

-   Easily scriptable to evaluate many parameter settings, or many weighting models in batch form.

-   Built-in version of NIST's trec_eval software for evaluating precision and recall measures, see [evaluation tools](evaluation.md).



------------------------------------------------------------------------

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
