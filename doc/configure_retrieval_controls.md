# Configuring Retrieval - Controls

Controls are methods for configuring retrieval. Controls are set using the setControl() method in SearchRequest, and accessible to retrieval code via the Request object. They are re-read for every query, therefore, they allow the configuration of the search engine to be adjusted *on a per-query basis*.

## Manager

| Control Name   | Default  | Description                                                |
|----------------|----------|------------------------------------------------------------|
| `start`        |    0     | Offset of first document to be retrieved, 0-based          | 
| `end`          |    unset | Offset of last document to be retrieved, 0-based           | 
| `wmodel`       |    DPH   | Name of the weighting model to use during retrieval        | 
| `terrierql`    | on       | Asks the Manager to parse an end-user query                |
| `parseql`      | on       | Asks the Manager to map a parsed end-user query into MatchOp format |
| `parsecontrols`| on       | Asks the Manager to remove any controls from the user's query |
| `matchopql`    | off      | Asks the Manager to parse a MatchOp formatted query. When used, `terrierql`, `parseql` should be set to off |
| `applypipeline`| on       | Asks the Manager to apply the term pipeline to the query   |
| `localmatching`| on       | Controls if matching should be applied to the query        |
| `matching`     | org.terrier.matching.daat.Full | Name of the matching class to run    |
| `decorate`     | on       | Controls if decoration should occur, i.e. decorating the ResultSet with metadata |
| `qe`           | off      | Controls if query expansion should be applied              |
| `filters`      | on       | Controls if any post-filters should be applied for the query |
| `site`         | off      | Performs hostname suffix matching as a PostFilter, like on web search engines. Requires the ResultSet to be decorated with "url" metadata |


## Query Expansion

QueryExpansion occurs when the `qe` control is set to on. From the batchretrieve commandline, this can be achieved using `-q` or `-c qe:on` arguments.

| Control Name | Default  | Description                                               |
|--------------|----------|-----------------------------------------------------------|
| `qemodel`    |  Bo1     | Name of the query expansion model                         |
| `qe_fb_terms`|  10 (obtained from property) | Number of expansion terms to add      |
| `qe_fb_docs` |  3 (obtained from property) | Number of pseudo-relevance feedback documents to analyse   |

------------------------------------------------------------------------

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2020 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
