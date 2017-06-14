import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

/**
 * Created by nico on 27/08/16.
 */
public class ContentRemote extends RemoteObject implements ContentRemoteInterface {

    private static final long serialVersionUID = 6L;

    private Hashtable<String, ContentClientInterface> clients;

    public ContentRemote(){
        this.clients = new Hashtable<>();
    }

    @Override
    public void RegisterCallback(String token, ContentClientInterface callback) throws RemoteException {
        // salvo nella Hashtable la callback associata al token dell'utente che l'ha inviata
        this.clients.put(token, callback);

        // invio al client i vecchi contenuti che non ha ancora ricevuto
        try {
            Statement stmt = RequestHandler.storage.getConn().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT Users.username, posts.text FROM posts\n" +
                    "INNER JOIN FollowRelations ON  posts.UserID = followedID\n" +
                    "INNER JOIN Users ON posts.UserID = Users.ID\n" +
                    "WHERE followerID = " + Helper.token2ID(token));

            while (rs.next()) {
                // invoco il metodo Notify remoto per inviare il contenuto al client che ha registrato la callback
                callback.Notify(rs.getString("username") + ": " + rs.getString("text"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void Follow(String token, String username) throws RemoteException {
        // inserisco nella tabella delle relazioni di interesse la nuova relazione
        RequestHandler.storage.PerformSQL("INSERT INTO FollowRelations VALUES ("+Helper.username2ID(username)+","+Helper.token2ID(token)+")");
    }

    public void RemoveClient(String token){
        // se il token è presente lo rimuovo
        if (this.clients.containsKey(token))
            this.clients.remove(token);
    }

    public void SendContent(String dest_token, String content){
        try {
            clients.get(dest_token.trim()).Notify(content);
        } catch (RemoteException e ){
            e.printStackTrace();
        }
    }
}
