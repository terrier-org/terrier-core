Non English language support in Terrier
=======================================

Indexing
--------

Terrier internally represents all terms as UTF. All provided Document classes use [Tokeniser](javadoc/org/terrier/indexing/tokenisation/Tokeniser.html) classes to tokenise text into terms during indexing. Likewise, during retrieval, [TRECQuery](javadoc/org/terrier/structures/TRECQuery.html) uses the same tokeniser to parse queries (note that, different from TRECQuery, [SingleLineTRECQuery](javadoc/org/terrier/structures/SingleLineTRECQuery.html) does not perform any tokenisation by default). To change the tokeniser being used for indexing and retrieval, set the `tokeniser` property to the name of the tokeniser you wish to use (NB: British English spelling). The default Tokeniser is [EnglishTokeniser](javadoc/org/terrier/indexing/tokenisation/EnglishTokeniser.html).

File Encodings
--------------

While Terrier uses UTF internally to represent terms, the Collection and Document classes need to ensure that they are correctly opening files using the correct character encodings. For instance, while valid XML files will specify the encoding at the top of the file, a corpus of Hungarian in TREC format may be encoded in ISO 8859-16 or UTF-8. You should specify the encoding that TRECCollection should use to open the files, using the `trec.encoding` property. Note that Terrier will default to the Java default encoding if `trec.encoding` is not set. In a Unix-like operating system, Java's choice of default encoding may be influenced by the LANG environment variable - e.g. LANG=en\_US.UTF-8 will cause Java to default to opening files using UTF-8 encoding, while en\_US will use ISO-8859-1.

Tokenisers
----------

Tokenisers are designed to identify the terms from a stream of text. It is expected that no markup will be present in the text passed to the tokenisers (for indexing, the removal of markup is handled by [Document](javadoc/org/terrier/indexing/Document.html) implementations - e.g. HTML tags are parsed by [TaggedDocument](javadoc/org/terrier/indexing/TaggedDocument.html)). The choice of tokeniser to use depends on the language being dealt with. Terrier ships with three different tokenisers for use when indexing text or parsing queries. The choice of tokeniser is specified by the tokeniser property, e.g. `tokeniser=EnglishTokeniser`.

-   [EnglishTokeniser](javadoc/org/terrier/indexing/tokenisation/EnglishTokeniser.html) - assumes that all valid characters in terms are A-Z, a-z and 0-9. Obviously this assumption is incorrect when indexing documents in languages other than English.

-   [UTFTokeniser](javadoc/org/terrier/indexing/tokenisation/UTFTokeniser.html) - uses Java’s Character class to determine what valid characters in indexing terms are. In particular, a term can only contain characters matching one of Character.isLetterOrDigit(), Character.getType() returns Character.NON\_SPACING\_MARK or Character.getType() returns Character.COMBINING\_SPACING\_MARK.

-   [IdentityTokeniser](javadoc/org/terrier/indexing/tokenisation/IdentityTokeniser.html) - a simple tokeniser that returns the input text as is, and is used internally by [SingleLineTRECQuery](javadoc/org/terrier/structures/SingleLineTRECQuery.html).

### Stemmers

Terrier includes all stemmers from the [Snowball](http://snowball.tartarus.org/) stemmer project, namely:

-   [DanishSnowballStemmer](javadoc/org/terrier/terms/DanishSnowballStemmer.html)

-   [DutchSnowballStemmer](javadoc/org/terrier/terms/DutchSnowballStemmer.html)

-   [EnglishSnowballStemmer](javadoc/org/terrier/terms/EnglishSnowballStemmer.html)

-   [FinnishSnowballStemmer](javadoc/org/terrier/terms/FinnishSnowballStemmer.html)

-   [FrenchSnowballStemmer](javadoc/org/terrier/terms/FrenchSnowballStemmer.html)

-   [GermanSnowballStemmer](javadoc/org/terrier/terms/GermanSnowballStemmer.html)

-   [HungarianSnowballStemmer](javadoc/org/terrier/terms/HungarianSnowballStemmer.html)

-   [ItalianSnowballStemmer](javadoc/org/terrier/terms/ItalianSnowballStemmer.html)

-   [NorwegianSnowballStemmer](javadoc/org/terrier/terms/NorwegianSnowballStemmer.html)

-   [PortugueseSnowballStemmer](javadoc/org/terrier/terms/PortugueseSnowballStemmer.html)

-   [RomanianSnowballStemmer](javadoc/org/terrier/terms/RomanianSnowballStemmer.html)

-   [RussianSnowballStemmer](javadoc/org/terrier/terms/RussianSnowballStemmer.html)

-   [SpanishSnowballStemmer](javadoc/org/terrier/terms/SpanishSnowballStemmer.html)

-   [SwedishSnowballStemmer](javadoc/org/terrier/terms/SwedishSnowballStemmer.html)

-   [TurkishSnowballStemmer](javadoc/org/terrier/terms/TurkishSnowballStemmer.html)

Batch Retrieval
---------------

When experimenting with topics in files other than English, use the same `tokeniser` setting used during indexing. Moreover, you should also use the property `trec.encoding` to ensure that the correct encoding is used when reading the topic files.

-------------------------
> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
