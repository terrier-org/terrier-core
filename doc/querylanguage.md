Query Language
==============

Terrier offers a flexible and powerful query language for searching with phrases, fields, or specifying that terms are required to appear in the retrieved documents. Some examples of queries are the following:

 - `term1 term2`  -- retrieves documents that contain term1 or term2 (they do not need to contain both of them).
 - `{term1 term2}` -- retrieves documents that contain term1 or term2, where they are treated as synonyms of each other (they do not need to contain both of them)
 - term1^2.3 -- the weight of term1 is multiplied by 2.3.
 - `+term1 +term2`  -- retrieves documents that contain both term1 and term2.
 - `+term1 -term2` -- retrieves documents that contain term1 and do not contain term2.
 - `title:term1` -- retrieves documents that contain term1 in the title field ([Field indexing](configure_indexing.html#fields) must be configured to record the title field). NB: The semantics of this query language will change in Terrier 5.0.
 - `title:(term1 term2)` -- retrieves documents that contain term1 or term2 in the title field. As above, field indexing must be enabled.
 - `term1 -title:term2` -- retrieves documents that contain term1, but must not contain term2 in the title field.
 - `"term1 term2"` -- retrieves documents where the terms term1 and term2 appear in a phrase.
 - `"term1 term2"~n` -- retrieves documents where the terms term1 and term2 appear within a distance of n blocks. The order of the terms is not considered.

Combinations of the different constructs are possible as well. For example, the query `term1 term2 -term1 term2` would retrieve all the documents that contain at least one of the terms term1 and term2, but not the documents where the phrase “term1 term2” appears.

Note that in some configurations, the Terrier query language may not be available by default. In particular, if batch processing queries from a file using a class that extends [TRECQuery](javadoc/org/terrier/applications/batchquerying/TRECQuery.html), then the queries are pre-processed by a tokeniser that may remove the query language characters (e.g. brackets and colons). To use the Terrier query language in this case, you should use [SingleLineTRECQuery](javadoc/org/terrier/applications/batchquerying/SingleLineTRECQuery.html) and set `SingleLineTRECQuery.tokenise` to false in the `terrier.properties` file.

------------------
> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2016 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
