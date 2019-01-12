Learning to Rank with Terrier
=============================

Since version 4.0, Terrier supports the deployment of many retrieval features, and integration with learning to rank techniques. This page explains how to configure Terrier to enable learning and the application of a learned model. A worked example using the TREC .GOV corpus is also provided below.

Introduction
------------

Learning to rank is the ability to (a) use multiple features in a uniform way during ranking, and (b) to learn an appropriate method to combine those features. For learning, Terrier has the ability to calculate multiple features, be they query-dependent (c.f. multiple weighting models, such as BM25, PL2 etc), or be they query-independent (c.f. Document Length).

If you wish to use Terrier with a learning to rank technique, you must generate a LETOR-formatted file – which can be done using Terrier's Normalised2LETOROutputFormat class – to provide to a learning to rank technique such as Jforests. A LETOR formatted file looks as follows:

    #1: featureName
    #2: featureName
    0 qid:1 1:2.9 2:9.4 # docid=clueweb09-00-01492

At the top is an optional comment header giving the names of the features. Then, each line has a "label", i.e. derived from relevance assessments such as TREC qrel files, the query Id, and featureId:featureValue pairs. Finally, each line can have the docno.

Fat Component
-------------

What is "Fat" about? Fat is a method for allowing many features to be computed within one run of Terrier. In particular, computing every feature for every posting of every query term is very expensive, and in practice, unnecessary. Instead, Liu [1] suggests ranking a *sample* of documents using a simple weighting model (e.g. BM25) is sufficient, before computing the features on the documents in the sample.

However, once the sample has been identified, the posting lists have been iterated, and it is no longer possible to compute the other weighting model features. The Fat component [2] addresses this problem, by storing copies of the postings for every document that makes the top k retrieved documents. These can then be later used to calculate other features.

**NB:** If you use the Fat component in your research, you should cite [2].

Moreover, in addition to the deployed features, the number of documents to retrieve in the sample is a key parameter - for a study of this parameter in Web search, see [3].

In the following, we firstly define the classes related to Fat within the learning component of Terrier, before showing how they can be combined in pipelines for various applications.

Fat Classes
-----------

-   `daat.FatFull`: A DAAT exhaustive matching strategy based on `daat.Full`, however the postings for each document that enters the CandidateResultSet has its postings stored. In particular, the Posting.asWritablePosting() method is used to obtain a copy of a given posting, "breaking it out" of its IterablePosting iterator. Returns a FatResultSet.

-   `FatResultSet`: A ResultSet which is fat, i.e. it has copies of all necessary statistics to compute other weighting models on the documents it contains. These statistics include the lengths of documents, the EntryStatistics of each term, the CollectionStatistics of the index, etc. There are various implementations: FatQueryResultSet, and FatCandidateResultSet. All FatResultSets are Writable, and hence can be serialized to disk for later use.

-   `FatScoringMatching`: Takes a FatResultSet obtained from a parent Matching class, and computes new scores based on a pre-determined weighting model.

-   `FatRescoringMatching`: Takes a FatResultSet obtained from a parent Matching class and re-scores the documents contained within it based upon a predetermined weighting model. This differs from FatScoringMatching in that it returns the original FatResultSet rather than a new QueryResultSet.

-   `FatFeaturedScoringMatching`: Permits many features to be calculated using a FatResultSet. In particular, takes a FatResultSet, and returns a FeaturedResultSet, where each document has calculated of a predefined number of features. Features can be one of three types:

    1.  a query-dependent weighting model (denoted by a WMODEL prefix, and actually computed using FatScoringMatching) calculated for all query terms in the query. More advanced formulations of WMODEL are discussed in the (separate advanced guide)[learning_advanced.md].

    2.  a query-independent weighting model (typically a feature) loaded by [StaticFeature](javadoc/org/terrier/matching/models/StaticFeature.html) (NB: Terrier supports the loading of query independent features from a [variety of input file formats](javadoc/org/terrier/matching/models/StaticScoreModifierWeightingModel.html), however no methods of generating such features are provided out-of-the-box.) and

    3.  the scores from a document score modifier.

    The names of features can be specified on a property, or read from a file, `etc/features.list`. E.g.

```
WMODEL:BM25
WMODEL:PL2
QI:StaticFeature(OIS,/home/terrier4/var/results/data.inlinks.oos.gz)
DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier
```

-   `WritableOutputFormat` and `FatResultsMatching`: these classes permit FatResultSets to be written to file and read back in again, for the purposes of faster experimentation.

Example Using .GOV corpus - Named Page Retrieval
------------------------------------------------

