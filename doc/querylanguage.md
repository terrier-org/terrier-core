Query Language
==============

Terrier offers two query languages - a high-level, user facing query language, and a low-level query language for developers which is expressed in terms of matching operations (matching ops). All user queries are rewritten down into matching operations. The matching op query language borrows from the Indri and Galago query languages.

User Query Language
-------------------

Terrier offers a user flexible query language for searching with phrases, fields, or specifying that terms are required to appear in the retrieved documents.

Some examples of Terrier's query language are the following:

 - `term1 term2`  -- retrieves documents that contain term1 or term2 (they do not need to contain both of them).
 - `{term1 term2}` -- retrieves documents that contain term1 or term2, where they are treated as synonyms of each other (they do not need to contain both of them)
 - `term1^2.3` -- the weight of term1 is multiplied by 2.3.
 - `+term1 +term2`  -- retrieves documents that contain both term1 and term2.
 - `+term1 -term2` -- retrieves documents that contain term1 and do not contain term2.
 - `title:term1` -- retrieves documents that contain term1 in the title field ([Field indexing](configure_indexing.html#fields) must be configured to record the title field). NB: The semantics of this query language changed in Terrier 5.0.
 - `+title:term1` -- retrieves documents that contain term1 in the title field. As above, field indexing must be enabled.
 - `title:(term1 term2)^2.3` -- retrieves documents that contain term1 or term2 in the title field, and boost their weights by 2.3. Field indexing must be enabled.
 - `term1 -title:term2` -- retrieves documents that contain term1, but must not contain term2 in the title field.
 - `"term1 term2"` -- retrieves documents where the terms term1 and term2 appear in a phrase. Terrier considers this equivalent to `+"term1 term2"`, i.e. that a positive requirement is expressed.
 - `"term1 term2"~n` -- retrieves documents where the terms term1 and term2 appear within a distance of n blocks. The order of the terms is not considered.

Combinations of the different constructs are possible as well. For example, the query `term1 term2 -"term1 term2"` would retrieve all the documents that contain at least one of the terms term1 and term2, but not the documents where the phrase "term1 term2" appears.

Note that in some configurations, the Terrier query language may not be available by default. In particular, if batch processing queries from a file using a class that extends [TRECQuery](javadoc/org/terrier/applications/batchquerying/TRECQuery.html), then the queries are pre-processed by a tokeniser that may remove the query language characters (e.g. brackets and colons). To use the Terrier query language in this case, you should use [SingleLineTRECQuery](javadoc/org/terrier/applications/batchquerying/SingleLineTRECQuery.html) and set `SingleLineTRECQuery.tokenise` to false in the `terrier.properties` file.

Matching Op Query Language
--------------------------
In general, this follows a subset of the Indri query language:

 - `term1` -- scores documents containing this single query term.
 - `term1.title` -- scores documents containing this single query term in the title.
 - `#band(term1 term2)` -- scores a term containing both query terms. The frequency of each matching document is 1.
 - `#syn(term1 term2) -- scores documents containing either term1 or term2. The frequency of each matching document is the sum of the frequencies of the constituent words.
 - `#uw8(term1 term2)` -- the #uwN operator scores documents term1 or term2 within unordered windows of N tokens -- in this case windows of 8 tokens in size.
 - `#1(term1 term2)` -- the #1 operator scores documents term1 or term2 appearing adjacently.
 
 
**Using the Matching Op Query Language**

You can use the matchingop query language in `interactive` querying command by passing the `-m` option. The prompt will be `matchop query>`, as shown in the exxample below:

```
$ bin/terrier interactive -m
Setting TERRIER_HOME to /home/Terrier
23:33:14.496 [main] INFO  o.t.structures.CompressingMetaIndex - Structure meta reading lookup file into memory
23:33:14.503 [main] INFO  o.t.structures.CompressingMetaIndex - Structure meta loading data file into memory
matchop query> #combine:0=0.85:1=0.15:2=0.05(#combine(dramatise personae) #1(dramatise personae) #uw8(dramatise personae))
etc
```

Similarly, `batchretrieve` command also takes a `-m` option, whereby the queries will be assumed to be in matchingop query language. 
```
$ cat mytopics
1 terrier #1(information retrieval)
2 systems
$ bin/terrier batchretrieve -s -m -t mytopics
```
where `-m` defines that matchingop query language will be used, and `-s` defines that topics are in single-line format.
 

------------------
> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2018 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
