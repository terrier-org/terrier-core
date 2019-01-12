What's New in Terrier
=====================

Terrier 5.1 - 14/01/2019
------------------------
Minor update with bug-fixes, improving features introduced in 5.0. New features include enhancements to the query language, including fuzzy term matching.

### Indexing
 - [TR-517](http://terrier.org/issues/browse/TR-517) org.terrier.indexing TRECCollection classes etc are in the wrong module (Nicola Tonellotto, ISTI-CNR)
 - [TR-521](http://terrier.org/issues/browse/TR-521) TRECWebCollection missing a constructor (Peilin Yang, Twitter)
 - [TR-534](http://terrier.org/issues/browse/TR-534) parallel indexing does not merge blocks correctly
 - [TR-535](http://terrier.org/issues/browse/TR-535) batchindexing -b command doesnt work for classical indexing
 - [TR-537](http://terrier.org/issues/browse/TR-537) NPE in parallel indexing with SimpleFileCollection (Ian Soboroff, NIST)
 - [TR-538](http://terrier.org/issues/browse/TR-538) TRECWebCollection doesnt parse malformed HTTP headers (Ian Soboroff, NIST)
 - [TR-542](http://terrier.org/issues/browse/TR-542) TwitterJSONCollection missing constructor
 - [TR-551](http://terrier.org/issues/browse/TR-551) Allow docnos to be overridden for SimpleFileCollection
 - [TR-553](http://terrier.org/issues/browse/TR-553) Transition to a more generic JSON document reader for tweets

### Retrieval
 - [TR-512](http://terrier.org/issues/browse/TR-512) Set approximate stats for ngram statistics
 - [TR-513](http://terrier.org/issues/browse/TR-513) Allow wmodel and tags to be specified as parameters to #combine
 - [TR-514](http://terrier.org/issues/browse/TR-514) Prefix/levenshtein matching in the matchopql
 - [TR-515](http://terrier.org/issues/browse/TR-515) #base64 support in matchopql
 - [TR-519](http://terrier.org/issues/browse/TR-519) getId() should return EOL in (AND|Field|Block|BlockField)IterablePosting after EOL (Nicola Tonellotto, ISTI-CNR)
 - [TR-527](http://terrier.org/issues/browse/TR-527) ANDIterablePosting and descendents should support next(int)
 - [TR-528](http://terrier.org/issues/browse/TR-528) Each MatchingOp can have more than one tag
 - [TR-529](http://terrier.org/issues/browse/TR-529) Clone support for matchops 
 - [TR-530](http://terrier.org/issues/browse/TR-530) Refactor FatScoringMatching and FatFeaturedScoringMatching to make easier subclasses
 - [TR-532](http://terrier.org/issues/browse/TR-532) interactive query formulation errors results in interactive ending
 - [TR-533](http://terrier.org/issues/browse/TR-533) control values are lowercased
 - [TR-536](http://terrier.org/issues/browse/TR-536) Some command-line settings in batchretrieve not reflected in .settings file (Ian Soboroff, NIST)
 - [TR-539](http://terrier.org/issues/browse/TR-539) Dependence model rewriter should support #syn
 - [TR-540](http://terrier.org/issues/browse/TR-540) QueryResultSet.getMetaKeys() does not give a defined order
 - [TR-541](http://terrier.org/issues/browse/TR-541) REST API should set Content-Type headers
 - [TR-543](http://terrier.org/issues/browse/TR-543) CollectionResultSet should warn/error when metadata is added to it
 - [TR-544](http://terrier.org/issues/browse/TR-544) SimpleDecorate swallows Exceptions
 - [TR-545](http://terrier.org/issues/browse/TR-545) Proximity shouldn't encapsulate terms with fields set
 - [TR-547](http://terrier.org/issues/browse/TR-547) JSON output format for REST API
 - [TR-552](http://terrier.org/issues/browse/TR-552) batch retrieval from a remote index doesnt create valid run files
 

### Other
 - [TR-522](http://terrier.org/issues/browse/TR-522), [TR-549](http://terrier.org/issues/browse/TR-549) wrong paths in .bat files (Artur Cieslewicz, Poznan University of Medical Sciences)
 - [TR-523](http://terrier.org/issues/browse/TR-523) -I option to bin/terrier doesn't work
 - [TR-524](http://terrier.org/issues/browse/TR-524) Terrier Aether downloads "too many" dependencies
 - [TR-525](http://terrier.org/issues/browse/TR-525) switch from jcabi-aether to maven-resolver to reduce dependencies on Spring/hibernate
 - [TR-526](http://terrier.org/issues/browse/TR-526) CLIParsedTool help formatting can be improved
 - [TR-546](http://terrier.org/issues/browse/TR-546) "Show document" application
 - [TR-548](http://terrier.org/issues/browse/TR-548) Bump versions as recommended by Github
 - [TR-554](http://terrier.org/issues/browse/TR-554) Current build requires maven 3.3
 - documentation fixes (Meng Dong, Iadh Ounis, Richard McCreadie, Graham McDonald, University of Glasgow)

Terrier 5.0 - 04/07/2018
------------------------
Major update. New features include a new commandline API, revisions to the retrieval API, a Mavenized layout, a low-level matching/query language inspired/partly compatible with Indri/Galago; Revised Lexicon data structure that records the maximum term frequency, to facilitate dynamic pruning; RESTful retrieval API and client.

More information can be found in the [documentation on migration to Terrier 5](Tr5migration.md).

### Indexing
 - [TR-443](http://terrier.org/issues/browse/TR-443) Index data.properties should record the termpipeline
 - [TR-466](http://terrier.org/issues/browse/TR-466) SimpleXMLCollection - requested constructor not found (Jiho Noh, University of Kentucky)
 - [TR-489](http://terrier.org/issues/browse/TR-489) TaggedDocument cannot make abstract if no global tag (Kiril Mihaylov, University of Glasgow)
 - [TR-479](http://terrier.org/issues/browse/TR-479) Control number of threads during threaded indexing
 - [TR-493](http://terrier.org/issues/browse/TR-493) Record maxtf in lexicon during indexing
 - [TR-504](http://terrier.org/issues/browse/TR-504) Tokenisers can be serializable

### Retrieval
 - A new "matching op" query language, Indri-esque in nature - see the [query language documentation](querylanguage.md#matching-op-query-language)
 - [TR-317](http://terrier.org/issues/browse/TR-317) ProximityIterablePosting does not calculate the total number of matching phrases
 - [TR-442](http://terrier.org/issues/browse/TR-442) IterablePosting.EOL should be Integer.MAX_VALUE (Nicola Tonellotto, ISTI-CNR)
 - [TR-472](http://terrier.org/issues/browse/TR-472) Request not passed to the WeightingModel (Aldo Lipani, Vienna University of Technology)
 - [TR-487](http://terrier.org/issues/browse/TR-487) MatchingQueryTerms should be decorated with the EntryStatistics
 - [TR-499](http://terrier.org/issues/browse/TR-499) ExplicitMultiTermQuery does not support query weights
 - [TR-500](http://terrier.org/issues/browse/TR-500) Expose MatchingOp QL to TRECQuerying
 - [TR-502](http://terrier.org/issues/browse/TR-502) Simplify Manager API
 - [TR-507](http://terrier.org/issues/browse/TR-507) Migrate Terrier query parser from Antlr to javacc
 - [TR-508](http://terrier.org/issues/browse/TR-508) Querying (interactive, batchretrieval) commands should have common base class
 - [TR-510](http://terrier.org/issues/browse/TR-510) bump jtreceval for Windows versions
 - [TR-511](http://terrier.org/issues/browse/TR-511) RESTful server and client
 
### Other
 - [TR-445](http://terrier.org/issues/browse/TR-445) Infrastructure support for non-String lexicons
 - [TR-480](http://terrier.org/issues/browse/TR-480) Resolve additional Terrier "plugins" from Maven repositories. Ramification of this change is that Class.forName() should be replaced by ApplicationSetup.getClass(). 
 - [TR-483](http://terrier.org/issues/browse/TR-483) pom.xml should not specify javac
 - [TR-491](http://terrier.org/issues/browse/TR-491) Split out MapReduce indexer from core, upgrade Hadoop
 - [TR-494](http://terrier.org/issues/browse/TR-494) bump jtreceval to print error message from bin/trec_terrier.sh -e
 - [TR-495](http://terrier.org/issues/browse/TR-495) Build should work on JDK9
 - [TR-498](http://terrier.org/issues/browse/TR-498) Separate into separate Maven modules
 - [TR-501](http://terrier.org/issues/browse/TR-498) More refined commandline CLI

Terrier 4.2 - 22/12/2016
------------------------
Minor update with mostly bug fixes and minor improvements. Inclusion of new feature of **experimental** multi-threaded indexing [TR-450](http://terrier.org/issues/browse/TR-450), and some time was spent trying to improve indexing efficiency. Terrier now requires Java 1.8.

### Indexing
- [TR-377](http://terrier.org/issues/browse/TR-377) TagSet.isTagToProcess and isTagToSkip perform upper-casing even if hashmaps have no size
- [TR-379](http://terrier.org/issues/browse/TR-379) Use a faster toLowerCase() during indexing of tags or English documents
- [TR-378](http://terrier.org/issues/browse/TR-378) TaggedDocument. getNextTerm() :: if (tag_open) and if (tag_close) blocks could be more efficient
- [TR-441](http://terrier.org/issues/browse/TR-441) index properties for blocks are not written for default compression config
- [TR-444](http://terrier.org/issues/browse/TR-444) UpdatingCollectionStatistics doesn't check num.Tokens correctly
- [TR-450](http://terrier.org/issues/browse/TR-450) Multithreaded indexer
- [TR-451](http://terrier.org/issues/browse/TR-451) TermCodes should not be global
- [TR-452](http://terrier.org/issues/browse/TR-452) CollectionFactory should allow Collections to be split
- [TR-449](http://terrier.org/issues/browse/TR-449) Allow Tokeniser act directly on Strings

### Retrieval
- [TR-389](http://terrier.org/issues/browse/TR-389) TrecQuerying Cache error (Agustin Marrone, Universidad Nacional de Luján)
- [TR-183](http://terrier.org/issues/browse/TR-183) Correct Hiemstra_LM model implementation (Jens Kürsten and Thomas Wilhelm-Stein)
- [TR-458](http://terrier.org/issues/browse/TR-458) Change default setting of ignore.low.idf.terms
- [TR-460](http://terrier.org/issues/browse/TR-460) Add refactoring of DependenceScoreModifier DSM, to allow more easier features to be created

### Other
- [TR-381](http://terrier.org/issues/browse/TR-381) Require Maven 3
- [TR-383](http://terrier.org/issues/browse/TR-383) Default constructors dont work for WARC Collection implementations
- [TR-384](http://terrier.org/issues/browse/TR-384) Hardcoded pathname in compression test (Nicola Tonellotto, CNR)
- [TR-385](http://terrier.org/issues/browse/TR-385) Don't build tar/zip assemblies for default maven package goal
- [TR-386](http://terrier.org/issues/browse/TR-386) java8 Maven puts javadoc in wrong location
- [TR-453](http://terrier.org/issues/browse/TR-453) Port docs/ pages to markdown
- [TR-454](http://terrier.org/issues/browse/TR-454) Move Desktop to own project
- [TR-455](http://terrier.org/issues/browse/TR-455) Add missing information from pom.xml file to ensure can be distribution on MavenCentral
- [TR-457](http://terrier.org/issues/browse/TR-457) Use stopword list as a resource (including resource file system)
- [TR-463](http://terrier.org/issues/browse/TR-463) Generate jforests.properties and sample features.list automatically within trec_setup.sh

### Acknowledgements

The release of Terrier 4.2 was supported by the following projects and funding bodies

-   [SUPER: Social sensors for security assessments and proactive emergencies management](http://super-fp7.eu) - EU FP7 Project, Grant Number 606853. Partial support for improvements in the Terrier 4.1 release.


Terrier 4.1 - 04/12/2015
------------------------

Substantial update that includes a re-structuring of the Terrier build routines and dependencies to [support compilation using Maven](terrier_develop.md#compiling), along with a number of other minor improvements and bug fixes.

### Indexing

-   [TR-320](http://terrier.org/issues/browse/TR-320): Blocks for Integer compression fails for large documents (blocks.max) - thanks to Matteo Catena, Ben He.

-   [TR-330](http://terrier.org/issues/browse/TR-330): Make SimpleXMLCollection be quiet - thanks to Ian Soboroff

-   [TR-333](http://terrier.org/issues/browse/TR-333): Integer compressor/decompressor does not work on Windows platform

-   [TR-347](http://terrier.org/issues/browse/TR-347): CompressionConfiguration for DirectInvertedDocidOnlyOuptutStream

-   [TR-348](http://terrier.org/issues/browse/TR-348): TwitterJSONDocument should not do stemming/stopwording

-   [TR-349](http://terrier.org/issues/browse/TR-349): Switch to slf4j

-   [TR-351](http://terrier.org/issues/browse/TR-351): Default constructor of FieldLexiconEntry$Factory should never be used.

-   [TR-355](http://terrier.org/issues/browse/TR-355): Some older Terrier 3.x indices don't upgrade correctly

-   [TR-356](http://terrier.org/issues/browse/TR-356): TRECCollection and WARC\*Collections should have a common base class

-   [TR-357](http://terrier.org/issues/browse/TR-357): Target non-forked version of Java\_FastPFOR - thanks to Matteo Catena

-   [TR-359](http://terrier.org/issues/browse/TR-359): MemBitSet is very inefficient

-   [TR-336](http://terrier.org/issues/browse/TR-336): readMinimalBinary in BitInBase.java does not match with its writeMinimalBinary counterpart

-   [TR-337](http://terrier.org/issues/browse/TR-337): Elias delta coding not working

-   [TR-340](http://terrier.org/issues/browse/TR-340): TaggedDocument.saveToAbstract is expensive even when no abstracts enabled

-   [TR-360](http://terrier.org/issues/browse/TR-360): Support updating the contents of documents in MemoryIndex

-   [TR-361](http://terrier.org/issues/browse/TR-361): Trailing empty documents cause the classical indexer to NPE

-   [TR-362](http://terrier.org/issues/browse/TR-362): Duplication between indexDocument() methods in MemoryIndex & MemoryFieldIndex

-   [TR-364](http://terrier.org/issues/browse/TR-364): TwitterJSONCollection incorrectly assumes all files are gzipped.

-   [TR-368](http://terrier.org/issues/browse/TR-368): Snowball stemmers dont work

-   [TR-369](http://terrier.org/issues/browse/TR-369): Remove diacritics term pipeline

-   [TR-370](http://terrier.org/issues/browse/TR-370): Documentation incorrectly discusses max.blocks

-   [TR-366](http://terrier.org/issues/browse/TR-366): A metaindex creation error isn’t FATAL, and can result in a metaindex with too few entries

### Retrieval

-   [TR-341](http://terrier.org/issues/browse/TR-341): hyper-geometric models (DPH, DLH and DLH13) produces Not a Number (NaN)

-   [TR-222](http://terrier.org/issues/browse/TR-222): Remove the deprecated 5-param score method in favor of the 2-param method - thanks to Francois Rousseau

-   [TR-342](http://terrier.org/issues/browse/TR-342): Improve LTR documentation to use Jforests’ input.valid.out-of-train

-   [TR-221](http://terrier.org/issues/browse/TR-221): Proposed score methods for org.terrier.matching.models.BM25.java - thanks to Francois Rousseau

-   [TR-318](http://terrier.org/issues/browse/TR-318): DisjunctiveQuery.toString() doesn’t report weights

-   [TR-367](http://terrier.org/issues/browse/TR-367): Disjunctive queries should have weights

-   [TR-319](http://terrier.org/issues/browse/TR-319): Query parsers doesn’t record weights for Disjunctive query groups

-   [TR-326](http://terrier.org/issues/browse/TR-326): Index folder not exists error message is needed. - thanks to Ian Soboroff

-   [TR-363](http://terrier.org/issues/browse/TR-363): incorrect assertion in RandomDataInputMemory.java

-   [TR-365](http://terrier.org/issues/browse/TR-365): docs/querylanguage.html discussion paragraph is incorrect.

### Other

-   [TR-187](http://terrier.org/issues/browse/TR-187): Maven support - thanks to Benjamin Piwowarski and Nicola Tonellotto

-   [TR-190](http://terrier.org/issues/browse/TR-190): Upgrade to Jetty 6

-   [TR-353](http://terrier.org/issues/browse/TR-353): Delete compile package script from bin

-   [TR-352](http://terrier.org/issues/browse/TR-352): Build .tar.gz and .zip distributions from Maven

-   [TR-332](http://terrier.org/issues/browse/TR-332): Windows batch files don’t work

-   [TR-334](http://terrier.org/issues/browse/TR-334): Terrier can not parse topic file when it contains only IDs (not English words)

-   [TR-350](http://terrier.org/issues/browse/TR-350): Support literal values in parameter values in the index properties

-   [TR-324](http://terrier.org/issues/browse/TR-324): Evaluation fails to parse .res files

-   [TR-325](http://terrier.org/issues/browse/TR-325): http\_terrier.sh contains extra copy of some other shell script - thanks to Ian Soboroff

-   [TR-339](http://terrier.org/issues/browse/TR-339): Unit Utils to avoid expressing Terrier properties with too many zeroes like inverted2direct.processtokens=100000000 - thanks to Nicola Tonellotto

-   [TR-354](http://terrier.org/issues/browse/TR-354): Unit tests should pass with assertions enabled

### Acknowledgements

The release of Terrier 4.1 was supported by the following projects and funding bodies

-   [SMART: Search engine for MultimediA enviRonment generated contenT](http://www.smartfp7.eu/) - EU FP7 Project, Grant Number 287583. Partial support for improvements in the Terrier 4.1 release.

-   [SUPER: Social sensors for security assessments and proactive emergencies management](http://super-fp7.eu) - EU FP7 Project, Grant Number 606853. Partial support for improvements in the Terrier 4.1 release.

Terrier 4.0 - 18/06/2014
------------------------

Major update adding significant new features:

-   [Real-time index structures](realtime_indices.md) facilitate incremental indexing of new documents as over time.

-   [Pluggable state-of-the-art index compression](compression.md) reduces the size of Terrier’s index structures.

-   [Learning-to-rank](learning.md) support enables out-of-the-box supervised ranking models.

-   [A website Search application](website_search.md) is now provided, illustrating real-time crawling, indexing and retrieval within Terrier.

Additionally various bugs fixes and other small improvements are also included. Some code may need to have imports adjusted, and indices will need to be upgraded.

### Indexing

-   [TR-240](http://terrier.org/issues/browse/TR-240): WARC10Collection logger info

-   [TR-289](http://terrier.org/issues/browse/TR-289): Docid alignment is broken for MapReduce indexing when map tasks are repeated

-   [TR-292](http://terrier.org/issues/browse/TR-292): All structure classes that take Index in their constructor instead take IndexOnDisk

-   [TR-295](http://terrier.org/issues/browse/TR-295): WARC10Collection incorrectly misses some documents

-   [TR-297](http://terrier.org/issues/browse/TR-297): Should reverse metakeys not include docno by default?

-   [TR-303](http://terrier.org/issues/browse/TR-303): Make compression pluggable/selectable during indexing (contributed by Matteo Catena)

-   [TR-306](http://terrier.org/issues/browse/TR-306): Locking index structures during writes

-   [TR-307](http://terrier.org/issues/browse/TR-307): PostingIndex.getPostings (POINTERTYPE extends Pointer lEntry) becomes PostingIndex.getPostings(Pointer lEntry)

-   [TR-308](http://terrier.org/issues/browse/TR-308): Implement extensible flushing and loading of memory indices

-   [TR-290](http://terrier.org/issues/browse/TR-290): Integer compression properties should be more uniform with others

-   [TR-291](http://terrier.org/issues/browse/TR-291): NoDuplicatesSinglePassIndexing could be simpler

-   [TR-300](http://terrier.org/issues/browse/TR-300): (Block)Inverted2Direct should not use old int<span>\[</span><span>\]</span><span>\[</span><span>\]</span> getNextDocuments()

-   [TR-314](http://terrier.org/issues/browse/TR-314): InvertedIndexBuilder should use DirectInvertedOutputStream internally

### Retrieval

-   [TR-284](http://terrier.org/issues/browse/TR-284): Improve logging on CompressingMetaIndex on disk to suggest fix SLOW WARNings

-   [TR-287](http://terrier.org/issues/browse/TR-287): Terrier assumes that all retrieved document should have score &gt; 0

-   [TR-293](http://terrier.org/issues/browse/TR-293): Breakout the many inner classes of TRECQuerying

-   [TR-299](http://terrier.org/issues/browse/TR-299): Document the use of DAAT for fast (default?) retrieval

-   [TR-301](http://terrier.org/issues/browse/TR-301): Negative requirement not functioning correctly

-   [TR-304](http://terrier.org/issues/browse/TR-304): Make appropriate fat and learning to rank techniques within the open source release

### Testing

-   [TR-315](http://terrier.org/issues/browse/TR-315): TestTaggedDocument does not test abstract extracting

-   [TR-286](http://terrier.org/issues/browse/TR-286): MemoryFieldsIndex doesn’t have a unit test

### Other

-   [TR-296](http://terrier.org/issues/browse/TR-296): Support indexing upgrading 3.x -&gt; 4.x

-   [TR-288](http://terrier.org/issues/browse/TR-288): TrecTerrier moved to the trec subpackage to facilitate ant building

-   [TR-302](http://terrier.org/issues/browse/TR-302): Significant source layout changes

-   [TR-305](http://terrier.org/issues/browse/TR-305): Simple website search engine

-   [TR-294](http://terrier.org/issues/browse/TR-294): BlockLexiconEntry & BlockFieldLexiconEntry can be deprecated

-   [TR-298](http://terrier.org/issues/browse/TR-298): Deprecate old index structure classes

### Acknowledgements

The release of Terrier 4.0 was supported by the following projects and funding bodies

-   [SMART: Search engine for MultimediA enviRonment generated contenT](http://www.smartfp7.eu/) - EU FP7 Project, Grant Number 287583. Funded the development of the new learning to rank and pluggable compression technologies in the Terrier 4.0 release.

-   [ReDites: Real-time Detection, Tracking, Monitoring and Interpretation of Events in Social Media](http://demeter.inf.ed.ac.uk/redites/) - EPSRC Project, Grant Number EP/L010690/1. Funded the development of the new real-time indexing and search technologies in the Terrier 4.0 release.

Terrier 3.6 - 03/04/2014
------------------------

Substantial update: Provides numerous updates and fixes to issues in Terrier 3.5. This is primarily a bug-fix release clearing issues before the release of Terrier 4.0 that adds new functionality.

### Indexing

-   [TR-174](http://terrier.org/issues/browse/TR-174): Indexing a directory breaks on special pdf- or excel files

-   [TR-178](http://terrier.org/issues/browse/TR-178): Private method setPostingImplementation in BitPostingIndex

-   [TR-179](http://terrier.org/issues/browse/TR-179): BitIn implementations should both inherit from a common base class

-   [TR-180](http://terrier.org/issues/browse/TR-180): RandomDataInputMemory has an unnecessary binary search for every read()

-   [TR-181](http://terrier.org/issues/browse/TR-181): Hadoop indexing should not copy hadoop libraries to a job classpath (contributed by Marco Didonna)

-   [TR-186](http://terrier.org/issues/browse/TR-186): Inverted to Direct indexing is not exposed via the Trec\_Terrier application

-   [TR-188](http://terrier.org/issues/browse/TR-188): Stopwords incorrectly handles reset (contributed by Steven)

-   [TR-192](http://terrier.org/issues/browse/TR-192): On Windows, the document.fsarrayfile is not closed, resulting in a \_1 in the filename when indexing

-   [TR-194](http://terrier.org/issues/browse/TR-194): TRECCollection docnos should be trimmed of whitespace

-   [TR-197](http://terrier.org/issues/browse/TR-197): Terrier refuses to parse some topics (example included)

-   [TR-199](http://terrier.org/issues/browse/TR-199): Block compression support (contributed by Benjamin Piwowarski)

-   [TR-200](http://terrier.org/issues/browse/TR-200): Non unique keys in reverse index

-   [TR-201](http://terrier.org/issues/browse/TR-201): Log4j conflicts can occur for hadoop indexing

-   [TR-202](http://terrier.org/issues/browse/TR-202): Documentation for tokeniser property

-   [TR-205](http://terrier.org/issues/browse/TR-205): Hadoop jar folder in distribution should not mention 0.20

-   [TR-206](http://terrier.org/issues/browse/TR-206): Tag information are not loaded within BlockDirectIndex & BlockInvertedIndex (contributed by Sadi Samy)

-   [TR-207](http://terrier.org/issues/browse/TR-207): Adhoc Evaluation returns bad precision at percent (contributed by Sadi Samy)

-   [TR-209](http://terrier.org/issues/browse/TR-209): Allow long metaindex values to be cropped automatically by the MetaIndex

-   [TR-211](http://terrier.org/issues/browse/TR-211): Indexer meta keys are case-sensitive, apart from docno

-   [TR-214](http://terrier.org/issues/browse/TR-214): Indexing of metatags for XMLDocuments (contributed by Daniel Jimenez Kwast, Menno Tammens, Nicolas Faessel and Dennis Pallett)

-   [TR-216](http://terrier.org/issues/browse/TR-216): Changing Hadoop temporary folder without recompiling

-   [TR-220](http://terrier.org/issues/browse/TR-220): SimpleXMLCollection raise null pointer exception if document contains doctype with same the name than xml.doctag (contributed by Nicolas Faessel)

-   [TR-235](http://terrier.org/issues/browse/TR-235): LexiconBuilder fails on empty term

-   [TR-247](http://terrier.org/issues/browse/TR-247): WARC09Collection and TRECWebCollection are not consistent about the return format for parseDate

-   [TR-252](http://terrier.org/issues/browse/TR-252): Update Apache POI versions to parse newer Word/Excel/Powerpoint files

-   [TR-257](http://terrier.org/issues/browse/TR-257): IndexUtil.rename() should check rename() returns.

-   [TR-262](http://terrier.org/issues/browse/TR-262): PostingIndex is the new DirectIndex and InvertedIndex abstract type

-   [TR-279](http://terrier.org/issues/browse/TR-279): Termids should be assigned by decreasing frequency for highest direct file compression <span>\[</span>single pass indexers<span>\]</span>

### Retrieval

-   [TR-170](http://terrier.org/issues/browse/TR-170): ArrayIndexOutOfBoundsException in PostingListManager, add unit test for PostingListManager

-   [TR-173](http://terrier.org/issues/browse/TR-173): The Decorate class incorrectly adds meta index properties when used as a PostProcess rather than a PostFilter

-   [TR-175](http://terrier.org/issues/browse/TR-175): Decorate class does not remove field qualifiers when generating query-biased summaries

-   [TR-176](http://terrier.org/issues/browse/TR-176): Allow abitrary context objects in SearchRequest

-   [TR-185](http://terrier.org/issues/browse/TR-185): TRECQuery should not tokenise the topic number

-   [TR-189](http://terrier.org/issues/browse/TR-189): TRECFullTokenizer may discard DOCNO tag, causing terrier to crash (contributed by Steven)

-   [TR-198](http://terrier.org/issues/browse/TR-198): Conservative QE incorrectly weights queryterms (contributed by Saul Vargas)

-   [TR-203](http://terrier.org/issues/browse/TR-203): MRF formula applies w\_o twice

-   [TR-204](http://terrier.org/issues/browse/TR-204): Relevance feedback for query expansion in queries without relevance judgements could throw NullPointerException

-   [TR-217](http://terrier.org/issues/browse/TR-217): CS query expansion model is incorrect

-   [TR-228](http://terrier.org/issues/browse/TR-228): ML2 and MDL2 are missing default constructors

-   [TR-229](http://terrier.org/issues/browse/TR-229): BasicIterablePosting next(int)

-   [TR-230](http://terrier.org/issues/browse/TR-230): Proximity operator ()

-   [TR-242](http://terrier.org/issues/browse/TR-242): Problem with query terms frequency (key frequency = 1) using BM25

-   [TR-248](http://terrier.org/issues/browse/TR-248): Error instantiating topic file QuerySource called TRECQuery

-   [TR-251](http://terrier.org/issues/browse/TR-251): ResultSet implementation should know how to sort themselves

-   [TR-258](http://terrier.org/issues/browse/TR-258): PhraseScoreModifier should use IterablePosting

-   [TR-259](http://terrier.org/issues/browse/TR-259): FieldORIterablePosting doesnt save field lengths correctly

-   [TR-260](http://terrier.org/issues/browse/TR-260): Block(Field)ORIterablePosting are inefficient

-   [TR-263](http://terrier.org/issues/browse/TR-263): PostingListManager should retain the String of the term

-   [TR-264](http://terrier.org/issues/browse/TR-264): Manager crops resultsets unnecessarily

-   [TR-266](http://terrier.org/issues/browse/TR-266): TRECQuerying should support non textual OutputFormats

-   [TR-268](http://terrier.org/issues/browse/TR-268): Query term counting doesn’t work

-   [TR-276](http://terrier.org/issues/browse/TR-276): Bump DFI models in core

-   [TR-278](http://terrier.org/issues/browse/TR-278): The cache in WeightingModelFactory should be clearable

### Documentation

-   [TR-195](http://terrier.org/issues/browse/TR-195): Documentation should make clear that inverted files produced by different methods are identical

-   [TR-218](http://terrier.org/issues/browse/TR-218): Documentation confuses block.size and blocks.size

-   [TR-223](http://terrier.org/issues/browse/TR-223): Refactoring/Cleaning up of the package org.terrier.matching.models (contributed by Francois Rousseau)

-   [TR-224](http://terrier.org/issues/browse/TR-224): configure\_retrieval.html doesn’t mention Dirichlet

-   [TR-227](http://terrier.org/issues/browse/TR-227): Errors in Javadoc of In\_expB2 and InB2 models

-   [TR-239](http://terrier.org/issues/browse/TR-239): Clarify when Terrier Query language can be used viz TREC

-   [TR-269](http://terrier.org/issues/browse/TR-269): Document default values for indexing.singlepass.max.postings.memory and indexing.max.docs.per.builder

### Other

-   [TR-172](http://terrier.org/issues/browse/TR-172): Upgrade PDFBox

-   [TR-210](http://terrier.org/issues/browse/TR-210): InteractiveQuerying displays documents with scores of negative infinity

-   [TR-212](http://terrier.org/issues/browse/TR-212): Evaluation doesn’t support graded relevance judgements

-   [TR-219](http://terrier.org/issues/browse/TR-219): TRECQrels has poor error messages

-   [TR-243](http://terrier.org/issues/browse/TR-243): Terrier query language does not document multi-term field search syntax FIELD:(term1 term2)

-   [TR-253](http://terrier.org/issues/browse/TR-253): Decorate & SimpleDecorate needs unit tests

-   [TR-254](http://terrier.org/issues/browse/TR-254): Refactor query-biased summarisation out of Decorate

-   [TR-255](http://terrier.org/issues/browse/TR-255): PostingListManager has no unit test

-   [TR-256](http://terrier.org/issues/browse/TR-256): Files should check FileSystem return code for rename operation

-   [TR-261](http://terrier.org/issues/browse/TR-261): CollectionStatistics should be Writable

-   [TR-264](http://terrier.org/issues/browse/TR-264): Make TerrierTimer more useful

-   [TR-267](http://terrier.org/issues/browse/TR-267): Get a clone of an EntryStatistics

-   [TR-270](http://terrier.org/issues/browse/TR-270): Sorting of resultsets needs unit testing

-   [TR-271](http://terrier.org/issues/browse/TR-271): CollectionStatistics should have a toString()

-   [TR-272](http://terrier.org/issues/browse/TR-272): TerrierTimer is too verbose

-   [TR-273](http://terrier.org/issues/browse/TR-273): DFR constituent models & PFN models should be Cloneable

-   [TR-274](http://terrier.org/issues/browse/TR-274): Block Shakespeare tests are failing

-   [TR-275](http://terrier.org/issues/browse/TR-275): TRECWebCollection doesn’t normalise encodings

-   [TR-277](http://terrier.org/issues/browse/TR-277): RandomDataInputMemory improvements

Terrier 3.5 - 16/06/2011
------------------------

Significant update: Added Document-at-a-time (DAAT) retrieval for large indices; Refactored tokenisation for enhanced multi-language support; Upgraded Hadoop support to version 0.20 (NB: Terrier now requires Java 1.6); Added synonym support in query language and retrieval; Added out-of-the-box indexing support for query-biased summaries and improved example web-based interface; Added new, 2nd generation DFR models as well as other recent effective information-theoretic effective models; Included many more JUnit tests (now 300+). Terrier 3.0 indices are compatible with Terrier 3.5.

### Indexing

-   [TR-117](http://terrier.org/issues/browse/TR-117): Improve fields support by SimpleXMLCollection

-   [TR-120](http://terrier.org/issues/browse/TR-120): Error loading an additional MetaIndex structure (contributed by Javier Ortega, Universidad de Sevilla)

-   [TR-106](http://terrier.org/issues/browse/TR-106): Pipeline Query/Doc Policy Lifecycle (contributed by Giovanni Stilo, University degli Studi dell’Aquila and Nestor Laboratory - University of Rome “Tor Vergata”)

-   [TR-116](http://terrier.org/issues/browse/TR-110): Lexicon not properly renamed on Windows

-   [TR-118](http://terrier.org/issues/browse/TR-118): SimpleXMLCollection - the term near the closing tag is ignored (contributed by Damien Dudognon, Institut de Recherche en Informatique de Toulouse)

-   [TR-123](http://terrier.org/issues/browse/TR-123): Null pointer exception while trying to index simple document (contributed by Ilya Bogunov)

-   [TR-126](http://terrier.org/issues/browse/TR-123): Logging improvements

-   [TR-124](http://terrier.org/issues/browse/TR-123): When processing docid tag in MEDLINE format XML file, xml context path is needed

-   [TR-127](http://terrier.org/issues/browse/TR-127): Easier refactoring of SinglePass indexers (contributed by Jonathon Hare, University of Southampton)

-   [TR-108](http://terrier.org/issues/browse/TR-108): Some indexers do not set the IterablePosting class for the DirectIndex (contributed by Richard Eckart de Castilho, Darmstadt University of Technology)

-   [TR-136](http://terrier.org/issues/browse/TR-136): Hadoop indexing misbehaves when terrier.index.prefix is not “data”

-   [TR-137](http://terrier.org/issues/browse/TR-137): TRECCollection cannot add properties from the document tags to the meta index at indexing time

-   [TR-150](http://terrier.org/issues/browse/TR-150): TRECCollection parse DOCHDR tags, including URLs should they exist (see [TRECWebCollection](javadoc/org/terrier/indexing/TRECWebCollection.html))

-   [TR-138](http://terrier.org/issues/browse/TR-138): IndexUtil.copyStructure fails when source and destination indices are same

-   [TR-140](http://terrier.org/issues/browse/TR-140): Indexing support for query-biased summarisation

-   [TR-144](http://terrier.org/issues/browse/TR-144): CollectionRecordReader.next should not be recursive

-   [TR-146](http://terrier.org/issues/browse/TR-146), [TR-148](http://terrier.org/issues/browse/TR-148): Tokenisation should be done separately from Document parsing (the tokeniser can be set using the property `tokeniser` - see [Non English language support in Terrier](languages.md) for more information on changing the tokenisation used by Terrier); Refactor Document implementations (e.g. [TRECDocument](javadoc/org/terrier/indexing/TRECDocument.html) and [HTMLDocument](javadoc/org/terrier/indexing/HTMLDocument.html) are now deprecated in favour of the new [TaggedDocument](javadoc/org/terrier/indexing/TaggedDocument.html))

-   [TR-147](http://terrier.org/issues/browse/TR-147): Allow various Collection implementations to use different Document implementations

-   [TR-158](http://terrier.org/issues/browse/TR-158): Single pass indexing with default configuration doesn’t ever flush memory

### Retrieval

-   [TR-16](http://terrier.org/issues/browse/TR-16),[TR-166](http://terrier.org/issues/browse/TR-166): Extending query language and Matching to support synonyms

-   [TR-157](http://terrier.org/issues/browse/TR-157): Remove TRECQuerying scripting files: `trec.models`, `qemodels`, `trec.topics.list` and `trec.qrels` - use properties in [TRECQuerying](javadoc/org/terrier/applications/TRECQuerying.html) instead.

-   [TR-156](http://terrier.org/issues/browse/TR-156): Deploy a DAAT matching strategy - see [org.terrier.matching.daat](javadoc/org/terrier/matching/daat/package-summary.html) (partially contributed by Nicola Tonellotto, CNR)

-   [TR-113](http://terrier.org/issues/browse/TR-113): The [LGD](javadoc/org/terrier/matching/models/LGD.html) Loglogistic weighting model (contributed by Gianni Amati, FUB)

-   [TR-105](http://terrier.org/issues/browse/TR-105): Index should check version number as it can’t open older indices

-   [TR-107](http://terrier.org/issues/browse/TR-105): DirectIndex.getTerms() is broken

-   [TR-110](http://terrier.org/issues/browse/TR-110): TRECDocnoOutputFormat assumes metadata key is “docno”

-   [TR-112](http://terrier.org/issues/browse/TR-110): “Term not found” log message should not be a warning

-   [TR-121](http://terrier.org/issues/browse/TR-121): Distance.noTimesSameOrder() can throw ArrayIndexOutOfBoundsException

-   [TR-129](http://terrier.org/issues/browse/TR-129): Posting.getDocumentLength() does not work for postings from the direct file

-   [TR-130](http://terrier.org/issues/browse/TR-130): Manager should use Index specified in Request object

-   [TR-131](http://terrier.org/issues/browse/TR-131): Parsing of WeightingModel class names could be better

-   [TR-132](http://terrier.org/issues/browse/TR-132): Some BitIn implementations don’t pass unit tests

-   [TR-139](http://terrier.org/issues/browse/TR-139): Manager should balk at null Index in constructor

-   [TR-141](http://terrier.org/issues/browse/TR-141): GammaFunction is not good enough for proximity - this fixes the retrieval effectiveness of [DFRDependenceScoreModifier](javadoc/org/terrier/matching/dsms/DFRDependenceScoreModifier.html)

-   [TR-142](http://terrier.org/issues/browse/TR-142): Matching implementations should not overwrite the EntryStatistics stored in the MatchingQueryTerms object

-   [TR-143](http://terrier.org/issues/browse/TR-143): BitFileBuffered creates unnecessary byte arrays

-   [TR-145](http://terrier.org/issues/browse/TR-145): ResultSet implementations don’t retain exactResultSize() in child ResultSets

-   [TR-149](http://terrier.org/issues/browse/TR-149): Added first Divergence from Independence model, [DFI0](javadoc/org/terrier/matching/models/DFI0.html) (contributed by B.T. Dincer, Mugla University)

-   [TR-153](http://terrier.org/issues/browse/TR-153),[TR-154](http://terrier.org/issues/browse/TR-154),[TR-155](http://terrier.org/issues/browse/TR-155): Provide a Matching implementation that reads results from TREC run files (see [TRECResultsMatching](javadoc/org/terrier/matching/TRECResultsMatching.html))

-   [TR-160](http://terrier.org/issues/browse/TR-160): Inv2DirectMultiReduce needs improvement to allow direct split across multiple files

-   [TR-161](http://terrier.org/issues/browse/TR-161): Use Tokenisers in query side tokenisation

-   [TR-163](http://terrier.org/issues/browse/TR-163): Index does not explicitly close the properties file

-   [TR-164](http://terrier.org/issues/browse/TR-164): Document index structure is left open when index.close() is called

-   [TR-165](http://terrier.org/issues/browse/TR-165): SingleLineTRECQuery opens all files as UTF

-   [TR-167](http://terrier.org/issues/browse/TR-167): Large document metadata are stored incorrectly by MetaIndex

-   Two new 2nd generation Divergence from Randomness models: [JsKLs](javadoc/org/terrier/matching/models/Js_KLs.html) and [XSqrA\_M](javadoc/org/terrier/matching/models/XSqrA_M.html) (contributed by Gianni Amati, Fondazione Ugo Bordoni)

### Testing

-   Added a considerable number of additional JUnit tests

-   [TR-134](http://terrier.org/issues/browse/TR-134): BitPostingIndexInputFormat needs a unit test

-   [TR-135](http://terrier.org/issues/browse/TR-135): TestPostingStructures should test skipping of stream structures

-   [TR-151](http://terrier.org/issues/browse/TR-151): SimpleFileCollection and chums (FileDocument etc) have no unit test

-   [TR-159](http://terrier.org/issues/browse/TR-159): Junit end-to-end test for WT2G test collection

### Desktop

-   [TR-103](http://terrier.org/issues/browse/TR-103): Desktop search cant open files on 64bit Windows

### Other

-   [TR-168](http://terrier.org/issues/browse/TR-168): Terrier batch scripts can fail when the TERRIER\_HOME environment variable is set on Windows 64bit

-   [TR-115](http://terrier.org/issues/browse/TR-115): Upgrade Hadoop support for 0.20

-   [TR-104](http://terrier.org/issues/browse/TR-115): Move to Java 6

-   [TR-119](http://terrier.org/issues/browse/TR-119): Temporary jar/properties in HDFS /tmp are not deleted

-   [TR-152](http://terrier.org/issues/browse/TR-152): TagSet should detect a tag in both process and skip entries

Terrier 3.0 - 10/03/2010
------------------------

Major update: Support for indexing WARC collections; improved index structure layout; improved MapReduce mode indexing; refined, scalable structure access at retrieval time; moved all code to terrier.org namespace; added field-based and proximity term dependence models; added HTTP-based retrieval interface; added many JUnit tests. **All indices must be rebuilt**.

### Indexing

-   [TR-14](http://terrier.org/issues/browse/TR-14), [TR-42](http://terrier.org/issues/browse/TR-42), [TR-56](http://terrier.org/issues/browse/TR-56), [TR-102](http://terrier.org/issues/browse/TR-102): Various changes to the format of the index, to promote reuse, scalability and speed.

-   [TR-17](http://terrier.org/issues/browse/TR-17), [TR-50](http://terrier.org/issues/browse/TR-50), [TR-54](http://terrier.org/issues/browse/TR-54), [TR-77](http://terrier.org/issues/browse/TR-77): Added MetaIndex for document metadata. DOCNOs etc need not be in lexographical order.

-   [TR-43](http://terrier.org/issues/browse/TR-), [TR-48](http://terrier.org/issues/browse/TR-48), [TR-69](http://terrier.org/issues/browse/TR-69), [TR-70](http://terrier.org/issues/browse/TR-70): Fields should contain frequency information.

-   [TR-39](http://terrier.org/issues/browse/TR-39), [TR-40](http://terrier.org/issues/browse/TR-40), [TR-41](http://terrier.org/issues/browse/TR-41), [TR-46](http://terrier.org/issues/browse/TR-46), [TR-50](http://terrier.org/issues/browse/TR-50), [TR-83](http://terrier.org/issues/browse/TR-83), [TR-88](http://terrier.org/issues/browse/TR-88): Various improvements and bug fixes to MapReduce indexing.

-   [TR-44](http://terrier.org/issues/browse/TR-44), [TR-55](http://terrier.org/issues/browse/TR-55): Improve robustness of single-pass indexing.

-   [TR-71](http://terrier.org/issues/browse/TR-71), [TR-98](http://terrier.org/issues/browse/TR-98): Allow Bit posting structures to be split across multiple files.

-   [TR-28](http://terrier.org/issues/browse/TR-28), [TR-91](http://terrier.org/issues/browse/TR-91): Index WARC collections (UK-2006, ClueWeb09).

-   [TR-34](http://terrier.org/issues/browse/TR-34): Documentation update: Property values for single-pass indexing are not scaled.

-   [TR-37](http://terrier.org/issues/browse/TR-37), [TR-38](http://terrier.org/issues/browse/TR-38), [TR-47](http://terrier.org/issues/browse/TR-47),[TR-57](http://terrier.org/issues/browse/TR-57), [TR-78](http://terrier.org/issues/browse/TR-78), [TR-79](http://terrier.org/issues/browse/TR-79), [TR-93](http://terrier.org/issues/browse/TR-93), [TR-94](http://terrier.org/issues/browse/TR-94): Generate the direct file from an inverted index as a MapReduce job.

### Retrieval

-   [TR-20](http://terrier.org/issues/browse/TR-20), [TR-42](http://terrier.org/issues/browse/TR-42), [TR-64](http://terrier.org/issues/browse/TR-64): Access the posting list for one term as a stream - see [Posting](javadoc/org/terrier/structures/postings/Posting.html) and [IterablePosting](javadoc/org/terrier/structures/postings/IterablePosting.html).

-   [TR-86](http://terrier.org/issues/browse/TR-86): Matching should be an interface.

-   [TR-87](http://terrier.org/issues/browse/TR-87): PorterStemmer doesn’t match expected output by Porter himself.

-   [TR-81](http://terrier.org/issues/browse/TR-81): Implements proximity term dependence models. For more information, see [Configuring Retrieval](configure_retrieval.md#proximity).

-   [TR-19](http://terrier.org/issues/browse/TR-19): Support relevance feedback as well as pseudo-relevance feedback.

-   [TR-68](http://terrier.org/issues/browse/TR-68), [TR-73](http://terrier.org/issues/browse/TR-73), [TR-74](http://terrier.org/issues/browse/TR-73), [TR-94](http://terrier.org/issues/browse/TR-94): Implement field-based weighting models. For more information, see [Configuring Retrieval](configure_retrieval.md#fields).

-   [TR-99](http://terrier.org/issues/browse/TR-99): Provide way to integrate static doc prior easily. For more information, see [Configuring Retrieval](configure_retrieval.md#priors).

-   [TR-90](http://terrier.org/issues/browse/TR-90): MatchingQueryTerms does not retain query term order.

-   [TR-26](http://terrier.org/issues/browse/TR-26): Parse Million Query track topic files.

-   [TR-49](http://terrier.org/issues/browse/TR-49): Let TRECQuerying filename be predetermined by property.

-   [TR-75](http://terrier.org/issues/browse/TR-75): Allow to set runtag in runs.

-   [TR-60](http://terrier.org/issues/browse/TR-60): Removed PonteCroft language modelling.

-   [TR-66](http://terrier.org/issues/browse/TR-66), [TR-84](http://terrier.org/issues/browse/TR-84): Refactor TRECQuery.

-   [TR-67](http://terrier.org/issues/browse/TR-67): Request object should contain the Index.

-   TermScoreModifiers have been deprecated, and no longer work. You should use WeightingModel instead.

### Testing

-   Added considerable number of end-to-end and unit tests.

-   [TR-59](http://terrier.org/issues/browse/TR-59): Fixed reset problem in Terrier evaluation tool.

-   [TR-76](http://terrier.org/issues/browse/TR-76): Bump Junit version.

### Desktop

-   [TR-61](http://terrier.org/issues/browse/TR-61): Desktop example app should use MetaIndex.

### Other

-   [TR-89](http://terrier.org/issues/browse/TR-89): Check all .java and .sh files have Terrier license header.

-   [TR-82](http://terrier.org/issues/browse/TR-82): Have a simple webapps search results interface.

-   [TR-80](http://terrier.org/issues/browse/TR-80): Move code to terrier.org Java package namespaces.

-   [TR-45](http://terrier.org/issues/browse/TR-45): Add (readwrite)(DeltaGolomb) etc to BitIn/BitOut.

-   [TR-52](http://terrier.org/issues/browse/TR-52): FSOrderedMapFile causes seek(-1) when searching for an entry less than the first.

-   [TR-72](http://terrier.org/issues/browse/TR-): FSOrderedMapFile.EntryIterator.skip() breaks FSOrderedMapFile.EntryIterator.hasNext().

-   [TR-95](http://terrier.org/issues/browse/TR-95): FSArrayFile.ArrayFileIterator.skip() does not update entry index correctly.

-   [TR-92](http://terrier.org/issues/browse/TR-92): utility.io.CountingInputStream does not count single bytes correctly.

-   [TR-53](http://terrier.org/issues/browse/TR-53): Rounding.toString() doesnt work for 10dp.

-   [TR-62](http://terrier.org/issues/browse/TR-62): Files layer can transparently cache files.

-   [TR-2](http://terrier.org/issues/browse/TR-2), [TR-65](http://terrier.org/issues/browse/TR-): Replace Terrier’s Makefile with Ant build.xml. Makefile, compile.sh, compile.bat have now been removed. See [Developing with Terrier](terrier_develop.md) to see how to compile Terrier.

-   [TR-63](http://terrier.org/issues/browse/TR-63),[TR-101](http://terrier.org/issues/browse/TR-101): Documentation updates.

-   [TR-100](http://terrier.org/issues/browse/TR-100): Update default and sample terrier.properties files.

Terrier 2.2.1 - 29/01/2009
--------------------------

Minor update - fixes some small bugs in 2.2 relating to MapReduce mode indexing, clarifies some documentation, and includes a missing source file.

-   Added missing source file for [SkipTermPipeline](javadoc/org/terrier/terms/SkipTermPipeline.html).

-   Clarified java documentation for single-pass indexing memory control.

-   [TR-8](http://terrier.org/issues/browse/TR-8): Delay index path checking during indexing till HDFS is loaded.

-   [TR-7](http://terrier.org/issues/browse/TR-7): Files.list() does not work for HDFS paths.

-   [TR-4](http://terrier.org/issues/browse/TR-4): Update the year for the copyright to 2009.

-   [TR-3](http://terrier.org/issues/browse/TR-3): Partitioned Mode fails unexpectedly due to missing run status files.

Terrier 2.2 - 23/12/2008
------------------------

Substantial update, consisting of new support for [Hadoop](http://hadoop.apache.org/core/), a Hadoop MapReduce indexing system, and various minor improvements and bug fixes. This is intended to be the ultimate release in the 2.x series.

### Indexing

-   Added new [Hadoop MapReduce indexing system](hadoop_indexing.md), and corresponding support for [Hadoop MapReduce](hadoop_configuration.md) jobs.

-   Refactoring of various indexing (in particular single pass indexing) to support MapReduce indexing.

-   Block indexing can now use marker tokens to designate block boundaries. See [Configuring Indexing](configure_indexing.md) for more details.

-   Indexing supports named tokens which should not be passed through the term pipeline. See [Configuring Indexing](configure_indexing.md) and [SkipTermPipeline](javadoc/org/terrier/terms/SkipTermPipeline.html) for more details.

-   TRECCollection and TRECUTFCollection now index CDATA sections, to support indexing of [CDIP1](http://www.ir.iit.edu/projects/CDIP.html) collection, as used by the [TREC Legal track](http://trec-legal.umiacs.umd.edu/).

-   SimpleXMLCollection now indexes CDATA sections. contributed by Giovanni Stilo (University of Roma “Tor Vergata”).

-   LexiconBuilder no longer uses java.io.File to create temporary directories.

-   Double memory reserve threshold for singlepass indexing on 64bit Sun JVMs.

-   Record `docno.byte.length` setting in index properties file, so that indices with different docno lengths can be loaded at once.

-   CollectionFactory now supports non-default Collection constructors.

### Retrieval

-   BUG: Lexicon and sub-classes could give incorrect results for very large lexicons (15M terms+) due to overflow of file offsets. contributed by Giovanni Stilo (University of Roma “Tor Vergata”) and Gianni Amati (Fondazione Ugo Bordoni).

-   BUG: Lexicon hashing would produce NPE when no term in lexicon had same initial character as query term. contributed by Gianni Amati (Fondazione Ugo Bordoni).

-   BUG: .res.setting files not created for query expansion runs.

-   BUG: `rocchio_beta` was property name while `rocchio.beta` was documented. `rocchio.beta` is now the property, while `rocchio_beta` is supported but deprecated.

-   WeightingModels are now cloneable.

### Other

-   Added [HadoopPlugin](javadoc/org/terrier/utility/io/HadoopPlugin.html), allowing the HDFS filesystem paths (e.g. `hdfs://namenode:9000/path/to/file`) to be accessed directly from Terrier. Hadoop MapReduce jobs can also be created, using Hadoop on Demand if so-configured.

-   Filesystem layer can now support deleteOnExit() semantics for filesystems.

-   Worked to reduce javac generics and other compiler warnings.

-   BUG: Adding a Terrier Application plugin does not cause an NPE.

-   Added method to reconfigure ApplicationSetup during MapReduce jobs.

Terrier 2.1 - 19/03/2008
------------------------

Minor update, containing mostly bug fixes, and a FileSystem abstraction layer.

### Indexing

-   BUG: `trec_terrier -i -d` then `trec_terrier -i -v` doesn’t work.

-   BUG: Indexing on Windows doesn’t work as the index properties output streams aren’t closed.

-   BUG: DocumentExtraction doesn’t work (thanks Brantman).

-   BUG: LM Indexing throws NPE if Index doesnt exist (thanks Brantman).

-   BUG: Mixed-case TrecDocTag specifications dont work (thanks Marco Bianchi and Giovanni Stilo, University of Rome “Tor Vergata”).

-   BUG: Corner-case HTML terms from HTMLDocument and TRECDocument may contain whitespace. trim() terms in both classes to prevent indexing problems (contributed by Carlos M Lorenzetti).

-   Allow case-sensitive and case-insensitive TrecDocTag specifications. Default is case-sensitive, change using `TrecDocTags.casesensitive` property.

-   Add useful error message when DOCNO is longer than `docno.byte.length`

-   Retrofit all indexing file IO to [Files](javadoc/org/terrier/utility/Files.html) class.

### Retrieval

-   Retrofit all retrieval file IO to [Files](javadoc/org/terrier/utility/Files.html) class.

-   Added a new [TREC topics tokeniser](javadoc/org/terrier/indexing/TRECFullUTFTokenizer.html) for non-English topics. This is automatically used by [TRECQuery](javadoc/org/terrier/structures/TRECQuery.html) if `string.use_utf` is set.

-   Allow case-sensitive and case-insensitive TrecQueryTag specifications. Default is case-insensitive, change using `TrecQueryTags.casesensitive` property.

-   Interactive Querying: Added properties to allow model, manager and matching to be controlled: `interactive.manager`, `interactive.matching`, and `interactive.model`.

-   Try harder to allow weighting models to use their default parameter value, if no parameter value is specified.

### Desktop

-   Added a property `desktop.indexing.singlepass` to the DesktopTerrier to allow the use of single-pass indexing.

-   Added properties to allow model, manager and matching to be controlled: `desktop.manager`, `desktop.matching`, and `desktop.model`.

### Testing

-   Test the `trec_terrier -i -d` then `trec_terrier -i -v` indexing strategy.

### Other

-   Trec\_setup.bat should sort collection.spec file: change to FindFiles.

-   Adding missing environment options to trec\_terrier.bat and anyclass.bat.

-   Fix the documentation concerning specifying TREC topic file tags in [Configuring Retrieval](configure_retrieval.md).

-   Add API to TRECQrelsInMemory for getting docnos of pooled documents with graded/non-graded relevance assessments.

-   Add the FileSystem abstraction layer, which allows various types of files to be accessed through a uniform API. For example, indexing an HTTP Web page is as straightforward as indexing a local document. The FileSystem abstraction layer is described in detail in [Developing Terrier](terrier_develop.md).

-   Removed deprecated code in CollectionStatistics.

-   Removed entire `uk.ac.gla.terrier.structures.trees` package which had been deprecated since 1.1.0. Indexing code based on the 1.0.x release series may break.

-   FindBugs: Some minor changes to the source-code reflect issues identified by [FindBugs](http://findbugs.sourceforge.net/). We continue to work to address issues raised by FindBugs.

Terrier 2.0 - 04/01/2008
------------------------

Major update, integrating a new (alternative) single-pass indexing architecture, and a new index format. Some bug fixes and some change of APIs.

### Indexing

-   New single-pass indexing, contributed by [Roi Blanco](http://www.dc.fi.udc.es/~roi/) ([University of A Coruña](http://www.udc.es/principal/en/)). This indexing method is faster, can build larger indices, and is more robust to memory usage during indexing. To use single-pass indexing, add the `-j` option to TrecTerrier: `bin/trec_terrier.sh -i -j`. For more information on single-pass indexing, see [Configuring Indexing](configure_indexing.md), or [TREC Example](trec_examples.md) for examples of its use.

-   New index on disk format: `prefix.log` has been replaced by `prefix.properties`, which records much more metadata about an index, including the Terrier version used to create it, the classes (and their constructor parameters) to read the Index, and the statistics of the index. The compressed data structures (DirectIndex and InvertedIndex) have also changed (the endian-ness of the index format changed, for efficiency reasons). However, Terrier 2.0 can read all Terrier 1.x indices, so *indices do **not** have to be upgraded or recreated.*

-   The format of block (term positions) has been altered slightly, so that a term can exist in 0 blocks in a document. Again, this change does not require older indices to be upgrade or recreated.

### Retrieval

-   The new [DFRee](javadoc/org/terrier/matching/models/DFRee.html) DFR weighting model is included, which provides robust performance on a range of test collections.

-   Query parsing is now performed by the [Manager.newSearchRequest(String,String)](javadoc/org/terrier/querying/Manager.html#newSearchRequest(java.lang.String,%20java.lang.String)). Direct access to the query parser is now not recommended, as the query parsing is likely to evolve in future versions of Terrier. See [Extending Retrieval](extending_retrieval.md) for the recommended querying code for applications.

### Desktop

-   BUG: When no terrier.properties present, the Desktop cannot perform query expansion using `qe:on` control.

-   BUG: Set reasonable defaults when no terrier.properties file open.

### Testing

-   Merchant of Venice test harness now tests indexing using TRECUTFCollection.

-   Merchant of Venice test harness now tests indexing using merging.

-   Merchant of Venice test harness now tests single-pass indexing.

-   Merchant of Venice test harness now tests required (+/-) term queries.

### Other

-   Documentation: We provide indexing times and recommended parameters settings with corresponding MAP performances in [Example TREC Experiments](trec_examples.md).

-   Deprecation: Methods which do not return a LexiconEntry have been deprecated in the Lexicon.

-   FindBugs: Many minor changes to the source-code reflect issues identified by [FindBugs](http://findbugs.sourceforge.net/). We continue to work to address issues raised by FindBugs.

-   Upgraded [trove4j](trove4j.sourceforge.net/) to latest version.

Terrier 1.1.1 - 24/10/2007
--------------------------

Minor update. Mostly bug fixes. Some minor code enhancements, plus the inclusion of a test harness. Snowball stemmers were added to boost support for languages other than English. This will likely be the last release in the 1.x.x series.

### Indexing

-   BUG: When merging block indices, ensure that the resulting inverted index has blocks.

-   BUG: Field indexing not working properly.

-   BUG: Block ids recorded incorrectly when fields are enabled.

-   BUG Resilience: Don’t throw NPE in SimpleFileCollection if no files are processed.

-   BUG Resilience: Don’t throw exceptions if index has no terms/documents - fail more gracefully (LexiconBuilder, Indexer).

-   When parsing a TREC-like document collection, use Streams at TRECCollection level, and Reader at Document level. This allows easier change of encoding, etc.

### Retrieval

-   BUG: When retrieving phrases, prevent Exception from debugging code in Manager.

-   BUG: Regression when retrieving phrases, some documents not matched.

-   BUG: DFRWeightingModel breaks when first normalisation or tf normalisation is not specified.

-   BUG Resilience: Do not throw NPE in ExpansionTerms if original query terms are not set by client code.

-   Create a .settings file for each TREC results file, so that it is easy to determine the setting for a run.

-   Added an alternative batch query parser, known as [SingleLineTRECQuery](javadoc/org/terrier/structures/SingleLineTRECQuery.html), mostly to support the test harness.

### Desktop

-   BUG: PDF parsing too noisy through log4j, indexing may never finish. Turned down default logging level to info.

-   BUG: Logging may not appear for indexing Terrier’s own documentation. Indexing run in new Thread, not SwingUtilities.invokeLater().

### Other

-   Tokenisation: Added Snowball stemmers. For more information, see documentation on [Non English language support](languages.md).

-   Java: Various Java Generics changed.

-   Testing: Added test harness, which checks that the correct documents are retrieved for various queries and index formats. Uses Shakespeare’s Merchant of Venice play for the test document collection.

-   Shell scripts: Take notice of TERRIER\_ETC environment variable and pass to Terrier.

-   Shell scripts: added anyclass.bat.

Terrier 1.1.0 - 15/06/2007
--------------------------

Major update. Many changes to the source code, including more robust indexing and index structure merging.

### Indexing

Indexing architecture has been updated for Terrier 1.1.0, however indices created with 1.1.0 are completely compatible with those created with 1.0.x, and vice-versa.

-   Separated `string.byte.length property` into two properties: `max.term.length` and `docno.byte.length`.

-   Allow UTF characters in indexing, and use a compatible method for saving these in the Lexicon. This enables Terrier to be used for non-English languages. Set `string.use_utf` to true when indexing, and use [TRECUTFCollection](javadoc/org/terrier/indexing/TRECUTFCollection.html) to parse the collection.

-   Merge multiple temporary lexicons at once in [LexiconBuilder](javadoc/org/terrier/structures/indexing/LexiconBuilder.html)s. 16 seems to be a good default setting.

-   Don’t use tree structures for indexing, they are slower and larger (20% indexing speed improvement). New classes [DocumentPostingList](javadoc/org/terrier/structures/indexing/DocumentPostingList.html) and [LexiconMap](javadoc/org/terrier/structures/indexing/LexiconMap.html).

-   Writing structures (direct and inverted) flush normally, to reduce memory consumption.

-   Add lexicon hashing to reduce size of binary search.

-   [LookAheadStream](javadoc/org/terrier/utility/LookAheadStream.html) and [LookAheadReader](javadoc/org/terrier/utility/LookAheadReader.html) are now case-sensitive, as the [String.toUpperCase()](http://java.sun.com/j2se/1.5.0/docs/api/java/lang/String.html#toUpperCase()) affects indexing speed.

-   When the current indexing hits a threshold, finish it, then start a new index. Merge indices at completion. See [Indexer](javadoc/org/terrier/indexing/Indexer.html).

-   Added code for merging indices - see [StructureMerger](javadoc/org/terrier/structures/merging/StructureMerger.html) and [BlockStructureMerger](javadoc/org/terrier/structures/merging/StructureMerger.html).

-   Added a [CollectionFactory](javadoc/org/terrier/indexing/CollectionFactory.html), to allow [Collection](javadoc/org/terrier/indexing/Collection.html)s to wrap other Collections.

-   [TRECCollection](javadoc/org/terrier/indexing/TRECCollection.html) no longer throws exception when used for re-indexing and docPointers.col exists (contributed by Dolf Trieschnigg, Univ of Twente).

### Retrieval

-   CollectionStatistics is now non-static.

-   Added Hiemstra LM and Lemur TF\_IDF weighting models.

-   BUG: [Lexicon](javadoc/org/terrier/structures/Lexicon.html) would match prefixes of terms when the desired term does not exist in the Lexicon.

-   Use a [LexiconEntry](javadoc/org/terrier/structures/LexiconEntry.html), to support easier thread-safety with the [Lexicon](javadoc/org/terrier/structures/Lexicon.html).

-   Added generic [DFRWeightingModel](javadoc/org/terrier/matching/models/DFRWeightingModel.html), which can generate many DFR document weighting models. More information in [Extending Retrieval](extend_retrieval.md).

### Other

-   Improved documentation.

-   Java: Move to Java 1.5 source, and upgrade GNU Trove jar.

-   Logging: Use log4j throughout source. Log4j config can be read from etc/terrier-log.xml.

-   Java: Various source code changes, to allow easier extension and re-use.

-   Compiling: Included compile.bat, by Jurrie Overgoor (Univ of Twente).

Terrier 1.0.2 - 17/03/2005
--------------------------

-   BUG: Language modelling didn’t index properly when block indexing was enabled.

-   BUG: Lexicon Merging compare strings the same way as the LexiconTree outputs them, to ensure sorting is correct.

-   BUG: Block ids are correctly recorded in the inverted index for large collections.

-   BUG: Block ids are correctly read from the direct index.

-   BUG: The phrase score modifier has been rewritten to a more correct implementation.

-   BUG: HTML Stack only lives for one document.

-   BUG: Cropping the resultset did not function properly with metadata.

-   BUG: If more than one control mapped to a post(process/filter) then only the last one would be noted. This is now fixed, and simpler data structures are used for the controls and the post(process/filter).

-   TREC: During indexing, start indexing from the beginning of a new file, not from the previous state.

-   TREC: Added `trec.collection.class` property to allow TRECIndexing to determine the TREC class to be used during indexing.

-   Added DLH Divergence From Randomness model - this hyper-geometric weighting model is completely parameter free and is very robust over many test collections.

-   Query Parser: Allow characters in the extended character set to be in terms.

-   LookAheadReader: Corrected implementation of Reader interface to give better support wrt EOF and subsequent method calls.

-   Added more TermPipeline classes: CropTerm, DumpTerm.

-   Updated and organised documentation and Javadoc.

Terrier 1.0.1 - 09/02/2005
--------------------------

-   BUG 1: `bin/interactive_terrier.bat` doesn’t run the correct class.

-   BUG 2: `bin/compile.sh` compiles the ANTLR parser correctly.

-   BUG: Lexicon binary search failed when searching for the last entry. Binary search has been updated.

-   Document Index binary search made more robust for different types of documentIds.

-   Desktop Terrier: starts new threads using correct Swing utility API.

-   Desktop Terrier: close PDF documents correctly.

-   Desktop Terrier: search text logging is slightly more robust.

-   Desktop Terrier: always disable search tab while indexing.

-   Desktop Terrier: temporary lexicon folders are deleted if they exist in the index folder before indexing.

-   Desktop Terrier: process only 25,000 terms at a time during block inverted index building, as only 120MB heap space is restrictive.

-   TREC: Model, QEModel & C value is displayed correctly in TREC querying and results file.

-   Documentation: Removed Known Issue 1 from doc/todo.html.

-   Documentation: Updated javadoc in ApplicationSetup.java.

-   Documentation: Added more details about compiling in doc/terrier\_develop.html.

Terrier 1.0.0 - 28/01/2005
--------------------------

-   New Indexing APIs, that allow more diverse forms of collections to be easily indexed.

-   New Querying API and languages (eg fields, phrases, proximity, requirements).

-   More Statistical IR Models: tf-idf, BM25, Divergence From Randomness models, and Ponte-Croft language model.

-   More example applications, including a Desktop Search application.

Terrier 1.0 Beta2 - 22/11/2004
------------------------------

-   Minor bugfix release - documentation error.

Terrier 1.0 Beta - 18/11/2004
-----------------------------

-   First public release of Terrier.

---------
> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
