#!/bin/bash

POM=$1

if [ ! -f $POM ] ; then
    echo "ERROR: Unable to find pom - $POM"
    exit 1
fi

BASEDIR=`dirname $POM`
NOW=`date +%Y%m%d.%H%M%S`

CURDIR=`pwd`
cd `dirname $0`
SCRIPTHOME=`pwd`
cd $BASEDIR

SVNURL=`svn info | grep URL | sed -e "s@URL: http[s]://@@"`

BACKUP=pom.xml-${NOW}~

cp $POM $BACKUP

SCRIPTHOME=`dirname $SCRIPTHOME`
POMREL=`pwd | sed -e "s@$SCRIPTHOME@@"`

echo "Fixing <scm> in $POMREL/pom.xml"

cat $BACKUP | sed -e "s@scm:svn:\(http[s]*\)[^< ]*@scm:svn:\1://$SVNURL@g" > pom.xml

cd $CURDIR

