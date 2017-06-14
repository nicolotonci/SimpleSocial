import java.sql.*;
import java.io.File;
/**
 * Created by nico on 21/08/16.
 *
 * Classe di interfaccia al database SqLite
 */
public class Db {

    private Connection conn = null;

    public Db(String dbName){
        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection("jdbc:sqlite:" + getClass().getProtectionDomain().getCodeSource().getLocation().toString().split(":")[1].replace("SocialServer.jar","") + dbName);
            this.conn.setAutoCommit(false);
        } catch (Exception e ){
            e.printStackTrace();
        }

    }

    /*
    Restituisce la connessione per l'esecuzioni di QUERY (esclusivamente SELECT)
     */
    public Connection getConn(){
        return this.conn;
    }

    /*
    Metodo che implementa l'unico accesso in scrittura verso il database, thread-safe
     */
    public synchronized void PerformSQL(String statement){
        try (Statement stmt = this.conn.createStatement()) {
            stmt.executeUpdate(statement);
            this.conn.commit();
            stmt.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
