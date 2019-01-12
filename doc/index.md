Documentation for Terrier v5.0
=============================

Welcome to the documentation for the Terrier IR platform v5.0. If you are a new user, we recommend that you begin with a quickstart guide from those listed below. The quickstart guides will introduce you to core concepts when using Terrier within different scenarios. If you are looking to find out about a particular function or component of Terrier, scroll down this page to the main Table of Contents.

Quickstart Guides
-----------------------

> ### [Running Batch IR Experiments with Terrier](quickstart_experiments.md)
>This quickstart guide is designed for information retrieval students and researchers looking to use Terrier to experiment with or learn about some aspect of a search engine. The main learning outcomes are: how to download and install a local copy of the Terrier platform; how to produce an on-disk index from a collection of documents; and how to issue single queries as well as batches of queries over that index from the command line.   

> ### [Integrating Terrier as a Search Engine into your Java Application](quickstart-integratedsearchdisk.md)
> This quickstart guide is for software developers that want to use Terrier as a search engine within their own application. The guide covers how to import Terrier as an application dependancy using Maven, how to create an index, how to index files within your java program, and how to issue queries to the index. A [variant of the quickstart](quickstart-integratedsearch.md) shows the same using exclusively in-memory data structures.


Table of Contents
----------------------------

### Platform Information

> [Overview](overview.md)  
> An overview of what the Terrier platform is, and what it can be used for.

>[What's New](whats_new.md)  
>What has changed in the Terrier platform in the recent releases.

>[Terrier Components](basicComponents.md)  
>An overview of the main components of Terrier.

>[Query Language](querylanguage.md)  
>A description of the query language that Terrier supports.

>[Future Features & Known Issues](todo.md)  
>Upcoming features in future releases.

### Quickstart Guides

>[Running Batch IR Experiments with Terrier](quickstart_experiments.md)
>A quickstart guide is designed for information retrieval students and researchers.

>[Integrating Terrier as a Search Engine into your Java Application with a persistent index](quickstart-integratedsearchdisk.md)  
>[Integrating Terrier as a Search Engine into your Java Application, with a memory index](quickstart-integratedsearch.md)  
>Quickstart guides for software developers.

### Common Configuration Options

>[Configuring Terrier](configure_general.md)  
>A brief introduction to the configuration of Terrier

>[Configuring Indexing](configure_indexing.md)  
>A guide of indexing, and how it can be configured to your needs.

>[Configuring Retrieval](configure_retrieval.md)  
>A guide of the retrieval functionalities, covering frequently-used retrieval methodologies, such as TF-IDF, Okapi’s BM25, language models (Hiemstra and Ponte & Croft) and weighting models from the probabilistic Divergence From Randomness (DFR), as well as query expansion (pseudo-relevance feedback).

>[Configuring Real-time Index Structures](realtime_indices.md)  
>An introduction to the real-time index structures in Terrier.

### Advanced Functionality

>[Learning to Rank with Terrier](learning.md)  
>A guide to using multiple retrieval features with learning to rank techniques to enhance search effectiveness.

>[Advanced Learning to Rank using Tagged Query Terms](learning_advanced.md)  
>A discussion of how query terms can be tagged to allow different retrieval features.

>[Pluggable Compression](compression.md)  
>A guide to configuring byte-level compression schemes to reduce the size of Terrier’s index structures.

>[Non English language support](languages.md)  
>Description of support functionalities in Terrier for indexing and retrieving from documents written in languages other than English.

### Search Applications

>[Web-based Terrier](terrier_http.md)  
>A guide to using the Web-based application of Terrier.

>[Website Search Application](website_search.md)  
>A guide to using the website search application, which illustrates real-time crawling, indexing and retrieval functionalities in Terrier.

>[Desktop Search](terrier_desktop.md)  
>A summary of the Desktop Search application of Terrier available from Github.

### Experiment Support

>[TREC Experiment Examples](trec_examples.md)  
>An example of how to create an index and produce a TREC run on the WT2G and Blogs06 collections.

>[Evaluation of Experiments](evaluation.md)  
>Shows how the results of experiments can be evaluated using the in-built evaluation package in Terrier.

### Extending Terrier

>[Developing with Terrier](terrier_develop.md)  
>Introduction to developing applications using Terrier.

>[Extending Indexing](extend_indexing.md)  
>In depth guide about extending indexing

>[Indexer Details](indexer_details.md)
>More information about the roles of various classes in the indexing process.

>[Extending Retrieval](extend_retrieval.md)  
>In depth guide about retrieval, and how various retrieval functionalities can be integrated into Terrier, as well as, how you can use Terrier to obtain various statistics about the terms and the collection.

### Other Resources

>[Terrier API Javadoc](javadoc/index.html)  
>API documentation of each class in Terrier.

>[Description of DFR](dfr_description.md)  
>Description of the Divergence From Randomness framework that Terrier implements.

>[Terrier Forum](http://terrier.org/forum/)  
>The Terrier discussion forum is for developers and users of the Terrier platform to discuss the software, ask questions, post patches and share tips.

>[Terrier Wiki](http://ir.dcs.gla.ac.uk/wiki/Terrier)  
>Hints and tips, and configurations for various well-known corpora.

>[Bibliography](bibliography.md)  
>If you use Terrier in your research, please cite us!

>[Contacts](contacts.md)  
>Terrier Contacts

------------------------------------------------------------------------

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
