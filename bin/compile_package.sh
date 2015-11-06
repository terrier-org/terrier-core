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
# The Original Code is compile_package.sh
#
# The Initial Developer of the Original Code is the University of Glasgow.
# Portions created by The Initial Developer are Copyright (C) 2004-2011
# the initial Developer. All Rights Reserved.
#
# Contributor(s):
#	Vassilis Plachouras <vassilis@dcs.gla.ac.uk> (original author)
#	Craig Macdonald <craigm@dcs.gla.ac.uk>
#
# Compiles the source code of Terrier and creates a jar file 
# terrier-$VERSION.jar in the directory lib

# -----------------------------------------------------------------

fullPath () {
	t='TEMP=`cd $TEMP; pwd`'
	for d in $*; do
		eval `echo $t | sed 's/TEMP/'$d'/g'`
	done
}

# -----------------------------------------------------------------

echo "************* COMPILING ***************"

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

if [ ! -n "$VERSION" ]
then
	VERSION=`date +%Y%m%d`
fi

if [ "$*" == "0" ];
then
	echo "No package specified"
	echo "Usage: $0 packagename"
	exit 1
fi

PRODUCT=$1
rm -f $TERRIER_HOME/lib/terrier-${PRODUCT}-*.jar
JARNAME=terrier-${PRODUCT}-$VERSION.jar
TMPDIR=$TERRIER_HOME/tmp_classes

#if [ ! -n "$CLASSPATH" ]
#then
#	CLASSPATH=$TERRIER_HOME/src
#else
#	CLASSPATH=$CLASSPATH:$TERRIER_HOME/src
#fi
for jar in $TERRIER_HOME/lib/*.jar TERRIER_LIB/hadoop/*.jar  $TERRIER_LIB/poi/*.jar $TERRIER_LIB/crawler4j/*.jar; do
	if [ ! -n "$CLASSPATH" ]
	then
		CLASSPATH=$jar
	else
		CLASSPATH=$CLASSPATH:$jar
	fi
done

#pushd $TERRIER_HOME/src/uk/ac/gla/terrier/querying/parser &>/dev/null
#$JAVA_HOME/bin/java -cp $CLASSPATH antlr.Tool terrier.g
#popd $CURDIR &>/dev/null

BUILDSOURCE=$TERRIER_HOME/src_${PRODUCT}/

mkdir $TMPDIR
find $BUILDSOURCE -name '*.java' |\
	xargs $JAVA_HOME/bin/javac -source 1.5\
		-classpath $CLASSPATH \
		-d $TMPDIR && \
$JAVA_HOME/bin/jar cfM $TERRIER_HOME/lib/$JARNAME \
		-C $TMPDIR .
EXIT_CODE=$?

rm -rf $TMPDIR
exit $EXIT_CODE

