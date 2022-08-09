package edu.mcw.rgd;

import edu.mcw.rgd.dao.DataSourceFactory;
import edu.mcw.rgd.datamodel.ontologyx.Ontology;
import edu.mcw.rgd.datamodel.ontologyx.Term;
import edu.mcw.rgd.process.FileExternalSort;
import edu.mcw.rgd.process.Utils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author mtutaj
 * @since 08/09/2022
 */
public class Main {

    private String version;
    private Dao dao = new Dao();

    Logger log = LogManager.getLogger("status");

    long time0 = System.currentTimeMillis();

    public static void main(String[] args) throws Exception {

        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new FileSystemResource("properties/AppConfigure.xml"));
        Main manager = (Main) (bf.getBean("manager"));

        try {
            for( int i=0; i<args.length; i++ ) {
                if( args[i].equals("--run_sql") ) {
                    manager.runSql(args[++i], args[++i]);
                }
                else if( args[i].equals("--export_ontology_terms") ) {
                    manager.exportOntologyTerms(args[++i]);
                }
            }
        }catch (Exception e) {
            Utils.printStackTrace(e, manager.log);
            throw e;
        }
    }

    void printHello() {
        log.info(getVersion());
        log.info("   "+dao.getConnectionInfo());
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("   started at "+sdt.format(new Date(time0)));
    }

    void runSql(String sqlFileName, String outputFileName) throws Exception {

        printHello();

        String sql = Utils.readFileAsString(sqlFileName);
        BufferedWriter out = Utils.openWriter(outputFileName);

        try(Connection conn = DataSourceFactory.getInstance().getDataSource().getConnection() ) {

            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            // write out column names
            int columnCount = rs.getMetaData().getColumnCount();
            for( int i=0; i<columnCount; i++ ) {
                if( i>0 ) {
                    out.write('\t');
                }
                out.write(rs.getMetaData().getColumnName(i+1));
            }
            out.write("\n");

            while( rs.next() ) {
                for( int col=0; col<columnCount; col++ ) {

                    if( col>0 ) {
                        out.write('\t');
                    }

                    String val = rs.getString(col+1);
                    if( val!=null ) {
                        val = val.replace("[\\t\\r\\n]", " ");
                        out.write(val);
                    }
                }
                out.write("\n");
            }
        }
        out.close();

        log.info("SQL exec complete -- time elapsed: "+Utils.formatElapsedTime(time0, System.currentTimeMillis()));
    }


    void exportOntologyTerms(String outputFileName) throws Exception {

        printHello();

        // load all terms for public ontologies
        List<Term> terms = loadTerms();
        log.info("  terms loaded: "+Utils.formatThousands(terms.size()));

        final Map<String, Term> termMap = new HashMap<>();
        for( Term t1: terms ) {
            termMap.put(t1.getAccId(), t1);
        }
        log.info("  term map created");

        // load all synonyms for public ontologies
        Map<String, List<String>> synonymMap = loadSynonyms();
        log.info("  synonyms loaded for terms: "+Utils.formatThousands(synonymMap.size()));

        final List<String> emptySynonymList = new ArrayList<>();
        emptySynonymList.add("");

        // unsorted lines with output
        String [] inputFiles = {"data/ont_unsorted.tsv"};
        final BufferedWriter out = Utils.openWriter(inputFiles[0]);

        terms.parallelStream().forEach( t -> {

            try {

                // build 'ancestor' string
                List<String> ancestorAccs = dao.getAncestorAccIdsForTerm(t.getAccId());
                StringBuilder buf = new StringBuilder();
                for( String acc: ancestorAccs ) {

                    Term term = termMap.get(acc);
                    String termName = Utils.defaultString(term.getTerm());
                    // sanitize term name
                    termName = termName.replace("[\\t\\r\\n;,:]", " ");

                    if( buf.length()>0 ) {
                        buf.append(", ");
                    }
                    buf.append(acc).append("; ").append(termName);
                }
                String anc = buf.toString();
                // if empty, the original query emitted '(null)'
                if( anc.isEmpty() ) {
                    anc = "(null)";
                }

                // generate output for all synonyms
                List<String> synonyms = synonymMap.get(t.getAccId());
                if( synonyms==null ) {
                    synonyms = emptySynonymList;
                }

                for( String synonym: synonyms ) {

                    String sanitizedSynonymName = synonym.replace("[\\t\\r\\n]", " ");

                    StringBuilder line = new StringBuilder();
                    line.append(t.getAccId())
                            .append("\t").append(t.getOntologyId())
                            .append("\t").append(t.getTerm())
                            .append("\t").append(t.getDefinition())
                            .append("\t").append(anc)
                            .append("\t").append(sanitizedSynonymName)
                            .append("\n");

                    synchronized(out) {
                        out.write(line.toString());
                    }
                }
            } catch( Exception e ) {
                throw new RuntimeException(e);
            }
        });

        out.close();
        log.info("  unsorted file generated");

        String sortedFileName = "data/ont_sorted_tsv";

        final boolean skipDuplicates = true;
        FileExternalSort.mergeAndSortFiles(inputFiles, sortedFileName, skipDuplicates);
        log.info("  sorted file generated");

        final String header = "TERM_ACC\tONT_ID\tTERM\tDEF\tANC\tSYNONYMS\n";
        BufferedWriter out2 = Utils.openWriter(outputFileName);
        out2.write(header);

        long lineCount = 1;
        BufferedReader in = Utils.openReader(sortedFileName);
        String line;
        while( (line=in.readLine())!=null ) {
            out2.write(line);
            out2.write("\n");
            lineCount++;
        }
        out2.close();

        log.info("  final file generated; lines: "+Utils.formatThousands(lineCount));
        log.info("time elapsed: "+Utils.formatElapsedTime(time0, System.currentTimeMillis()));
    }

    List<Term> loadTerms() throws Exception {

        List<Term> terms = new ArrayList<>();

        List<Ontology> ontologies = dao.getPublicOntologies();
        for( Ontology o: ontologies ) {
            terms.addAll( dao.getOntologyTerms(o.getId()));
        }

        // sanitize term names and definitions
        terms.parallelStream().forEach( t -> {

            if( t.getTerm()!=null ) {
                String sanitizedTermName = t.getTerm().replace("[\\t\\r\\n]", " ");
                t.setTerm(sanitizedTermName);
            }

            if( t.getDefinition()!=null ) {
                String sanitizedDef = t.getDefinition().replace("[\\t\\r\\n]", " ");
                t.setDefinition(sanitizedDef);
            }
        });
        return terms;
    }

    Map<String, List<String>> loadSynonyms() throws Exception {

        Map<String, List<String>> synonymMap = new HashMap<>();

        List<Ontology> ontologies = dao.getPublicOntologies();
        for( Ontology o: ontologies ) {

            Map<String, List<String>> synonymsInOntology = dao.getSynonyms(o.getId());
            synonymMap.putAll(synonymsInOntology);
        }

        return synonymMap;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}

