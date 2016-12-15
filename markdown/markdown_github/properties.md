<span>\[</span>[Previous: Hadoop MapReduce Indexing with Terrier](hadoop_indexing.html)<span>\]</span> <span>\[</span>[Contents](index.html)<span>\]</span> <span>\[</span>[Next: Developing with Terrier](terrier_develop.html)<span>\]</span>

Description of Configurable properties of Terrier
=================================================

Terrier allows the user to configure many different aspects of the framework, in order to be adaptable to the specific needs of different applications. Here, we describe the properties that are used while indexing or retrieving. A sample of how to set up the basic properties can be found in [etc/terrier.properties.sample](../etc/terrier.properties.sample). This page contains many of the properties in Terrier, broken down by category: , , , and .

General properties
------------------

<span>@ll@</span> Property & **terrier.setup**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html)Possible values & Absolute directory pathDefault value & not specifiedConfigures & Specifies where Terrier finds the terrier.properties file, which is usually in the etc/ directory. Analogous to terrier.etc property

<span>@ll@</span> Property & **terrier.home**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html)Possible values & Absolute directory pathDefault value & not specifiedConfigures & ApplicationSetup.TERRIER\_HOME. Where Terrier is installed.

<span>@ll@</span> Property & **terrier.etc**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html)Possible values & Absolute directory pathDefault value & TERRIER\_HOME + “etc/”Configures & TERRIER\_ETC. Where terrier finds it’s terrier.properties file if -Dterrier.setup is not specified

<span>@ll@</span> Property & **terrier.share**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html), [org.terrier.terms.Stopwords](javadoc/org/terrier/terms/Stopwords.html)Possible values & Absolute directory pathDefault value & TERRIER\_HOME + “share/”Configures & ApplicationSetup.TERRIER\_SHARE. Where static distribution files are found, for instance the stopword files.

Property

**terrier.var**

Used in

[org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html), [org.terrier.applications.desktop.filehandling.WindowsFileOpener](javadoc/org/terrier/applications/desktop/filehandling/WindowsFileOpener.html), [org.terrier.structures.Index](javadoc/org/terrier/structures/Index.html)

Possible values

Absolute directory path

Default value

TERRIER\_HOME + “var/”

Configures

TERRIER\_VAR. Where Terrier puts files that it creates, e.g. indices and results files.

<span>@ll@</span> Property & **terrier.plugins**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html)Possible values & A comma-separated list of plugins.Default value & not specifiedConfigures & The list of plugins to be preloaded.

<span>@ll@</span> Property & **log4j.config**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html)Possible values & A valid log4j configuration fileDefault value & terrier-log.xmlConfigures & ApplicationSetup.LOG4J\_CONFIG. The configuration file used by log4j.

<span>@ll@</span> Property & **terrier.index.path**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html), [org.terrier.indexing.SimpleFileCollection](javadoc/org/terrier/indexing/SimpleFileCollection.html), [org.terrier.indexing.TRECCollection](javadoc/org/terrier/indexing/TRECCollection.html)Possible values & fully path of a directoryDefault value & TERRIER\_VAR + “index/”Configures & TERRIER\_INDEX\_PATH. The name of the directory in which the data structures created by Terrier are stored

<span>@ll@</span> Property & **terrier.index.prefix**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html), [org.terrier.structures.indexing.Indexer](javadoc/org/terrier/structures/indexing/Indexer.html)Possible values & Filename prefix for all the indicesDefault value & “data”Configures & TERRIER\_INDEX\_PREFIX. Filename prefix for all the indices.

<span>@ll@</span> Property & **stopwords.filename**Used in & [org.terrier.terms.Stopwords](javadoc/org/terrier/terms/Stopwords.html)Possible values & absolute path to fileDefault value & TERRIER\_SHARE + “stopword-list.txt”Configures & The name of the file which contains a list of stopwords.

<span>@ll@</span> Property & **collection.spec**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html), [org.terrier.indexing.SimpleFileCollection](javadoc/org/terrier/indexing/SimpleFileCollection.html), [org.terrier.indexing.TRECCollection](javadoc/org/terrier/indexing/TRECCollection.html)Possible values & Absolute filenameDefault value & TERRIER\_ETC + value of “collection.spec”Configures & COLLECTION\_SPEC. Where the indexing process should find it’s configuration for the Collection object. This is often a list of files or directories.

<span>@ll@</span> Property & **ignore.empty.documents**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html), [org.terrier.structures.indexing.Indexer](javadoc/org/terrier/structures/indexing/Indexer.html)Possible values & true, falseDefault value & falseConfigures & IGNORE\_EMPTY\_DOCUMENTS. Whether empty documents have an entry in the document index.

