
Using Terrier for Web-based Search
==================================

Terrier supports dynamic search functionality in a Web browser environment. In particular, Terrier provides a customisable Web-based interface to facilitate retrieval of documents for a query and the summarisation of those documents in the form of snippets or abstracts, for display, in a similar way to major Web search engines. This page explains how to configure Terrier to enable a Web-based search interface like the one shown below:


![Website Search Crawling](images/WT2GWebInterface.png "Website Search Crawling")

Requirements
------------

Firstly, the Web-based interface has slightly higher requirements than Terrier – in particular, as [JSPs](http://en.wikipedia.org/wiki/JavaServer_Pages) are used, the full Java Development Kit (JDK) is required, instead of the JRE. To download the JDK, see [Java downloads](http://java.sun.com/javase/downloads/index.jsp). Relatedly, ensure that the JAVA\_HOME environment variable points to your JDK installation. Secondly, for existing users of Terrier, it is important to note that normal indices cannot be used with the Web-based interface as by default Terrier does not store document snippets/abstracts/meta-data. Hence, the collections need to be indexed from scratch using the correct indexing configuration.

Indexing for a Web-based interface
----------------------------------

As noted above, to use the Web-based interface, document snippets/abstracts/meta-data need to be stored such that they can be returned with each document retrieved. Normally, this will take the form of one or more document abstracts, e.g. the first paragraph or so of each document. The Terrier [Document](javadoc/org/terrier/indexing/Document.html) classes are responsible for generating document abstracts. Since version 3.5, the following Terrier classes implementing the [Document](javadoc/org/terrier/indexing/Document.html) interface support some form of abstract generation:

-   [FileDocument](javadoc/org/terrier/indexing/FileDocument.html): First `N` characters of the document.

-   [POIDocument](javadoc/org/terrier/indexing/POIDocument.html) (for Microsoft Office documents, extends [FileDocument](javadoc/org/terrier/indexing/FileDocument.html)): First `N` characters of the document.

-   [PDFDocument](javadoc/org/terrier/indexing/PDFDocument.html) (extends [FileDocument](javadoc/org/terrier/indexing/FileDocument.html)): First `N` characters of the document.

-   [TaggedDocument](javadoc/org/terrier/indexing/TaggedDocument.html): First `N` characters from the content of each specified tag within the document.

-   [TRECDocument](javadoc/org/terrier/indexing/TRECDocument.html) (extends [TaggedDocument](javadoc/org/terrier/indexing/TaggedDocument.html)): First `N` characters from the content of each specified tag within the document.

To configure Terrier's indexing process to store one or more document abstracts, the appropriate properties specified in either [FileDocument](javadoc/org/terrier/indexing/FileDocument.html) or [TaggedDocument](javadoc/org/terrier/indexing/TaggedDocument.html) must be set. Which document class to use is determined by the Collection to be indexed. For example, [TRECCollection](javadoc/org/terrier/indexing/TRECCollection.html) and [WARC018Collection](javadoc/org/terrier/indexing/WARC018Collection.html) use the TaggedDocument class by default.

During indexing, Terrier stores each abstract generated as document properties in the [MetaIndex](javadoc/org/terrier/structures/MetaIndex.html). Note that this can cause the MetaIndex to become quite large! To configure this, the abstract names should be added to `indexer.meta.forward.keys` and the abstract lengths should be added to `indexer.meta.forward.keylens`.

An example where we save two abstracts using a TREC Collection is shown below:

```
    #For TREC collections
    trec.collection.class=TRECCollection
    #For ClueWeb09 collection
    #trec.collection.class=WARC018Collection
    #For ClueWeb12 collection
    #trec.collection.class=WARC10Collection

    # Do not skip any of the tags, in particular, do not skip the DOCHDR which we want to parse
    # the url and crawldate from!
    TrecDocTags.skip=

    #TRECWebCollection uses TaggedDocument to generate abstracts
    # We will save two abstracts named 'title' and 'body'
    TaggedDocument.abstracts=title,body
    # The tags from which to save the text. ELSE is special tag name, which means anything not consumed by other tags.
    TaggedDocument.abstracts.tags=title,ELSE
    # Should the tags from which we create abstracts be case-sensitive?
    TaggedDocument.abstracts.tags.casesensitive=false
    # The max lengths of the abstracts. Abstracts will be cropped to this length. Defaults to empty.
    TaggedDocument.abstracts.lengths=256,2048

    # If the document class had been a FileDocument then we would use different properties, e.g.
    # FileDocument.abstract=title
    # FileDocument.abstract.length=256

    # We also need to tell the indexer to store the abstracts generated
    # In addition to the docno, we also need to move the 'title' and 'abstract' abstracts generated to the meta index
    indexer.meta.forward.keys=docno,title,abstract
    # The maximum lengths for the meta index entries.
    indexer.meta.forward.keylens=26,256,2048
    # We will not be doing reverse lookups using the abstracts and so they are not listed here.
    indexer.meta.reverse.keys=docno
```

In this example, we store the first 256 characters of the title tag in one abstract called *title* and a further 2048 characters from the rest of the document in a second abstract called *body*. The Document class used in this case is TaggedDocument.

### TRECCollection, TRECWebCollection and Meta-Data

Beyond generating an abstract of each document, it is often useful to store other meta-data about each document, e.g. the URL of the document or the timestamp when the page was created, which we might also wish to display. Importantly, this data may be held in the header of the document or in special tags, which would otherwise be ignored by the document parser. As such, this meta-data cannot be collected by the document class. Instead, for collections of Web documents that have such meta-data, the related collection class is responsible for storing this meta-data. Currently [TRECCollection](javadoc/org/terrier/indexing/TRECCollection.html) and [TRECWebCollection](javadoc/org/terrier/indexing/TRECWebCollection.html) can save additional meta-data about each document as follows:

[TRECCollection](javadoc/org/terrier/indexing/TRECCollection.html) provides a simple way to directly add the content of specified document tags to the MetaIndex. In particular, by setting `(tagset).propertytags` -- a comma separated list of tags -- will add those tags to the MetaIndex if they exist. Note that tags are assumed to be IN ORDER after the docno, and that property tags are not subsequently indexed. For example, given the following TRECDocument:

```
    <DOC>
    <DOCNO>Example-0001</DOCNO>
    <URL>http://terrier.org</URL>
    <CONTENT>The Terrier Project</CONTENT>
    </DOC>
```

Setting the property `TRECDocTags.propertytags=URL` will add the contents of the URL tag to the MetaIndex with the name URL.

[TRECWebCollection](javadoc/org/terrier/indexing/TRECWebCollection.html) was designed to parse out additional meta-data from the header of each document in a TRECCollection. For example, the header of a TREC WT2G document is as follows:

```
    <DOC>
    <DOCNO>WT01-B01-1</DOCNO>
    <DOCOLDNO>IA073-000475-B029-48</DOCOLDNO>
    <DOCHDR>
    http://www.city.geneva.ny.us:80/index.htm 192.108.245.124 19970121041510 text/html 2407
    HTTP/1.0 200 OK
    Date: Tue, 21 Jan 1997 04:14:08 GMT
    Server: Apache/1.1.1
    Content-type: text/html
    Content-length: 2236
    Last-modified: Fri, 18 Oct 1996 17:33:56 GMT
    </DOCHDR>
```

In particular, the TRECWebCollection class parses out the following document meta-data where available:

-   url (all corpora)

-   ip (WT2G, WT10G only)

-   docbytelength (WT2G, WT10G, Blogs06, Blogs08 only)

-   contenttype (WT2G, WT10G only, but usually identified in the HTTP headers)

-   crawldate (WT2G, WT10G only)

Note that when using these collection classes, the Terrier indexing process needs to be told to add this additional meta-data to the MetaIndex. As before, the meta-data names should be added to `indexer.meta.forward.keys` and the meta-data lengths should be added to `indexer.meta.forward.keylens`.

### Indexing Example: WT2G

Below are the indexing properties to set when indexing the TREC WT2G corpus such that the interface example shown earlier can be generated. Note that these properties should be used *in addition to* the standard [indexing](configure_indexing.html) and [retrieval](configure_retrieval.html) properties.

```
    # WT2G is a TRECCollection and we want to get the crawldate and urls from the header
    # hence we use TRECWebCollection. No additional properties are needed as TRECWebCollection
    # extracts the data by default
    trec.collection.class=TRECWebCollection

    # Do not skip any of the tags, in particular, do not skip the DOCHDR which we want to parse
    # the url and crawldate from!
    TrecDocTags.skip=

    #TRECWebCollection uses TaggedDocument to generate abstracts
    # We will save two abstracts called 'title' and 'body'
    TaggedDocument.abstracts=title,body
    # We create abstracts from the title tag and the rest each document (ELSE).
    TaggedDocument.abstracts.tags=title,ELSE
    # Should the tags from which we create abstracts be case-sensitive?
    TaggedDocument.abstracts.tags.casesensitive=false
    # The max lengths of the abstracts. 256 for the title, 2048 for the body.
    TaggedDocument.abstracts.lengths=256,2048

    # We also need to tell the indexer to store the abstracts generated from TaggedDocument and
    # the crawldate/urls saved by TRECWebCollection
    # In addition to the docno which we always have, we also need tell the indexer to store the
    # 'title' and 'body'  abstracts generated, in addition to the url and crawldate extracted in the
    # meta index
    indexer.meta.forward.keys=docno,title,body,url,crawldate
    # The maximum lengths for the meta index entries.
    indexer.meta.forward.keylens=32,256,2048,200,35
    # We will not be doing reverse lookups using the abstracts and so they are not listed here.
    indexer.meta.reverse.keys=docno
```

Using the Web-based interface
-----------------------------

Once you have an index with the necessary abstract entries and/or meta-data, you can start a Web-based interface, and begin searching with it. We provide two basic interfaces for illustration, *simple* and *wt2g*. These are stored in: `src/webapps/`. By default, the *simple* interface can be launched using the following command:

```shell
bin/terrier http
```

This will start a local HTTP server hosting the src/webapps/simple folder at [http://localhost:8080/](http://localhost:8080/).

Below is an example of a the top search result returned using the *simple* interface for the query *Estonia economy* when searching the WT2G collection using BM25. The simple interface lists all stored meta entries for each of the retrieved documents, including the docno, title and url.

![image](images/SimpleWebInterface.png)

This interface is a [JSP](http://en.wikipedia.org/wiki/JavaServer_Pages) file stored at `src/webapps/simple/results.jsp`. When it is called, it opens the Terrier index specified in the terrier.properties file (if not already open), initialises a manager and retrieves the results for the specified query as a standard terrier ResultSet just like the normal Java application. Of importance is that Terrier must be instructed to decorate the ResultSet with all of the meta-data that we stored previously such that results.jsp can display it. This is done as a Terrier [PostFilter](javadoc/org/terrier/querying/PostFilter.html), in particular using either the [SimpleDecorate](javadoc/org/terrier/querying/SimpleDecorate.html) or [Decorate](javadoc/org/terrier/querying/Decorate.html) classes. SimpleDecorate adds all meta index entries for each document retrieved into the ResultSet. Decorate is more advanced, providing query text highlighting and query-biased summarisation. Below we provide an example where we decorate the Result set using the more advanced Decorate class in the terrier.properties file:


	# We are using org.terrier.querying.Decorate which we are going to name decorate 
    #(IMPORTANT: results.jsp expects it to be called 'decorate')
	querying.processes=terrierql:TerrierQLParser,parsecontrols:TerrierQLToControls,parseql:TerrierQLToMatchingQueryTerms,matchopql:MatchingOpQLParser,applypipeline:ApplyTermPipeline,localmatching:LocalManager$ApplyLocalMatching,qe:QueryExpansion,filters:LocalManager$PostFilterProcess
	
	#here we use Decorate rather than SimpleDecorate
	querying.postfilters=decorate:Decorate,site:SiteFilter,scope:Scope
    # expects it to be called 'decorate')

	# decorate:on - activate the decorate process
    # summaries:body - special control for the org.terrier.querying.Decorate class, tells it to create a
    #                  query-biased summary based on the text stored in the abstract named 'body'
    # emphasis:title;body - special control for the org.terrier.querying.Decorate class, tells it to create
    #                       additional meta-data where the query terms are emphasised. These are named
    #                       _emph, where name is the meta entry name. In this case, we create two new
    #                       entries called title_emph and body_emph based on the title and body abstracts. 
    #default and allowed controls
	querying.default.controls=parsecontrols:on,parseql:on,applypipeline:on,terrierql:on,localmatching:on,filters:on,decorate:on,decorate:on,summaries:body,emphasis:title;body
	querying.allowed.controls=scope,start,end,site,scope

### Customising look & feel

The simple interface provides only the basic functionality. You can change the look and feel of the search results by editing src/webapps/simple/results.jsp, or the stylesheet in the same location. Changes made to results.jsp should have an immediate effect on the results. We provide a second example interface designed to display documents from WT2G, named wt2g (`src/webapps/wt2g/`)

If you wish to use another webapps folder, or start the interface on a port other than 8080, you can override both on the command line.

    bin/terrier http 8080 src/webapps/wt2g/

The results for the same example query using the wt2g interface are shown below.

![WT2G Web Interface](images/WT2GWebInterface.png "WT2G Web Interface")

### Extending the Query Language

Terrier provides several [PostFilters](javadoc/org/terrier/querying/PostFilter.html) that are useful for interactive operation. For example, the [SiteFilter](javadoc/org/terrier/querying/SiteFilter.html) is an implementation of the `site:` control from standard Web search engines, and ensures that returned results have a hostname matching the desired expression. The hostname is obtained from the `url` metadata from the MetaIndex. For example, adding `site:com` to the query will ensure that all results have URLs from the .com domain. For TREC or similar collections, the [Scope](javadoc/org/terrier/querying/Scope.html) filter permits a starting prefix on the docno document metadata from the MetaIndex.

### Extending Results.jsp

The two initial interfaces provided with Terrier can be easily extended to add more control when searching or to add new functionality. Below we provide a commented extract from results.jsp that covers the retrieval component of the interface.

```java
    // Get the index if already stored in terrier.jsp.index or load a new one
    Index index = (Index)application.getAttribute("terrier.jsp.index");
    if (index == null)
    {
        index = IndexFactory.of("/path/to/index");
        application.setAttribute("terrier.jsp.index", index);
    }

    // Initialise the manager which controls the querying process   
    Manager queryingManager = (Manager)application.getAttribute("terrier.jsp.manager");
    if (queryingManager == null)
    {
        queryingManager = ManagerFatory.from(index);
        application.setAttribute("terrier.jsp.manager", queryingManager);
    }

    // Make a new search request with the query ('query' is parsed from the HTML form earlier)
    SearchRequest srq = queryingManager.newSearchRequest("results.jsp.query", query);
    srq.setOriginalQuery(query);

    // Are we starting at rank 1? We could be on page 1 or 2 of the results, where sStart would
    // be 11 or 21 respectively (ten per page)
    srq.setControl("start", sStart);

    // This actives the 'decorate' post process that adds all of the meta entries for each document
    // to the ResultSet such that this jsp can display them
    srq.setControl("decorate", "on");

    // Extra controls could be added here, for example query expansion or site filtering
    // The appropriate properties in terrier.properties need to be set and then the post process
    // activated if requested in the html form, e.g. assuming there is a field called doQueryExpansion:
    // String doQueryExpansion = request.getParameter("doQueryExpansion");
    // if (doQueryExpansion == null || doQueryExpansion.length() == 0)
    //  doQueryExpansion=null;
    // doQueryExpansion = doQueryExpansion.trim();
    // if ( doQueryExpansion!=null ) srq.setControl("qe", "on");

    // The last document to return
    srq.setControl("end", String.valueOf(iStart + NUM_RESULTS_PER_PAGE -1));
    // Run any preprocessing (e.g. run the query through the term pipeline)
    queryingManager.runSearchRequest(srq);
    // Get our decorated result set
    ResultSet rs = srq.getResultSet();
```

### Further Details

`bin/terrier http` invokes [SimpleJettyHTTPServer](javadoc/org/terrier/utility/SimpleJettyHTTPServer.html), which starts a [Jetty](http://www.eclipse.org/jetty/) server on the port specified on the command line. The second command line argument is the path to a webapps folder. `share/images` is also mounted as `/images` directory.




------------------------
> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved. 
 
