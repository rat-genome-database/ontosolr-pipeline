#!/bin/sh
. /etc/profile
source ~/.bashrc

host=`hostname`

if [ "$host" != "garak.rgd.mcw.edu" ]; then
  echo "This is $host. Please run it on garak.rgd.mcw.edu."
  exit 1
fi

set -e

APPHOME=/rgd/pipelines/ontosolr-pipeline
#ONTODATA=/data/OntoSolr_data
ONTODATA=data

cd $APPHOME

echo "--EXPORTING ONTOLOGY TERMS to .tsv file"
RESULT=`$APPHOME/export_terms.sh $ONTODATA/onto_solr.tsv`
echo "$RESULT"

echo "--EXPORTING GENES to .tsv file"
$APPHOME/run_sql.sh $APPHOME/sql/export_genes.sql $ONTODATA/onto_solr_genes.tsv.unsorted
sort -ro $ONTODATA/onto_solr_genes.tsv $ONTODATA/onto_solr_genes.tsv.unsorted

echo "--EXPORTING HP terms as XP to .tsv file"
$APPHOME/run_sql.sh $APPHOME/sql/export_HP_terms.sql $ONTODATA/HP_solr.tsv
sed 's/HP/XP/g' $ONTODATA/HP_solr.tsv > $ONTODATA/XP_solr.tsv

echo "--EXPORTING HP to RDO mappings"
$APPHOME/run_sql.sh $APPHOME/sql/HP_and_RDO_mapped.sql $ONTODATA/HP_and_RDO_mapped.tsv
awk -F '\t' '{print "\<query\>id:(\"" $1 "\")\</query\>"}' $ONTODATA/HP_and_RDO_mapped.tsv |sed 's/HP/XP/g' > del_redundant_XP_tmp.xml
echo '<delete>' > del_redundant_XP.xml
cat del_redundant_XP_tmp.xml >> del_redundant_XP.xml
echo '</delete>' >> del_redundant_XP.xml


echo "--CONVERT .tsv to .xml"
$APPHOME/convert_tsv.sh
