<span>\[</span>[Previous: What’s New](whats_new.html)<span>\]</span> <span>\[</span>[Contents](index.html)<span>\]</span> <span>\[</span>[Next: Terrier Components](basicComponents.html)<span>\]</span>

Installing and Running Terrier
==============================

If you are interested in using Terrier straightaway in order to index and retrieve from standard test collections, then you may follow the steps described below. We provide step-by-step instructions for the installation of Terrier on Linux and Windows operating systems and guide you through your first indexing and retrieval steps on the TREC WT2G test collection.

Terrier Requirements
--------------------

Terrier’s single requirement consists of an installed Java JRE 1.7.0 or higher. You can download the JRE, or the JDK (if you want to [develop with Terrier](terrier_develop.html), or run the [web-based interface](terrier_http.html)), from the [Java website](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

Download Terrier
----------------

Terrier version 4.1 can be downloaded from the following location: <span>\[</span>[Terrier Home](http://terrier.org/)<span>\]</span>. The site offers pre-compiled releases of the newest and previous Unix and Windows versions of Terrier.

Step by Step Unix Installation
------------------------------

After having downloaded Terrier, copy the file to the directory where you want to install Terrier. Navigate to this directory and execute the following command to decompress the distribution:

    tar -zxvf terrier-core-4.1-bin.tar.gz

This will result in the creation of a terrier directory in your current directory. Next we will have to make sure that you have the correct Java version available on the system. Type:

    echo $JAVA_HOME

If the environment variable $JAVA\_HOME is set, this command will output the path of your Java installation. (e.g. /usr/java/jre1.7.0). If this command shows that you have a correct Java version (1.7.0 or later) installed then your all done. If your system does not meet these requirements you can download a Java 1.7 from the [JRE 1.7 download website](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and set the environment variable by including the following line either in your /etc/profile or ~/.bashrc files:

    export JAVA_HOME=<absolute-path-of-java-installation>

e.g.

    export JAVA_HOME=/usr/java/jre1.7.0

Step by Step Windows Installation
---------------------------------

In order to be able to use Terrier you simply have to extract the contents of the downloaded Zip file into a directory of your choice. Terrier requires Java version 1.7 or higher. If your system does not meet this requirement you can download an appropriate version from the [JRE download website](http://www.oracle.com/technetwork/java/javase/downloads/index.html). Finally, Terrier assumes that java.exe is on the path, so you should use the System applet in the control panel, to ensure that your [Java\\bin folder is in your PATH environment variable](http://www.oracle.com/technetwork/java/javase/install-windows-189425.html#Environment).

Using Terrier
-------------

Terrier comes with three applications:

### Batch (TREC) Terrier

This allows you to easily index, retrieve, and evaluate results on TREC collections. In the next session, we provide you with a step-by-step tutorial of how to use this application.

### Interactive Terrier

This allows you to to do interactive retrieval. This is a quick way to test Terrier. Given that you have installed Terrier on Windows, you can start Interactive Terrier by executing the `interactive_terrier.bat` file in Terrier’s `bin` directory. On a Unix system or Mac, you can run interactive Terrier by executing the `interactive_terrier.sh` file. You can configure the retrieval functionalities of Interactive Terrier using properties described in the [InteractiveQuerying](javadoc/org/terrier/applications/InteractiveQuerying.html) class.

### Desktop Terrier

A sample Desktop search application. If you are interested in getting to know more about it you should take a look at its [tutorial](terrier_desktop.html).

Tutorial: How to use the Batch (TREC) Terrier
---------------------------------------------

### Indexing

This guide will provide step-by-step instructions for using Terrier to index a TREC collection. We assume that the operating system is Linux, and that the collection, along with the topics and the relevance assessments (qrels), is stored in the directory `/local/collections/WT2G/`.

1. Go to the Terrier folder.

    cd terrier-core-4.1

2. Setup Terrier for using a TREC test collection by calling

    ./bin/trec_setup.sh <absolute-path-to-collection-files>

In our example we are using a collection located at `/local/collections/WT2G/` – you will need to adjust this for your environment, e.g.:

    ./bin/trec_setup.sh /local/collections/WT2G/

This will result in the creation of a `collection.spec` file in the “etc” directory. This file contains a list of the files contained in the specified corpus directory.

3. *If necessary*, check/modify the `collection.spec` file. This might be required if the collection directory contained files that you do not want to index. Alternatively, you can do this directly by using the following command:

    find /local/collections/WT2G/ -type f | grep -v "PATTERN" > etc/collection.spec

where `PATTERN` is the regular expression used to identify the files that should not be indexed.

4. Now we are ready to begin the indexing of the collection. This is achieved using the `trec_terrier.sh` script, adding the `-i` option, as follows:

    ./bin/trec_terrier.sh -i

With Terrier’s default settings, the resulting index will be created in the `var/index` folder within the Terrier installation folder.

**Note:** If you do not need the direct index structure for e.g. for query expansion, then you can use `bin/trec_terrier.sh -i -j` for the faster single-pass indexing.

Once indexing completes, you can verify your index by obtaining its statistics, using the `--printstats` option of Terrier.

    bin/trec_terrier.sh --printstats

This displays the number of documents, number of tokens, number of terms, etc.

### Retrieval

In order to perform retrieval from the test collection that has just been indexed, follow the steps described below.

1. First of all we have to do some configuration. Much of Terrier’s functionality is controlled by properties. You can pre-set these in the etc/terrier.properties file, or specify each on the command line. In the following, we are going to use the command line to specify the appropriate properties. To perform retrieval and evaluate the results of a batch of queries, we need to know:

1.  The location of the queries (also known as topic files) - specified using `trec.topics`

2.  The weighting model (e.g. TF\_IDF) to use - specified using `trec.model` - along with any parameter.

3.  The corresponding relevance assessments file (or qrels) for the topics - specified by `trec.qrels`.

2. Let’s do a retrieval run. The `-r` option tells Terrier to do a batch retrieval run, i.e. retrieving the documents estimated to be the most relevant for each query in the topics file. Assuming that you have a `Dtrec.topics` property set to the location of the topics file, you can simply use Terrier’s default settings, by executing:

    ./bin/trec_terrier.sh -r

If all goes well this will result in a `.res` file in the `var/results` directory called `PL2c1_0.res`. We call each `.res` a run.

You can also configure more options on the command line, e.g.:

    ./bin/trec_terrier.sh -r -Dtrec.model=DPH -c 10.99 -Dtrec.topics=/local/collections/WT2G/info/topics.401-450

So what are these? The `-r` parameter instructs Terrier to perform retrieval, while `-c` tells Terrier the parameter for the weighting model. DPH is another Divergence From Randomness weighting model, which is usually more effective than TF\_IDF (to learn more about the model see [the description of the DFR framework](dfr_description.html)).

3. Now we will evaluate the obtained results by using the `-e` option of trec\_terrier.

    ./bin/trec_terrier.sh -e -Dtrec.qrels=/local/collections/WT2G/info/qrels.trec8.small_web.gz

Note that Terrier can read compressed files (e.g. Gzip compression - indicated by the .gz suffix).

Terrier will look at the `var/results` directory, evaluate each .res file and save the output in a .eval file named the same as the corresponding .res file.

4. Now we will perform retrieval again but this time with query expansion (QE) enabled by using the “-q” parameter in addition to “-r”.

    ./bin/trec_terrier.sh -r -q -Dtrec.model=PL2 -c 10.99 -Dtrec.topics=/local/collections/WT2G/info/topics.401-450

See [the guide for configuring retrieval](configure_retrieval.html) for more information about QE. Note that your index must have a direct index structure to support QE, which is not built by default with single-pass indexing (see [Configuring Indexing](configure_indexing.html) for more information). Afterwards we can run the evaluation again by using trec\_terrier.sh with the “-e” parameter.

    ./bin/trec_terrier.sh -e -Dtrec.qrels=/local/collections/WT2G/info/qrels.trec8.small_web.gz

5. Now we can look at all the Mean Average Precision (MAP) values of the runs by executing:

    tail -n 1 var/results/*.eval

The obtained MAP for the first run should be 0.3140.

The obtained MAP for the run using query expansion should be 0.3305

### Interacting with Terrier

You can interact with your index using the Web-based querying interface. Firstly, start the included HTTP server:

    ./bin/http_terrier.sh

You can then enter queries and view results at <http://localhost:8080>. If your running Terrier on another machine, replace localhost with the hostname of the remote machine. For more information on configuring the Web interface, please see [Using Web-based results](terrier_http.html).

<span>\[</span>[Previous: What’s New](whats_new.html)<span>\]</span> <span>\[</span>[Contents](index.html)<span>\]</span> <span>\[</span>[Next: Terrier Components](basicComponents.html)<span>\]</span>

------------------------------------------------------------------------

Webpage: <http://terrier.org>
Contact: [](mailto:terrier@dcs.gla.ac.uk)
[School of Computing Science](http://www.dcs.gla.ac.uk/)
[Information Retrieval Group](http://ir.dcs.gla.ac.uk/)
Copyright (C) 2004-2015 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
