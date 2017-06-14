import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.lang.ClassNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.Vector;

/**
 * Created by nico on 21/08/16.
 */
public class RequestHandler implements Runnable {
    static Db storage;
    static Vector<String> current_online;
    static Vector<String> next_online;

    static ContentRemote cr;

    private Socket client;
    public RequestHandler(Socket client){
        this.client = client;

    }

    @Override
    public void run() {
        try(ObjectInputStream in= new ObjectInputStream(client.getInputStream());
            ObjectOutputStream out= new ObjectOutputStream(client.getOutputStream());){
            while(true){

                // leggo la richiesta dal client
                Request req = (Request) in.readObject();

                /* se non è un comando di LOGIN oppure non è un token online (quindi valido) richiedo un nuovo login */
                if (current_online.contains(req.token) || req.cmd == Request.LOGIN_CMD) {

                    /* se il token non è in lista dei prossimi online e il token non è vuoto, lo inserisco nella lista*/
                    if (!next_online.contains(req.token) && !req.token.equals(""))
                        next_online.add(req.token);

                    /* distinguo qual'è il comando ricevuto sul server principale TCP */
                    switch (req.cmd) {
                        case Request.LOGIN_CMD:
                            out.writeObject(PerformLogin(req.data));
                            break;
                        case Request.LOGOUT_CMD:
                            out.writeObject(PerformLogout(req.token));
                            break;
                        case Request.REGISTER_CMD:
                            out.writeObject(PerformRegistration(req.data));
                            break;
                        case Request.FLIST_CMD:
                            out.writeObject(PerformFriendList(req.token));
                            break;
                        case Request.FSEARCH_CMD:
                            out.writeObject(PerformUserSearch(req.token, req.data));
                            break;
                        case Request.FREQUESTSEND_CMD:
                            out.writeObject(PerformFRequest(req.token, req.data));
                            break;
                        case Request.CONTENTP_CMD:
                            out.writeObject(new String("ok"));
                            PerformContentDistribution(req.token, req.data);
                            break;
                        case Request.FREQUESTREPLY_CMD:
                            out.writeObject(PerformFRequestReply(req.token, req.data));
                            break;
                        case Request.UPDATEPORT_CMD:
                            out.writeObject(PerformUpdatePort(req.token, req.data));
                            break;

                    }
                } else {
                        /* il client non è nella lista degli utenti online, quindi non ha comunicato con il server
                        per più di 10 secondi, dunque il suo token è scaduto
                         */
                        out.writeObject(new String("tokenExpired"));
                }

                // svuoto il buffer in uscita
                out.flush();

            }
        } catch (IOException e) {}
        catch (ClassNotFoundException e) {System.err.println("Class not found: "+
                e.getMessage());
        } finally{try {client.close();} catch (IOException e) {}}
}

    private String PerformLogin(String data){
        // parsing dei parametri di input (uguale anche per le altre funzioni che usano concatenazione con i 3 simboli di pipe)
        String[] parts = data.split(Pattern.quote("|||"));
        String ret = null;
        try {
            Statement stmt = storage.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM Users WHERE username = '" + parts[0] + "' AND password = '"+parts[1]+"'");
            rs.next();
                // verifico che ci sia un utente con i dati specificati
            if (rs.getInt("count") > 0){
                String token = Helper.GenerateToken();

                // aggiorno la tabella degli utenti con il nuovo token dell'utente loggato e il suo indirizzo ip
                storage.PerformSQL("UPDATE Users SET token = '"+token+"', host_addr = '"+client.getRemoteSocketAddress().toString()+"' WHERE username = '"+parts[0]+"'");
                ret = token;

                // inserisco l'utente nelle due liste degli utenti online
                current_online.add(token);
                next_online.add(token);
            } else
                ret = "error";

            rs.close();
            stmt.close();
        } catch (SQLException e ){
            e.printStackTrace();
            return "error";
        }

        return ret;
    }

