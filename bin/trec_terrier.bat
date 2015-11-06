@echo off
REM Terrier - Terabyte Retriever
REM Webpage: http://terrier.org
REM Contact: terrier@dcs.gla.ac.uk
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
REM The Original Code is trec_terrier.bat
REM
REM The Initial Developer of the Original Code is the University of Glasgow.
REM Portions created by The Initial Developer are Copyright (C) 2004-2011
REM the initial Developer. All Rights Reserved.
REM
REM Contributor(s):
REM   Craig Macdonald <craigm@dcs.gla.ac.uk> (original author)
REM


if "Windows_NT"=="%OS%" setlocal

rem keep %0 in case we overwrite
SET PROGRAM=%0
rem SCRIPT contains the full path filename of this script
SET SCRIPT=%~f0
rem BIN contains the path of the BIN folder
SET BIN=%~dp0


REM --------------------------
REM Load a settings batch file if it exists
REM --------------------------
if NOT EXIST "%BIN%\terrier-env.bat" GOTO defaultvars
CALL "%BIN%\terrier-env.bat" "%BIN%\.."

:defaultvars

set COLLECTIONPATH=%~f1

REM --------------------------
REM Derive TERRIER_HOME, then call anyclass.bat
REM --------------------------

if defined TERRIER_HOME goto run_anyclass
CALL "%BIN%\fq.bat" "%BIN%\.."
SET TERRIER_HOME=%FQ%
echo Set TERRIER_HOME to be %TERRIER_HOME%

:run_anyclass
%TERRIER_HOME%\bin\anyclass.bat org.terrier.applications.TrecTerrier %* 

if "Windows_NT"=="%OS%" endlocal