In the following, we give an example of effective retrieval using learning to rank, using the topics and qrels from the named page tasks of the TREC 2002-2004 Web tracks. Other possible invocations of the Fat framework are listed at the bottom of the page. Firstly, we setup the Terrier environment. We use simple variables in the form of a Unix Bash shell script, but this could be easily ported to a Windows environment.

    #available from http://ir.dcs.gla.ac.uk/test_collections/access_to_data.html
    CORPUS=/extra/Collections/TREC/GOV/

    #available from http://trec.nist.gov/data/webmain.html
    TR_TOPICS=/extra/TopicsQrels/TREC/GOV/namedpage/TREC2002/webnamed_page_topics.1-150.txt
    VA_TOPICS=/extra/TopicsQrels/TREC/GOV/namedpage/TREC2003/topics.NP151-NP450.np
    TE_TOPICS=/extra/TopicsQrels/TREC/GOV/namedpage/TREC2004/topics.WT04-1-WT04-225.np

    TR_QRELS=/extra/TopicsQrels/TREC/GOV/namedpage/TREC2002/qrels.named-page.txt
    VA_QRELS=/extra/TopicsQrels/TREC/GOV/namedpage/TREC2003/qrels.NP151-NP450.np
    TE_QRELS=/extra/TopicsQrels/TREC/GOV/namedpage/TREC2004/qrels.WT04-1-WT04-225.np

In each of the following, we provide the exact commands to be copied & pasted into a terminal.

Firstly, we setup Terrier. This also generates configuration files for learning to rank, namely `features.list` and `jforests.properties` in the `etc/` folder.

```shell
    bin/trec_setup.sh $CORPUS
```

Next, we need to create an index, with fields and blocks enabled. For brevity, we set the appropriate properties on the command line:

```
    bin/terrier batchindexing -j -b -DFieldTags.process=TITLE,ELSE

    Setting TERRIER_HOME to /home/terrier
    INFO - TRECCollection read collection specification (4613 files)
    INFO - Processing /extra/Collections/TREC/DOTGOV/G00/02.gz
    INFO - Indexer using 2 fields
    Starting building the inverted file (with blocks)...
    ...
    INFO - Collection #0 took 3835 seconds to build the runs for 1247753 documents
    INFO - Merging 2 runs...
    INFO - Collection #0 took 151 seconds to merge
    INFO - Collection #0 total time 3986
    INFO - Optimising structure lexicon
    INFO - lexicon has 2 fields
    INFO - Optimising lexicon with 2759934 entries
    INFO - All ids for structure lexicon are aligned, skipping .fsomapid file
    Finished building the inverted index...
    Time elapsed for inverted file: 3989.925
    Time elapsed: 3990.343 seconds.
```

Next, we wish to configure retrieval. We will use the Fat framework to retrieve 1000 documents using the DPH weighting model, and then calculate several additional query dependent and query independent features. Let's edit the file `etc/features.list`, to set the list of features we will use (lines starting with `#` are comments):

```
#BM25 calculated on each field.
WMODEL:SingleFieldModel(BM25,0)
WMODEL:SingleFieldModel(BM25,1)
#title and body length features (Dl means length)
QI:SingleFieldModel(Dl,0)
QI:SingleFieldModel(Dl,1)
#proximity features
DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier
DSM:org.terrier.matching.dsms.MRFDependenceScoreModifier
```

Next, we want to retrieve results for the training topics. In doing so, we are going to be identifying our candidate documents, and then calculating multiple features for each document (as listed in the `etc/features.list` file), so we use a series of Matching classes: [FatFull](javadoc/org/terrier/matching/daat/FatFull.html) with DPH to make a [FatResultSet](javadoc/org/terrier/matching/FatResultSet.html) (i.e. a ResultSet scored by DPH, but with extra posting information), and [FatFeaturedScoringMatching](javadoc/org/terrier/matching/FatFeaturedScoringMatching.html) to add the additional features, and return a FeaturedResultSet. We then add the document relevance labels from the qrels using LabelDecorator, and write the results in a LETOR-compatible results file using Normalised2LETOROutputFormat:

