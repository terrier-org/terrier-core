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
REM The Original Code is lcp.bat
REM
REM The Initial Developer of the Original Code is the University of Glasgow.
REM Portions created by The Initial Developer are Copyright (C) 2004-2011
REM the initial Developer. All Rights Reserved.
REM
REM Contributor(s):
REM   Craig Macdonald <craigm@dcs.gla.ac.uk> (original author)
REM
rem echo Adding %1 to localclasspath
SET LOCALCLASSPATH=%1;%LOCALCLASSPATH%
