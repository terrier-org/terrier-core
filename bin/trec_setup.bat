@echo off
REM Terrier - Terabyte Retriever
REM Webpage: http://terrier.org/
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
REM The Original Code is trec_setup.bat
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

set COLLECTIONPATH=%~f1

REM --------------------------
REM Load a settings batch file if it exists
REM --------------------------
if NOT EXIST "%BIN%\terrier-env.bat" GOTO defaultvars
CALL "%BIN%\terrier-env.bat" "%BIN%\.."

:defaultvars

REM --------------------------
REM Derive TERRIER_HOME, TERRIER_ETC, TERRIER_LIB
REM --------------------------

if defined TERRIER_HOME goto terrier_etc
CALL "%BIN%\fq.bat" "%BIN%\.."
SET TERRIER_HOME=%FQ%
echo Set TERRIER_HOME to be %TERRIER_HOME%

:terrier_etc
if defined TERRIER_ETC goto terrier_lib
SET TERRIER_ETC=%TERRIER_HOME%\etc

:classpath
echo TERRIER_HOME is set to %TERRIER_HOME%
REM ------------------------
REM -- Build up class path 
REM ------------------------
call "%BIN%\lcp.bat" %CLASSPATH%
SET LOCALCLASSPATH=
FOR /f "tokens=*" %%G IN ('dir /b %TERRIER_HOME%\target\terrier-core-*-jar-with-dependencies.jar') DO call "%BIN%\lcp.bat" "%TERRIER_HOME%\target\%%G"
call "%BIN%\lcp.bat" "%TERRIER_HOME%\etc\logback.xml"


echo %CLASSPATH%
REM ------------------------
REM -- Run TRECSetup
REM ------------------------
java -Dterrier.home="%TERRIER_HOME%" -Dterrier.setup="%TERRIER_ETC%\terrier.properties" -cp %LOCALCLASSPATH% org.terrier.applications.batchquerying.TRECSetup "%TERRIER_HOME%"



REM ------------------------
REM -- Building collection.spec
REM ------------------------
echo Now building collection.spec
REM No find on Windows, so use java app for same purpose
java -cp %LOCALCLASSPATH% org.terrier.applications.FileFind "%COLLECTIONPATH%" >> "%TERRIER_ETC%\collection.spec"

REM ------------------------
REM -- Display collection.spec
REM ------------------------
echo.
echo.
echo Updated collection.spec file. Please check that it contains all and only all
echo files to indexed, or create it manually
echo.
echo collection.spec:
echo -----------------------------------------------------------------------------
type "%TERRIER_ETC%\collection.spec"
echo -----------------------------------------------------------------------------
echo.

if "Windows_NT"=="%OS%" endlocal
