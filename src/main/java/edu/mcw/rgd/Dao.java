package edu.mcw.rgd;

import edu.mcw.rgd.dao.impl.OntologyXDAO;
import edu.mcw.rgd.datamodel.ontologyx.Ontology;
import edu.mcw.rgd.datamodel.ontologyx.Term;
import edu.mcw.rgd.datamodel.ontologyx.TermSynonym;

import java.util.*;

public class Dao {

    private OntologyXDAO odao = new OntologyXDAO();

    private List<String> excludedPublicOntologies;

    public String getConnectionInfo() {
        return odao.getConnectionInfo();
    }

    public List<Ontology> getPublicOntologies() throws Exception {
        List<Ontology> ontologies = odao.getPublicOntologies();
        ontologies.removeIf(o -> getExcludedPublicOntologies().contains(o.getId()));
        return ontologies;
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

    public List<String> getExcludedPublicOntologies() {
        return excludedPublicOntologies;
    }

    public void setExcludedPublicOntologies(List<String> excludedPublicOntologies) {
        this.excludedPublicOntologies = excludedPublicOntologies;
    }
}
