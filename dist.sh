#!/bin/sh

DISTDIR=facebook-feed-analyzer

mkdir $DISTDIR
cp run.sh $DISTDIR
cp target/facebook-feed-analyzer-*-jar-with-dependencies.jar $DISTDIR/facebook-feed-analyzer.jar
cp README $DISTDIR

zip -r facebook-feed-analyzer.zip $DISTDIR

#rm -rf $DISTDIR
