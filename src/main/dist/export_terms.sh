#!/usr/bin/env bash
#
# OntoSolr pipeline
# export ontology terms for all active ontologies in RGD into a file in TSV format
#
. /etc/profile
APPNAME="ontosolr-pipeline"
SERVER=`hostname -s | tr '[a-z]' '[A-Z]'`

#deploy to garak server
APPDIR=/rgd/pipelines/$APPNAME
cd $APPDIR

java -Dspring.config=$APPDIR/properties/default_db2.xml \
    -Dlog4j.configurationFile=file://$APPDIR/properties/log4j2.xml \
    -jar lib/$APPNAME.jar --export_ontology_terms "$@"


