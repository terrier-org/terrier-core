
Quick start Guide: Integrating Search into your Application
=========================================

One of the common use-cases for Terrier is as a search component within a larger application. For example, you might want a custom search service within an email management system, or use the search results produced for one or more queries as input to another system, such as a classifier. This page will describe a quick way to integrate Terrier into an existing Java application, programatically index documents and issue search requests.  

### 30 Second Overview

In Terrier, the textual items to be indexed, e.g. emails or tweets, are represented using the Document class. FileDocument is a useful Document class implementation that can wrap arbitrary text strings.

```java
String text = "It is easy to index documents in Terrier";
Document document = new FileDocument(new StringReader(text), new HashMap(), Tokeniser.getTokeniser());
```

Documents are typically accessed through Collection objects. Collections are effectively iterators over Documents. An Index is the storage structure that records (indexes) each document such that it is available for search. IndexOnDisk is an index implementation that uses disk-based data structures. An IndexOnDisk can be created using an Indexer from one or more Collections.

```java
Collection coll;
Indexer indexer = new BasicIndexer("/path/to/an/index", "data");
indexer.index(new Collection[]{ coll });
```

Once the indexer has completed, the IndexOnDisk is available to be opened for reading.

```java
Index index = IndexOnDisk.createIndex("/path/to/an/index", "data");
System.out.println("We have indexed " + index.getCollectionStatistics().getNumberOfDocuments() + " documents");
```


