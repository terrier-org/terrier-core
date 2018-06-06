Quickstart Guide: Using Terrier for Experiments
==============================

If you are interested in using Terrier straight-away in order to index and retrieve from standard test collections, then you may follow the steps described below. We provide step-by-step instructions for the installation of Terrier on Linux and Windows operating systems and guide you through your first indexing and retrieval steps on a test collection.

Terrier Requirements
--------------------

Terrier’s single requirement consists of an installed Java JRE 1.8.0 or higher. You can download the JRE, or the JDK (if you want to [develop with Terrier](terrier_develop.md), or run the [web-based interface](terrier_http.md)), from the [Java website](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

Download Terrier
----------------

Terrier can be downloaded from the following location:[Terrier Home](http://terrier.org/). The site offers pre-compiled releases of the newest and previous Unix and Windows versions of Terrier.

Step by Step Unix Installation
------------------------------

After having downloaded Terrier, copy the file to the directory where you want to install Terrier. Navigate to this directory and execute the following command to decompress the distribution:

    tar -zxvf terrier-project-5.0-bin.tar.gz

This will result in the creation of a terrier directory in your current directory. Next we will have to make sure that you have the correct Java version available on the system. Type:

    echo $JAVA_HOME

If the environment variable $JAVA\_HOME is set, this command will output the path of your Java installation. (e.g. /usr/java/jre1.8.0). If this command shows that you have a correct Java version (1.8.0 or later) installed then your all done. If your system does not meet these requirements you can download a Java 1.8 from the [JRE 1.8 download website](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and set the environment variable by including the following line either in your /etc/profile or ~/.bashrc files:

    export JAVA_HOME=<absolute-path-of-java-installation>

e.g.

    export JAVA_HOME=/usr/java/jre1.8.0

Step by Step Windows Installation
---------------------------------

In order to be able to use Terrier you simply have to extract the contents of the downloaded Zip file into a directory of your choice. Terrier requires Java version 1.8 or higher. If your system does not meet this requirement you can download an appropriate version from the [JRE download website](http://www.oracle.com/technetwork/java/javase/downloads/index.html). Finally, Terrier assumes that java.exe is on the path, so you should use the System applet in the control panel, to ensure that your [Java\\bin folder is in your PATH environment variable](http://www.oracle.com/technetwork/java/javase/install-windows-189425.html#Environment).

**The following instructions are equally applicable to Windows, with the exception that the script filenames are suffixed by .bat**.

Using Terrier
-------------

Terrier has a number of in-built commands. All of these can be accessed through the in-built `terrier` commandline script. While In Terrier's home directory, type `bin/terrier` to see the available commands.

### Batch Indexing and Retrieval using Terrier

This allows you to easily index, retrieve, and evaluate results on TREC collections. In the next session, we provide you with a step-by-step tutorial of how to use this application.

### Interactive Terrier

This allows you to to do interactive retrieval. This is a quick way to test Terrier. If that you have installed Terrier on Windows, you can start Interactive Terrier by executing the `bin/terrier.bat interactive`. On a Unix system or Mac, you can run interactive Terrier by executing the `bin/terrier interactive` file. You can configure the retrieval functionalities of Interactive Terrier using properties described in the [InteractiveQuerying](javadoc/org/terrier/applications/InteractiveQuerying.html) class.

### Desktop Terrier

A sample Desktop search application, is available [separately from Github](https://github.com/terrier-org/terrier-desktop).

Tutorial: How to use the Batch (TREC) Terrier
---------------------------------------------

### Indexing

This guide will provide step-by-step instructions for using Terrier to index a TREC collection. We assume that the operating system is Linux, and that the collection, along with the topics and the relevance assessments (qrels), is stored in the directory `share/vaswani_npl/`.

1. Go to the Terrier folder.

```shell
    cd terrier-project-5.0
```

2. Setup Terrier for using a TREC test collection by calling

```shell
  bin/trec_setup.sh <absolute-path-to-collection-files>
```

In our example we are using a collection called VASWANI_NPL located at `share/vaswani_npl/`. It follows a traditional TREC test collection, with a corpus file, topics, and relevance assessments (qrels), and using the same format.

    $head share/vaswani_npl/corpus/doc-text.trec
    <DOC>
    <DOCNO>1</DOCNO>
    compact memories have flexible capacities  a digital data storage
    system with capacity up to bits and random and or sequential access
    is described
    </DOC>

To setup for this corpus, run:

```shell
bin/trec_setup.sh share/vaswani_npl/corpus/
```

This will result in the creation of a `collection.spec` file in the `etc` directory. This file contains a list of the document files contained in the specified corpus directory.

3. *If necessary*, check/modify the `collection.spec` file. This might be required if the collection directory contained files that you do not want to index (READMEs, etc).

4. Now we are ready to begin the indexing of the collection. This is achieved using the batchindexing command called from the `terrier` script, as follows:

```
$bin/terrier batchindexing
16:00:03.028 [main] INFO  o.terrier.indexing.CollectionFactory - Finished reading collection specification
16:00:03.046 [main] INFO  o.t.i.MultiDocumentFileCollection - TRECCollection 0% processing share/vaswani_npl/corpus//doc-text.trec
16:00:03.116 [main] INFO  o.t.structures.indexing.Indexer - creating the data structures data_1
16:00:04.885 [main] INFO  o.t.structures.indexing.Indexer - Collection #0 took 1 seconds to index (11429 documents)
16:00:04.918 [main] INFO  o.t.s.indexing.LexiconBuilder - 6 lexicons to merge
16:00:05.045 [main] INFO  o.t.s.indexing.LexiconBuilder - Optimising structure lexicon
16:00:05.047 [main] INFO  o.t.structures.FSOMapFileLexicon - Optimising lexicon with 7756 entries
16:00:05.761 [main] INFO  o.t.structures.indexing.Indexer - Started building the inverted index...
16:00:05.761 [main] INFO  o.t.structures.indexing.Indexer - Started building the inverted index...
16:00:05.766 [main] INFO  o.t.s.i.c.InvertedIndexBuilder - Iteration 1 of 1 iterations
16:00:06.929 [main] INFO  o.t.s.indexing.LexiconBuilder - Optimising structure lexicon
16:00:06.930 [main] INFO  o.t.structures.FSOMapFileLexicon - Optimising lexicon with 7756 entries
16:00:06.954 [main] INFO  o.t.structures.indexing.Indexer - Finished building the inverted index...
16:00:06.954 [main] INFO  o.t.structures.indexing.Indexer - Time elapsed for inverted file: 1
```

With Terrier's default settings, the resulting index will be created in the `var/index` folder within the Terrier installation folder.

**Note:** If you do not need the direct index structure for e.g. for query expansion, then you can use `bin/terrier batchindexing -j` for the faster single-pass indexing.

Once indexing completes, you can verify your index by obtaining its statistics, using the `indexstats` command of Terrier.

    $bin/terrier indexstats
    16:21:45.086 [main] INFO  org.terrier.applications.TrecTerrier - Collection statistics:
    16:21:45.088 [main] INFO  org.terrier.applications.TrecTerrier - number of indexed documents: 11429
    16:21:45.088 [main] INFO  org.terrier.applications.TrecTerrier - size of vocabulary: 7756
    16:21:45.088 [main] INFO  org.terrier.applications.TrecTerrier - number of tokens: 271581
    16:21:45.089 [main] INFO  org.terrier.applications.TrecTerrier - number of pointers: 224573

This displays the number of documents, number of tokens, number of terms, etc.

### Querying

Firstly, lets see if we can get search results from our index. We can use the `bin/terrier interactive` command to query the index for results.

    $bin/terrier interactive
    16:30:07.139 [main] INFO  o.t.structures.CompressingMetaIndex - Structure meta reading lookup file into memory
    16:30:07.146 [main] INFO  o.t.structures.CompressingMetaIndex - Structure meta loading data file into memory
    16:30:07.152 [main] INFO  o.t.applications.InteractiveQuerying - time to intialise index : 0.086
    Please enter your query: compressed
    16:30:26.624 [main] INFO  o.t.matching.PostingListManager - Query 1 with 1 terms has 1 posting lists

    	Displaying 1-22 results
    0 11196 6.965311483754079
    1 6891 6.861351572397433
    2 8706 6.6285666210018395
    3 6812 6.419975936835514
    4 3286 6.0561185692309065
    5 4007 5.744292373685925
    6 70 5.603313027948017
    ...
    Please enter your query: exit

In responding to the query `compressed`, Terrier found document 11196 was estimated to be most relevant, scoring 6.96. 11196 was recorded from the DOCNO tag of the corresponding document.

### Retrieval

Information retrieval has a history of evaluating search effectiveness automatically, using queries with associated relevance assessments. In order to perform retrieval using an existing index, follow the steps described below.

1. First of all we have to do some configuration. Much of Terrier's functionality is controlled by properties. You can pre-set these in the `etc/terrier.properties` file, or specify each on the command line. In the following, we are going to use the command line to specify the appropriate properties. To perform retrieval and evaluate the results of a batch of queries, we need to know:

a.  The location of the queries (also known as topic files) - specified using `trec.topics`

b.  The weighting model (e.g. TF\_IDF) to use - specified using `trec.model` - along with any parameter. The default is InL2.

c.  The corresponding relevance assessments file (or qrels) for the topics - specified by `trec.qrels`.

2. Let's do a retrieval run. The `batchretrieve` command tells Terrier to do a batch retrieval run, i.e. retrieving the documents estimated to be the most relevant for each query in the topics file. However, instead of having `trec.topics` property set in the `terrier.properties` file, we specify it on the command line (all other configuration remains using Terrier’s default settings):

```
    $bin/terrier batchretrieve -Dtrec.topics=share/vaswani_npl/query-text.trec
    ...
    16:14:43.440 [main] INFO  o.t.matching.PostingListManager - Query 93 with 10 terms has 10 posting lists
    16:14:43.444 [main] INFO  o.t.a.batchquerying.TRECQuerying - Time to process query: 0.006
    16:14:43.461 [main] INFO  o.t.a.batchquerying.TRECQuerying - Settings of Terrier written to var/results/InL2c1_0.res.settings
    16:14:43.461 [main] INFO  o.t.a.batchquerying.TRECQuerying - Finished topics, executed 93 queries in 0.866 seconds, results written tovar/results/InL2c1_0.res
    Time elapsed: 0.987 seconds.
```

If all goes well this will result in a `.res` file in the `var/results` directory called `InL2c1_0.res`. We call each `.res` a run.

For example, you can also configure more options on the command line, e.g.:

    $bin/terrier batchretrieve -Dtrec.model=BM25 -c c:0.4 -Dtrec.topics=share/vaswani_npl/query-text.trec

So what are these? The `batchretrieve` command instructs Terrier to perform retrieval, while `-Dtrec.model=BM25`  tells Terrier to use the BM25 weighting model --  BM25 is a classical Okapi model firstly defined by Stephen Robertson, while InL2 is a Divergence From Randomness weighting model (to learn more, see [the description of the DFR framework](dfr_description.md)).  `-c c:0.4` tells Terrier the parameter for the weighting model. Note - if you do not specify `c:0.4`, then the default parameter will be used for that weighting model.

3. Now we will evaluate the obtained results by using the `batchevaluate` option of trec\_terrier:

```
    $bin/terrier batchevaluate -Dtrec.qrels=share/vaswani_npl/qrels
    16:27:28.527 [main] INFO  o.t.evaluation.TrecEvalEvaluation - Evaluating result file: var/results/InL2c1.0_0.res
    Average Precision: 0.2948
```

Terrier will look at the `var/results` directory, evaluate each .res file and save the output in a .eval file named the same as the corresponding .res file.

4. We can change the retrieval approach usrd by Terrier to perform retrieval. For instance, query expansion (QE) can enabled by using the `-q` parameter in addition to `-r`.

```shell
bin/terrier batchretrieve -q
```

See [the guide for configuring retrieval](configure_retrieval.md) for more information about QE. Note that your index must have a direct index structure to support QE, which is not built by default with single-pass indexing (see [Configuring Indexing](configure_indexing.md) for more information). Afterwards we can run the evaluation again by using trec\_terrier.sh with the `-e` parameter.

```shell
bin/terrier batchevaluate -Dtrec.qrels=share/vaswani_npl/qrels
```

5. Now we can look at all the Mean Average Precision (MAP) values of the runs by inspecting the `.eval` files in `var/results`:

The obtained MAP for the InL2 should be 0.2948.
The obtained MAP for BM25 should be 0.2992.
The obtained MAP for the run using InL2 with query expansion should be 0.3020.

### Interacting with Terrier

You can interact with your index using a Web-based querying interface. Firstly, start the included HTTP server:

    $bin/terrier http

You can then enter queries and view results at <http://localhost:8080> (If your running Terrier on another machine, replace localhost with the hostname of the remote machine). Terrier can provide more information in the search results -- for more information on configuring the Web interface, please see [Using Web-based results](terrier_http.md).


------------------------------------------------------------------------

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2018 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
