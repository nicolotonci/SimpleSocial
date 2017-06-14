import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by nico on 27/08/16.
 *
 * Interfaccia Remota per i metodi del server
 */
public interface ContentRemoteInterface extends Remote {

    String OBJECT_NAME="SIMPLE_SOCIAL";

    /*
    Metodo remoto per la registrazione della callback del client
     */
    void RegisterCallback(String token, ContentClientInterface callback) throws RemoteException;

    /*
    Metodo remoto per esprimere interesse dei contenuti di un certo utente
     */
    void Follow(String token, String username) throws RemoteException;
}