<span>@ll@</span> Property & **???.process**Used in & [org.terrier.utility.TagSet](javadoc/org/terrier/utility/TagSet.html)Possible values & Comma delimited list of tags to processDefault value & not specifiedConfigures & For many of the tokenisers, configures which tags should be processed. ??? can be TrecDocTags or TrecQueryTags, to configure the TREC Collection and Query parsers respectively. ??? as FieldTags specifies the field that should be stored in the index.

<span>@ll@</span> Property & **???.skip**Used in & [org.terrier.utility.TagSet](javadoc/org/terrier/utility/TagSet.html)Possible values & Comma delimited list of tags to not processDefault value & not specifiedConfigures & For many of the tokenisers, configures which tags should be skipped completely. ??? can be TrecDocTags or TrecQueryTags, to configure the TREC Collection and Query parsers respectively.

<span>@ll@</span> Property & **???.doctag**Used in & [org.terrier.utility.TagSet](javadoc/org/terrier/utility/TagSet.html)Possible values & Name of tag that marks the start of the document (trec only)Default value & not specifiedConfigures & For some of the tokenisers, configures which tag which contains the opening tag (or query ID). ??? can be TrecDocTags or TrecQueryTags, to configure the TREC Collection and Query parsers respectively.

<span>@ll@</span> Property & **???.idtag**Used in & [org.terrier.utility.TagSet](javadoc/org/terrier/utility/TagSet.html)Possible values & Name of tag that contains the unique identifier (trec only)Default value & not specifiedConfigures & For some of the tokenisers, configures which tag which contains the document ID (or query ID). ??? can be TrecDocTags or TrecQueryTags, to configure the TREC Collection and Query parsers respectively.

<span>@ll@</span> Property & **???.casesensitive**Used in & [org.terrier.utility.TagSet](javadoc/org/terrier/utility/TagSet.html) [org.terrier.indexing.TRECCollection](javadoc/org/terrier/indexing/TRECCollection.html)Possible values & true or falseDefault value & true for TrecDocTags, false otherwiseConfigures & For some of the tokenisers, configures if the tag matching is case-sensitive or not. The default is true for TRECCollection (TrecDocTags), and false for FieldTags and TrecQueryTags (TRECFullTokenizer which is used by the TREC query parser (TRECQuery)).

<span>@ll@</span> Property & **???.propertytags**Used in & [org.terrier.utility.TagSet](javadoc/org/terrier/utility/TagSet.html) [org.terrier.indexing.TRECCollection](javadoc/org/terrier/indexing/TRECCollection.html)Possible values & Comma delimited list of tags to add as document propertiesDefault value & not specifiedConfigures & During indexing this enables document tags to be saved as document properties instead of being indexed. This is useful to store document properties in the meta index for use later, e.g. for display by the Terrier Web-based interface.

<span>@ll@</span> Property & **block.indexing**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html), [org.terrier.applications.TRECIndexing](javadoc/org/terrier/applications/TRECIndexing.html)Possible values & true, falseDefault value & falseConfigures & ApplicationSetup.BLOCK\_INDEXING. Sets whether block positions should be saved during indexing. This is required to do phrasal searches. Client code should examine this to determine whether to use the BasicIndexer or the BlockIndexer.

<span>@ll@</span> Property & **blocks.size**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html), [org.terrier.structures.indexing.classical.BasicIndexer](javadoc/org/terrier/structures/indexing/classical/BasicIndexer.html)Possible values & integer &gt; 0Default value & 1Configures & ApplicationSetup.BLOCK\_SIZE. The number of terms contained in the same block

<span>@ll@</span> Property & **blocks.max**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html), [org.terrier.structures.indexing.classical.BasicIndexer](javadoc/org/terrier/structures/indexing/classical/BasicIndexer.html)Possible values & integer &gt;= 0Default value & 100000Configures & MAX\_BLOCKS. The maximum number of blocks a document may contain.

<span>@ll@</span> Property & **lowercase**Used in & [org.terrier.indexing.TaggedDocument](javadoc/org/terrier/indexing/TaggedDocument.html), [org.terrier.indexing.TRECFullTokenizer](javadoc/org/terrier/indexing/TRECFullTokenizer.html)Possible values & true, or falseDefault value & trueConfigures & Whether text is converted to lowercase before parsing

<span>@ll@</span> Property & **tokeniser**Used in & [org.terrier.indexing.tokenisation.Tokeniser](javadoc/org/terrier/indexing/tokenisation/Tokeniser.html)Possible values & a classname implementing the Tokeniser interfaceDefault value & EnglishTokeniserConfigures & The Tokeniser implementation to be used when splitting text into tokens. This allows for corpora in different languages to be indexed by setting a Tokeniser implementation appropriate for each language.

