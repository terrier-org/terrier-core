#!/bin/bash
#
# Terrier - Terabyte Retriever
# Webpage: http://terrier.org/
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
# The Original Code is trec_setup.sh
#
# The Initial Developer of the Original Code is the University of Glasgow.
# Portions created by The Initial Developer are Copyright (C) 2004-2011
# the initial Developer. All Rights Reserved.
#
# Contributor(s):
#	Vassilis Plachouras <vassilis@dcs.gla.ac.uk> (original author)
#	Craig Macdonald <craigm@dcs.gla.ac.uk>
#
# Configures Terrier, creates the required files in the directory 
# etc and sets the location of the collection to index from 
# the given parameter

if [ $# != 1 ]
then
  echo "usage: setup.sh <collection directory>"
  echo "where collection directory is where the test"
  echo "collection to index is stored."
  exit 1;
fi
	
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

#setup CLASSPATH
JAR=`ls $TERRIER_HOME/target/terrier-core-*-jar-with-dependencies.jar`
if [ ! -n "$CLASSPATH" ]
then
	CLASSPATH=$JAR:$TERRIER_HOME/etc/logback.xml
else
	CLASSPATH=$CLASSPATH:$JAR:$TERRIER_HOME/etc/logback.xml
fi

echo $CLASSPATH


$JAVA_HOME/bin/java $JAVA_OPTIONS -cp $CLASSPATH \
	 -Dterrier.etc=$TERRIER_ETC \
	 -Dterrier.home=$TERRIER_HOME \
	 -Dterrier.setup=$TERRIER_ETC/terrier.properties \
	 org.terrier.applications.batchquerying.TRECSetup $TERRIER_HOME

#updating the address_collection file
find $1 -type f | sort >> $TERRIER_ETC/collection.spec
tail $TERRIER_ETC/collection.spec
echo "Updated collection.spec file. Please check that it contains"
echo "all and only all the files to be indexed, or create it manually."
