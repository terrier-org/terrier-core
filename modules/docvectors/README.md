# terrier-docvectors

This is an module contains an alternative to the Fat framework for calculating more query dependent features. 

Similar to fat's ([FatFeaturedScoringMatching](http://terrier.org/docs/current/javadoc/org/terrier/matching/FatFeaturedScoringMatching.html) class, there is a requirement of a list of features (usually features.list). 

## Recommended Usage

This class is available through PyTerrier, using  pt.FeaturesBatchRetrieve, where method is set to `'dv'`:
```python
retr = pt.FeaturesBatchRetrieve("./tests/fixtures/index/", ["WMODEL:PL2", "WMODEL:Tf"], wmodel="DPH", method='dv')
```

## Advanced Usage

This framework has an additional advantage in that it can use rewriters to make additional query formulations, e.g. using [QueryExpansion](http://terrier.org/docs/current/javadoc/org/terrier/querying/QueryExpansion.html) upon the candidate set of ranked documents.


However, DVFeaturedScoringMatching also has an extra file, a list of rewriters. Rewriters are Process classes that alter the query, and expected to extend [MQTRewritingProcess](http://terrier.org/docs/current/javadoc/org/terrier/querying/MQTRewritingProcess.html) (this includes the classical [QueryExpansion](http://terrier.org/docs/current/javadoc/org/terrier/querying/QueryExpansion.html) class). For example:

	qeBo1:QueryExpansion(DFRBagExpansionTerms,Bo1)
	qeKL:QueryExpansion(DFRBagExpansionTerms,KL)
	prox:DependenceModelPreProcess

Each of these create different query formulations, and tag the resulting query terms with the String before the colon (qeBo1, qeKL, etc). This allows a feature.list to be generated that targets these tagged query terms directly. For instance, if we write the following to etc/rewriters.list:

	SAMPLE
	WMODEL$qeBo1:BM25
	WMODEL$qeKL:BM25
	WMODEL$prox:org.terrier.matching.models.dependence.pBiL
	WMODEL$prox:org.terrier.matching.models.dependence.MRF

This feature definition file adds 4 additional features targetting the terms identified by the two query expansion and the proximity rewriter.

Retrieval can then occur as follows:

	bin/terrier br -Dtrec.matching=DVFeaturedScoringMatching,org.terrier.matching.daat.Full -Ddv.featured.scoring.matching.rewriters=FILE -Ddv.featured.scoring.matching.rewriters.file=./etc/rewriters.list -Dfat.featured.scoring.matching.features=FILE -Dfat.featured.scoring.matching.features.file=./etc/features.list

## Contributors

Written by Craig Macdonald
