import java.io.Serializable;

/**
 * Created by nico on 21/08/16.
 */
public class Request implements Serializable {

    public static final int LOGIN_CMD = 1; // comando di Login
    public static final int LOGOUT_CMD = 2; // comando di Logout
    public static final int REGISTER_CMD = 3; // comando di nuova registrazione
    public static final int FSEARCH_CMD = 4; // comando per cercare utenti del sistema
    public static final int FLIST_CMD = 5; // comando per ricevere la lista degli amici
    public static final int FREQUESTSEND_CMD = 6; // comando per inviare una richiesta di amicizia al server
    public static final int CONTENTP_CMD = 7; // comando per inviare un nuovo contenuto al server
    public static final int FREQUESTREPLY_CMD = 8; // comando per inviare al server un riscontro di richiesta di amcizia


    public static final int UPDATEPORT_CMD = 255; // comando per uso interno, aggiorna la porta in cui il client è in attesa di richieste di amcizia

    private static final long serialVersionUID = 1L;

    public String token;
    public int cmd;
    public String data;

    public Request(String token, int cmd, String data) {
        this.token = token;
        this.cmd = cmd;
        this.data = data;
    }

    public String toString(){
        return "Client: " + token + "|Cmd: " + cmd + "|Data: " + data;
    }
}
