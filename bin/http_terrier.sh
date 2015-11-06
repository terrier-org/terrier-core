#!/bin/bash
#
# Terrier - Terabyte Retriever
# Webpage: http://ir.dcs.gla.ac.uk/terrier 
# Contact: terrier@dcs.gla.ac.uk 
#
# The contents of this file are subject to the Mozilla Public
# License Version 1.1 (the "License"); you may not use this file
# except in compliance with the License. You may obtain a copy of
# the License at http://www.mozilla.org/MPL/
#
# Software distributed under the License is distributed on an "AS
# IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
# implied. See the License for the specific language governing
# rights and limitations under the License.
#
# The Original Code is anyclass.sh
#
# The Initial Developer of the Original Code is the University of Glasgow.
# Portions created by The Initial Developer are Copyright (C) 2004 
# the initial Developer. All Rights Reserved.
#
# Contributor(s):
#    Vassilis Plachouras <vassilis{a.}dcs.gla.ac.uk> (original author)
#    Craig Macdonald <craigm{a.}dcs.gla.ac.uk>
#
# a script for running an arbitrary class, setup for Terrier

fullPath () {
	t='TEMP=`cd $TEMP; pwd`'
	for d in $*; do
		eval `echo $t | sed 's/TEMP/'$d'/g'`
	done
}

TERRIER_BIN=`dirname $0`
if [ -e "$TERRIER_BIN/terrier-env.sh" ];
then
    . $TERRIER_BIN/terrier-env.sh
fi

#setup TERRIER_HOME
if [ ! -n "$TERRIER_HOME" ]
then
	#find out where this script is running
	TEMPVAR=`dirname $0`
	#make the path abolute
	fullPath TEMPVAR
	#terrier folder is folder above
	TERRIER_HOME=`dirname $TEMPVAR`
	echo "Setting TERRIER_HOME to $TERRIER_HOME"
fi

#setup TERRIER_ETC
if [ ! -n "$TERRIER_ETC" ]
then
	TERRIER_ETC=$TERRIER_HOME/etc
fi

#setup JAVA_HOME
if [ ! -n "$JAVA_HOME" ]
then
	#where is java?
	TEMPVAR=`which java`
	#j2sdk/bin folder is in the dir that java was in
	TEMPVAR=`dirname $TEMPVAR`
	#then java install prefix is folder above
	JAVA_HOME=`dirname $TEMPVAR`
	echo "Setting JAVA_HOME to $JAVA_HOME"
fi

if [ ! -n "$TERRIER_LIB" ];
then
	TERRIER_LIB=$TERRIER_HOME/lib/
fi

if [ "$TERRIER_LIB_TMP" == "1" ];
then
	NEWLIB=$TMP/terrier_lib_$$/
	#echo Copying $TERRIER_LIB to $NEWLIB
	mkdir $NEWLIB
	cp -r $TERRIER_LIB/* $NEWLIB
	TERRIER_LIB=$NEWLIB
fi

#setup CLASSPATH
if [ ! -n "$CLASSPATH" ];
then
	CLASSPATH=$JAVA_HOME/lib/tools.jar
else
	CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/tools.jar
fi

for jar in $TERRIER_LIB/*.jar $TERRIER_LIB/hadoop/*.jar  $TERRIER_LIB/poi/*.jar $TERRIER_LIB/crawler4j/*.jar; do
	if [ ! -n "$CLASSPATH" ]
	then
		CLASSPATH=$jar
	else
		CLASSPATH=$CLASSPATH:$jar
	fi
done


if [ ! -n "$TERRIER_HEAP_MEM" ];
then
    TERRIER_HEAP_MEM=1024M
fi

if [ -e "$TERRIER_ETC/terrier-log.xml" ];
then
    LOGGER_OPTIONS="-Dlog4j.configuration=file:$TERRIER_ETC/terrier-log.xml"
elif [ -e "$TERRIER_ETC/log4j.xml" ];
then
    LOGGER_OPTIONS="-Dlog4j.configuration=file:$TERRIER_ETC/log4j.xml"
elif [ -e "$TERRIER_ETC/log4j.properties" ];
then
    LOGGER_OPTIONS="-Dlog4j.configuration=file:$TERRIER_ETC/log4j.properties"
fi

#JAVA_OPTIONS=

$JAVA_HOME/bin/java -Xmx$TERRIER_HEAP_MEM $JAVA_OPTIONS $LOGGER_OPTIONS $TERRIER_OPTIONS \
	 -Dterrier.etc=$TERRIER_ETC \
	 -Dterrier.home=$TERRIER_HOME \
     -Dterrier.setup=$TERRIER_ETC/terrier.properties \
     -cp $CLASSPATH $@

if [ "$TERRIER_LIB_TMP" == "1" ];
then
	rm -rf $NEWLIB
fi
[richardm@chagulak terrier-4.0]$cat bin/http_terrier.sh 
#!/bin/bash
#
# Terrier - Terabyte Retriever
# Webpage: http://terrier.org
# Contact: terrier@dcs.gla.ac.uk 
#
# The contents of this file are subject to the Mozilla Public
# License Version 1.1 (the "License"); you may not use this file
# except in compliance with the License. You may obtain a copy of
# the License at http://www.mozilla.org/MPL/
#
# Software distributed under the License is distributed on an "AS
# IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
# implied. See the License for the specific language governing
# rights and limitations under the License.
#
# The Original Code is http_server.sh
#
# The Initial Developer of the Original Code is the University of Glasgow.
# Portions created by The Initial Developer are Copyright (C) 2004-2011
# the initial Developer. All Rights Reserved.
#
# Contributor(s):
#	Vassilis Plachouras <vassilis@dcs.gla.ac.uk> (original author)
#	Craig Macdonald <craigm@dcs.gla.ac.uk>
#
# a script for starting a Jetty webserver, for retrieval results
# by default, port 8080

fullPath () {
	t='TEMP=`cd $TEMP; pwd`'
	for d in $*; do
		eval `echo $t | sed 's/TEMP/'$d'/g'`
	done
}

TERRIER_BIN=`dirname $0`
if [ -e "$TERRIER_BIN/terrier-env.sh" ];
then
	. $TERRIER_BIN/terrier-env.sh
fi

#setup TERRIER_HOME
if [ ! -n "$TERRIER_HOME" ]
then
	#find out where this script is running
	TEMPVAR=`dirname $0`
	#make the path abolute
	fullPath TEMPVAR
	#terrier folder is folder above
	TERRIER_HOME=`dirname $TEMPVAR`
	#echo "Setting TERRIER_HOME to $TERRIER_HOME"
fi

if [ -z "$TERRIER_LIB" ];
then
	TERRIER_LIB=$TERRIER_HOME/lib/
fi

for f in $TERRIER_LIB/jetty-ext/*.jar $TERRIER_LIB/crawler4j/*.jar;
do
	if [ -z "$CLASSPATH" ]
	then
		CLASSPATH=$f
	else
		CLASSPATH=$CLASSPATH:$f
	fi
done

export CLASSPATH

if [ "$#" == "0" ];
then
	$TERRIER_HOME/bin/anyclass.sh org.terrier.utility.SimpleJettyHTTPServer 8080 $TERRIER_HOME/src/webapps/simple/
else
	$TERRIER_HOME/bin/anyclass.sh org.terrier.utility.SimpleJettyHTTPServer $*
fi
