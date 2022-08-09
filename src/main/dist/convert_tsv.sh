TSVDIR=/rgd/pipelines/ontosolr-pipeline/data

java -cp /rgd/lib/rgd-text-mining-tools.jar \
  edu.mcw.rgd.nlp.utils.solr.DocConverter \
  ${TSVDIR}/onto_solr.tsv ${TSVDIR}/onto_solr.xml

java -cp /rgd/lib/rgd-text-mining-tools.jar \
  edu.mcw.rgd.nlp.utils.solr.DocConverter \
  ${TSVDIR}/onto_solr_genes.tsv ${TSVDIR}/onto_solr_genes.xml

java -cp /rgd/lib/rgd-text-mining-tools.jar \
  edu.mcw.rgd.nlp.utils.solr.DocConverter \
  ${TSVDIR}/XP_solr.tsv ${TSVDIR}/XP_solr.xml