    private String PerformRegistration(String data){
        String ret = null;
        String[] parts = data.split(Pattern.quote("|||"));
        System.out.println(parts[0]);
        // controllo che l'username non sia gia in uso
        try {
            Statement stmt = storage.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS count FROM Users WHERE username = '" + parts[1] + "'");
            rs.next();
            if (rs.getInt("count") > 0)
                ret = "InUse";
            rs.close();
            stmt.close();
        } catch (SQLException e ){
            e.printStackTrace();
            ret = "error";
        }

        if (ret == null) {
            String token = Helper.GenerateToken();
            // registro i dati dell'utente appena registrato nella tabella degli utenti
            storage.PerformSQL("INSERT INTO Users (email, username, password, token) VALUES ('" + parts[0] + "', '" + parts[1] + "', '" + parts[2] + "', '" + token + "')");

            // inserisco l'utente nelle due liste degli utenti online (come in login)
            current_online.add(token);
            next_online.add(token);
            return token;
        }

        return ret;
    }

    private String PerformLogout(String token){
        // rimuovo l'utente che ha effettuato il logout da entrambe le liste
        next_online.remove(token);
        current_online.remove(token);

        // rimuovo anche la callback dalla Hashtable
        cr.RemoveClient(token);
        return "ok";
    }

    private ArrayList<String> PerformFriendList(String token){
        ArrayList<String> result = new ArrayList<>();
        try {
            Statement stmt = storage.getConn().createStatement();
            // cerco tutti gli amici dell'utente che ne ha fatto richiesta
            ResultSet rs = stmt.executeQuery("SELECT  Users.ID as myID, Users2.username, Users2.token, followerID From Users\n" +
                    "INNER JOIN FriendsRelations ON Users.ID = FriendsRelations.friendID1\n" +
                    "INNER JOIN Users as Users2 ON FriendsRelations.friendID2 = Users2.ID\n" +
                    "LEFT JOIN FollowRelations ON followedID = Users2.ID AND followerID = Users.ID\n" +
                    "WHERE Users.token = '"+token+"' ");

            // popolo la lista da inviare al client con i risultati ottenuti dalla query
            while (rs.next()) {
                result.add(rs.getString("username") + " (" + (current_online.contains(rs.getString("token")) ? "Online" : "Offline") + ")" + (rs.getInt("myID") == rs.getInt("followerID") ? " (Following)" : ""));
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {ex.printStackTrace();}
        return result;

    }

    private ArrayList<String> PerformUserSearch(String token, String data){
        ArrayList<String> result =  new ArrayList<>();
        try {
            Statement stmt = storage.getConn().createStatement();
            // cerco tutti gli utenti che abbiano contenuto nell'username il filtro nel parametro 'data'
            ResultSet rs = stmt.executeQuery("SELECT Users.username, Fr.friendID1 FROM Users\n" +
                    "LEFT JOIN (\n" +
                    "SELECT FriendsRelations.* FROM FriendsRelations\n" +
                    "INNER JOIN Users ON friendID1 = ID\n" +
                    "WHERE token = '"+token+"') As Fr ON Users.ID = Fr.friendID2\n" +
                    "WHERE Users.username LIKE '%"+data+"%'");

            // popolo la lista da inviare al client con i risultati ottenuti dalla query
            while (rs.next()) {
                result.add(rs.getString("username") + (rs.getInt("friendID1") > 0 ? " (Friends)" : ""));
            }

            rs.close();
            stmt.close();
        } catch (SQLException ex) {ex.printStackTrace();}
        return result;
    }

    private String PerformFRequest(String token, String data){
        String result = "error";
        try {
            Statement stmt = storage.getConn().createStatement();
            // trovo il token, ip_addr e porta di ascolto dell'utente destinatario della richiesta di amicizia
            ResultSet rs = stmt.executeQuery("SELECT token, host_addr, Port FROM Users WHERE username = '"+data+"'");
            rs.next();

            // primo controllo se l'utente destinatario è online
            if (current_online.contains(rs.getString("token"))){

                // tento la connessione con il destinatario
                String ip_addr = rs.getString("host_addr").split(":")[0].replace("/","");
                Socket sck = new Socket();
                try {
                    sck.setSoTimeout(1000);
                    sck.setTcpNoDelay(true);
                    sck.connect(new InetSocketAddress(ip_addr, rs.getInt("Port")));
                    ObjectOutputStream out= new ObjectOutputStream(sck.getOutputStream());
                    ObjectInputStream in= new ObjectInputStream(sck.getInputStream());

                    String myusername = Helper.token2username(token);

                    // invio il nome dell'utente mittente della richiesta di amicizia
                    out.writeObject(myusername);
                    out.flush();

                    // se ricevo l'ack ("Rcvd") da parte del destinatario la richiesta di amicizia è stata recapitata
                    if (in.readObject().equals("Rcvd")) {
                        result = "ok";

                        // inserisco dunque la richiesta nella tabella delle richieste di amicizia pendenti
                        storage.PerformSQL("INSERT INTO pendingFRequest (fromUSer, toUSer) VALUES ('"+myusername+"','"+data+"')");
                    }

                    sck.close();


                } catch (IOException e){
                    result = "offline";
                } catch (ClassNotFoundException e){
                    e.printStackTrace();
                }

            } else
                // il destinatario è offline
                result = "offline";

            rs.close();
            stmt.close();
        } catch (SQLException e){
            e.printStackTrace();
        }

        return result;
    }

    private void PerformContentDistribution(String token, String data){
        try {
            Statement stmt = storage.getConn().createStatement();

            // trovo l'insieme di utenti interessati ai contenuti dell'utente che ha pubblicato il post
            ResultSet rs = stmt.executeQuery("SELECT Users.token FROM FollowRelations\n" +
                    "INNER JOIN Users ON followerID = Users.ID\n" +
                    "WHERE followedID = " + Helper.token2ID(token));
            String sender = Helper.token2username(token);

            // salvo il contenuto nella tabella dei contenuti
            storage.PerformSQL("INSERT INTO posts VALUES ("+Helper.token2ID(token)+", '"+data+"')");

            // per ogni utente interessato al contenuto chiamo la callback
            while (rs.next()){
                cr.SendContent(rs.getString("token"), sender + ": " + data);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    private String PerformFRequestReply(String token, String data){
        String res = "error";
        String myusername = Helper.token2username(token);
        String parts[] = data.split(Pattern.quote("|||"));
        try {
            Statement stmt = storage.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Users.ID as fromID, USers2.ID as toID, COUNT(*) as count FROM pendingFRequest\n" +
                    "INNER JOIN Users ON pendingFRequest.fromUser = Users.username\n" +
                    "INNER JOIN Users AS Users2 ON pendingFRequest.toUser = Users2.username\n" +
                    "WHERE fromUser = '"+parts[0]+"' AND toUser = '"+myusername+"'");
            rs.next();
            // verifico che ci sia una richiesta di amicizia pendente tra i due utenti
            if (rs.getInt("count") > 0){

                // se l'utente destinatario ha accettato la richiesta di amicizia modifico la rete sociale
                if (parts[1].equals("yes")){

                    // inserisco due volte perchè le amicizie sono bidirezionali!
                    storage.PerformSQL("INSERT INTO FriendsRelations VALUES ("+rs.getInt("fromID")+","+rs.getInt("toID")+")");
                    storage.PerformSQL("INSERT INTO FriendsRelations VALUES ("+rs.getInt("toID")+","+rs.getInt("fromID")+")");

                }
                // elimino la richiesta di amicizia dalla tabella delle richieste pendenti
                storage.PerformSQL("DELETE FROM pendingFRequest WHERE fromUser = '"+parts[0]+"' AND toUser = '"+myusername+"'");
                res = "ok";
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        return res;
    }

    private String PerformUpdatePort(String token, String data){
        storage.PerformSQL("UPDATE Users SET Port = '"+ data +"' WHERE token = '"+token+"'");
        return "ok";
    }
}