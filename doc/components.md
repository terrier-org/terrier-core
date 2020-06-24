# Terrier Additional Components

Terrier's ecosystem has a number of components: 

## PyTerrier

[PyTerrier](http://github.com/terrier-org/pyterrier/) are Python bindings for Terrier.

----

The following components can be used from Terrier or Pyterrier. In Terrier, using the `-P` commandline option to include the package. In Pyterrier, include the components in `pt.init(packages=[])` startup option.

## terrier-lucene
[Terrier-lucene](https://github.com/terrierteam/terrier-lucene) allows Terrier to read a Lucene index, including those created by Anserini.

## terrier-ciff
[Terrier-CIFF](https://github.com/terrierteam/terrier-ciff) allows Terrier to read Common Index Exchange Format index files.

## terrier-wapo
[Terrier-WAPO](https://github.com/terrierteam/terrier-wapo) allows Terrier to read the TREC WAPO test collection.

## terrier-prf
[Terrier-PRF](https://github.com/terrierteam/terrier-prf) provides additional query expansion classes, including RM1, RM3 and Axiomatic QE.

## terrier-ef
[Terrier-EF](https://github.com/tonellotto/terrier-ef) provides support for writing and reading Elias Fano indices in Terrier. Developed by Nicola Tonellotto.

## terrier-micro
[Terrier-micro](https://github.com/tonellotto/terrier-micro) provides implementations of MaxScore, WAND, BlockMaxWAND for Terrier. Developed by Nicola Tonellotto.

## Legacy

A number of legacy components are available - these are now unmaintained:
 - [terrier-desktop](https://github.com/terrier-org/terrier-desktop) - a Java Swing UI for searching files on your desktop (removed from terrier-platform)
 - [terrier-integer-compression](https://github.com/terrierteam/terrier-integer-compression) - allows to change Terriers indices to PForDelta compression etc. (removed from terrier-platform)
 - [terrier-website-search](https://github.com/terrierteam/terrier-website-search) - a website crawling application (removed from terrier-platform)


----------------------------------
> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2020 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
