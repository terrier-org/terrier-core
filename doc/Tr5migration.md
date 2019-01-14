Migrating to Terrier 5 from earlier versions
=============================================

Command Line changes
--------------------

Firstly, the bin/anyclass.sh and bin/trec_terrier.sh scripts are deprecated, and will be removed in a future release. You should aim to use bin/terrier from now on. For instance:

* `bin/trec_terrier.sh -i` becomes `bin/terrier batchindexing`
* `bin/trec_terrier.sh -r` becomes `bin/terrier batchretrieve`
* `bin/trec_terrier.sh --printstats` becomes `bin/terrier indexstats`

You can see all possible commands by typing `bin/terrier help`.

If you want to run an arbitrary class, you can still use `bin/terrier com.org.MyClass`. If you want your command to support commandline parsing, and appear in the `bin/terrier help` list, you should extend org.terrier.applications.CLITool.


Source code layouts
-------------------
We have gone full Maven on the layout. The open source Terrier project has been broken down into Maven modules. See the [documentation on Terrier's components](basicComponents.md) for a list of the modules.

Each module is a separate dependency exported to MavenCentral. If you find that your project that depends on terrier-core no longer compiles, then you will need to select additional appropriate dependencies. 

Index References and Remote Indices
-----------------------------------

Terrier 5 introduces IndexRef as a way to refer to an index. An IndexRef may not even be on the same machine - it may refer to an index served from an RESTful HTTP server. You can load a Manager (note Manager is now an interface rather than a concrete class) using `ManagerFactory.from(indexRef)`

How you interact with Terrier defines which Maven dependencies you need to load. If you only need to connect to a remote RESTful index, you need only depend on `org.terrier:terrier-retrieval-api` for compiling and `org.terrier:terrier-rest-client` at runtime. If you want a local index, then you will also need `org.terrier:terrier-core` at runtime.

There are minor changes to the API of the application-facing SearchRequest interface -- for more information, see the relevant Javadoc.


Index Formats
-------------

Terrier's index format has changed for Terrier 5. By default, Lexicons now retain the maximum tf observed in each term's posting list. This will allow future easier integration of dynamic pruning techniques such as WAND using a standard Terrier index.

Terrier 5 is backwardly compatible with Terrier 4 indices, i.e. **Terrier 5 can use indices created by Terrier 4**, without any need to re-index. Support for some earlier Terrier indices has not been retained (e.g. Terrier 3 block indices that use BlockLexiconEntry).

MatchingOp Query Language
-------------------------

Terrier now supports a subset of the Indri query language, called the matchop query language. See the new [documentation about query language](querylanguage.md). You can use this query language by specifying `-m` to the `interactive` or `batchretrieve` Terrier commands. From your own code, you should set the `terrierql:off` and `matchopql:on`.

```
bin/terrier interactive -m
16:30:07.139 [main] INFO  o.t.structures.CompressingMetaIndex - Structure meta reading lookup file into memory
16:30:07.146 [main] INFO  o.t.structures.CompressingMetaIndex - Structure meta loading data file into memory
16:30:07.152 [main] INFO  o.t.applications.InteractiveQuerying - time to intialise index : 0.086
Please enter your query: compressed chicken #combine(0.1 #uw1(compressed chicken))
16:30:26.624 [main] INFO  o.t.matching.PostingListManager - Query 1 with 3 terms has 3 posting lists
```

Using Extensions of Terrier
---------------------------

Terrier now supports loading in additional Maven dependencies. You can specify these using the `terrier.mvn.coords` property. For instance, if you have a number of your own custom Terrier weighting models installed in your local Maven repository, you could use these during retrieval by specifying:

```
terrier.mvn.coords=com.org:myWmodels:5.0
```

This will search your local `.m2` repository, as well as MavenCentral. The `terrier.mvn.coords` property takes a comma-delimited list of Maven dependencies, in the format of `groupId:package:version`. Snapshot versions are supported.
