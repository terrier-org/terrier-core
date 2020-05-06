# Terrier Additional Components

Terrier's ecosystem has a number of components: 

## PyTerrier

[PyTerrier](http://github.com/terrier-org/pyterrier/) are Python bindings for Terrier.


----

The following components can be used from Terrier or Pyterrier. In Terrier, using the `-P` commandline option to include the package. In Pyterrier, include the components in `pt.init(packages=[])` startup option.

## terrier-lucene
Terrier-lucene allows Terrier to read a Lucene index, including those created by Anserini.

## terrier-ciff
Terrier-CIFF allows Terrier to read Common Index Exchange Format index files.

## terrier-wapo
Terrier-WAPO allows Terrier to read the TREC WAPO test collection.

## terrier-prf
Terrier-PRF provides additional query expansion classes, including RM1, RM3 and Axiomatic QE.

## terrier-ef
Terrier-EF provides support for writing and reading Elias Fano indices in Terrier. Developed by Nicola Tonellotto.

## terrier-micro
Terrier-micro provides implementations of MaxScore, WAND, BlockMaxWAND for Terrier. Developed by Nicola Tonellotto.

----------------------------------
> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2020 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
