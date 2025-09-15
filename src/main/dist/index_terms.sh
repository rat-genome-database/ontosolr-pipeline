#!/bin/sh

XMLDATADIR=/rgd/pipelines/ontosolr-pipeline/data


echo "  --OntoSolr: deleting all"
curl http://localhost:8983/solr/OntoSolr/update --data-binary @${XMLDATADIR}/delete_all.xml -H 'Content-type:application/xml'
curl http://localhost:8983/solr/OntoSolr/update --data-binary '<commit/>' -H 'Content-type:application/xml'

echo "  --OntoSolr: uploading ontologies"
curl http://localhost:8983/solr/OntoSolr/update --data-binary @${XMLDATADIR}/onto_solr.xml -H 'Content-type:application/xml'
curl http://localhost:8983/solr/OntoSolr/update --data-binary '<commit/>' -H 'Content-type:application/xml'

echo "  --OntoSolr: uploading custom maps"
curl http://localhost:8983/solr/OntoSolr/update --data-binary @${XMLDATADIR}/custom_maps.xml -H 'Content-type:application/xml'
curl http://localhost:8983/solr/OntoSolr/update --data-binary '<commit/>' -H 'Content-type:application/xml'

echo "  --OntoSolr: uploading genes"
curl http://localhost:8983/solr/OntoSolr/update --data-binary @${XMLDATADIR}/onto_solr_genes.xml -H 'Content-type:application/xml'
curl http://localhost:8983/solr/OntoSolr/update --data-binary '<commit/>' -H 'Content-type:application/xml'

echo "  --OntoSolr: uploading XP ontology"
curl http://localhost:8983/solr/OntoSolr/update --data-binary @${XMLDATADIR}/XP_solr.xml -H 'Content-type:application/xml'
curl http://localhost:8983/solr/OntoSolr/update --data-binary '<commit/>' -H 'Content-type:application/xml'

echo "  --OntoSolr: deleting redundant XP terms"
curl http://localhost:8983/solr/OntoSolr/update --data-binary @${XMLDATADIR}/del_redundant_XP.xml -H 'Content-type:application/xml'
curl http://localhost:8983/solr/OntoSolr/update --data-binary '<commit/>' -H 'Content-type:application/xml'

echo "  --OntoSolr: optimizing"
curl http://localhost:8983/solr/OntoSolr/update --data-binary '<optimize/>' -H 'Content-type:application/xml'
