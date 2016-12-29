Terrier Future Features and Known Issues
========================================

List of features and known issues that are marked for future Terrier versions:

Future Features
---------------

-   The addition of Terrier's own Exceptions for setup, indexing, querying.

Known Issues
------------

-   CompressionFactory/CompressionConfig is not supported by single-pass or Hadoop indexing.

-   Query language should be refined: In particular, ambiguity warnings when generating the parser with ANTLR should be removed, and a more structured query language should be introduced. We plan to use a subset of the Indril query language internally from version 5.0.

-   The real-time index structures do not currently support block indexing

Deprecations, Scheduled Refactorings
------------------------------------

The following classes and interfaces are/may be deprecated in this version of Terrier and will likely be removed or refactored in a future release:

-   structures.BlockLexiconEntry & structures.BlockFieldLexiconEntry will be removed in a future version of Terrier.

-   structures.bit.DirectIndex and structures.bit.InvertedIndex will be removed in a future version of Terrier.

-   [ApplicationSetup](javadoc/org/terrier/utility/ApplicationSetup.html) will be replaced with a new non-global configuration API.

-   Terrier currently uses Hadoop version 0.20. Future releases of Terrier will upgrade to a later branch. Instructions to upgrade Terrier yourself can be found [here](http://ir.dcs.gla.ac.uk/wiki/Terrier/UpgradingHadoop)

**Contributions**
All community contributions to the Terrier framework are welcome. In addition, you can find more information about contributing on the [Terrier website](http://terrier.org/).



------------------------
> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2016 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved. 
