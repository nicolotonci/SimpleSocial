import javax.swing.*;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

/**
 * Created by nico on 27/08/16.
 */
public class ContentClient extends RemoteObject implements ContentClientInterface  {

    @Override
    public void Notify(String content) throws RemoteException {

        // aggiunge alla lista dei contenuti il contenuto appena ricevuto (thread-safe)

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                HomeScreen.contentListModel.addElement(content);
            }
        });
    }
}
