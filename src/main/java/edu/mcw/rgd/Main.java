package edu.mcw.rgd;

import edu.mcw.rgd.dao.AbstractDAO;
import edu.mcw.rgd.dao.DataSourceFactory;
import edu.mcw.rgd.process.Utils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.FileSystemResource;

import java.io.BufferedWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author mtutaj
 * @since 08/09/2022
 */
public class Main {

    private String version;
    private AbstractDAO dao = new AbstractDAO();

    Logger log = LogManager.getLogger("status");


    public static void main(String[] args) throws Exception {

        DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
        new XmlBeanDefinitionReader(bf).loadBeanDefinitions(new FileSystemResource("properties/AppConfigure.xml"));
        Main manager = (Main) (bf.getBean("manager"));

        try {
            for( int i=0; i<args.length; i++ ) {
                if( args[i].equals("--run_sql") ) {
                    manager.runSql(args[++i], args[++i]);
                }
            }
        }catch (Exception e) {
            Utils.printStackTrace(e, manager.log);
            throw e;
        }
    }

    void runSql(String sqlFileName, String outputFileName) throws Exception {

        long time0 = System.currentTimeMillis();

        log.info(getVersion());
        log.info("   "+dao.getConnectionInfo());
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info("   started at "+sdt.format(new Date(time0)));

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

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}

