

Examples of using Terrier to index TREC collections: WT2G & Blogs06
===================================================================

Terrier can index all known TREC test collections. We refer readers to the [Terrier wiki](http://ir.dcs.gla.ac.uk/wiki/Terrier) for latest configuration for indexing various collections:

-   [Disks1&2](http://ir.dcs.gla.ac.uk/wiki/Terrier/Disks1&2)

-   [Disks4&5](http://ir.dcs.gla.ac.uk/wiki/Terrier/Disks4&5)

-   [WT2G](http://ir.dcs.gla.ac.uk/wiki/Terrier/WT2G)

-   [WT10G](http://ir.dcs.gla.ac.uk/wiki/Terrier/WT10G)

-   [DOTGOV](http://ir.dcs.gla.ac.uk/wiki/Terrier/DOTGOV) – see also the [learning to rank](learning.md) documentation page.

-   [DOTGOV2](http://ir.dcs.gla.ac.uk/wiki/Terrier/DOTGOV2)

-   [Blogs06](http://ir.dcs.gla.ac.uk/wiki/Terrier/Blogs06)

-   [Blogs08](http://ir.dcs.gla.ac.uk/wiki/Terrier/Blogs08)

-   [ClueWeb09-B](http://ir.dcs.gla.ac.uk/wiki/Terrier/ClueWeb09-B)

-   [ClueWeb12](http://ir.dcs.gla.ac.uk/wiki/Terrier/ClueWeb12)

TREC WT2G Collection
--------------------

Here we give an example of using Terrier to index WT2G - a standard [TREC](http://trec.nist.gov) test collection. We assume that the operating system is Linux, and that the collection, along with the topics and the relevance assessments, is stored in the directory `/local/collections/WT2G`. The following configurations are sufficient for batch retrieval, however if you want to build a web-based search interface for searching WT2G, see [Web-based Terrier](terrier_http.md).

```shell
    #goto the terrier folder
    cd terrier

    #get terrier setup for using a trec collection
    bin/trec_setup.sh /local/collections/WT2G/

    #rebuild the collection.spec file correctly
    find /local/collections/WT2G/ -type f | sort |grep -v info > etc/collection.spec

    #use In_expB2 DFR model for querying
    echo trec.model=org.terrier.matching.models.In_expB2 >> etc/terrier.properties

    #use this file for the topics
    echo trec.topics=/local/collections2/WT2G/info/topics.401-450.gz >> etc/terrier.properties

    #use this file for query relevance assessments
    echo trec.qrels=/local/collections2/WT2G/info/qrels.trec8.small_web.gz >> etc/terrier.properties

    #index the collection
    bin/terrier batchindexing

    #run the topics, with suggested c value 10.99
    bin/batchretrieval -c c:10.99
    #run topics again with query expansion enabled
    bin/batchretrieval -r -q -c 10.99

    #evaluate the results in var/results/
    bin/batchevaluate

    #display the Mean Average Precision
    grep ^map var/results/*.eval
    #MAP should be
    #In_expB2 Average Precision: 0.3160
```

TREC Blogs06 Collection
-----------------------

This guide will provide a step-by-step example on how to use Terrier for indexing, retrieval and evaluation. We use TREC Blogs06 test collection, along with the corresponding topics and the qrels from TREC 2006 Blog track. We assume that these are stored in the directory `/local/collections/Blogs06/`

### Indexing

In the Terrier folder, use trec\_setup.sh to generate a collection.spec for indexing the collection:

    $ ./bin/trec_setup.sh /local/collections/Blogs06/
    $ find /local/collections/Blogs06/ -type f  | grep 'permalinks-' | sort > etc/collection.spec

This will result in the creation of a `collection.spec` file, in the `etc` directory, containing a list of the files in the `/local/collections/Blog06/` directory. At this stage, you should check the `etc/collection.spec`, to ensure that it only contains files that should be indexed, and that they are sorted (ie `20051206/permalinks-000.gz` is the first file).

The TREC Blogs06 collection differs from other TREC collections in that not all tags should be indexed. For this reason, you should configure the parse in TRECCollection not to process these tags. Set the following properties in your `etc/terrier.properties` file:

    TrecDocTags.doctag=DOC
    TrecDocTags.idtag=DOCNO
    TrecDocTags.skip=DOCHDR,DATE_XML,FEEDNO,BLOGHPNO,BLOGHPURL,PERMALINK

Finally, the length of the DOCNOs in the TREC Blogs06 collection are 31 characters, longer than the default 20 characters in Terrier. To deal with this, update properties relating to the MetaIndex in terrier.properties:

    indexer.meta.forward.keys=docno
    indexer.meta.forward.keylens=31
    indexer.meta.reverse.keys=docno

Now you are ready to start indexing the collection.

    $ bin/terrier batchindexing
    Setting TERRIER_HOME to /local/terrier
    INFO - TRECCollection read collection specification
    INFO - Processing /local/collections/Blogs06/20051206/permalinks-000.gz
    INFO - creating the data structures data_1
    INFO - Processing /local/collections/Blogs06/20051206/permalinks-001.gz
    INFO - Processing /local/collections/Blogs06/20051206/permalinks-002.gz
    <snip>

If we did not plan to use Query Expansion initially, then the faster single-pass indexing could be enabled, using the -j option of TrecTerrier. If we decide to use query expansion later, we can use the [Inverted2DirectIndexBuilder](javadoc/org/terrier/structures/indexing/singlepass/Inverted2DirectIndexBuilder.html) to create the direct index ([BlockInverted2DirectIndexBuilder](javadoc/org/terrier/structures/indexing/singlepass/BlockInverted2DirectIndexBuilder.html) for blocks).

    $ bin/terrier batchindexing -j
    Setting TERRIER_HOME to /local/terrier
    INFO - TRECCollection read collection specification
    INFO - Processing /local/collections/Blogs06/20051206/permalinks-000.gz
    Starting building the inverted file...
    INFO - creating the data structures data_1
    INFO - Creating IF (no direct file)..
    INFO - Processing /local/collections/Blogs06/20051206/permalinks-001.gz
    INFO - Processing /local/collections/Blogs06/20051206/permalinks-002.gz
    <snip>
    [user@machine terrier]$ ./bin/anyclass.sh org.terrier.structures.indexing.singlepass.Inverted2DirectIndexBuilder
    INFO - Generating a direct index from an inverted index
    INFO - Iteration - 1 of 20 iterations
    INFO - Generating postings for documents with ids 0 to 120435
    INFO - Writing the postings to disk
    <snip>
    INFO - Finishing up: rewriting document index
    INFO - Finished generating a direct index from an inverted index

Indexing will take a reasonable amount of time on a modern machine. Additionally, expect to double indexing time if block indexing is enabled. Using single-pass indexing is significantly faster, even if the direct file has to be built later.

### Retrieval

Once the index is built, we can do retrieval using the index, following the steps described below.

First, tell Terrier the location of the topics and relevance assessments (qrels).

    [user@machine terrier]$ echo trec.topics=/local/collections/Blog06/06.topics.851-900 >> etc/terrier.properties
    [user@machine terrier]$ echo trec.qrels=/local/collections/Blog06/qrels.blog06 >> etc/terrier.properties

Next, we should specify the retrieval weighting model that we want to use. In this case we will use the DFR model called PL2 for ranking documents (blog posts).

    echo trec.model=org.terrier.matching.models.PL2 >> etc/terrier.properties

Now we are ready to start retrieval. We use the `-c` to set the parameter of the weighting model to the value 1. Terrier will do retrieval by taking each query (called a topic) from the specified topics file, query the index using it, and save the results to a file in the `var/results` folder, named similar to `PL2c1.0_0.res`. The file `PL2c1.0_0.res.settings` contains a dump of the properties and other settings used to generated the run.

    $ bin/terrier batchretrieval -c c:1
    Setting TERRIER_HOME to /local/terrier
    INFO - 900 : mcdonalds
    INFO - Processing query: 900
    <snip>
    INFO - Finished topics, executed 50 queries in 27 seconds, results written to
        terrier/var/results/PL2c1.0_0.res
    Time elapsed: 40.57 seconds.

### Evaluation

We can now evaluate the retrieval performance of the generated run using the qrels specified earlier:

    $ bin/terrier batchevaluate
    Setting TERRIER_HOME to /local/terrier
    INFO - Evaluating result file: /local/terrier/var/results/PL2c1.0_0.res
    Average Precision: 0.2703
    Time elapsed: 3.177 seconds.

Note that more evaluation measures are stored in the file `var/results/PL2c1.0_0.eval`.

[]()

Common TREC Settings
--------------------

This page provides examples of settings for indexing and retrieval on TREC collections. For example, to index the disk1&2 collection, the `etc/terrier.properties` should look like as follows:


    #default controls for query expansion
    querying.postprocesses.order=QueryExpansion
    querying.postprocesses.controls=qe:QueryExpansion

    #default and allowed controls
    querying.default.controls=c:1.0,start:0,end:999
    querying.allowed.controls=c,scope,qe,start,end

    matching.retrieved_set_size=1000

    #document tags specification
    #for processing the contents of
    #the documents, ignoring DOCHDR
    TrecDocTags.doctag=DOC
    TrecDocTags.idtag=DOCNO
    TrecDocTags.skip=DOCHDR
    #the tags to be indexed
    TrecDocTags.process=TEXT,TITLE,HEAD,HL
    #do not store position information in the index. Set it to true otherwise.
    block.indexing=false

    #query tags specification
    TrecQueryTags.doctag=TOP
    TrecQueryTags.idtag=NUM
    TrecQueryTags.process=TOP,NUM,TITLE
    TrecQueryTags.skip=DOM,HEAD,SMRY,CON,FAC,DEF,DESC,NARR

    #stop-words file. default folder is ./share
    stopwords.filename=stopword-list.txt

    #the processing stages a term goes through
    #the following setting applies standard stopword removal and Porter's stemming algorithm.
    termpipelines=Stopwords,PorterStemmer

The following table lists the indexed tags (corresponding to the property `TrecDocTags.process`) and the running time for a singlepass inverted index creation on 6 TREC collections. No indexed tags are specified for the WT2G, WT10G, DOTGOV and DOTGOV2 collections, which means the system indexes everything in these collections. The indexing was done on a CentOS 5 Linux machine with Intel Core2 2.4GHz CPU and 2GB RAM (a maximum of 1GB RAM is allocated to the Java virtual machine).

**NB**: These times are quite dated.

|Collection|Indexed tags (`TrecDocTags.process`)|Indexing time (seconds)|
|--|--|--|
|disk1&2|TEXT,TITLE,HEAD,HL|766.85|
|disk4&5|TEXT,H3,DOCTITLE,HEADLINE,TTL|92.115|
|WT2G||709.906|
|WT10G||3,556.09|
|DOTGOV||4,435.12|
|DOTGOV2||96,340.00|

The following table compares the indexing time using the classical two-phase indexing and single-pass indexing with and without storing the terms positions (blocks). The table shows that the single-pass indexing is markedly faster than the two-phase indexing, particular when block indexing is enabled.

|Collection|Two-phase|Single-pass|Two-phase + blocks|Single-pass + blocks|
|--|--|--|--|--|
|disk1&2|13.5 min|8.65 min|32.6 min|12.1 min|
|disk4&5|11.7 min|7.63 min|25.0 min|10.2 min|
|WT2G|9.95 min|7.52 min|23.6 min|10.8 min|
|WT10G|62.5 min|34.7 min|2hour 18min|53.1 min|
|DOTGOV|71.0min|47.1min|2hour 45min|1hour 11min|


The following table lists the retrieval performance achieved using three weighting models, namely the Okapi [BM25](javadoc/org/terrier/matching/models/BM25.html), DFR [PL2](javadoc/org/terrier/matching/models/PL2.html) and the new parameter-free [DFRee](javadoc/org/terrier/matching/models/DFRee.html) model on a variety of standard TREC test collections. We provide the best values for the b and c parameters of BM25 and PL2 respectively, by optimising MAP using a simulated annealing process. In contrast, DFRee performs robustly across all collections while it does not require any parameter tuning or training.

| Collection and tasks | B25| BM25| PL2 | PL2 | DFRee |
|--|--|--|--|--|--|
| | b value| MAP| c value| MAP | MAP |
|disk1&2, TREC1-3 adhoc tasks|0.3277|0.2324|4.607|0.2260|0.2175|
|disk4&5, TREC 2004 Robust Track|0.3444|0.2502|9.150|0.2531|0.2485|
|WT2G, TREC8 small-web task|0.2381|0.3186|26.04|0.3252|0.2829|
|WT10G, TREC9-10 Web Tracks|0.2505|0.2104|12.33|0.2103|0.2030|
|DOTGOV, TREC11 Topic-distillation task|0.7228|0.1910|1.280|0.2030|0.1945|
|DOTGOV2, TREC2004-2006 Terabyte Track adhoc tasks|0.39|0.3046|6.48|0.3097|0.2935|

Many of the above TREC collections can be obtained directly from either [TREC (NIST)](http://trec.nist.gov), or from the [University of Glasgow](http://ir.dcs.gla.ac.uk/test_collections/)


------------------------------------------------------------------------


> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
