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
In general, this follows a subset of the Indri query language. We have two types of operators, namely: *semantic*, in that they cause a posting list with particular semantics to be generated at matching time; in contrast, *syntactic* operators are syntactic sugar defined within the query language, which allow attributes of the semantic operators to be changed.

The semantic operators will be familiar to those who have used Indri or Galago:

 - `term1` -- scores documents containing this single query term.
 - `term1.title` -- scores documents containing this single query term in the title.
 - `#band(op1 op2)` -- scores a term containing both operators. The frequency of each matching document is 1.
 - `#syn(op1 op2)` -- scores documents containing either op1 or op2. The frequency of each matching document is the sum of the frequencies of the constituent words.
 - `#prefix(term1)` -- scores documents containing terms prefixed by term1.
 - `#fuzzy(term1)` -- scores documents containing terms that fuzzily match term1. See [FuzzyTermOp](javadoc/org/terrier/matching/matchops/FuzzyTermOp.html) for more information.
 - `#uw8(op1 op2)` -- the #uwN operator scores documents op1 or op2 within unordered windows of N tokens -- in this case windows of 8 tokens in size.
 - `#1(op1 op2)` -- the #1 operator scores documents op1 or op2 appearing adjacently.
 - `#band(op1 op2)` -- the #band operator scores documents that contain both op1 and op2. 
 - `#base64(term1)` -- allows a base64 representation of a query term to be expressed that is not directly compatible with the matchop ql.

There are currently two syntactic operators:

 - `#tag(tagName op1 op2)` -- this sets the tag attribute of these query terms.
 - `#combine:k=v(op1 op2)` -- the `#combine` operator allows several operators to be grouped together. Moreover, multiple key-value pairs can be specified to control those query terms. In particular, the (query term frequency) weight of a term can be controlled by setting the index of that operator -- for example `#combine:0=2:1=1(op1 op2)` will set twice as much weight on op1 as on op2. The weighting model (`wmodel`) and tag (`tag`) of the query term(s) can also be set, e.g. `#combine:tag=second:wmodel=PL2(op1 op2)`


Note that semantic operators cannot contain syntactic operators.
 
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

**Matching Op Query Language Specification**

Each top-level match operator must resolve to an expression that can be defined as posting list during Matching. The PostingListManager is responsible for opening the correct postings from the inverted index for each matching operator.

Some match operators may require a particular type of input posting. For instance, a `#uwN` operator requires that each input operator generates postings that implements BlockPosting (i.e. an index created with position information). Moreover, each match operator may return a different type of posting - for instance, the IterablePosting created by a `#uwN` operator does not return the position information, although that from a `#1` does.

|Operator|Class|Type|Input Posting Type|Output Posting Type|
|------|-------|----|-------|-----|
| term | SingleTermOp | - | Any | (as input postings) |
| term.FIELD | SingleTermOp | - | Fields (FieldPosting) | Frequency |
| #band| AndQueryOp | AND | Any | Binary (i.e. frequency=1) |
| #uwN | UnorderedWindowOp | AND | Positional (BlockPosting) | Frequency |
| #1   | PhraseOp | AND | Positional (BlockPosting) | Positions |
| #syn | SynonymOp | OR | Any | (depends on input postings) |
| #prefix | PrefixTermOp | OR | Any | (depends on input postings) |
| #prefix | FuzzyTermOp | OR | Any | (depends on input postings) |

On the other hand, the syntactic operators (such as `#combine` and `#tag`)  are defined solely in the matchop query parser, and hence there is no equivalent matchop class. As these cannot result in a single posting list, their positioning within a matchop is restricted. For instance, all of the following queries are **invalid**:

 - `#uw2( #combine(a b) )` -- a `#combine` cannot create a posting list, so it cannot appear within a `#uwN`.
 - `#uw2( #band(a b) c)` -- `#band` is a binary operator and does not supply positions. Hence, it cannot be used within a `#uwN`.
 - `#uw2( #uw5(a b) c)` -- the inner `#uwN` operator does not return position information, and hence is not valid input to the outer `#uwN` operator.


------------------
> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
