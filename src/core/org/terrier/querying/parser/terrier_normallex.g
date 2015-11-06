/*
 * Terrier - Terabyte Retriever 
 * Webpage: http://terrier.org/
 * Contact: terrier{a.}dcs.gla.ac.uk
 * University of Glasgow - School of Computing Science
 * http://www.gla.ac.uk/
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is terrier_normallex.g
 *
 * The Original Code is Copyright (C) 2004-2011 the University of Glasgow.
 * All Rights Reserved.
 *
 * Contributor(s):
 *   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original contributor)
 */
header {
package org.terrier.querying.parser;
}


// ----------------------------------------------------------------------------
// the main lexer

class TerrierLexer extends Lexer;

options 
{
    k = 1;
	/* we need to set this for  antlr < 2.7.5, so the set complement
	 * functions correctly. */
    // Allow any char but \uFFFF (16 bit -1)
    charVocabulary='\u0003'..'\uFFFE';
//	charVocabulary = '\3'..'\377';
	exportVocab=Main;
	importVocab=Numbers;
	defaultErrorHandler=false;
}

protected
ALPHANUMERIC_CHAR:	'0'..'9'|'a'..'z'|'A'..'Z'|'\200'..'\uFFFE';

ALPHANUMERIC:   (ALPHANUMERIC_CHAR)+;

//used for fields and boosting weights
COLON:        ':';

//before weights
HAT:          '^';

//start and end of a phrase
QUOTE:        '\"';

//required token
REQUIRED:     '+';

//not required token
NOT_REQUIRED: '-';

//opening parenthesis
OPEN_PAREN: '(';

//closing parenthesis
CLOSE_PAREN: ')';

//open for disjunctive query
OPEN_DISJUNCTIVE : '{';

//close for disjunctive query
CLOSE_DISJUNCTIVE: '}';

//opening segment parenthesis
OPEN_SEGMENT: '[';

//closing segment parenthesis
CLOSE_SEGMENT: ']';

//proximity operator
PROXIMITY: '~';


IGNORED:
    (~(
		':'|'^'|'\"'|'-'|'+'|'['|']'|'('|')'|'~'|'.'|'{'|'}'|
		'A'..'Z'|'a'..'z'|'0'..'9'|'\200'..'\uFFFE'))
   { $setType(Token.SKIP); }
;
