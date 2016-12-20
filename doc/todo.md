<span>\[</span>[Previous: DFR Description](dfr_description.html)<span>\]</span> <span>\[</span>[Contents](index.html)<span>\]</span> <span>\[</span>[Next: Bibliography](bibliography.html)<span>\]</span>

Terrier Future Features and Known Issues
========================================

List of features and known issues that are marked for future Terrier versions:

Future Features
---------------

-   Consider making Terrier available upon a Maven repository.

-   The addition of Terrier’s own Exceptions for setup, indexing, querying.

Known Issues
------------

-   Building with Java 8 causes javadoc weirdness (compiles to wrong directory)

-   CompressionFactory/CompressionConfig is not supported by single-pass or Hadoop indexing.

-   Query language should be refined: In particular, ambiguity warnings when generating the parser with ANTLR should be removed, and a more structured query language should be introduced.

-   The real-time index structures do not currently support block indexing

Deprecations, Scheduled Refactorings
------------------------------------

The following classes and interfaces are/may be deprecated in this version of Terrier and will likely be removed or refactored in a future release:

-   BlockLexiconEntry & BlockFieldLexiconEntry will be removed in a future version of Terrier

-   [ApplicationSetup](http://www.terrier.org/docs/current/javadoc/org/terrier/utility/ApplicationSetup.html) will be replaced with a new non-global configuration API.

-   Terrier currently uses Hadoop version 0.20. Future releases of Terrier will upgrade to a later branch. Instructions to upgrade Terrier yourself can be found [here](http://ir.dcs.gla.ac.uk/wiki/Terrier/UpgradingHadoop)

**Contributions**
All community contributions to the Terrier framework are welcome. In addition, you can find more information about contributing on the [Terrier website](http://terrier.org/).

<span>\[</span>[Previous: DFR Description](dfr_description.html)<span>\]</span> <span>\[</span>[Contents](index.html)<span>\]</span> <span>\[</span>[Next: Bibliography](bibliography.html)<span>\]</span>

------------------------------------------------------------------------

Webpage: <http://terrier.org>
Contact: [](mailto:terrier@dcs.gla.ac.uk)
[School of Computing Science](http://www.dcs.gla.ac.uk/)
Copyright (C) 2004-2015 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
