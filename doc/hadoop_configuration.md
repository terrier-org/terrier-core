Configuring Terrier for Hadoop
==============================

Overview
--------

From version 2.2 onwards, Terrier has supported the Hadoop MapReduce framework. Currently, Terrier provides [single-pass distributed indexing under MapReduce](hadoop_indexing.md), however, Terrier has been designed to be compatible with other Hadoop driven functionality. In this document, we describe how to integrate your Hadoop and Terrier setups. Hadoop is useful because it allows extremely large-scale operations, using MapReduce technology, built on a distributed file system. More information can be found about deploying Hadoop using a cluster of nodes in the [Hadoop Core documentation](http://hadoop.apache.org/core/docs/current/).

**NB**: Terrier supports only Hadoop v0.22. We have issues with more modern versions. You can track progress on [TR-380](http://terrier.org/issues/browse/TR-380)

Pre-requisites
--------------

Terrier requires a working Hadoop setup, built using a cluster of one or more machines, currently of Hadoop version 0.20.x. The core *may not* currently support newer versions of Hadoop due to minor changes in the Hadoop API. However, should you wish to use a newer version to take advantage of the numerous bug fixes and efficiency improvements introduced, we anticipate that the core can be updated to achieve this (see the [Terrier/Hadoop wiki page](http://ir.dcs.gla.ac.uk/wiki/Terrier/Hadoop) for more information on upgrading Hadoop in Terrier). In the Hadoop Core documentation, we recommend [quickstart](http://hadoop.apache.org/docs/r0.19.0/quickstart.html) and [cluster setup](http://hadoop.apache.org/docs/r0.19.0/cluster_setup.html) documents. If you do not have a dedicated cluster of machines with Hadoop running and do not wish to create one, an alternative is to use use [Hadoop on Demand (HOD)](http://hadoop.apache.org/docs/r0.19.0/hod_user_guide.html). In particular, HOD allows a MapReduce cluster to be built upon an existing [Torque PBS job](http://www.adaptivecomputing.com/products/open-source/torque/) cluster.

In general, Terrier can be configured to use an existing Hadoop installation, by two changes:

1.  Add the location of your $HADOOP\_HOME/conf folder to the CLASSPATH environment variable before running Terrier.

2.  Set property `terrier.plugins=org.terrier.utility.io.HadoopPlugin` in your terrier.properties file.

3.  You must also ensure that there is a world-writable `/tmp` directory on Hadoop’s default file system.

This will allow Terrier to access the shared file system described in your `core-site.xml`. If you also have the MapReduce job tracker setup specified in `mapred-site.xml`, then Terrier can now directly access the MapReduce job tracker to submit jobs.

Using Hadoop On Demand (HOD)
----------------------------

If you don’t have a dedicated Hadoop cluster yet, don't worry. Hadoop provides a utility called Hadoop On Demand (HOD), which can use a [Torque](http://www.adaptivecomputing.com/products/open-source/torque/) PBS cluster to create a Hadoop cluster. Terrier fully supports accessing Hadoop clusters created by HOD, and can even call HOD to create the cluster when its needed for a job. If your cluster is based on [Sun Grid Engine](http://gridengine.sunsource.net/), this supports Hadoop.

If you are using HOD, then Terrier can be configured to automatically access it. Firstly, ensure HOD is working correctly, as described in the HOD [user](http://hadoop.apache.org/docs/r0.19.0/hod_user_guide.html) and [admin](http://hadoop.apache.org/docs/r0.19.0/hod_admin_guide.html) guides. When Terrier wants to submit a MapReduce job, it will use the [HadoopPlugin](javadoc/org/terrier/utility/io/HadoopPlugin.html) to request a MapReduce cluster from HOD. To configure this use the following properties:

-   `plugin.hadoop.hod` - set the full path to the local HOD executable. If this is not set, then HOD will not be used.

-   `plugin.hadoop.hod.nodes` - the number of nodes to request from HOD. Defaults to 6 nodes (sometimes CPUs).

-   `plugin.hadoop.hod.params` - any additional options you want to set on the HOD command line.

For more information on using HOD, see our [HadoopPlugin documentation](javadoc/org/terrier/utility/io/HadoopPlugin.html).

Indexing with Hadoop MapReduce
------------------------------

We provide a [guide for configuring single-pass indexing with MapReduce under Hadoop](hadoop_indexing.md).

Developing MapReduce jobs with Terrier
--------------------------------------

Importantly, it should be possible to modify Terrier to perform other information retrieval tasks using MapReduce. Terrier requires some careful configuration to use in the MapReduce setting. The included, [HadoopPlugin](javadoc/org/terrier/utility/io/HadoopPlugin.html) and [HadoopUtility](javadoc/org/terrier/utility/io/HadoopUtility.html) should be used to link Terrier to Hadoop. In particular, HadoopPlugin/HadoopUtility ensure that Terrier’s share/ folder and the terrier.properties file are copied to a shared space that all job tasks can access. In the configure() method of the map and reduce tasks, you must call `HadoopUtility.loadTerrierJob(jobConf)`. For more information, see [HadoopPlugin](javadoc/org/terrier/utility/io/HadoopPlugin.html). Furthermore, we suggest that you browse the MapReduce indexing source code, both for the map and reduce functions stored in the [Hadoop\_BasicSinglePassIndexer](javadoc/org/terrier/structures/indexing/singlepass/hadoop/Hadoop_BasicSinglePassIndexer.html) and as well as the [input format](javadoc/org/terrier/structures/indexing/singlepass/hadoop/MultiFileCollectionInputFormat.html) and [partitioner](javadoc/org/terrier/structures/indexing/singlepass/hadoop/SplitEmittedTerm.html).

------------------------------------------------------------------------

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2016 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved. 
