import javax.swing.*;
import java.net.Socket;
import java.net.InetAddress;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
/**
 * Created by nico on 21/08/16.
 */
public class ServerConnection {
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String currentToken;

    public ServerConnection(String token) {
        this.currentToken = token;
        try {
            // istanzio una nuova connessione con il server (Main Server)
            this.socket = new Socket(InetAddress.getLocalHost(), 1500);
            // inizializzo gli stream
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());

        } catch (UnknownHostException e) {
            System.err.println("Unknown host");
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void Close(){
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    Richiesta di login al server
    Return: (String) token
     */
    public String PerformLogin(String username, String password){
        String result = "error";
        Request rqst = new Request(this.currentToken, Request.LOGIN_CMD, username + "|||" + password);
        try {
            this.out.writeObject(rqst);
            this.out.flush();
            result = (String) readFromStream();
            this.Close();
        } catch (IOException e) { e.printStackTrace();}
        return result;

    }

    /*
    Richiesta di registrazione nuovo utente al server
    Return: (String) token
     */
    public String PerformRegistration(String email, String username, String password){
        String result = "error";
        Request rqst = new Request(this.currentToken, Request.REGISTER_CMD, email + "|||" + username + "|||" + password);
        try {
            this.out.writeObject(rqst);
            this.out.flush();
            result = (String) readFromStream();
            this.Close();
        } catch (IOException e) { e.printStackTrace();}

        return result;
    }

    /*
    Richiesta di logout al server
    Return: (String) "ok"
     */
    public String PerformLogout(){
        Request rqst = new Request(this.currentToken, Request.LOGOUT_CMD, null);
        try {
            this.out.writeObject(rqst);
            this.out.flush();
            this.Close();
        } catch (IOException e) { e.printStackTrace();}

        return "ok";
    }

    /*
    Richiesta della lista degli amici al sever
    Return: (List<String>) list (anche vuota)
     */
    public ArrayList<String> RetriveFriendList(){
        ArrayList<String> result = null;
        Request rqst = new Request(this.currentToken, Request.FLIST_CMD, null);
        try{
            this.out.writeObject(rqst);
            this.out.flush();
            result = (ArrayList<String>) readFromStream();
            this.Close();
        } catch (IOException e ){ e.printStackTrace();}
        return result;
    }

    /*
    Richiesta di una lista di utenti della rete che contengano il filtro 'query'
    Return: (List<String>) list (anche vuota)
     */
    public ArrayList<String> PerformSearch(String query){
        ArrayList<String> result = null;
        Request rqst = new Request(this.currentToken, Request.FSEARCH_CMD, query);
        try{
            this.out.writeObject(rqst);
            this.out.flush();
            result = (ArrayList<String>) readFromStream();
            this.Close();
        } catch (IOException e ){ e.printStackTrace();}
        return result;
    }

    /*
    Richiesta di inoltro richiesta di amicizia
    Return: (String)
     */
    public String PerformFRequest(String username){
        String result = null;
        Request rqst = new Request(this.currentToken, Request.FREQUESTSEND_CMD, username);
        try{
            this.out.writeObject(rqst);
            this.out.flush();
            result = (String) readFromStream();
            this.Close();
        } catch (IOException e ){ e.printStackTrace();}
        return result;
    }

    /*
    Richiesta di pubblicazione e distribuzione nuovo contenuto
    Return: (String)
     */
    public String PublishContent(String text){
        String result = null;

        try{
            this.out.writeObject(new Request(this.currentToken, Request.CONTENTP_CMD, text));
            this.out.flush();
            result = (String) readFromStream();
            this.Close();
        } catch (IOException e ){ e.printStackTrace();}
        return result;
    }

    /*
    Richiesta di invio riscontro richiesta di amicizia al server
    Return: (String)
     */
    public String PerformFRequestReply(String text, boolean accept){
        String result = null;
        String data = text + "|||" + (accept ? "yes" : "no");

        try {
            this.out.writeObject(new Request(this.currentToken, Request.FREQUESTREPLY_CMD, data));
            this.out.flush();
            result = (String) readFromStream();
            this.Close();
        } catch (IOException e) {e.printStackTrace();}

        return result;
    }

    /*
    Richiesta di aggiornamento porta client (server di inoltro richieste di amiciazia)
    Return: (String)
     */
    public String PerformUpdatePort(int portnr){
        String result = null;
        try {
            this.out.writeObject(new Request(this.currentToken, Request.UPDATEPORT_CMD, new Integer(portnr).toString()));
            this.out.flush();
            result = (String) readFromStream();
            this.Close();
        } catch (IOException e){
            e.printStackTrace();
        }

        return result;
    }


    private Object readFromStream(){
       Object tmp = null;
        try {
            tmp = in.readObject();
            try {
                String test = (String) tmp;
                if (test.equals("tokenExpired")) {
                    JOptionPane.showMessageDialog(null, "Token Expired. Please login again!");
                    Main.restart();
                }
            } catch (Exception e){}
        } catch (IOException e ){
            e.printStackTrace();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        return tmp;
    }

}
