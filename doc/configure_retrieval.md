

# Configuring Retrieval in Terrier

## Topics

After the end of the indexing process, we can proceed with retrieving from the document collection. At this stage, the configuration properties for applying stemming or not, removing stopwords or not, and the maximum length of terms, should be exactly the same properties as used for indexing the collection.

Firstly, in the property `trec.topics`, we need to specify the files containing the queries to process.

There are two formats for TREC topics files that are supported by Terrier. In the first, topics are marked up in XML-like (actually SGML) tags; the second is a plain text format with one topic per line, with the topic number as the first word.

### SGML topic files

Before processing SGML topic files, the tags of the topics files to be processed should be specified. We can do that by setting the properties `TrecQueryTags.process`, which denotes which tags to process, `TrecQueryTags.idtag`, which stands for the tag containing the query identifier, and `TrecQueryTags.skip`, which denotes which query tags to ignore.

For example, suppose that the format of topics is the following:

    <TOP>
    <NUM>123<NUM>
    <TITLE>title
    <DESC>description
    <NARR>narrative
    </TOP>

If we want to skip the description and narrative (DESC and NARR tags respectively), and consequently use the title only, then we need to setup the properties as follows:

    TrecQueryTags.doctag=TOP
    TrecQueryTags.process=TOP,NUM,TITLE
    TrecQueryTags.idtag=NUM
    TrecQueryTags.skip=DESC,NARR

If alternatively, we want to use the title, description and the narrative tags to create the query, then we need to setup the properties as follows:

    TrecQueryTags.doctag=TOP
    TrecQueryTags.process=TOP,NUM,DESC,NARR,TITLE
    TrecQueryTags.idtag=NUM
    TrecQueryTags.skip=

The tags specified by TrecQueryTags are case-insensitive (note the difference from TrecDocTags). If you want them to be case-sensitive, then set `TrecQueryTags.casesensitive=false`.

### Single-line topic files

Single-line topic files have a simpler format, without the additional description and narrative information:

1. a few query terms
2. some different query terms

Support for single-line topic files is provided by the SingleLineTRECQuery class. To use a topics file in this format, you must firstly set `trec.topics.parser=SingleLineTRECQuery`. The `batchretrieval` command has a handy `-s` command line argument:

	bin/terrier batchretrieve -s



## Weighting Models and Parameters

Next, we need to specify which of the available weighting models we will use for assigning scores to the retrieved documents. We do this by specifying the name of the corresponding model class in the property `trec.model`. E.g. `trec.model=PL2`, or using the `-w` option to the `batchretreve` command, e.g. `bin/terrier batchretrieve -w PL2`.

Terrier provides implementations of many weighting models (see [org.terrier.matching.models](javadoc/org/terrier/matching/models/package-summary.html) for the full list). In particular, some of the notable weighting models implemented include many from the [Divergence from Randomness (DFR) framework](dfr_description.md), among others:

-   [BB2](javadoc/org/terrier/matching/models/BB2.html) (DFR): Bose-Einstein model for randomness, the ratio of two Bernoulli’s processes for first normalisation, and Normalisation 2 for term frequency normalisation .

-   [BM25](javadoc/org/terrier/matching/models/BM25.html): The BM25 probabilistic model.

-   [DFR\_BM25](javadoc/org/terrier/matching/models/DFR_BM25.html) (DFR): The DFR version of BM25 .

-   [DLH](javadoc/org/terrier/matching/models/DLH.html) (DFR): The DLH hyper-geometric DFR model (parameter free).

-   [DLH13](javadoc/org/terrier/matching/models/DLH13.html) (DFR): An improved version of DLH (parameter free).

-   [DPH](javadoc/org/terrier/matching/models/DPH.html) (DFR): A different hyper-geometric DFR model using Popper’s normalization (parameter free) .

-   [DFRee](javadoc/org/terrier/matching/models/DFRee.html) (DFR): Another hyper-geometric models which takes an average of two information measures.

