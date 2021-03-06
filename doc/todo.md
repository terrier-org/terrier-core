Terrier Future Features and Known Issues
========================================

List of features and known issues that are marked for future Terrier versions:

Future Features
---------------

-   Better exception handling. Some retrieval errors should result in a query being aborted, rather than trying to blindly continue running the query with the index is an invalid state.
-   See also out [Github issue tracker](https://github.com/terrier-org/terrier-core/issues)

Known Issues
------------

-   The real-time index structures do not currently support block indexing
-   The real-time fields index cannot write direct index structures

Deprecations, Scheduled Refactorings
------------------------------------

The following classes and interfaces are/may be deprecated in this version of Terrier and will likely be removed or refactored in a future release:

-   [ApplicationSetup](http://terrier.org/docs/v5.2/javadoc/org/terrier/utility/ApplicationSetup.html) will be replaced with a new non-global configuration API.

**Contributions**
All community contributions to the Terrier framework are welcome. In addition, you can find more information about contributing on the [Terrier website](http://terrier.org/).



------------------------
> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2020 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved. 