<span>@ll@</span> Property & **indexing.max.tokens**Used in & [org.terrier.structures.indexing.Indexer](javadoc/org/terrier/structures/indexing/Indexer.html)Possible values & integer &gt;=0Default value & 0Configures & Sets a limit to the maximum number of tokens indexed for a document. The default value 0 means that there is no limit.

<span>@ll@</span> Property & **indexing.max.docs.per.builder**Used in & [org.terrier.structures.indexing.Indexer](javadoc/org/terrier/structures/indexing/Indexer.html)Possible values & integer &gt;=0Default value & 18,000,000Configures & Sets a limit to the maximum number of documents in one index during indexing. After this point, a new index will be created, and at the end, all the indices will be merged. Reasoning: During classical two-pass indexing, memory is constrained by the TermCodes table. If too many different unique terms are indexed, then an OutOfMemoryError will occur. For TREC GOV2 collection, 18 million documents is a good point to start a new index. The special value 0 means that there is no limit. This property also applies for single-pass indexing, although it can be safely set higher. It does not apply for MapReduce indexing.

### Advanced

<span>@ll@</span> Property & **termpipelines**Used in & [org.terrier.querying.Manager](javadoc/org/terrier/querying/Manager.html), [org.terrier.structures.indexing.Indexer](javadoc/org/terrier/structures/indexing/Indexer.html)Possible values & Comma delimited list of term pipeline entities to pass query terms through. Use blank to denote no termpipeline objectsDefault value & Stopwords,PorterStemmerConfigures & Defines which term pipeline entities to pass query terms through.

<span>@ll@</span>

\[t\]<span>0.47</span>Property

&

\[t\]<span>0.47</span>**invertedfile.processpointers**

\[t\]<span>0.47</span>Used in

&

\[t\]<span>0.47</span>[org.terrier.structures.indexing.classical.InvertedIndexBuilder](javadoc/org/terrier/structures/indexing/classical/InvertedIndexBuilder.html), [org.terrier.structures.indexing.classical.BlockInvertedIndexBuilder](javadoc/org/terrier/structures/indexing/classical/BlockInvertedIndexBuilder.html)

\[t\]<span>0.47</span>Possible values

&

\[t\]<span>0.47</span>Integer value &gt; 0

\[t\]<span>0.47</span>Default value

&

\[t\]<span>0.47</span>20000000

\[t\]<span>0.47</span>Configures

&

\[t\]<span>0.47</span>Defines the number of pointers that should be processed at once when building the inverted index. The InvertedIndexBuilder first works out how many terms correspond to that many pointers, then scans the direct index looking for each of these term, then writes them to inverted index, then repeats scan for next bunch of terms. Increasing this speeds up inverted index building for large collections, but uses more memory. Decrease this if you encounter OutOfMemory errors while building the inverted index. Note that for block indexing, the default is lower: 2,000,000 pointers.

This option supersedes invertedfile.processterms. For the invertedfile.processterms strategy to be used, set invertedfile.processpointers to 0.

<span>@ll@</span> Property & **lexicon.builder.merge.lex.max**Used in & [org.terrier.structures.indexing.LexiconBuilder](javadoc/org/terrier/structures/indexing/LexiconBuilder.html),Possible values & integer values &gt; 1Default value & 16Configures & The number of temporary lexicons to merge at once during indexing. during lexicon building. Bigger is generally faster, but too many open file-handles causes slowness. 16 is a good trade-off. (See also the MERGE\_FACTOR in GNU sort source code).

<span>@ll@</span> Property & **indexing.excel.maxfilesize.mb**Used in & [org.terrier.indexing.MSExcelDocument](javadoc/org/terrier/indexing/MSExcelDocument.html)Possible values & size of a file in megabytesDefault value & 0.5Configures & The maximum file size of an Excel spreadsheet to be parsed.

<span>@ll@</span> Property & **indexing.simplefilecollection.extensionsparsers**Used in & [org.terrier.indexing.SimpleFileCollection](javadoc/org/terrier/indexing/SimpleFileCollection.html)Possible values & comma delimited list of file extensions and associated parsers to use for the corresponding files.Default value & txt:FileDocument,text:FileDocument,tex:FileDocument,bib:FileDocument, pdf:PDFDocument,html:HTMLDocument,htm:HTMLDocument,xhtml:HTMLDocument, xml:HTMLDocument,doc:MSWordDocument,ppt:MSPowerpointDocument,xls:MSExcelDocumentConfigures & The parsers to be used for processing files with the specified extensions.

