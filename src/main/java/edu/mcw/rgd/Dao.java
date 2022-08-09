package edu.mcw.rgd;

import edu.mcw.rgd.dao.impl.OntologyXDAO;
import edu.mcw.rgd.datamodel.ontologyx.Ontology;
import edu.mcw.rgd.datamodel.ontologyx.Term;
import edu.mcw.rgd.datamodel.ontologyx.TermSynonym;

import java.util.*;

public class Dao {

    private OntologyXDAO odao = new OntologyXDAO();

    public String getConnectionInfo() {
        return odao.getConnectionInfo();
    }

    public List<Ontology> getPublicOntologies() throws Exception {
        if( true ) {
            return odao.getPublicOntologies();
        }
        else {
            List<Ontology> ontologies = new ArrayList<>();
            ontologies.add(odao.getOntology("MMO"));
            return ontologies;
        }
    }

    public List<Term> getOntologyTerms(String ontId) throws Exception {
        return odao.getActiveTerms(ontId);
    }

    public Map<String, List<String>> getSynonyms(String ontId) throws Exception {

        Map<String, List<String>> results = new HashMap<>();

        String[] synTypes = {"narrow_synonym", "related_synonym", "broad_synonym", "synonym", "exact_synonym"};
        for( String synType: synTypes ) {
            List<TermSynonym> termSynonyms = odao.getActiveSynonymsByType(ontId, synType);
            for( TermSynonym tsyn: termSynonyms ) {
                List<String> list = results.get(tsyn.getTermAcc());
                if( list==null ) {
                    list = new ArrayList<>();
                    results.put(tsyn.getTermAcc(), list);
                }
                list.add(tsyn.getName());
            }
        }
        return results;
    }

    public List<String> getAncestorAccIdsForTerm(String termAcc) throws Exception {
        List<String> accIds = odao.getAllActiveTermAncestorAccIds(termAcc);
        Collections.sort(accIds);
        return accIds;
    }


    public Object getOntologyTerms2() {

        // original sql query that we try to replace

        String sql = "select t.term_acc,t.ONT_ID,\n" +
                "t.term as term, t.TERM_DEFINITION as def,\n" +
                "(\n" +

                // ORIGINAL phrase -- WM_CONCAT discontinued by Oracle
                "/* select wm_concat(unique concat(' ', concat(o1.term_acc, concat('; ', o1.term)))) */\n" +
                "\n" +

                // attempt to rewrite SQL by using LISTAGG
                // worked great for small ontologies,
                // but it had limit of 4,000 characters and it was failing for larger ontologies
                "/* select LISTAGG(unique concat(' ', concat(o1.term_acc, concat('; ', o1.term))),',')\n" +
                "  within group( order by o1.term_acc) */\n" +
                "  \n" +

                // attempt to go around 4000 limit of LISTAGG
                // unfortunately, it was producing duplicate entries ...
                "select rtrim(xmlagg(xmlelement(e,o1.term_acc||'; '||o1.term,', ').extract('//text()')\n" +
                "                order by o1.term_acc).getclobval(),', ')\n" +

                "  from ont_dag, ont_terms o1\n" +
                "where o1.ont_id not in('EFO','CVCL') and ont_id='MMO'\n" +
                "and o1.term_acc = ont_dag.parent_term_acc\n" +
                "start with child_term_acc=t.term_acc\n" +
                "connect by prior parent_term_acc=child_term_acc\n" +
                ") as anc,\n" +
                "synonyms\n" +
                "from ont_terms t\n" +
                "left join\n" +
                "(select term_acc as acc, synonym_name as synonyms\n" +
                "from ont_synonyms where ont_synonyms.SYNONYM_TYPE in ('narrow_synonym', 'related_synonym','broad_synonym', 'synonym', 'exact_synonym')) s\n" +
                "on t.TERM_ACC = s.acc\n" +
                "where t.IS_OBSOLETE = 0 and  t.ont_id not in('EFO','CVCL') and ont_id='MMO'\n" +
                "order by t.term_acc";
        return sql;
    }
}
