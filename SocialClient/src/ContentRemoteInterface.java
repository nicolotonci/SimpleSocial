import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by nico on 27/08/16.
 */
public interface ContentRemoteInterface extends Remote {

    public static final String OBJECT_NAME="SIMPLE_SOCIAL";

    void RegisterCallback(String token, ContentClientInterface callback) throws RemoteException;

    void Follow(String token, String username) throws RemoteException;
}