<span>@ll@</span> Property & **indexing.simplefilecollection.defaultparser**Used in & [org.terrier.indexing.SimpleFileCollection](javadoc/org/terrier/indexing/SimpleFileCollection.html)Possible values & fully qualified class nameDefault value & not specifiedConfigures & The parser to use by default for processing files with unknown extensions

<span>@ll@</span> Property & **trec.blacklist.docids**Used in & [org.terrier.indexing.TRECCollection](javadoc/org/terrier/indexing/TRECCollection.html)Possible values & full path to filenameDefault value & not specifiedConfigures & The name of a file that contains a black list of document identifiers to be ignored during indexing

<span>@ll@</span> Property & **trec.collection.class**Used in & [org.terrier.applications.TRECIndexing](javadoc/org/terrier/applications/TRECIndexing.html)Possible values & a classname implementing Collection interfaceDefault value & TRECCollectionConfigures & The Collection object to be used to parse the collection. This allows test collection similar but not identical to TREC to be parsed using Terrier’s TREC tools. New in Terrier 1.1.0 is the ability to chain Collections. The Collection specified last is the inner-most one of the chain, the first is the outer-most (i.e. instantiation right-to-left). the first collection should have a default constructor (no arguments), while the other collections should take as argument in their constructor the inner-collection class. E.g. `trec.collection.class=RemoveSmallDocsCollection,TRECCollection`. Instantiation handled by the CollectionFactory class.

<span>@ll@</span> Property & **indexer.meta.forward.keys**Used in & [CompressingMetaIndexBuilder](javadoc/org/terrier/structures/indexing/CompressingMetaIndexBuilder.html)Possible values & comma delimited list of properties of a [Document](javadoc/org/terrier/indexing/Document.html) object that should be used as metadata.Default value & docnoConfigures & The document properties that should be recorded as document metadata.

<span>@ll@</span> Property & **indexer.meta.forward.keylens**Used in & [CompressingMetaIndexBuilder](javadoc/org/terrier/structures/indexing/CompressingMetaIndexBuilder.html)Possible values & comma delimited list of the lengths of the values corresponding to the keys to be used as document metadata.Default value & 20Configures & How long values can be in the MetaIndex.

<span>@ll@</span> Property & **indexer.meta.reverse.keys**Used in & [CompressingMetaIndexBuilder](javadoc/org/terrier/structures/indexing/CompressingMetaIndexBuilder.html)Possible values & comma delimited list of the keys that can be used to uniquely identify documents.Default value & 20Configures & The MetaIndex keys that can unique identify a document. E.g. docno,url.

<span>@ll@</span> Property & **max.term.length**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html), [org.terrier.indexing.FileDocument](javadoc/org/terrier/indexing/FileDocument.html), [org.terrier.indexing.TRECFullTokenizer](javadoc/org/terrier/indexing/TRECFullTokenizer.html), [org.terrier.structures.Lexicon](javadoc/org/terrier/structures/Lexicon.html),Possible values & Integer value &gt; 0Default value & 20Configures & MAX\_TERM\_LENGTH. The size in the lexicon reserved for a string, i.e. the max length of any term in the index. term.

<span>@ll@</span> Property & **memory.reserved**Used in & [org.terrier.structures.indexing.singlepass.BasicSinglePassIndexer](javadoc/org/terrier/structures/indexing/singlepass/BasicSinglePassIndexer.html)Possible values & integer &gt; 0, probably around 50 millionDefault value & 50000000Configures & Free memory threshold that forces a run to be committed to disk in the single-pass indexer. Higher values means less chance of OutOfMemoryError occurring, but slower indexing speed as more runs will be generated.

<span>@ll@</span> Property & **memory.heap.usage**Used in & [org.terrier.structures.indexing.singlepass.BasicSinglePassIndexer](javadoc/org/terrier/structures/indexing/singlepass/BasicSinglePassIndexer.html)Possible values & positive float, range 0.0f - 1.0fDefault value & 0.70Configures & amount of max heap allocated to JVM before a run is committed. Smaller values mean more runs and hence slower indexing. Larger values means more risk of OutOfMemoryError occurrences.

<span>@ll@</span> Property & **docs.check**Used in & [org.terrier.structures.indexing.singlepass.BasicSinglePassIndexer](javadoc/org/terrier/structures/indexing/singlepass/BasicSinglePassIndexer.html)Possible values & positive integer &gt; 0Default value & 20Configures & how often to check the amount of free memory. Lower values gives more protection from OutOfMemoryError.

