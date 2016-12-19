
Quickstart Guide: Integrating Search into your Application
=========================================

One of the common use-cases for Terrier is as a search component within a larger application. For example, you might want a custom search service within an email managment system, or use the search results produced for one or more queries as input to another system, such as a classifier. This page will describe a quick way to integrate Terrier into an existing Java application, programatically index documents and issue search requests.  

### 30 Second Overview

In Terrier, the textual items to be indexed, e.g. emails or tweets, are represented using the Document class. FileDocument is a useful Document class implementation that can wrap arbitrary text strings. 

```java
String text = "It is easy to index documents in Terrier";
Document document = new FileDocument(new StringReader(text), new HashMap(), Tokeniser.getTokeniser());
```

An Index is the storage structure that saves (indexes) each document. MemoryIndex is a simple type of index structure to get started with. It simply stores each document in your local machine's memory. 

```java
MemoryIndex memIndex = new MemoryIndex();
```
MemoryIndex belongs to a class of updatable indices, which means that it implements an indexDocument() method, allowing us to add new documents to the index via a single line of code. 

```java
memIndex.indexDocument(document);
```

To search our index, we need to use a querying Manager, which does the work of scoring each document for your query. Your query is stored in a SearchRequest object that the Manager can generate for you. We also need to specify which scoring function to use when ranking documents via the addMatchingModel() method (in this case we are using [BM25](https://en.wikipedia.org/wiki/Okapi_BM25)).

```java
Manager queryingManager = new Manager(memIndex);
SearchRequest srq = queryingManager.newSearchRequest("query1", "my terrier query");
srq.addMatchingModel("Matching","BM25");
```
Finally, we issue the search, which is comprised of four stages.

```java
queryingManager.runPreProcessing(srq);
queryingManager.runMatching(srq);
queryingManager.runPostProcessing(srq);
queryingManager.runPostFilters(srq);
```

Once the search is finished, the results are stored in the SearchRequest as a ResultSet.

```java
ResultSet results = srq.getResultSet();
```


Prerequisites
----------------------------------------
* Java (developer version) 1.8 or later
* Apache Maven (automated build and dependency manager)

### Java
If you don’t have Java 1.8 or later, download the current Java Development Kit (JDK) [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). To check if you have a compatible version of Java installed, use the following command:

```shell
java --version
```

### Apache Maven
Apache Maven is a dependency management and automated build tool for Java-based projects. Install or update Maven to the latest release following their instructions for your system. To check if you have the most recent version of Maven installed, enter the following:

```shell
mvn --version
```

Maven is widely used among Java developers and is the recommended way of integrating Terrier into your project, as it handles the import process for all of Terrier's depedancies automatically. You can also use other build tools such as Ivy and Gradle (however the use of these other build managers is not covered in the Terrier documentation).

Importing Terrier
----------------------------------------

As mentioned above, to import Terrier into your application we recommend that you use the Maven dependancy manager. In effect, Maven is an alternative way to build your project that uses a special [pom.xml](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html) file to control the importing of other pieces of software that your project depends on at compile-time. If you are unfamiliar with Maven, then try working through [this tutorial](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html). 

If your project is not yet a Maven project, then we recommend that you convert it to one, following the steps in [the above tutorial](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html). If you are using an integrated development environemnt (IDE) such as [Eclipse](https://eclipse.org/ide/) or [IntelliJ](https://www.jetbrains.com/idea/), then they support plugins to help you use Maven from within the IDE, such as [M2Eclipse](http://www.eclipse.org/m2e/). 

Once you have your project setup with Maven, its time to add Terrier to your project. To do this, you will need to modify the Maven pom.xml file. In particular, you will need to copy/paste the following dependency definition into the `<dependencies>...</dependencies>` block of your pom.xml (if you do not have a dependencies block, you will need to add one).  

```xml
<dependency>
	<groupId>org.terrier</groupId>
	<artifactId>terrier-core</artifactId>
	<version>4.2</version>
</dependency>
```

Save the pom.xml file and then trigger the building of your project. How you do this will depend on whether you are using the command line or an IDE. During the build process, Terrier along with all of its dependancies will be downloaded and compiled. If you are using an IDE, then Terrier and its dependencies should also be automatically added to your [Java classpath](https://docs.oracle.com/javase/tutorial/essential/environment/paths.html). At this point you should be ready to start coding!

Making an Index
----------------------------------------

### Reading Documents
The first step when creating an index is to convert each file or piece of text we want to search into a form that Terrier can understand. This is done by converting all files and/or pieces of text into Terrier Documents. A Terrier Document is comprised of three main components:
* The raw text of the document, stored as a String
* A Tokenizer that specifies how to break that string down into individual tokens (words)
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

We can convert this to a Terrier Document using the FileDocument class as follows:

```java
Document document = new FileDocument(new FileReader("test.html"), new HashMap(), Tokeniser.getTokeniser());
```
However, if we do so the stored text String will read:

```java
"<html><head></head><body><p>This is a sample HTML document</p></body></html>"
```

On the other hand, if we instead convert this to a Terrier Document using the TaggedDocument class the stored text string will read:
```java
"This is a sample HTML document"
```
since TaggedDocument (by default) only stores text contained within the tags, not the tags themselves. In practice, we recommend that you use a Document implementation that stores only the text that you are interested in searching for within each document, as this will often result in better search effectiveness, consume less memory and make searches faster. 

### Giving Each Document A Meaningful Identifier

When we add a document to a Terrier index, it is automatically assigned a numeric identifier (known as a 'docid'). However, in practice, we nearly always want to define a different identifier for each document that is more meaningful. For instance, when indexing a collection of webpages, we might want to refer to each page by its name or url. 

To do this, we need to do two things:
1. Configure Terrier such that it knows to store a named key
2. Add the key to each document

The Terrier configuration is stored in a static class called ApplicationSetup. We can add new configuration settings to Terrier using the `ApplicationSetup.setProperty(key, value)` method. Earlier, we noted that each Terrier Document optionally has a String->String map that contains metadata about that document. When a document is indexed, Terrier checks the keys in this map against a pre-defined list of keys to store. If it finds a match, it records the value of that key within the index itself. You can specify which keys to store via the `indexer.meta.forward.keys` and `indexer.meta.forward.keylens` properties as shown below:

```java
ApplicationSetup.setProperty("indexer.meta.forward.keys", "docno");
ApplicationSetup.setProperty("indexer.meta.forward.keylens", "30");
```
In this case, we are specifying that each document will have a key called 'docno' that we want to store and that the maximum 'docno' length is 30 characters. Both `indexer.meta.forward.keys` and `indexer.meta.forward.keylens` are comma-delimited lists, such that you can specify multiple keys to store. 

> **Troubleshooting Tips**:
> *  ApplicationSetup.setProperty() must be called before the index is initalized, i.e. before `new MemoryIndex()` is called
> *  `indexer.meta.forward.keys` and `indexer.meta.forward.keylens` must have the same number of entries

Once we have configured Terrier, we also need to add the new identifiers to each document. Assuming we have already created a document called 'document', we can add an identifier as follows:

```java
document.getAllProperties().put("docno", "This-is-test.html");
```

### Indexing the Documents
Now that we know how to convert files or pieces of text into Terrier Documents, the next step it to create an index and add those documents to it. The index is a storage structure that contains the documents that are to be made available for search. Indices extend the Index class in Terrier. There are three categories of index currently supported in Terrier, namely: IndexOnDisk, IncrementalIndex and MemoryIndex. For this quickstart we will be focusing on the MemoryIndex. 

MemoryIndex is a convenient index class to use when you have a reletively small number of documents that you need to search over, e.g. 10,000 to about 100,000 web pages. The memory index stores documents as a series of arrays in local memory (RAM).  

A new memory index can be made using the default MemoryIndex constructor:

```java 
MemoryIndex memIndex = new MemoryIndex();
```
Once we have an index, new documents can be easily added using the `indexDocument()` method:

```java
memIndex.indexDocument(document);
```

### Full Indexing Example

```java
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import org.terrier.indexing.Document;
import org.terrier.indexing.TaggedDocument;
import org.terrier.indexing.tokenisation.Tokeniser;
import org.terrier.realtime.memory.MemoryIndex;
import org.terrier.utility.ApplicationSetup;

public class IndexingExample {

	public static void main(String[] args) throws Exception {
    
        // Directory containing files to index
    	String aDirectoryToIndex = "/my/directory/containing/files/";
		
        // Configure Terrier
		ApplicationSetup.setProperty("indexer.meta.forward.keys", "docno");
        ApplicationSetup.setProperty("indexer.meta.forward.keylens", "30");
        
        // Create a new Index
		MemoryIndex memIndex = new MemoryIndex();
		
        // For each file
		for (String filename : new File(aDirectoryToIndex).list() ) {
			
			String fullPath = aDirectoryToIndex+filename;
			
            // Convert it to a Terrier Document
			Document document = new TaggedDocument(new FileReader(fullPath), new HashMap(), Tokeniser.getTokeniser());
            
            // Add a meaningful identifier
			document.getAllProperties().put("docno", filename);

			// index it
			memIndex.indexDocument(document);
		}
    }
}
```


Issuing Searches
----------------------------------------


Saving an Index and Loading it Later
----------------------------------------

------------------------------------------------------------------------

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2017 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved. 

 