-   [Hiemstra\_LM](javadoc/org/terrier/matching/models/Hiemstra_LM.html): Hiemstra’s language model.

-   [IFB2](javadoc/org/terrier/matching/models/IFB2.html) (DFR): Inverse Term Frequency model for randomness, the ratio of two Bernoulli’s processes for first normalisation, and Normalisation 2 for term frequency normalisation .

-   [In\_expB2](javadoc/org/terrier/matching/models/In_expB2.html) (DFR): Inverse expected document frequency model for randomness, the ratio of two Bernoulli’s processes for first normalisation, and Normalisation 2 for term frequency normalisation .

-   [In\_expC2](javadoc/org/terrier/matching/models/In_expC2.html) (DFR): Inverse expected document frequency model for randomness, the ratio of two Bernoulli’s processes for first normalisation, and Normalisation 2 for term frequency normalisation with natural logarithm .

-   [InL2](javadoc/org/terrier/matching/models/InL2.html) (DFR): Inverse document frequency model for randomness, Laplace succession for first normalisation, and Normalisation 2 for term frequency normalisation .

-   [LemurTF\_IDF](javadoc/org/terrier/matching/models/LemurTF_IDF.html): Lemur’s version of the tf\*idf weighting function.

-   [LGD](javadoc/org/terrier/matching/models/LGD.html) (DFR): A log-logistic DFR model , .

-   [PL2](javadoc/org/terrier/matching/models/PL2.html) (DFR): Poisson estimation for randomness, Laplace succession for first normalisation, and Normalisation 2 for term frequency normalisation .

-   [TF\_IDF](javadoc/org/terrier/matching/models/TF_IDF.html): The tf\*idf weighting function, where tf is given by Robertson’s tf and idf is given by the standard Sparck Jones’ idf.

-   [DFRWeightingModel](javadoc/org/terrier/matching/models/DFRWeightingModel.html): This class provides an alternative way of specifying an arbitrary DFR weighting model, by mixing the used components . For usage, see [Extending Retrieval](extend_retrieval.md) and background material in [Description of DFR](dfr_description.md).

To process the queries, ensure the topics are specified in the `trec.topics` property, then type the following:

    bin/terrier batchretrieval -c c:1.0

where the option `-r` specifies that we want to perform retrieval, and the option `-c c:1.0` specifies the parameter value for the term frequency normalisation.

Field-Based Weighting Models
----------------------------

Since version 3.0, Terrier has support for field-based weighting models. In particular, field-based models take into account not just the presence of a term in a field, but the actual frequency of the occurrence in that field. For instance, for a document where the query term occurs once in the body of the text, then there is only a small chance that the document is really related to that term. However, if the term occurs in the title of the document, then this chance is greatly increased. Terrier provides several field-based weighting models:

-   [PL2F](javadoc/org/terrier/matching/models/PL2F.html): this is a per-field normalisation model, which is based on PL2 .

-   [BM25F](javadoc/org/terrier/matching/models/BM25F.html): this is a per-field normalisation model, which is based on BM25.

-   [ML2](javadoc/org/terrier/matching/models/ML2.html): this is multinomial field-based model .

-   [MDL2](javadoc/org/terrier/matching/models/MDL2.html): this is another multinomial field-based model, where the multinomial is replaced by an approximation .

-   Arbitrary per-field normalisation weighting models can be generated using [PerFieldNormWeightingModel](javadoc/org/terrier/matching/models/PerFieldNormWeightingModel.html) in a similar manner to DFRWeightingModel.

To use a field-based model, you have to index using fields. See [Configuring Indexing](configure_indexing.md) for more details on how to configure fields during indexing.

Different field-based models have different parameters, as controlled by various properties. These generally include weights for each field, namely `w.0`, `w.1`, etc. Per-field normalisation models, such as BM25F and PL2F also require the normalisation parameters for each field, namely `c.0`, `c.1`, and so on. To run with a field-based model:

    bin/terrier batchretrieval -w PL2F -Dc.0=1.0 -Dc.1=2.3 -Dc.3=40 -Dw.0=4 -Dw.1=2 -Dw.3=25

