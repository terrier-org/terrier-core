
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

Making a Basic Index
----------------------------------------



Issuing Searches
----------------------------------------


Saving an Index and Loading it Later
----------------------------------------

------------------------------------------------------------------------

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2017 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved. 

 

