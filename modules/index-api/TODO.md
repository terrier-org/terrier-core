Index APIs Improvement Suggestions:

* BitFilePosition: the method "void setOffset(BitFilePosition pos)" simpified to "void set(BitFilePosition pos)".
* Index: transform the whole abstract class into an interface with default methods.
         introduce an Enum with toString() method and state for commonly used index structures.
* Lexicon: the inner static class "public static class LexiconFileEntry<KEY2>" should be not exposed as public. It is mainly used in Lexicon builder for its own stuff and some other classes.
* LexiconEntry: transfor the abstract class into an interface with default methods.
                Javadocs need improvements.
* Pointer: the method "void setPointer(Pointer p)" simplified to "void set(Pointer p)".
* PostingIndex: the generic type is not used in the interface. Is it necessary?
                the method "IterablePosting getPostings(Pointer lEntry)" throws an IOException. Throwing an IOException assumes everything is on file/network?
* FieldPosting: the method "void setFieldLengths(int[] newLengths)" should not be exposed as public.
* IterablePosting: the methods "int next()" and "int next(int targetId)" should not raise an IOException because implementation-dependent. Consider posting lists in memory for example.
