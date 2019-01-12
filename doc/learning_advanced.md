Advanced Learning to Rank using Tagged Query Terms - Experimental
=================================================================

Terrier's MatchingOp query language was introduced in Terrier 5.0, and permits additional flexibility in defining features for learning to rank. In particular, along with complex query operators such as #syn #1 and #uwN, Terrier's query language also allows to #tag terms.  

In particular, lets consider a query `term1 term2`. If we use rewrite the query to include dependency query terms using [DependenceModelPreProcess](javadoc/org/terrier/querying/DependenceModelPreProcess.html), the query will be rewritten to include #1 and #uw8 operators, as follows:

	#combine:0=0.85:1=0.15:2=0.05( #combine(term1 term2) #1(term1 term2) #uw8(term1 term2)). 
	
In addition, each of the query terms will be *tagged* with a String. All terms tagged with `firstmatchscore` will be scored in the first pass retrieval. Moreover, DependenceModelPreProcess will add the tag `sdm` to the #1 and #uw8 operators. Therefore the actual query can be represented as:

	#combine:0=0.85:1=0.15:2=0.05:tag=firstmatchscore( #combine(term1 term2) #combine:tag=sdm(#1(term1 term2))  #combine:tag=sdm(#uw8(term1 term2)))

These tags make it easy to identify groups of query terms to use as additional features during learning to rank. For instance, consider the following example `feature.list`:

	WMODELt:PL2
	WMODELp1:pBiL
	WMODELuw8:pBiL
	WMODEL$sdm:pBiL2

In constructing features using such a feature list, the following features would be calculated:

1. `WMODELt:PL2` would calculate PL2 only for plain query terms (e.g. `term1` and `term2`);
2. `WMODELp1:pBiL` and `WMODELuw8:pBiL` would calculate the pBiL weighting model for only the #1 and #uw8 query operators, respectively;
3. `WMODEL$sdm:pBiL2` would the pBiL2 weighting model only for the terms tagged with `sdm`.

We believe that this advanced functionality allows very expressive feature definitions. Note that any query term expressed in a feature will need to be matched in the first retrieval phase. If you do not wish such terms to be scored in the first phase, you are advised to set the weighting model to [Null](javadoc/org/terrier/matching/models/Null), which will prevent it affecting the retrieved document set.

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
