Pluggable Compression
=====================

Introduction
------------

The inverted index data structure contains a collection of postings lists, a data structure which maintains information about the occurrence of terms in documents. These are represented within Terrier as implementations of two specific interfaces, namely [Posting](javadoc/org/terrier/structures/postings/Posting.html) and [IterablePosting](javadoc/org/terrier/structures/postings/IterablePosting.html). Terrier supports four different types of payload contained within each Posting (or children interfaces):

-   document identifier

-   term frequency

-   term frequency by field (e.g.: URL, title, body, incoming anchor text) a.k.a. field frequencies (implemented by [FieldPosting](javadoc/org/terrier/structures/postings/FieldPosting.html))

-   term positions within the document (implemented by [BlockPosting](javadoc/org/terrier/structures/postings/BlockPosting.html))

By default, Terrier compresses posting lists as a stream of postings. It uses [Elias Gamma](http://en.wikipedia.org/wiki/Elias_gamma_coding) compression schema (codec) to compress doc ids and term positions; it uses [Unary](http://en.wikipedia.org/wiki/Unary_coding) codec to compress term and field frequencies. The particular compression configuration is defined by the CompressionConfiguration class. For more information, please refer to [org.terrier.structures.bit.DirectInvertedOutputStream](javadoc/org/terrier/structures/bit/DirectInvertedOutputStream.html) (and children) for documentation on postings compression, and [org.terrier.structures.postings.bit.BasicIterablePosting](javadoc/org/terrier/structures/postings/bit/BasicIterablePosting.html) (and children) documentation for postings decompression. Since version 4.0, Terrier now supports more modern compression codecs, such as the state-of-the-art PForDelta codec. In particular, a new integer compression layer allows the transparent use of compression schemes from [Java\_FastPFOR](https://github.com/lemire/FastPFOR) by Daniel Lemire, and [Kamikaze](http://data.linkedin.com/opensource/kamikaze) by LinkedIn. Indeed, the new integer compression layer defines a new CompressionConfiguration (namely [IntegerCodecCompressionConfiguration](javadoc/org/terrier/structures/integer/IntegerCodecCompressionConfiguration.html), which can be configured to use various codecs for each compression payload (document ids, term frequencies, field frequencies, term positions):

|**Name**|**Description**|**Codec Class name (in `org.terrier.compression.integer.codec`)**|
|--|--|--|
|VInt|[Hadoop](http://hadoop.apache.org)’s Variable byte implementation|[VIntCodec](javadoc/org/terrier/compression/integer/codec/VIntCodec.html) [1]|
|Simple16| JavaFastPFOR's Simple16 [2,3] implementation| [LemireSimple16Codec](javadoc/org/terrier/compression/integer/codec/LemireSimple16Codec.html)|
|Frame-of-Reference (FOR)|JavaFastPFOR's Frame-of-Reference [4] implementation|[LemireFORVBCodec](javadoc/org/terrier/compression/integer/codec/LemireFORVBCodec.html)|
|NewPFD|JavaFastPFOR's NewPFD [5] implementation|[LemireNewPFDVBCodec](javadoc/org/terrier/compression/integer/codec/LemireNewPFDVBCodec.html)|
|OptPFD|JavaFastPFOR's OptPFD [5]</span> implementation|[LemireOptPFDVBCodec](javadoc/org/terrier/compression/integer/codec/LemireOptPFDVBCodec.html)|
|FastPFOR|JavaFastPFOR's FastPFOR[6] implementation - NB: A larger chunk-size is recommended for this codec.|[LemireFastPFORVBCodec](javadoc/org/terrier/compression/integer/codec/LemireFastPFORVBCodec.html)|
|PForDelta|Linkedin's Kamikaze PForDelta [3,5]|[KamikazePForDeltaVBCodec](javadoc/org/terrier/compression/integer/codec/KamikazePForDeltaVBCodec.html)|

When using these codecs, the Terrier infrastructure (de)compresses postings in chunks. The size of these chunks can be set at indexing time using the properties `index.inverted.compression.integer.chunk.size` for the direct index, and `index.inverted.compression.integer.chunk.size` for the inverted index.

Indexing
--------

Terrier can perform classical two-pass indexing (i.e. bin/trec\_terrier.sh -i), using the aforementioned codecs. To do so, some properties have to be set. For instance, to store the direct and inverted index compressed in blocks of 1024 posting using NewPFD codec:

    index.direct.compression.integer.chunk.size=1024
    indexing.direct.compression.configuration=org.terrier.structures.integer.IntegerCodecCompressionConfiguration
    index.direct.compression.integer.chunk.size=1024
    index.direct.compression.integer.ids.codec=LemireNewPFDVBCodec
    index.direct.compression.integer.tfs.codec=LemireNewPFDVBCodec
    indexing.inverted.compression.configuration=org.terrier.structures.integer.IntegerCodecCompressionConfiguration
    index.inverted.compression.integer.chunk.size=1024
    index.inverted.compression.integer.ids.codec=LemireNewPFDVBCodec
    index.inverted.compression.integer.tfs.codec=LemireNewPFDVBCodec
    index.inverted.compression.integer.fields.codec=LemireNewPFDVBCodec
    index.inverted.compression.integer.blocks.codec=LemireNewPFDVBCodec

You can also plug into Terrier a new compression schema by implementing your own CompressionConfiguration. If IntegerCodec meets your requirements, you can implement it, and directly use IntegerCodecCompressionConfiguration. Below are a list of properties for indexing:

|**Name**|**Description**|**Values**|
|--|--|--|
|indexing.inverted.compression.configuration indexing.direct.compression.configuration|The class that defines the compression configuration to be used on the inverted (direct) index at indexing time. Only classical indexing supports pluggable compression.|org.terrier.structures.indexing.CompressionFactory$BitCompressionConfiguration (default); org.terrier.structures.integer.IntegerCodecCompressionConfiguration|
|index.inverted.compression.integer.chunk.size index.direct.compression.integer.chunk.size|Number of postings to be compressed at a time (used only w/ IntegerCodecCompressionConfiguration)|integer (default: 1024)|
|index.inverted.compression.integer.ids.codec index.direct.compression.integer.ids.codec |The codec to be used to compress document identifiers in the inverted index (used only w/ IntegerCodecCompressionConfiguration). For the direct index, the codec to be used for the term identifiers.|See codecs table|
|index.inverted.compression.integer.tfs.codec index.direct.compression.integer.tfs.codec | The codec to be used to compress term frequencies in the inverted (direct) index (used only w/ IntegerCodecCompressionConfiguration)| " |
|index.inverted.compression.integer.fields.codec index.direct.compression.integer.fields.codec|The codec to be used to compress field frequencies in the inverted (direct) index (used only w/ IntegerCodecCompressionConfiguration, optional)|"|
|index.inverted.compression.integer.blocks.codec index.direct.compression.integer.blocks.codec | The codec to be used to compress term positions in the inverted (direct) index (used only w/ IntegerCodecCompressionConfiguration, optional) |"|


Recompression
-------------

Inverted indices built by single-pass (i.e. `bin/trec\_terrier.sh -i -j`) or MapReduce (i.e. `bin/trec\_terrier.sh -i -H`) indexing can be re-compressed using the InvertedIndexRecompresser class. For example, one can re-compress an inverted index using the OptPFD codec. This can be performed using InvertedIndexRecompresser with the following properties:

    indexing.tmp-inverted.compression.configuration=org.terrier.structures.integer.IntegerCodecCompressionConfiguration
    index.tmp-inverted.compression.integer.chunk.size=1024
    index.tmp-inverted.compression.integer.ids.codec=LemireOptPFDVBCodec
    index.tmp-inverted.compression.integer.tfs.codec=LemireOptPFDVBCodec
    index.tmp-inverted.compression.integer.fields.codec=LemireOptPFDVBCodec
    index.tmp-inverted.compression.integer.blocks.codec=LemireOptPFDVBCodec

Please notice that InvertedIndexRecompresser overwrites the original inverted index with the re-compressed one. Be sure to have one backup copy of the inverted index before using InvertedIndexRecompresser. Different codecs have different effects on index size and query response time. When storage space is a concern, it is suggested to use Terrier’s default compression configuration (Simple16 and OptPFD are options too). Instead, when the inverted index can fit in main memory, the best practices derived in [7] recommend to use the FOR codec to reduce the query response time, as follows:

    index.direct.compression.integer.chunk.size=1024
    indexing.direct.compression.configuration=org.terrier.structures.integer.IntegerCodecCompressionConfiguration
    compression.direct.integer.ids.codec=LemireFORVBCodec
    compression.direct.integer.tfs.codec=LemireFORVBCodec
    indexing.inverted.compression.configuration=org.terrier.structures.integer.IntegerCodecCompressionConfiguration
    compression.inverted.integer.ids.codec=LemireFORVBCodec
    compression.inverted.integer.tfs.codec=LemireFORVBCodec
    compression.inverted.integer.fields.codec=LemireFORVBCodec
    compression.inverted.integer.blocks.codec=LemireFORVBCodec

Notes
-----

-  The format of recompressed indices containing blocks was amended between 4.0 and 4.1. Terrier produces a warning if an index from 4.0 with blocks enabled is opened. We recommend that you recompress/reindex as appropriate if this affects you. For more information, see [TR-320](http://terrier.org/issues/browse/TR-320).

Citation Policy
---------------

If you make use of this package for efficient research, please cite: Catena, M., Macdonald, C., Ounis, I.: On Inverted Index Compression for Search Engine Efficiency. In: Proceedings of ECIR 2014. [[PDF]](http://www.dcs.gla.ac.uk/~craigm/publications/catena14compression.pdf)

References
----------

1.  Williams, H.E., Zobel, J.: Compressing integers for fast file access. The Computer Journal 42 (1999)

2.  Anh, V.N., Moffat, A.: Inverted index compression using word-aligned binary codes. Inf.Retr. 8 (1) (2005)

3.  Zhang, J., Long, X., Suel, T.: Performance of compressed inverted list caching in search engines. In: Proc. WWW '08. (2008)

4.  Goldstein, J., Ramakrishnan, R., Shaft, U.: Compressing relations and indexes. In: Proc. ICDE '98. (1998)

5.  Yan, H., Ding, S., Suel, T.: Inverted index compression and query processing with optimized document ordering. In: Proc. WWW '09. (2009)

6.  Lemire, D., Boytsov, L.: Decoding billions of integers per second through vectorization. Software: Practice and Experience (2013)

7.  Catena, M., Macdonald, C., Ounis, I.: On Inverted Index Compression for Search Engine Efficiency. In: Proc. ECIR '14 (2014) [[PDF]]

8.  Elias, P.: Universal codeword sets and representations of the integers. Trans. Info. Theory 21 (2) (1975)

9.  Zukowski, M., Heman, S., Nes, N., Boncz, P.: Super-scalar RAM-CPU cache compression. In: Proc. ICDE '06. (2006)

------------------------------------------------------------------------

> Webpage: <http://terrier.org>  
> Contact: [School of Computing Science](http://www.dcs.gla.ac.uk/)  
> Copyright (C) 2004-2019 [University of Glasgow](http://www.gla.ac.uk/). All Rights Reserved.
 
