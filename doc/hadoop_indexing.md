

Configuring Hadoop Distributed Indexing in Terrier
==================================================

**NB**: Terrier supports only Hadoop v0.22. We have issues with more modern versions. You can track progress on [TR-380](http://terrier.org/issues/browse/TR-380)

Overview
--------

Due to the ever increasing size of test collections, particularly for those crawled from the Web, there has been a need to provide a distributed means to index such collections efficiently. As such, since version 2.2, Terrier has supported the indexing of large collections in a distributed manner using Hadoop’s MapReduce implementation. This approach uses the single-pass indexer to index sections of each collection (as batches of files) as map tasks. The output from the Map tasks take three forms: (a) terms and mini posting lists (known as runs in the single-pass indexer); (b) document indices from each map task; (c) information about the number of documents saved per run. For more information on our indexing strategy, we recommend the following papers, [Comparing Distributed Indexing: To MapReduce or Not?](http://ir.dcs.gla.ac.uk/terrier/publications/mccreadie09sigirLS.pdf), [On Single-Pass Indexing with MapReduce](http://ir.dcs.gla.ac.uk/terrier/publications/sigir09_mccreadie_mapreduce.pdf) and [MapReduce Indexing Strategies: Studying Scalability and Efficiency](http://dx.doi.org/10.1016/j.ipm.2010.12.003).

Configuration
-------------

To index using the MapReduce indexer, you need to have Terrier setup to use your Hadoop cluster. More information can be found in the [Configuring Terrier for Hadoop](hadoop_configuration.md) documentation. For indexing using MapReduce, your indexing Collection must have a InputStream constructor ([TRECCollection](javadoc/org/terrier/indexing/TRECCollection.html), [TRECWebCollection](javadoc/org/terrier/indexing/TRECWebCollection.html), [WARC09Collection](javadoc/org/terrier/indexing/WARC09Collection.html) and [WARC018Collection](javadoc/org/terrier/indexing/WARC018Collection.html) are all supported). Choose your collection using the property `trec.collection.class` as per normal.

Next, the location of your collection and your index are both important. You will get most benefit from Hadoop if your collection is stored on one of the supported distributed file systems (e.g. HDFS). Hadoop requires that the files of the collection that you are indexing are stored in the shared file system (the one named by `fs.default.name` in the Hadoop configuration). For example:

    $ cat etc/collection.spec
    hdfs://master:9000/Collections/WT2G/WT01/B01.gz
    hdfs://master:9000/Collections/WT2G/WT01/B02.gz
    hdfs://master:9000/Collections/WT2G/WT01/B03.gz
    (etc)

You should also ensure that your index is in on the shared filespace (again, the one named by `fs.default.name` in the Hadoop configuration), by setting the `terrier.index.path` property accordingly. For example you can set, `terrier.index.path=hdfs://master:9000/Indices/WT2G`. You **must** remember to give the appropriate file system prefix, in this case `hdfs://master:9000/`.

Terrier's MapReduce indexing can operate in two modes, which define the style of index or indices that are produced. In particular, in *term-partitioned* mode, one single index is created, but the inverted index is split across up to 26 files. In *document-partitioned* model, multiple Terrier indices are created, one by each reducer. This can be useful if the corpus is too large to be useful in one index. To use document partitioning, run [HadoopIndexing](javadoc/org/terrier/applications/HadoopIndexing.html) directly from the command line (instead of via TrecTerrier), and specify the `-p` argument. The advantage of *term-partitioning* is that Terrier can access the output as one single index, making retrieval simpler. However, note that the current upper limit on the number of files an inverted index can be split across is 31. To achieve this limit, we partition the inverted index with respect to each letter in the English alphabet – this naturally limits the parallelism of the reduce phase to 26 reduce tasks. We do note however, that should a higher level of parallelism be needed, then this might be achieved through careful modification of the [partitioner](javadoc/org/terrier/structures/indexing/singlepass/hadoop/SplitEmittedTerm.html).

Running Indexing Job
--------------------

Running the MapReduce indexer is straightforward, using the `-H` or `--hadoop` options to TrecTerrier. A run of the MapReduce indexer (using Hadoop on demand) is shown below:

    $ bin/trec_terrier.sh -i -H
    Setting TERRIER_HOME to /home/user/terrier
    INFO  HadoopPlugin - Processing HOD for HOD-TerrierIndexing at hod request for 6 nodes
    INFO  HadoopPlugin - INFO - Cluster Id 100.master
    INFO  HadoopPlugin - INFO - HDFS UI at http://master:50070
    INFO  HadoopPlugin - INFO - Mapred UI at http://node01:59794
    INFO  HadoopPlugin - INFO - hadoop-site.xml at /tmp/hod679442803
    INFO  HadoopUtility - Copying terrier share/ directory to shared storage area (hdfs://master:9000/tmp/1265627345-terrier.share)
    WARN  JobClient - Use GenericOptionsParser for parsing the arguments. Applications should implement Tool for the same.
    WARN  JobClient - No job jar file set.  User classes may not be found. See JobConf(Class) or JobConf#setJar(String).
    INFO  MultiFileCollectionInputFormat - Allocating 10 files across 2 map tasks
    INFO  JobClient - Running job: job_200812161322_0001
    INFO  JobClient -  map 0% reduce 0%
    INFO  JobClient -  map 20% reduce 0%
    INFO  JobClient -  map 40% reduce 0%
    INFO  JobClient -  map 60% reduce 0%
    INFO  JobClient -  map 70% reduce 0%
    INFO  JobClient -  map 80% reduce 0%
    INFO  JobClient -  map 90% reduce 0%
    INFO  JobClient -  map 100% reduce 0%
    INFO  JobClient -  map 100% reduce 16%
    INFO  JobClient -  map 100% reduce 70%
    INFO  JobClient -  map 100% reduce 98%
    INFO  JobClient - Job complete: job_200812161322_0001
    INFO  JobClient - Counters: 16
    INFO  JobClient -   File Systems
    INFO  JobClient -     HDFS bytes read=9540868
    INFO  JobClient -     HDFS bytes written=3020756
    INFO  JobClient -     Local bytes read=7937792
    INFO  JobClient -     Local bytes written=15875666
    INFO  JobClient -   Job Counters
    INFO  JobClient -     Launched reduce tasks=1
    INFO  JobClient -     Rack-local map tasks=2
    INFO  JobClient -     Launched map tasks=2
    INFO  JobClient -   Map-Reduce Framework
    INFO  JobClient -     Reduce input groups=46130
    INFO  JobClient -     Combine output records=0
    INFO  JobClient -     Map input records=2124
    INFO  JobClient -     Reduce output records=0
    INFO  JobClient -     Map output bytes=7724117
    INFO  JobClient -     Map input bytes=-4167449
    INFO  JobClient -     Combine input records=0
    INFO  JobClient -     Map output records=75700
    INFO  JobClient -     Reduce input records=75700
    INFO  HadoopPlugin - Processing HOD disconnect

**NB:** Please note that you must wait for the MapReduce job to end, and not kill the local Terrier process, otherwise temporary files may be left on the shared file system.

Creating a Direct Index
-----------------------

Once you have indexed your corpus using MapReduce, you can also create a direct file in MapReduce. In particular, [Inv2DirectMultiReduce](javadoc/org/terrier/structures/indexing/singlepass/hadoop/Inv2DirectMultiReduce.html) uses a MapReduce job to re-invert the inverted index and create an inverted index. This class is more scalable for large indices than using [Inverted2DirectIndexBuilder](javadoc/org/terrier/structures/indexing/singlepass/Inverted2DirectIndexBuilder.html).

Bibliography
------------

1.  MapReduce Indexing Strategies: Studying Scalability and Efficiency. Richard McCreadie, Craig Macdonald and Iadh Ounis. In Information Processing & Management J, 2011. DOI [10.1016/j.ipm.2010.12.003](http://dx.doi.org/10.1016/j.ipm.2010.12.003).

2.  Comparing Distributed Indexing: To MapReduce or Not? Richard McCreadie, Craig Macdonald, Iadh Ounis. In Proceedings of LSDS Workshop at SIGIR 2009. <http://ir.dcs.gla.ac.uk/terrier/publications/mccreadie09sigirLS.pdf>

3.  On Single-Pass Indexing with MapReduce. Richard McCreadie, Craig Macdonald and Iadh Ounis. In Proceedings of SIGIR 2009. DOI [10.1145/1571941.1572106](http://doi.acm.org/10.1145/1571941.1572106)

------------------------------------------------------------------------

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2016 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved. 
