import java.security.SecureRandom;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by nico on 21/08/16.
 *
 * Classe di aiuto con funzioni utili all'interno del server
 */

public final class Helper {

    /*
    Genera una stringa casuale di 32 caratteri
     */
    public static String GenerateToken(){
        return new BigInteger(130, new SecureRandom()).toString(32);
    }

    /*
    Verifica che il token contenuto nel parametro sia associato ad un utente
     */
    public static boolean checkToken(String token){
        boolean res = false;
        try {
            Statement stmt = RequestHandler.storage.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM Users WHERE token = '" + token + "'");
            rs.next();

            if (rs.getInt("count") == 1)
                res = true;
            rs.close();
            stmt.close();
        } catch (SQLException ex){
            ex.printStackTrace();
        }
        return res;
    }

    /*
    Restituisce l'username dell'utente a cui è associato il token nel parametro
     */
    public static String token2username(String token){
        String res = "";
        try {
            Statement stmt = RequestHandler.storage.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT username FROM Users WHERE token = '"+token+"'");
            rs.next();
            res = rs.getString("username");
            rs.close();
            stmt.close();

        } catch (SQLException e){
            e.printStackTrace();
        }

        return res;
    }

    /*
    Restituisce l'ID dell'utente a cui è associato il token nel parametro
     */
    public static int token2ID(String token){
        int res = -1;
        try {
            Statement stmt = RequestHandler.storage.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ID FROM Users WHERE token = '"+token+"'");
            rs.next();
            res = rs.getInt("ID");
            rs.close();
            stmt.close();

        } catch (SQLException e){
            e.printStackTrace();
        }

        return res;
    }

    /*
        Restituisce l'ID dell'utente a cui è associato l'username nel parametro
     */
    public static int username2ID(String username){
        int res = -1;
        try {
            Statement stmt = RequestHandler.storage.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT ID FROM Users WHERE username = '"+username+"'");
            rs.next();
            res = rs.getInt("ID");
            rs.close();
            stmt.close();

        } catch (SQLException e){
            e.printStackTrace();
        }

        return res;
    }

}
