#!/usr/bin/env bash
#
# OntoSolr pipeline
# --run_sql 'file_with_sql_statement' 'file_with_results_in_tsv_format'
#
. /etc/profile
APPNAME="ontosolr-pipeline"
SERVER=`hostname -s | tr '[a-z]' '[A-Z]'`

#deploy to garak server
APPDIR=/rgd/pipelines/$APPNAME
cd $APPDIR

java -Dspring.config=$APPDIR/../properties/default_db2.xml \
    -Dlog4j.configurationFile=file://$APPDIR/properties/log4j2.xml \
    -jar lib/$APPNAME.jar --run_sql "$@"

#mailx -s "[$SERVER] OntoSolr Pipeline Run" mtutaj@mcw.edu < $APPDIR/logs/summary.log