For improved efficiency of field-based weighting models, it is recommended that you manually alter the `data.properties` file of your index to change the DocumentIndex implementation in use, by updating it to read `index.document.class=org.terrier.structures.FSAFieldDocumentIndex`.

Proximity (Dependence) Models
-----------------------------

Since version 3.0, Terrier includes two dependence models. Such models highly weight documents where the query terms are in close proximity. To use a term dependence model, you have to index using blocks - see [Configuring Indexing](configure_indexing.md) for more details on how to configure block indexing.

Two dependence models are included:

-   [DFRDependenceScoreModifier](javadoc/org/terrier/matching/dsms/DFRDependenceScoreModifier.html) - this implements a Divergence from Randomness based dependence model.

-   [MRFDependenceScoreModifier](javadoc/org/terrier/matching/dsms/MRFDependenceScoreModifier.html) - this implements the Markov Random Field dependence model.

To enable the dependence models, use the `matching.dsms` property. E.g. :

    bin/terrier batchretrieval -Dmatching.dsms=DFRDependenceScoreModifier

The dependence models have various parameters to set. For more information, see the classes themselves.

Document Prior Features
-----------------------

Terrier can easily integrate a query-independent document feature (or prior) into your retrieval model. The simplest way to do this is using [SimpleStaticScoreModifier](javadoc/org/terrier/matching/dsms/SimpleStaticScoreModifier.html). For instance, say you generate a feature for all documents in the collection (e.g. using link analysis). You should export your file in one of the formats supported by SimpleStaticScoreModifier, e.g. feature value for each document, one per line. You can then add the feature as:

    bin/terrier batchretrieval -Dmatching.dsms=SimpleStaticScoreModifier -Dssa.input.file=/path/to/feature -Dssa.input.type=listofscores -Dssa.w=0.5

The property `ssa.w` controls the weight of your feature. For more information on the type of files supported, see [SimpleStaticScoreModifier](javadoc/org/terrier/matching/dsms/SimpleStaticScoreModifier.html). Finally, Terrier can support multiple DSMs, using them in a comma-delimited manner:

    bin/terrier batchretrieval -Dmatching.dsms=DFRDependenceScoreModifier,SimpleStaticScoreModifier -Dssa.input.file=/path/to/feature -Dssa.input.type=listofscores -Dssa.w=0.5

Query Expansion
---------------

