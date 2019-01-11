Developing Applications with Terrier
====================================

Terrier provides APIs for [indexing](extend_indexing.md) documents, and [querying](extend_retrieval.md) the generated indices. If you are developing applications using Terrier or extending it for your own research, then you may find the following information useful.

Extending Terrier
-----------------

Terrier has a very flexible and modular architecture, with many classes, some with various alternatives. It is very easy to change many parts of the indexing and retrieval process. Essential to any in-depth extension of Terrier is to examine the very many properties that can be configured in Terrier. For instance, if you write a new Matching class, you can use this in a TREC-like setting by setting the property `trec.matching`, while if you write a new document weighting model you should set the property `trec.model` to use it. For more information about extending the retrieval functionalities of Terrier, see [Extending Retrieval](extend_retrieval.md), and [Extending Indexing](extend_indexing.md) for more information about the indexing process Terrier uses.

### FileSystem Abstraction Layer

All File IO in Terrier (excluding the Desktop application and Terrier configuration) is performed using the [Files](javadoc/org/terrier/utility/Files.html) class. This affords various opportunities for allowing Terrier to run in various environments. In Terrier, a FileSystem abstraction layer was integrated into the Files class, such that other [FileSystem](javadoc/org/terrier/utility/io/FileSystem.html) implementations could be plugged in. By default, Terrier ships with two implementation, namely [LocalFileSystem](javadoc/org/terrier/utility/io/LocalFileSystem.html) for reading the local file system using the Java API, and [HTTPFileSystem](javadoc/org/terrier/utility/io/HTTPFileSystem.html) for reading files accessible by HTTP or HTTPS protocols. A filename is searched for a prefixing scheme (eg “file://”), similar to a URI or URL. If a scheme is detected, then Terrier will search through its known file system implementations for a file system supporting the found scheme. file:// is the default scheme if no scheme can be found in the filename; if the filename starts http://, then the file will be fetched by HTTP. This abstraction layer has also supported Hadoop Distributed Filesystem for prefixes with hdfs:// - for more information, see [Configuring Terrier for Hadoop](hadoop_configuration.md).

The Files layer can also transform paths to filenames on the fly. For example, if a certain HTTP namespace is accessible as a local file system, the Files layer can be informed using `Files.addPathTransformation()`. If you have a slow network file system, consider using the in-built caching layer in Files.

Additional implementations can implement methods of the FileSystem interface that they support, and register themselves by calling the `Files.addFileSystemCapability()` method. The FileSystem denotes the operations it supports on a file or path by returning the bit-wise OR of the constants named in Files.FSCapability.

Finally, the Files layer supports transparent compression for reading or writing file streams. In particular, compressed files using Gzip (.gz) and Bzip2 (.bz2) can be obtained by just adding the extension to the file. Moreover, alternative compression/decompression libraries can be added using the `addFilterInputStreamMapping()` method.

**NB**: We may replace the FileSystem Abstraction Layer with Java's nio FileSystem in a future release.


Using your own classes in Terrier
---------------------------------

If you are adding your own functionality to Terrier, you should have no need to compile Terrier unless you have altered the Terrier source code and wish to check or use your changes. Many configurable properties take class names, so using your own class is as simple as ensuring it is available on the classpath, and giving the class name as a property.

For your new functionality, make a new project with a compile-time dependency on the Terrier core components. For instance, the Maven pom.xml file for your project should contain the following dependency block:

```xml
<dependency>
  <groupId>org.terrier</groupId>
  <artifactId>terrier-core</artifactId>
  <version>5.0</version>
  <scope>provided</scope>
</dependency>
```

Once you have compiled your project into a jar file, you have two options:

1. Add the generated jar to Terrier's classpath manually, by altering the CLASSPATH environment variable:

	CLASSPATH=/path/to/my/project.jar bin/terrier batchretrieval  -Dtrec.model=my.project.MyWeightingModel

2. Install your project to your local Maven repository (e.g. using `mvn install`) or to a remote repository (`mvn deploy`). You can then tell Terrier to import that project at startup:

	#terrier.mvn.coords=<orgId>:<artifactId>:<version>
	terrier.mvn.coords=org.me:my-terrier-ext:5.1


Compiling Terrier
-----------------

The main Terrier distribution comes pre-compiled as Java, and can be run on any Java 1.8 JDK. As mentioned above, you should have no need to compile Terrier unless you need to change its own source.

Terrier uses [Maven](https://maven.apache.org) for dependencies, compiling testing and packaging. Terrier's layout does not yet follow the Maven standard layout, with various source folders located under `src/`. Finally, the Maven environment is configured to build with Eclipse also, although a few plugins are disabled.

The following Maven goals can be used for recompiling Terrier:

-   `mvn compile` - compile

-   `mvn package` - make the jar and jar-with-dependencies

-   `mvn package -DskipTests` - as above, but skipping the JUnit tests

Testing Terrier
---------------

Terrier has many JUnit test classes, located into the `modules/tests/src/test/java` folder. In particular, JUnit tests are provided for a great many of the classes in Terrier, including (but not limited to) indexers, tokenisation, retrieval, query parsing, compression, and evaluation.

In addition, there are JUnit-based end-to-end tests that ensure that the expected results are obtained from a small (22 document) corpus consisting of Shakespeare’s play, the Merchant of Venice. The end-to-end tests test all indexers, as well as retrieval functionality behaves as expected. The corpus, test topics and relevance assessments are located in `share/tests/shakespeare`. Running the unit and Shakespeare end-to-end tests takes about 5 minutes, and can be performed from the command line using the Maven `test` target.

Since Terrier 4.0, Terrier has an end-to-end test based on the TREC WT2G corpus. This is good method test to ensure that a change on Terrier has not negatively impacted on retrieval performance. Note that while indexing the WT2G corpus only takes a few minutes, the end-to-end test suite runs different indexing varieties, so you should allow about about an hour for this test to run. To run this test, you need to specify the location of the WT2G corpus, topics and relevance assessments (qrels):

    bin/anyclass.sh -Dwt2g.corpus=/path/to/WT2G/ -Dwt2g.topics=/path/to/WT2G_topics/small_web/topics.401-450 -Dwt2g.qrels=/path/to/WT2G_topics/small_web/qrels.trec8 org.junit.runner.JUnitCore org.terrier.tests.TRECWT2GEndtoEndTest


Running Terrier from Eclipse
----------------------------

You can run Terrier commands from the command line. For instance, you can run `batchindexing` by executing the `org.terrier.application.CLITool` class with arguments `batchindexing`. You will need system properties to specify terrier.home etc: `-Dterrier.home=/path/to/terrier -Dterrier.etc=/path/to/terrier/etc`. Finally, you need to adjust the classpath appropriately - for instance, if you are accessing a remote index, you will need `terrier-rest-client` on the classpath; in nearly all cases, you will need to include a logger, as terrier-core does not have dependency on logback ([explanation from SLF4J](https://www.slf4j.org/codes.html#noProviders)).

Contributing to Terrier
-----------------------

Terrier is available on [Github](https://github.com/terrier-org/terrier-core/). If you would like to contribute to Terrier, please open an issue on the [Terrier JIRA Issue Tracker](http://terrier.org/issues/), and make a pull request on Github.

------------------------------------------------------------------------


> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