<span>@ll@</span> Property & **inverted2direct.processtokens**Used in & [org.terrier.structures.indexing.singlepass.Inverted2DirectIndexBuilder](javadoc/org/terrier/structures/indexing/singlepass/Inverted2DirectIndexBuilder.html)Possible values & positive long &gt; 0Default value & 100000000, 10000000 for blocksConfigures & total number of tokens to attempt each iteration of building the direct index. Use a lower value if OutOfMemoryError occurs.

<span>@ll@</span> Property & **terrier.index.retrievalLoadingProfile.default**Used in & [org.terrier.structures.Index](javadoc/org/terrier/structures/Index.html)Possible values & true, falseDefault value & trueConfigures & Index.RETRIEVAL\_LOADING\_PROFILE. Whether index structures should be preloaded for retrieval.

<span>@ll@</span> Property & **TaggedDocument.abstracts**Used in & [org.terrier.indexing.TaggedDocument](javadoc/org/terrier/indexing/TaggedDocument.html)Possible values & Comma delimited list of abstract names to save as document propertiesDefault value & not specifiedConfigures & The list of abstract names to save as document properties when indexing a TaggedDocument or one of its subclasses.

<span>@ll@</span> Property & **TaggedDocument.abstracts.tags**Used in & [org.terrier.indexing.TaggedDocument](javadoc/org/terrier/indexing/TaggedDocument.html)Possible values & Comma delimited list of tags from which to save abstractsDefault value & not specifiedConfigures & The names of tags to save text from. ELSE is special tag name, which means anything not consumed by other tags.

<span>@ll@</span> Property & **TaggedDocument.abstracts.tags.casesensitive**Used in & [org.terrier.indexing.TaggedDocument](javadoc/org/terrier/indexing/TaggedDocument.html)Possible values & true or falseDefault value & falseConfigures & Configures if the tag matching is case-sensitive or not.

<span>@ll@</span> Property & **TaggedDocument.abstracts.lengths**Used in & [org.terrier.indexing.TaggedDocument](javadoc/org/terrier/indexing/TaggedDocument.html)Possible values & Comma delimited list of maximum lengths for each abstractDefault value & Length 0Configures & The max lengths of the abstracts. Defaults to empty.

<span>@ll@</span> Property & **FileDocument.abstract**Used in & [org.terrier.indexing.FileDocument](javadoc/org/terrier/indexing/FileDocument.html)Possible values & Name to call the abstractDefault value & not specifiedConfigures & The name of the abstract to save from the document. Note that only if this is set will an abstract be generated. Only a single abstract can be generated from a FileDocument.

<span>@ll@</span> Property & **FileDocument.abstract.length**Used in & [org.terrier.indexing.FileDocument](javadoc/org/terrier/indexing/FileDocument.html)Possible values & The maximum length for the abstractDefault value & 0Configures & The maximum length for the abstract.

### Model

<span>@ll@</span> Property & **ignore.low.idf.terms**Used in & [org.terrier.matching.Matching](javadoc/org/terrier/matching/Matching.html)Possible values & true, falseDefault value & trueConfigures & Ignores a term that has a low IDF, ie appears in many documents. You may wish to turn this off for small or focused collections.

### Interactive Retrieval

<span>@ll@</span> Property & **interactive.output.format.length**Used in & [org.terrier.applications.InteractiveQuerying](javadoc/org/terrier/applications/InteractiveQuerying.html)Possible values & integer number &gt; 0Default value & 1000Configures & the maximum number of results to be displayed for Interactive querying

### TREC-style Batch Retrieval

<span>@ll@</span> Property & **trec.model**Used in & [org.terrier.applications.batchquerying.TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html)Possible values & Name of weighting modelsDefault value & InL2Configures & The weighting model to use during retrieval.

<span>@ll@</span> Property & **trec.results**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html)Possible values & Absolute directory pathDefault value & TERRIER\_VAR + value of “trec.results”"Configures & TREC\_RESULTS. Where TREC\*Querying applications should store their results files and where evaluation files should be placed.

<span>@ll@</span> Property & **trec.results.file**Used in & [org.terrier.applications.batchquerying.TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html)Possible values & A valid file name.Default value & not specifiedConfigures & An arbitrary name for a TREC results file.

<span>@ll@</span> Property & **trec.querycounter.type**Used in & [org.terrier.applications.batchquerying.TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html)Possible values & sequential, randomDefault value & sequentialConfigures & Whether to use sequential (auto-incremented) or randomly generated suffixes for run names.

<span>@ll@</span> Property & **trec.results.suffix**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html)Possible values & stringDefault value & .resConfigures & ApplicationSetup.TREC\_RESULTS\_SUFFIX. The suffix to be used for result files.

