Configuring Terrier
===================

Configuring Overview
--------------------

Terrier is configured overall by a few files, all in the `etc/` directory. The most central files are `terrier.properties` and `terrier-log.xml`. In `terrier.properties`, you can specify any of the various properties that are defined in Terrier. The [Properties](properties.md) documentation page lists the most used properties that you need to configure Terrier, while the [javadoc](javadoc/) for any class lists the properties that directly affect the class. The default `terrier.properties` file is given below:

    #default controls for query expansion
	querying.processes=terrierql:TerrierQLParser,parsecontrols:TerrierQLToControls,parseql:TerrierQLToMatchingQueryTerms,matchopql:MatchingOpQLParser,applypipeline:ApplyTermPipeline,localmatching:LocalManager$ApplyLocalMatching,qe:QueryExpansion,filters:LocalManager$PostFilterProcess

    #default controls for the web-based interface. SimpleDecorate
    #is the simplest metadata decorator. For more control, see Decorate.
    querying.postfilters=decorate:SimpleDecorate,site:SiteFilter,scope:Scope

    #default and allowed controls
    querying.default.controls=parsecontrols:on,parseql:on,applypipeline:on,terrierql:on,localmatching:on,filters:on,decorate:on
	querying.allowed.controls=scope,qe,qemodel,start,end,site,scope

    #document tags specification
    #for processing the contents of
    #the documents, ignoring DOCHDR
    TrecDocTags.doctag=DOC
    TrecDocTags.idtag=DOCNO
    TrecDocTags.skip=DOCHDR

    #query tags specification
    TrecQueryTags.doctag=TOP
    TrecQueryTags.idtag=NUM
    TrecQueryTags.process=TOP,NUM,TITLE
    TrecQueryTags.skip=DESC,NARR

    #stop-words file
    stopwords.filename=stopword-list.txt

    #the processing stages a term goes through
    termpipelines=Stopwords,PorterStemmer

In the terrier.properties file, properties are specified in the format `name=value` (the default Java Properties format). Comments are lines starting with `#`.

### Scripting Properties

TrecTerrier supports specifying properties on the command line. This allows the easy over-riding of properties, even if they are specified in the `etc/terrier.properties` file. For example, to create an index without using a stemmer, you could use the command line:

    $ bin/terrier batchindexing -Dtermpipelines=Stopwords

Aside: When looking for properties, Terrier also checks the [System properties provided by Java](http://download.oracle.com/javase/tutorial/essential/environment/sysprop.html). This means that you can set a property anywhere within Java code, or on the Java command line.

As another example, you can use shell scripting (e.g. Bash) to run Terrier with many settings for the `expansion.terms` property of query expansion:

    $ for((i=2;i<10;i++)); do
        bin/terrier batchretrieval -q -Dexpansion.terms=$i
    done

### Configuring Logging

Terrier uses [Logback](http://logback.qos.ch/) for logging. You can control the amount of logging information (the logging level) that Terrier outputs by altering the Logback config in `etc/logback.xml` (e.g. INFO or DEBUG). For more information about configuring Logback, see the [Logback documentation](http://logback.qos.ch/documentation.html).

------------------------------------------------------------------------


> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
