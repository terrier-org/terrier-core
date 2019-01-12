Extending Retrieval in Terrier
==============================

Altering the retrieval process
------------------------------

It is very easy to alter the retrieval process in Terrier, as there are many *hooks* at which external classes can be involved. Firstly, you are free when writing your own application to render the results from Terrier in your own way. Results in Terrier come in the form of a [ResultSet](javadoc/org/terrier/matching/ResultSet.html).

An application's interface with Terrier is through the [Manager](javadoc/org/terrier/querying/Manager.html) class. The manager firstly pre-processes the query, by applying it to the configured [TermPipeline](javadoc/org/terrier/terms/TermPipeline.html). Then it calls the [Matching](javadoc/org/terrier/matching/Matching.html) class, which is responsible for matching documents to the query, and scoring the documents using a [WeightingModel](javadoc/org/terrier/matching/models/WeightingModel.html). Internally, Matching implementations use the [PostingListManager](javadoc/org/terrier/matching/PostingListManager.html) to open an [IterablePosting](javadoc/org/terrier/structures/postings/IterablePosting.html) for each query term. 

The terms used for matching are expressed as matching operators (matchops). It is possible to express queries as low-level matchops, e.g. using the `-m` option of the `batchretrieve` command.

The overall score of a document to the entire query can be modified by using a [DocumentScoreModifier](javadoc/org/terrier/matching/dsms/DocumentScoreModifier.html), which can be set by the `matching.dsms` property.

Once the [ResultSet](javadoc/org/terrier/matching/ResultSet.html) has been returned to the [Manager](javadoc/org/terrier/querying/Manager.html), there are two further phases, namely [PostProcessing](javadoc/org/terrier/querying/PostProcess.html) and [PostFiltering](javadoc/org/terrier/querying/PostFilter.html). In PostProcessing, the ResultSet can be altered in any way - for example, [QueryExpansion](javadoc/org/terrier/querying/QueryExpansion.html) expands the query, and then calls Matching again to generate an improved ranking of documents. PostFiltering is simpler, allowing documents to be either included or excluded - this is ideal for interactive applications where users want to restrict the domain of the documents being retrieved.

Changing Batch Retrieval
------------------------

[TRECQuerying](javadoc/org/terrier/applications/batchquerying/TRECQuerying.html) is the main way in which retrieval is deployed for batch retrieval experiments. It has a multitude of ways in which it can be extended:

-   **Format of input topics**: Terrier supports topics in two formats ([TREC tagged](javadoc/org/terrier/structures/TRECQuery.html), or [one query per line](javadoc/org/terrier/structures/SingleLineTRECQuery.html)). If neither of these is suitable, then you can implement another [QuerySource](javadoc/org/terrier/applications/batchquerying/QuerySource.html) that knows how parse your topics files. Use the `trec.topics.parser` property to configure Terrier to use your new QuerySource. E.g. `trec.topics.parser=my.package.DBTopicsSource`.

-   **Format of output results**: You can implement another [OutputFormat](javadoc/org/terrier/structures/outputformat/OutputFormat.html) to change the format of the results in the .res files. Use the `trec.querying.outputformat` property to configure Terrier to use your new OutputFormat. E.g. `trec.querying.outputformat=my.package.MyTRECResultsFormat`.

Altering query expansion
------------------------

[QueryExpansion](javadoc/org/terrier/querying/QueryExpansion.html) has various ways in which it can be extended:

-   To change the exact formula used to score occurrences, implement [QueryExpansionModel](javadoc/org/terrier/matching/models/queryexpansion/QueryExpansionModel.html).

-   Currently, terms are weighted from the entire feedback set as one a *bag of words* over the feedback set. To change this, extend [ExpansionTerms](javadoc/org/terrier/querying/ExpansionTerms.html).

-   To change the way feedback documents are selected, implement [FeedbackSelector](javadoc/org/terrier/querying/FeedbackSelector.html).

Advanced Weighting Models
-------------------------

It is very easy to implement your own weighting models in Terrier. Simply write a new class that extends [WeightingModel](javadoc/org/terrier/matching/models/WeightingModel.html). What’s more, there are many examples weighting models in [org.terrier.matching.models](javadoc/org/terrier/matching/models/package-summary.html).

**Generic Divergence From Randomness (DFR) Weighting Models**

The [DFRWeightingModel](javadoc/org/terrier/matching/models/DFRWeightingModel.html) class provides an interface for freely combining different components of the DFR framework. It breaks a DFR weighting model into three components: the basic model for randomness, the first normalisation by the after effect, and term frequency normalisation. Details of these three components can be found from [a description of the DFR framework](dfr_description.md). The DFRWeightingModel class provides an alternate and more flexible way of using the DFR weighting models in Terrier. For example, to use the [PL2](javadoc/org/terrier/matching/models/PL2.html) model, the name of the model `PL2` should be given in `etc/trec.models`, or set using the property `trec.model`. Alternatively, using the DFRWeightingModel class, we can replace `PL2` with `DFRWeightingModel(P, L, 2)`, where the three components of PL2 are specified in the brackets, separated by commas. If we do not want to use one of the three components, for example the first normalisation L, we can leave the space for this component blank (i.e. `DFRWeightingModel(P, , 2)`). We can also discard term frequency normalisation by removing the 2 between the brackets (i.e. `DFRWeightingModel(P, , )`). However, a basic randomness model must always be given.

