#!/bin/bash

rm src/core/org/terrier/querying/parser/TerrierQueryParser.java
rm src/core/org/terrier/querying/parser/TerrierQueryParserTokenTypes.java
rm src/core/org/terrier/querying/parser/TerrierLexer.java 
rm src/core/org/terrier/querying/parser/TerrierFloatLexer.java

ant