Terrier also offers a query expansion functionality. For a brief description of the query expansion module, you may view the [query expansion section of the DFR Framework description](dfr_description.md#queryexpansion). The term weighting model used for expanding the queries with the most informative terms of the top-ranked documents is specified by the property `trec.qe.model`, the default value is [Bo1](javadoc/org/terrier/matching/models/queryexpansion/Bo1.html), which refers to the class implemnting the term weighting model to be used for query expansion. Terrier has other query expansion models, including [Bo2](javadoc/org/terrier/matching/models/queryexpansion/Bo2.html) and [KL](javadoc/org/terrier/matching/models/queryexpansion/KL.html) - see [org.terrier.matching.models.queryexpansion](javadoc/org/terrier/matching/models/queryexpansion/package-summary.html) for the full list.

In addition, there are two parameters that can be set for applying query expansion. The first one is the number of terms to expand a query with, specified by the property `expansion.terms` - default value `10`. Moreover, the number of top-ranked documents from which these terms are extracted is specified by the property `expansion.documents`, the default value of which is 3.

To retrieve from an indexed test collection, using query expansion, with the term frequency normalisation parameter equal to 1.0, we can type:

    bin/terrier batchretrieval -q -c c:1.0

Relevance feedback is also supported by Terrier, assuming that the relevant documents are listed in a TREC format qrels file. To use feedback documents in query expansion, change the [FeedbackSelector](javadoc/org/terrier/querying/FeedbackSelector.html), as follows:

    bin/terrier batchretrieval -q -Dqe.feedback.selector=RelevantOnlyFeedbackDocuments,RelevanceFeedbackSelector -Dqe.feedback.filename=/path/to/feedback/qrels

Learning to Rank
----------------

Since version 4.0, Terrier has offered learning to rank functionality, based on the so-called Fat framework. This allows multiple features (which can be query-dependent or query-independent) to be calculated during the Matching process, and then combined using a machine learned ranking model. In particular, any weighting model in Terrier can be used as an additional query-dependent feature. For more information on these new, advanced functionalities in Terrier - including a worked example using a TREC Web track corpus - see [Learning to Rank with Terrier](learning.md).

Other Configurables
-------------------

The results are saved in the directory var/results in a file named as follows:

    "weighting scheme" c "value of c"_counter.res

For example, if we have used the weighting scheme PL2 with c=1.28 and the counter was 2, then the filename of the results would be `PL2c1.28_3.res`. If you wish to override the filename of the generated result file, use the `trec.results.file` property. Alternatively, if multiple instances of Terrier are writing files at same time, the use of the counter can fail due to a race condition. Instead, set `trec.querycounter.type=random`. Output files by TRECQuerying are always in the TREC-format. If you desire an alternative format, you can implement another [org.terrier.structures.outputformat.OutputFormat](javadoc/org/terrier/structures/outputformat/OutputFormat.html), then get TRECQuerying to use this with the property `trec.querying.outputformat`.

For each query, Terrier returns a maximum number of 1000 documents by default. We can change the maximum number of returned documents per query by changing `matching.retrieved_set_size`. For example, if we want to retrieve 10000 documents for each given query, we need to set `matching.retrieved_set_size` to 10000. In addition, if the `end` control is set in the property `querying.default.controls`, then amend this to 9999 as well (from Terrier 3.5, this is removed from the default configuration). TRECQuerying can also limit this number, according to the `trec.output.format.length` property (default 1000) also.

Some of the weighting models, e.g. BM25, assume low document frequencies of query terms. For these models, it is worth ignoring query terms with high document frequency during retrieval by setting `ignore.low.idf.terms` to true. Moreover, it is better to set `ignore.low.idf.terms` to false for high precision search tasks such as named-page finding. Since version 4.2, `ignore.low.idf.terms=false` is the default configuration, but may need to be set to true for some smaller test collections.

Bibliography
------------

1.  Probabilistic Models for Information Retrieval based on Divergence from Randomness. G. Amati. PhD Thesis, School of Computing Science, University of Glasgow, 2003.

2.  FUB, IASI-CNR and University of Tor Vergata at TREC 2007 Blog Track. G. Amati and E. Ambrosi and M. Bianchi and C. Gaibisso and G. Gambosi. Proceedings of the 16th Text REtrieval Conference (TREC-2007), 2008.

3.  Bridging Language Modeling and Divergence From Randomness Approaches: A Log-logistic Model for IR. Stephane Clinchant and Eric Gaussier. In Proceedings of ICTIR 2009, London, UK.

4.  Information-Based Models for Ad Hoc Information Retrieval. S. Clinchant and E. Gaussier. In Proceedings of SIGIR 2010, Geneva, Switzerland.

5.  A Markov Random Field Model for Term Dependencies. D. Metzler and W.B. Croft. Proceedings of the 28th annual international ACM SIGIR conference on Research and development in information retrieval (SIGIR 2005), 472-479, Salvador, Brazil, 2005.

6.  Incorporating Term Dependency in the DFR Framework. J. Peng, C. Macdonald, B. He, V. Plachouras and I. Ounis.

7.  University of Glasgow at WebCLEF 2005: Experiments in per-field normalisation and language specific stemming. C. Macdonald, V. Plachouras, B. He, C. Lioma and I. Ounis. In Working notes of the CLEF 2005 Workshop, Vienna, Austria, 2005.

8.  Multinomial Randomness Models for Retrieval with Document Fields. V. Plachouras and I. Ounis. Proceedings of the 29th European Conference on Information Retrieval (ECIR07). Rome, Italy, 2007.

------------------------------------------------------------------------

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