<span>@ll@</span> Property & **trec.runtag**Used in & [org.terrier.applications.batchquerying.TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html)Possible values & stringDefault value & not specifiedConfigures & An arbitrary runtag (6th field) for a TREC results file.

<span>@ll@</span> Property & **trec.topics**Used in & [org.terrier.applications.batchquerying.TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html)Possible values & A valid topics file nameDefault value & not specifiedConfigures & A single file containing the topics to be processed.

<span>@ll@</span> Property & **trec.topics.parser**Used in & [org.terrier.applications.batchquerying.TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html)Possible values & A sub-class of org.terrier.structures.QuerySourceDefault value & TRECQueryConfigures & The class to be used when parsing a topics file.

<span>@ll@</span> Property & **trec.encoding**Used in & [org.terrier.structures.TRECQuery](javadoc/org/terrier/structures/TRECQuery.html), [org.terrier.indexing.TRECCollection](javadoc/org/terrier/indexing/TRECCollection.html), [org.terrier.indexing.TRECUTFCollection](javadoc/org/terrier/indexing/TRECUTFCollection.html), [org.terrier.terms.Stopwords](javadoc/org/terrier/terms/Stopwords.html)Possible values & A valid encoding scheme.Default value & The system’s default charset.Configures & The encoding to use for topics, documents, and stopwords files.

<span>@ll@</span> Property & **trec.qrels**Used in & [org.terrier.utility.ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html)Possible values & Absolute filenameDefault value & not specifiedConfigures & A single file containing the qrels to evaluate with.

<span>@ll@</span> Property & **trec.output.format.length**Used in & [org.terrier.applications.batchquerying.TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html),Possible values & integer number &gt; 0Default value & 1000Configures & the maximum number of results to be displayed for TREC querying

<span>@ll@</span> Property & **trec.querying.outputformat**Used in & [org.terrier.applications.batchquerying.TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html)Possible values & A sub-class of TRECQuerying$OutputFormatDefault value & TRECQuerying$TRECDocnoOutputFormatConfigures & The class used to write the results file.

<span>@ll@</span> Property & **trec.querying.resultscache**Used in & [org.terrier.applications.batchquerying.TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html)Possible values & A sub-class of TRECQuerying$QueryResultCacheDefault value & TRECQuerying$NullQueryResultCacheConfigures & The class used to cache the results.

<span>@ll@</span> Property & **trec.querying.dump.settings**Used in & [org.terrier.applications.batchquerying.TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html)Possible values & true, falseDefault value & trueConfigures & Whether the settings used to generate a results file should be dumped to a .settings file in conjunction with the .res file.

<span>@ll@</span> Property & **trec.iteration**Used in & [org.terrier.applications.batchquerying.TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html)Possible values & StringDefault value & QConfigures & Related to standard format of TREC results

<span>@ll@</span> Property & **trec.manager**Used in & [org.terrier.applications.batchquerying.TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html),Possible values & String, Class name in org.terrier.queryingDefault value & ManagerConfigures & The Manager class to use during querying

<span>@ll@</span> Property & **trec.matching**Used in & [org.terrier.applications.batchquerying.TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html),Possible values & String, Class name in org.terrier.matchingDefault value & org.terrier.matching.taat.FullConfigures & The Matching class to use during querying

<span>@ll@</span> Property & **matching.trecresults.file**Used in & [org.terrier.matching.TRECResultsMatching](javadoc/org/terrier/matching/TRECResultsMatching.html)Possible values & A valid TREC results fileDefault value & not specifiedConfigures & The TREC-formatted results file containing search results for each of the topics specified in the `trec.topics` property

<span>@ll@</span> Property & **matching.trecresults.format**Used in & [org.terrier.matching.TRECResultsMatching](javadoc/org/terrier/matching/TRECResultsMatching.html)Possible values & `DOCNO`, `DOCID`Default value & `DOCNO`Configures & Whether the TREC-formatted results file contains DOCNOs or Terrier’s internal (integer) docids

<span>@ll@</span> Property & **matching.trecresults.scores**Used in & [org.terrier.matching.TRECResultsMatching](javadoc/org/terrier/matching/TRECResultsMatching.html)Possible values & true, falseDefault value & trueConfigures & Whether Terrier should use the relevance scores from the TREC-formatted results file

<span>@ll@</span> Property & **matching.trecresults.length**Used in & [org.terrier.matching.TRECResultsMatching](javadoc/org/terrier/matching/TRECResultsMatching.html)Possible values & a non-negative integerDefault value & 1000Configures & The maximum number of results to be retrieved from a TREC results file for each query. If set to 0, all available results are retrieved (note that setting this property to 0 may slow down the retrieval process for large collections, as a result set of the size of the collection will be allocated in memory)

