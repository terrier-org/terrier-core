@echo off
REM Terrier - Terabyte Retriever
REM Webpage: http://terrier.org
REM Contact: terrier{a.}dcs.gla.ac.uk
REM
REM The contents of this file are subject to the Mozilla Public
REM License Version 1.1 (the "License"); you may not use this file
REM except in compliance with the License. You may obtain a copy of
REM the License at http://www.mozilla.org/MPL/
REM
REM Software distributed under the License is distributed on an "AS
REM IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
REM implied. See the License for the specific language governing
REM rights and limitations under the License.
REM
REM The Original Code is debug.bat
REM
REM The Initial Developer of the Original Code is the University of Glasgow.
REM Portions created by The Initial Developer are Copyright (C) 2004-2011
REM the initial Developer. All Rights Reserved.
REM
REM Contributor(s):
REM   Craig Macdonald <craigm{a.}dcs.gla.ac.uk> (original author)
REM
rem Used to detect if --debug is mentioned on the command line
SET DEBUG=NO
:again

IF "%1"=="--debug" GOTO set

IF "%1"=="--DEBUG" GOTO set

IF "%1"=="-debug" GOTO set

IF "%1"=="-DEBUG" GOTO set

IF "%1"=="" GOTO end

SHIFT
GOTO again


:set
SET DEBUG=YES
GOTO end

:end
rem echo %DEBUG%