```
    bin/terrier batchretrieval -t $TR_TOPICS -w DPH -c labels:on -F Normalised2LETOROutputFormat -o tr.letor -Dtrec.matching=FatFeaturedScoringMatching,org.terrier.matching.daat.FatFull -Dfat.featured.scoring.matching.features=FILE -Dfat.featured.scoring.matching.features.file=$PWD/etc/features.list -Dlearning.labels.file=$TR_QRELS  -Dproximity.dependency.type=SD


    Setting TERRIER_HOME to /home/terrier
    INFO - Structure meta reading lookup file into memory
    INFO - Structure meta loading data file into memory
    INFO - time to intialise index : 1.113
    INFO - NP1 : america s century farms
    INFO - Processing query: NP1: 'america s century farms'
    INFO - Query NP1 with 3 terms has 3 posting lists
    term america ks=1.0 es=term339011 Nt=94640 TF=229637 @{0 500765856 0} TFf=3801,225836
    term centuri ks=1.0 es=term598109 Nt=39494 TF=81231 @{0 662970754 0} TFf=699,80532
    term farm ks=1.0 es=term983771 Nt=40908 TF=178987 @{0 1009035982 2} TFf=2488,176499
    Term: america qtw=1.0 es=term339011 Nt=94640 TF=229637 @{0 500765856 0} TFf=3801,225836
    Term: centuri qtw=1.0 es=term598109 Nt=39494 TF=81231 @{0 662970754 0} TFf=699,80532
    Term: farm qtw=1.0 es=term983771 Nt=40908 TF=178987 @{0 1009035982 2} TFf=2488,176499
    INFO - Rescoring found 222 docs with +ve score using SingleFieldModel(BM25,0)
    Term: america qtw=1.0 es=term339011 Nt=94640 TF=229637 @{0 500765856 0} TFf=3801,225836
    Term: centuri qtw=1.0 es=term598109 Nt=39494 TF=81231 @{0 662970754 0} TFf=699,80532
    Term: farm qtw=1.0 es=term983771 Nt=40908 TF=178987 @{0 1009035982 2} TFf=2488,176499
    INFO - Rescoring found 1000 docs with +ve score using SingleFieldModel(BM25,1)
    ngramC=1.0
    read: term0 Nt=94640 TF=229637 @{0 500765856 0} TFf=3801,225836 => 0
    read: term1 Nt=39494 TF=81231 @{0 662970754 0} TFf=699,80532 => 1
    read: term2 Nt=40908 TF=178987 @{0 1009035982 2} TFf=2488,176499 => 2
    INFO - Query NP1 with 3 terms has 3 posting lists
    phrase term: america
    phrase term: centuri
    phrase term: farm
    DFRDependenceScoreModifier altered scores for 1000 documents
    read: term0 Nt=94640 TF=229637 @{0 500765856 0} TFf=3801,225836 => 0
    read: term1 Nt=39494 TF=81231 @{0 662970754 0} TFf=699,80532 => 1
    read: term2 Nt=40908 TF=178987 @{0 1009035982 2} TFf=2488,176499 => 2
    INFO - Query NP1 with 3 terms has 3 posting lists
    phrase term: america
    phrase term: centuri
    phrase term: farm
    MRFDependenceScoreModifier altered scores for 1000 documents
    INFO - Applying LabelDecorator
    INFO - Writing results to /home/terrier/var/results/tr.letor
    ...
    INFO - Settings of Terrier written to /home/terrier/var/results/tr.letor.settings
    INFO - Finished topics, executed 150 queries in 361.379 seconds, results written to /home/terrier/var/results/tr.letor
```

Lets a have a look at what was output into `tr.letor`:

```
    # 1:score
    # 2:WMODEL:SingleFieldModel(BM25,0)
    # 3:WMODEL:SingleFieldModel(BM25,1)
    # 4:QI:SingleFieldModel(Dl,0)
    # 5:QI:SingleFieldModel(Dl,1)
    # 6:DSM:org.terrier.matching.dsms.DFRDependenceScoreModifier
    # 7:DSM:org.terrier.matching.dsms.MRFDependenceScoreModifier
    1 qid:NP1 1:1.0 2:0.8508957593838526 3:1.0 4:0.38888888888888884 5:0.03218193520703712 6:0.9170723523167136 7:0.9586800382580136 #docid = 1241 docno = G00-65-2264297
    1 qid:NP1 1:0.9020171593497216 2:0.8508957593838526 3:0.9169226938397537 4:0.38888888888888884 5:0.022634627762282773 6:1.0 7:1.0 #docid = 11394 docno = G00-04-3805407
    0 qid:NP1 1:0.6440567566141826 2:0.0 3:0.85986678612829 4:0.0 5:0.22902810555674746 6:0.21370705023195802 7:0.3992684087689166 #docid = 921940 docno = G34-15-0261249
```

The first 7 lines is a header of comments which contains the name of each of the features. For instance, "score" denotes that the first feature is the weighting model scores that were used to generate the sample -- i.e. the first pass retrieval, in our case DPH. After the header, for each retrieved document for each query, there is a single line in the output. The label obtained from the qrels file is the first entry on each row. The remainder are featureId:featureValue pairs. The docno and docids are denoted after a comment mark.

We repeat the retrieval step for the validation queries, this time from the 2003 TREC task:

```
    bin/terrier batchretrieval -t $VA_TOPICS -w DPH -c labels:on -o va.letor -F Normalised2LETOROutputFormat -Dtrec.matching=FatFeaturedScoringMatching,org.terrier.matching.daat.FatFull -Dfat.featured.scoring.matching.features=FILE -Dfat.featured.scoring.matching.features.file=$PWD/etc/features.list  -Dlearning.labels.file=$VA_QRELS  -Dproximity.dependency.type=SD
```