### Query Expansion

<span>@ll@</span> Property & **parameter.free.expansion**Used in & [org.terrier.matching.models.queryexpansion.QueryExpansionModel](javadoc/org/terrier/matching/models/queryexpansion/QueryExpansionModel.html)Possible values & true or falseDefault value & trueConfigures & Whether we apply parameter-free query expansion or not.

<span>@ll@</span> Property & **rocchio.beta**Used in & [org.terrier.matching.models.queryexpansion.QueryExpansionModel](javadoc/org/terrier/matching/models/queryexpansion/QueryExpansionModel.html)Possible values & floatDefault value & 0.4Configures & The parameter of Rocchio’s automatic query expansion

<span>@ll@</span> Property & **trec.qe.model**Used in & [org.terrier.applications.batchquerying.TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html)Possible values & Query expansion modelsDefault value & Bo1Configures & A name of a query expansion model

<span>@ll@</span> Property & **expansion.documents**Used in & [org.terrier.matching.models.queryexpansion.QueryExpansionModel](javadoc/org/terrier/matching/models/queryexpansion/QueryExpansionModel.html)Possible values & integerDefault value & 3Configures & The number of top-ranked documents to be considered in the pseudo relevance set

<span>@ll@</span> Property & **expansion.terms**Used in & [org.terrier.matching.models.queryexpansion.QueryExpansionModel](javadoc/org/terrier/matching/models/queryexpansion/QueryExpansionModel.html),Possible values & integerDefault value & 10Configures & The number of the highest weighted terms from the pseudo relevance set to be added to the original query. There can be overlap between the original query terms and the added terms from the pseudo relevance set