The basic randomness models, the first normalisation methods, and the term frequency normalisation methods are included in packages [org.terrier.matching.models.basicmodel](javadoc/org/terrier/matching/models/basicmodel/package-summary.html), [org.terrier.matching.models.aftereffect](javadoc/org/terrier/matching/models/aftereffect/package-summary.html) and [org.terrier.matching.models.normalisation](javadoc/org/terrier/matching/models/normalisation/package-summary.html), respectively. Many implementations of each are provided, allowing a vast number of DFR weighting models to be generated.

Matching strategies
-------------------

Terrier implements three main alternatives for matching documents for a given query, each of which implements the [Matching](javadoc/org/terrier/matching/Matching.html) interface:

-   Document-At-A-Time (DAAT) (as per [daat.Full](javadoc/org/terrier/matching/daat/Full.html)) - exhaustive Matching strategy that scores all matching query terms for a document before moving onto the next documemt. Using daat.Full is advantageous for retrieving from large indices, and is the default matching strategy in Terrier.

-   Term-At-A-Time (TAAT) (as per [taat.Full](javadoc/org/terrier/matching/taat/Full.html)) - exhaustive Matching strategy that scores all postings for a single query term, before moving onto the next query term. for large indices, taat.Full consumes excessive memory with large partial result sets.

-   [TRECResultsMatching](javadoc/org/terrier/matching/TRECResultsMatching.html) - retrieves results from a TREC result file rather than the current index, based on the query id. Such a result file must be compatible with [trec\_eval](http://trec.nist.gov/trec_eval). TRECResultsMatching can introduce a repeatable efficiency gain for batch experiments.

If you have a more complex document weighting strategy that cannot be handled as a [WeightingModel](javadoc/org/terrier/matching/models/WeightingModel.html) or [DocumentScoreModifier](javadoc/org/terrier/matching/dsms/DocumentScoreModifier.html), you may wish to implement your own Matching strategy. In particular, [BaseMatching](javadoc/org/terrier/matching/BaseMatching.html) is a useful base class. Moreover, the [PostingListManager](javadoc/org/terrier/matching/PostingListManager.html) should be used for opening the [IterablePosting](javadoc/org/terrier/structures/postings/IterablePosting.html) posting stream for each query term.


Learning to Rank
----------------
Terrier support the application of learning to rank techniques within Terrier's ranking process. This is described separately in the [learning to rank documentation](learning.md).

Using Terrier Indices in your own code
--------------------------------------

**How many documents does term X occur in?**
```java
Index index = Index.createIndex();
Lexicon<String> lex = index.getLexicon();
LexiconEntry le = lex.getLexiconEntry("term");
if (le != null)
	System.out.println("Term term occurs in "+ le.getDocumentFrequency() + " documents");
else
	System.out.println("Term term does not occur");
```

**What is the probability of term Y occurring in the collection?**
```java
Index index = Index.createIndex();
Lexicon<String> lex = index.getLexicon();
LexiconEntry le = lex.getLexiconEntry("X");
double p = le == null
	?  0.0d
	: (double) le.getFrequency() / index.getCollectionStatistics().getNumberOfTokens();
```

**What terms occur in the 11th document?**
```java
Index index = Index.createIndex();
PostingIndex<?> di = index.getDirectIndex();
DocumentIndex doi = index.getDocumentIndex();
Lexicon<String> lex = index.getLexicon();
int docid = 10; //docids are 0-based
IterablePosting postings = di.getPostings(doi.getDocumentEntry(docid));
while (postings.next() != IterablePosting.EOL) {
	Map.Entry<String,LexiconEntry> lee = lex.getLexiconEntry(postings.getId());
	System.out.print(lee.getKey() + " with frequency " + postings.getFrequency());
}
```

**What documents does term Z occur in, and at what position?**

We assume that the index contains positional information.

```java
Index index = Index.createIndex();
PostingIndex<?> inv = index.getInvertedIndex();
MetaIndex meta = index.getMetaIndex();
Lexicon<String> lex = index.getLexicon();
LexiconEntry le = lex.getLexiconEntry( "Z" );
IterablePosting postings = inv.getPostings((BitIndexPointer) le);
while (postings.next() != IterablePosting.EOL) {
	String docno = meta.getItem("docno", postings.getId());
	int[] positions = ((BlockPosting)postings).getPositions();
	System.out.println(docno + " with frequency " + postings.getFrequency() + " and positions " + Arrays.toString(positions));
}
```

Moreover, if you're not comfortable with using Java, you can dump the indices of a collection using the --print\* options of the indexutil command. See the javadoc of [IndexUtils](javadoc/org/terrier/structures/IndexUtils.html) for more information.

### Example Querying Code

Below, you can find a example sample of using the querying functionalities of Terrier.

```java
	IndexRef indexref = IndexRef.of("/path/to/data.properties");
	Manager queryingManager = ManagerFactory.from(indexref);
	String query = "term1 term2";
	SearchRequest srq = queryingManager.newSearchRequestFromQuery(query);
	srq.addMatchingModel("org.terrier.matching.daat.Full", "PL2");
	queryingManager.runSearchRequest(srq);
	List<ScoredDoc> rs = srq.getResults();
```

------------------------------------------------------------------------

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
