import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by nico on 27/08/16.
 *
 * Interfccia remota per i metodi del client
 */
public interface ContentClientInterface extends Remote {

    /*
    Invia al client il post (testo) contenuto nel parametro
     */
    void Notify(String content) throws RemoteException;
}