<span>@ll@</span> Property & **expansion.mindocuments**Used in & [org.terrier.querying.ExpansionTerms](javadoc/org/terrier/querying/ExpansionTerms.html)Possible values & integerDefault value & 2Configures & The minimum number of documents a term must exist in before it can be considered to be informative. Defaults to 2. For more information, see Giambattista Amati: Information Theoretic Approach to Information Extraction. FQAS 2006: 519-529 [DOI 10.1007/11766254\_44](http://dx.doi.org/10.1007/11766254_44)

<span>@ll@</span> Property & **qe.feedback.selector**Used in & [org.terrier.querying.QueryExpansion](javadoc/org/terrier/querying/QueryExpansion.html)Possible values & classname, or comma-delimited class namesDefault value & PseudoRelevanceFeedbackSelectorConfigures & Class(es) that select feedback documents for query expansion. All classes must implement [FeedbackSelector](javadoc/org/terrier/querying/FeedbackSelector.html). If more than one is specified, then a chain is assumed, with last being innermost in the chain.

<span>@ll@</span> Property & **qe.expansion.terms.class**Used in & [org.terrier.querying.QueryExpansion](javadoc/org/terrier/querying/QueryExpansion.html)Possible values & classname, or comma-delimited class namesDefault value & DFRBagExpansionTermsConfigures & Class(es) that select terms during query expansion. All classes must extend [ExpansionTerms](javadoc/org/terrier/querying/ExpansionTerms.html). If more than one is specified, then a chain is assumed, with last being innermost in the chain.

### Querying

<span>@ll@</span> Property & **match.empty.query**Used in & [org.terrier.matching.Matching](javadoc/org/terrier/matching/Matching.html)Possible values & true, falseDefault value & trueConfigures & If true, return all documents for an empty query. Use this if you have post filter/processes to filter out the documents. E.g. link: site: etc

<span>@ll@</span> Property & **querying.allowed.controls**Used in & [org.terrier.querying.Manager](javadoc/org/terrier/querying/Manager.html)Possible values & Comma delimited list of which controls are allowed to be specified on the query. For use in interactive querying.Default value & c, rangeConfigures & Comma delimited list of which controls are allowed to be specified on the query. For use in interactive querying. “String:String” in the query are assumed to be fields unless the first string is an allowed control. An example value would be: c, range, link, site.

<span>@ll@</span> Property & **querying.default.controls**Used in & [org.terrier.querying.Manager](javadoc/org/terrier/querying/Manager.html)Possible values & Comma delimited list of control names and values. Names and values are separated by colon.Default value & not specifiedConfigures & Sets the defaults control values for the querying process. Controls are used to control the querying process, and may be used to set matching models, post filters post processes etc. An example value would be: c:10,site:gla.ac.uk

<span>@ll@</span> Property & **querying.postprocesses.order**Used in & [org.terrier.querying.Manager](javadoc/org/terrier/querying/Manager.html)Possible values & Comma delimited list of all allowed post processes.Default value & not specifiedConfigures & Specifies the order in which post processes may be be called, and those that may be called. This is because post processes often have inter-dependencies. An example value would be: QueryExpansion,Scope,Site

<span>@ll@</span> Property & **querying.postprocesses.controls**Used in & [org.terrier.querying.Manager](javadoc/org/terrier/querying/Manager.html)Possible values & Comma and colon delimited list of control names and post process names.Default value & not specifiedConfigures & Specifies which controls enable which post processes. An example value would be: site:Site,qe:QueryExpansion,scope:Scope

<span>@ll@</span> Property & **querying.postfilters.order**Used in & [org.terrier.querying.Manager](javadoc/org/terrier/querying/Manager.html)Possible values & Comma delimited list of all allowed post filters.Default value & not specifiedConfigures & Specifies the order in which post filters may be be called, and those that may be called. This is because post filters often have inter-dependencies. An example value would be: LinkFilter

<span>@ll@</span> Property & **querying.postfilters.controls**Used in & [org.terrier.querying.Manager](javadoc/org/terrier/querying/Manager.html)Possible values & Comma and colon delimited list of control names and post filter names.Default value & not specifiedConfigures & Specifies which controls enable which post filters. An example value would be: link:LinkFilter

### Advanced

<span>@ll@</span> Property & **matching.dsms**Used in & [org.terrier.matching.Matching](javadoc/org/terrier/matching/Matching.html)Possible values & Comma delimited names of classes in uk/ac/gla/terrier/matching/dsms, or other fully qualified modelsDefault value & not specifiedConfigures & Specifies the static [org.terrier.matching.dsms.DocumentScoreModifier](javadoc/org/terrier/matching/dsms/DocumentScoreModifier.html)s that should be applied to all terms of all queries.

<span>@ll@</span> Property & **matching.retrieved\_set\_size**Used in & [org.terrier.matching.Matching](javadoc/org/terrier/matching/Matching.html)Possible values & integer values &gt; 0Default value & 1000Configures & Maximum size of the result set.

Desktop Terrier
---------------

<span>@ll@</span> Property & **desktop.file.associations**Used in & [org.terrier.applications.desktop.filehandling.AssociationFileOpener](javadoc/org/terrier/applications/desktop/filehandling/AssociationFileOpener.html)Possible values & absolute path to filenameDefault value & TERRIER\_VAR/desktop.fileassocConfigures & the name of the file in which we save the file type associations with applications. If no absolute path is specified it will be presumed by TERRIER\_HOME/var

<span>@ll@</span> Property & **desktop.indexing.singlepass**Used in & [org.terrier.applications.desktop.DesktopTerrier](javadoc/org/terrier/applications/desktop/DesktopTerrier.html)Possible values & true, falseDefault value & falseConfigures & Whether single-pass indexing is used by in the Desktop Terrier.

Property

**desktop.directories.spec**

Used in

[org.terrier.applications.desktop.DesktopTerrier](javadoc/org/terrier/applications/desktop/DesktopTerrier.html)

Possible values

absolute path to filename

Default value

TERRIER\_VAR/desktop.spec

Configures

the name of the file that holds a list of directories that are to be indexed by the Desktop Terrier application

<span>@ll@</span> Property & **desktop.directories.filelist**Used in & [org.terrier.applications.desktop.DesktopTerrier](javadoc/org/terrier/applications/desktop/DesktopTerrier.html)Possible values & absolute path to filenameDefault value & TERRIER\_VAR\\index\\data.filelistConfigures & the name of the file in which we list all files that have been indexed

Miscellaneous
-------------

<span>@ll@</span> Property & **stopwords.intern.terms**Used in & [org.terrier.terms.Stopwords](javadoc/org/terrier/terms/Stopwords.html)Possible values & true, falseDefault value & falseConfigures & Whether stopwords should be [interned](http://java.sun.com/j2se/1.5.0/docs/api/java/lang/String.html#intern()) during indexing.

<span>\[</span>[Previous: Hadoop MapReduce Indexing with Terrier](hadoop_indexing.html)<span>\]</span> <span>\[</span>[Contents](index.html)<span>\]</span> <span>\[</span>[Next: Developing with Terrier](terrier_develop.html)<span>\]</span>

------------------------------------------------------------------------

Webpage: <http://terrier.org>
Contact: [](mailto:terrier@dcs.gla.ac.uk)
[School of Computing Science](http://www.dcs.gla.ac.uk/)
Copyright (C) 2004-2015 [University of Glasgow](http://www.gla.ac.uk/)