To search our index, we need to use a querying Manager, which does the work of scoring each document for your query. Your query is stored in a SearchRequest object that the Manager can generate for you. We also need to specify which scoring function to use when ranking documents via the addMatchingModel() method (in this case we are using [BM25](https://en.wikipedia.org/wiki/Okapi_BM25)).

```java
Manager queryingManager = ManagerFactory.from(index.getIndexRef());
SearchRequest srq = queryingManager.newSearchRequestFromQuery("my terrier query");
srq.setControl(SearchResult.CONTROL_WMODEL, "BM25");
```
Finally, we issue the search:
```java
queryingManager.runSearchRequest(srq);
```

Once the search is finished, the results are stored in the SearchRequest as a ResultSet.

```java
ScoredDocList results = srq.getResults();
```


Prerequisites
----------------------------------------
* Java (developer version) 1.8 or later
* Apache Maven (automated build and dependency manager)

### Java
If you don't have Java 1.8 or later, download the current Java Development Kit (JDK) [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). To check if you have a compatible version of Java installed, use the following command:

```shell
java --version
```

### Apache Maven
Apache Maven is a dependency management and automated build tool for Java-based projects. Install or update Maven to the latest release following their instructions for your system. Maven is widely used among Java developers and is the recommended way of integrating Terrier into your project, as it handles the import process for all of Terrier's dependencies automatically. Terrier requires Maven 3. You can also use other build tools such as Ivy and Gradle (however the use of these other build managers is not covered in the Terrier documentation).

Importing Terrier
----------------------------------------

As mentioned above, to import Terrier into your application we recommend that you use the Maven dependency manager. In effect, Maven is an alternative way to build your project that uses a special [pom.xml](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html) file to control the importing of other pieces of software that your project depends on at compile-time. If you are unfamiliar with Maven, then try working through [this tutorial](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html).

If your project is not yet a Maven project, then we recommend that you convert it to one, following the steps in [the above tutorial](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html). If you are using an integrated development environment (IDE) such as [Eclipse](https://eclipse.org/ide/) or [IntelliJ](https://www.jetbrains.com/idea/), then they support plugins to help you use Maven from within the IDE, such as [M2Eclipse](http://www.eclipse.org/m2e/).

Once you have your project setup with Maven, its time to add Terrier to your project. To do this, you will need to modify the Maven pom.xml file. In particular, you will need to copy/paste the following dependency definition into the `<dependencies>...</dependencies>` block of your pom.xml (if you do not have a dependencies block, you will need to add one).  

```xml
<dependency>
	<groupId>org.terrier</groupId>
	<artifactId>terrier-core</artifactId>
	<version>5.0</version>
</dependency>

<dependency>
	<groupId>org.terrier</groupId>
	<artifactId>terrier-batch-indexers</artifactId>
	<version>5.0</version>
</dependency>
```

Save the pom.xml file and then trigger the building of your project. How you do this will depend on whether you are using the command line or an IDE. During the build process, Terrier along with all of its dependencies will be downloaded and compiled. If you are using an IDE, then Terrier and its dependencies should also be automatically added to your [Java classpath](https://docs.oracle.com/javase/tutorial/essential/environment/paths.html). At this point you should be ready to start coding!

Making an Index
----------------------------------------

### Reading Documents
The first step when creating an index is to convert each file or piece of text we want to search into a form that Terrier can understand. This is done by converting all files and/or pieces of text into Terrier Documents. A Terrier Document is comprised of three main components:
* The raw text of the document, stored as a String
* A Tokeniser that specifies how to break that string down into individual tokens (words)
* Optionally a String->String map that contains metadata about the document, such as a unique identifier

There are a variety of different Terrier Document implementations provided out-of-the-box. The reason for having different Document implementations is to make extracting (useful) text/metadata from different types of common document formats easier. FileDocument is the simplest Document implementation, it simply stores all text read from an input reader. In contrast, TaggedDocument is designed to perform text extraction from html/xml tagged documents and as such, only stores texts within tags named by the user. For instance, given the following text file (test.html):

```html
<html>
   <head></head>
   <body>
      <p>This is a sample HTML document</p>
   </body>
</html>
```

Say we had a directory of HTML documents to index, location at `/path/to/corpus`, this could be achieved using:

```java
Collection coll = new SimpleFileCollection(Arrays.asList("/path/to/corpus"), true);
Indexer indexer = new BasicIndexer("/path/to/an/index", "data");
indexer.index(new Collection[]{ coll });
```

Here we are using a BasicIndexer. There are other indexers, and each has some configurables. Further information about indexers can be found in the [configuration of indexing documentation](configure_indexing.md).


### Giving Each Document A Meaningful Identifier

When we add a document to a Terrier index, it is automatically assigned a numeric (integer) identifier (known as a *docid*). However, in practice, we nearly always want to define a different identifier for each document that is more meaningful. For instance, when indexing a collection of webpages, we might want to refer to each page by its URL. By default, Terrier usually assumes there is a *docno* which serves as each document's unique identifier.

To do this, we need to do to configure Terrier such that it knows to store a named key

The Terrier configuration is stored in a static class called ApplicationSetup. We can add new configuration settings to Terrier using the `ApplicationSetup.setProperty(key, value)` method. Earlier, we noted that each Terrier Document optionally has a String->String map that contains metadata about that document. When a document is indexed, Terrier checks the keys in this map against a pre-defined list of keys to store. If it finds a match, it records the value of that key within the index itself. You can specify which keys to store via the `indexer.meta.forward.keys` and `indexer.meta.forward.keylens` properties as shown below:

```java
ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
ApplicationSetup.setProperty("indexer.meta.forward.keylens", "200");
```
In this case, we are specifying that each document will have a key called 'filename' that we want to store and that the maximum 'filename' length is 200 characters. Both `indexer.meta.forward.keys` and `indexer.meta.forward.keylens` are comma-delimited lists, such that you can specify multiple keys to store.

> **Troubleshooting Tips**:
> *  ApplicationSetup.setProperty() must be called before the index is initialized, i.e. before `new BasicIndexer()` is called
> *  `indexer.meta.forward.keys` and `indexer.meta.forward.keylens` must have the same number of entries


### Indexing the Documents

Now that we know how to convert files into a Collection of Documents, the next step it to create an index and add those documents to it. The index is a storage structure that contains the documents that are to be made available for search. Indices extend the Index class in Terrier. There are three categories of index currently supported in Terrier, namely: IndexOnDisk, IncrementalIndex and MemoryIndex. For this quickstart we will be focusing on the IndexOnDisk.

### Indexing Example

```java
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import org.terrier.indexing.Collection;
import org.terrier.indexing.SimpleFileCollection;
import org.terrier.structures.indexing.classical.BasicIndexer;
import org.terrier.utility.ApplicationSetup;

public class IndexingExample {

	public static void main(String[] args) throws Exception {

		// Directory containing files to index
    		String aDirectoryToIndex = "/my/directory/containing/files/";

		// Configure Terrier
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "200");

		Indexer indexer = new BasicIndexer("/path/to/my/index", "data");
		Collection coll = new SimpleFileCollection(Arrays.asList(aDirectoryToIndex), true);
		indexer.index(new Collection[]{coll});
		indexer.close();
    }
}
```

### More on configuring Terrier

Terrier's properties can be configured using a `terrier.properties` file, usually located in the `etc/` folder. You can place Terrier configuration properties within this file instead of setting ApplicationSetup within Java. For instance, `terrier.properties` may contain:

	indexer.meta.forward.keys=filename
	indexer.meta.forward.keylens=200

When running Terrier, Terrier expects to be informed of the location of the `terrier.home` and `terrier.etc` directories, so that it can find its terrier.properties file:

	java -cp /path/to/terrier/target/terrier-5.0-jar-with-dependencies.jar -Dterrier.home=/path/to/terrier -Dterrier.etc=/path/to/terrier/etc

If you are running an application that depends on Terrier, you may wish to set these properties, either from the command line or System.setProperty().

Issuing Searches
----------------------------------------
### Configuring Search Result Enhancement/Transformations
Before you start setting up searches and running them, it is important to enable any enhancements or transformations that we want Terrier to apply to the search results that will be generated. There are a variety of enhancements/transformations that Terrier can apply out-of-the-box, such as re-ranking the results based on user-defined features or generating query-biased document snippets.

For this quickstart guide, we will cover enabling one very common type of search result enhancement, which is known as *decoration*. The goal of decoration is to copy metadata about each individual document into the search results returned, such as the 'docno' identifier that we configured earlier. Decorate (or more precisely the `org.terrier.querying.SimpleDecorate` class) belongs to a group of functions in Terrier known as post filters. To enable decorate functionality, we need to update the Terrier configuration that is stored in the static ApplicationSetup class. In particular, there is a parameter that needs to be set: `querying.postfilters`. Simply, this parameter specifies the 'what' and 'when' any post filters should be applied. It contains a  a comma delimited list of `name:class` pairs, where `name` is a short identifier for a post filter and `class` is the full classname. The order of the class names defines the order in which they are executed. Hence, we can enable the `org.terrier.querying.SimpleDecorate` post filter class by setting the Terrier configuration as follows:

```java
ApplicationSetup.setProperty("querying.postfilters", "decorate:org.terrier.querying.SimpleDecorate");
```
In this case, we have specified that org.terrier.querying.SimpleDecorate is a post filter we want to have access to, we have given it the name i.e. 'decorate' and we have added it to the list of filters to run.  

> **Troubleshooting Tips**:
> *  ApplicationSetup.setProperty() must be called before the a Manager is obtained, i.e. before `ManagerFactor.from(indexref)` is called.

### Using a Search Manager

Within Terrier, searches are performed using a Manager class. This class performs the nuts and bolts of actually matching your query against the documents that were indexed. If you are running multiple queries, you need to only create a single manager and use it multiple times. There is only one Manager implementation in Terrier, which you can instantiate as follows:

```java
Manager queryingManager = ManagerFactory.from(index.getIndexRef());
```

This creates a new querying manager with a default configuration and sets the index to be searched (to our 'memindex' in this case). The next step in the process is to create a SearchRequest, which contains both our query as well as some other information about how we want the search to be processed. The Manager can generate a SearchRequest for you with default settings as shown below:

```java
SearchRequest srq = queryingManager.newSearchRequestFromQuery("sample query");
```

In this case we have created a new SearchRequest for the query 'sample query'. The SearchRequest will have reasonable defaults for running a basic search. However, there is two configuration options that we need to manually set. First, we need to set which scoring function to use when ranking documents. This is done via the addMatchingModel() method as shown below:


```java
srq.addMatchingModel("org.terrier.matching.daat.Full","BM25");
```

In this case we are using [BM25](https://en.wikipedia.org/wiki/Okapi_BM25), a classical model from the Best Match familty of document weighting models. Second, we need to specify in the SearchRequest that we want to use the post filter we enabled above named 'decorate':

```java
srq.setControl("decorate", "on");
```

Finally, we issue the search:

```java
queryingManager.runSearchRequest(srq);
```

Once finished, the results are stored in the SearchRequest as a ScoredDocList, which can be accessed via:

```java
ScoredDocList results = srq.getResults();
```

### Understanding the ScoredDocList

The output of a search in Terrier is a ScoredDocList. The ScoredDocList contains five main pieces of information:
* The ranking of documents returned for the query in the form of a list of document identifiers 'docids' (highest scoring first).
* The scores for each of those documents.
* Metadata about each document returned (assuming that decoration was enabled).

### Indexing and Retrieval Example
```java
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import org.terrier.indexing.Collection;
import org.terrier.indexing.SimpleFileCollection;
import org.terrier.querying.*;
import org.terrier.structures.indexing.classical.BasicIndexer;

public class IndexingAndRetrievalExample {

	public static void main(String[] args) throws Exception {

		// Directory containing files to index
		String aDirectoryToIndex = "/my/directory/containing/files/";

		// Configure Terrier
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "filename");
		ApplicationSetup.setProperty("indexer.meta.forward.keylens", "200");

		Indexer indexer = new BasicIndexer("/path/to/my/index", "data");
		Collection coll = new SimpleFileCollection(Arrays.asList(aDirectoryToIndex), true);
		indexer.index(new Collection[]{coll});
		indexer.close();

		Index index = Index.createIndex("/path/to/my/index", "data");

        // Enable the decorate enhancement
		ApplicationSetup.setProperty("querying.postfilters", "decorate:org.terrier.querying.SimpleDecorate");

        // Create a new manager run queries
		Manager queryingManager = ManagerFactory.from(memIndex.getIndexRef());

		// Create a search request
		SearchRequest srq = queryingManager.newSearchRequestFromQuery("search for document");

		// Specify the model to use when searching
		srq.setControl(SearchResult.CONTROL_WMODEL, "BM25");

		// Turn on decoration for this search request
		srq.setControl("decorate", "on");

		// Run the search
		queryingManager.runSearchRequest(srq);

		// Get the result set
		ScoredDocList results = srq.getResults();

		// Print the results
		System.out.println("The top "+results.size()+" of documents were returned");
		System.out.println("Document Ranking");
		for(ScoredDoc doc : results) {
			int docid = doc.getDocid();
			double score = doc.getScore();
			String docno = doc.getMetadata("docno")
			System.out.println("   Rank "+i+": "+docid+" "+docno+" "+score);
		}
  }
}
```


------------------------------------------------------------------------

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2018 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