To obtain a learned model, we use the [Jforests learning to rank technique](https://github.com/yasserg/jforests/), which is included with Terrier. In particular, we use Jforests data preparation command to prepare the LETOR formatted results files, then learn a LambdaMART learned model. These use Jforests configuration own configuration file `etc/jforests.properties` -- in Terrier this is provided automatically by TRECSetup.

```
    bin/terrier jforests --config-file etc/jforests.properties --cmd=generate-bin --ranking --folder var/results/ --file tr.letor  --file va.letor

    bin/terrier jforests --config-file etc/jforests.properties --cmd=train --ranking --folder var/results/ --train-file var/results/tr.bin --validation-file var/results/va.bin --output-model ensemble.txt
```

Once the learned model (from Jforests, this is an XML file which takes the form of a gradient boosted regression tree) is obtained in `ensemble.txt`, we can use this to apply the learned model. The configuration for Terrier is similar to retrieval for the training topics, but we additionally use [JforestsModelMatching](javadoc/org/terrier/matching/JforestsModelMatching.html) for the application of the learned model, and to output the final results using the default, trec\_eval compatible [TRECDocnoOutputFormat](javadoc/org/terrier/structures/outputformat/TRECDocnoOutputFormat.html):

```
    bin/terrier batchretrieval -w DPH -t $TE_TOPICS -o te.res -Dtrec.matching=JforestsModelMatching,FatFeaturedScoringMatching,org.terrier.matching.daat.FatFull -Dfat.featured.scoring.matching.features=FILE -Dfat.featured.scoring.matching.features.file=$PWD/etc/features.list -Dfat.matching.learned.jforest.model=$PWD/ensemble.txt -Dfat.matching.learned.jforest.statistics=$PWD/var/results/jforests-feature-stats.txt -Dproximity.dependency.type=SD
```


Finally, for comparison, we additionally make a simple DPH run:

```
    bin/terrier batchretrieval -w DPH -t $TE_TOPICS
```

On evaluating the two runs using trec\_eval for Mean Reciprocal Rank, we note a marked increase in effectiveness, despite the deployment of no Web-specific features (such as anchor text, URL or link analysis features).

```
    bin/terrier trec_eval -m recip_rank $TE_QRELS var/results/DPH_0.res
    recip_rank              all 0.4447
    bin/terrier trec_eval -m recip_rank $TE_QRELS var/results/te.res
    recip_rank              all 0.5201
```

Other possible usages
---------------------

In the following, we give typical configurations for using the learning/Fat components of Terrier.

### From inverted index -> LETOR file with many features

```
    bin/terrier batchretrieval -Dtrec.matching=FatFeaturedScoringMatching,org.terrier.matching.daat.FatFull -Dfat.featured.scoring.matching.features=FILE -Dfat.featured.scoring.matching.features.file=/path/to/list.features -F Normalised2LETOROutputFormat
```

### From inverted index -> Fat result file -> LETOR file with many features

You can save intermediate FatResultSets, so that you can go back and compute different sets of features without retrieval from the inverted index.

```
    bin/terrier batchretrieval -Dtrec.matching=org.terrier.matching.daat.FatFull -F WritableOutputFormat

    bin/terrier batchretrieval -Dtrec.matching=FatFeaturedScoringMatching,FatResultsMatching -Dfat.results.matching.file=bla.fat.res.gz  -Dfat.featured.scoring.matching.features=FILE -Dfat.featured.scoring.matching.features.file=/path/to/list.features -F Normalised2LETOROutputFormat
```

### From inverted index -> Final Ranking having applied learned model to documents

```
    bin/terrier batchretrieval -Dtrec.matching=JforestsModelMatching,FatFeaturedScoringMatching,org.terrier.matching.daat.FatFull -Dfat.featured.scoring.matching.features=FILE -Dfat.featured.scoring.matching.features=$PWD/list.features -Dfat.matching.learned.jforest.model=/path/to/jforest.model
```

References
----------

1.  Tie-Yan Lui. Learning to Rank for Information Retrieval. Foundations & Trends in Information Retrieval. 3(3):225-331, 2009.

2.  Craig Macdonald, Rodrygo L.T. Santos, Iadh Ounis and Ben He. About Learning Models with Multiple Query Dependent Features. Transactions on Information Systems. 31(3):1-39. 2013.

3.  Craig Macdonald, Rodrygo L.T. Santos and Iadh Ounis. The Whens and Hows of Learning to Rank. Information Retrieval 16(5):584-628. 2012.



------------------------------------------------------------------------

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
