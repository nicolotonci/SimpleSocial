import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by nico on 27/08/16.
 */
public interface ContentClientInterface extends Remote {

    void Notify(String content) throws RemoteException;
}
