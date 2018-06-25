# terrier-concurrent

This package is intended to allow Terrier's /standard/ index structures to be used by multiple retrieval threads concurrently. Use to speed up batch retrieval can be achieved by replacing TRECQuerying with ParallelTRECQuerying in any client code.

An index has its data structures made re-entrant (i.e. thread safe) by use of `ConcurrentIndexUtils.makeConcurrentForRetrieval(Index)`. 

An indexref prefixed as `concurrent:` will be filtered through ConcurrentIndexUtils when loaded. E.g. 

	IndexRef ref = IndexRef.of("concurrent:/path/to/my/index/data.properties")


