#Migrating to Terrier 5 from earlier versions

##Command Line changes

Firstly, the bin/anyclass.sh and bin/trec_terrier.sh scripts are deprecated, and will be removed in a future release. You should aim to use bin/terrier from now on. For instance:

* `bin/trec_terrier.sh -i` becomes `bin/terrier batchindexing`
* `bin/trec_terrier.sh -r` becomes `bin/terrier batchretrieve`

You can see all possible commands by typing `bin/terrier help`.

If you want to run an arbitrary class, you can still use `bin/terrier com.org.MyClass`. If you want your command to support commandline parsing, and appear in the `bin/terrier help` list, you should extend org.terrier.applications.CLITool.

##Source code layouts

We have gone full-Maven on the layout. The open source Terrier project has been broken down into Maven modules. See basicComponents.md for a list of the components.

##Index References and Remote Indices

Terrier 5 introduces IndexRef as a way to refer to an index. An IndexRef may not even be on the same machine - it may refer to an index served from an RESTful HTTP server. You can load a Manager (note Manager is now an interface rather than a concrete class) using `ManagerFactory.from(indexRef)`

##MatchingOp Query Language

Terrier now supports a subset of the Indri query language.

