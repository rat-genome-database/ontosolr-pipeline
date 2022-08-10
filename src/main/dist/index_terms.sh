#!/bin/sh

XMLDATADIR=/rgd/pipelines/ontosolr-pipeline/data
#XMLDATADIR=/data/OntoSolr_data

cd /data/OntoSolr/bin


# note: for unknown reasons, post_to_onto_solr.sh often throws exceptions
#    if OntoSolr webapp is not reloaded on tomcat before running the script
echo "  --reloading OntoSolr.war"
touch /usr/local/tomcat/webapps/OntoSolr.war
sleep 15
wget -O /tmp/index.html "http://garak.rgd.mcw.edu:8080/OntoSolr/#/"
sleep 5


echo "  --OntoSolr: deleting all"
./post_to_onto_solr.sh $XMLDATADIR/delete_all.xml
if [ $? -ne 0 ]; then
  echo "ERROR: upload to OntoSolr delete_all failed!"
  exit 1
fi

echo "  --OntoSolr: uploading ontologies"
./post_to_onto_solr.sh $XMLDATADIR/onto_solr.xml
if [ $? -ne 0 ]; then
  echo "ERROR: upload ontologies to OntoSolr failed!"
  exit 1
fi

echo "  --OntoSolr: uploading custom maps"
./post_to_onto_solr.sh $XMLDATADIR/custom_maps.xml
if [ $? -ne 0 ]; then
  echo "ERROR: upload custom maps to OntoSolr failed!"
  exit 1
fi

echo "  --OntoSolr: uploading genes"
./post_to_onto_solr.sh $XMLDATADIR/onto_solr_genes.xml
if [ $? -ne 0 ]; then
  echo "ERROR: upload genes ontologies to OntoSolr failed!"
  exit 1
fi

echo "  --OntoSolr: uploading XP ontology"
./post_to_onto_solr.sh $XMLDATADIR/XP_solr.xml
if [ $? -ne 0 ]; then
  echo "ERROR: upload XP to OntoSolr failed!"
  exit 1
fi

echo "  --OntoSolr: deleting redundant XP terms"
./post_to_onto_solr.sh $XMLDATADIR/del_redundant_XP.xml
if [ $? -ne 0 ]; then
  echo "ERROR: delete redundant XP terms in OntoSolr failed!"
  exit 1
fi
