import javax.swing.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
/**
 * Created by nico on 25/08/16.
 */
public class FRequestsServer implements Runnable {

    private String token;

    public FRequestsServer(String token){
        this.token = token;
    }

    @Override
    public void run() {
        // avvio il server socket su cui ricevere le richieste di amicizia
        try(ServerSocket server= new ServerSocket(0)){

            // informo il server psu quale porta sto ascoltando (tramite connessione al Main Server)
            new ServerConnection(token).PerformUpdatePort(server.getLocalPort());

            while(true){
                // attendo richieste
                Socket client=server.accept();
                try(ObjectInputStream in= new ObjectInputStream(client.getInputStream());
                    ObjectOutputStream out= new ObjectOutputStream(client.getOutputStream())){

                    // ricevo il nome utente di chi mi ha inviato la richiesta di amicizia
                    String fusername = (String) in.readObject();

                    // aggiorno la lista delle richieste pendenti (thread-safe)
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            HomeScreen.listModel.addElement(fusername);
                        }
                    });

                    // rispondo al server che ho ricevuto la sua richiesta
                    out.writeObject("Rcvd");
                    out.flush();
                } catch (ClassNotFoundException e ){
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Error: "+ e.getMessage());
        }
    }
}
