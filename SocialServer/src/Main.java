import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

/**
 * Created by nico on 19/08/16.
 */
public class Main {

    public static void main(String args[]) {
        // collego l'applicazione al database
        Db database = new Db("database.sqlite3");
        RequestHandler.storage = database;

        // elimino tutte le richieste di amicizia pendenti di una precedente sessione del server
        database.PerformSQL("DELETE FROM pendingFRequest");
        // elimino tutti i post della sessione precedente del server
        database.PerformSQL("DELETE FROM posts");

        // creo le liste per gli utenti online (contengono token degli utenti online)
        Vector<String> online_users = new Vector<>();
        Vector<String> next_online_users = new Vector<>();

        RequestHandler.current_online = online_users;
        RequestHandler.next_online = next_online_users;

        // avvio il servizio KeepAlive
        new Thread(new KeepAlive(online_users, next_online_users)).start();


        // esporto l'interfaccia ContentRemoteInterface ed avvio il server Registry di RMI
        try {
            RequestHandler.cr = new ContentRemote();
            ContentRemoteInterface RemoteStub = (ContentRemoteInterface) UnicastRemoteObject.exportObject(RequestHandler.cr, 0);

            //register to RMI registry
            Registry registry= LocateRegistry.createRegistry(1501);
            registry.rebind(RequestHandler.cr.OBJECT_NAME, RemoteStub);

        } catch (RemoteException e){
            e.printStackTrace();
        }

        /* ******* configurazione e avvio del Main Server ****** */

        // creo il pool di thread per l'esecuzione dei RequestHandler
        ExecutorService executors = Executors.newFixedThreadPool(5);

        // mi metto in attesa di connessioni da parte dei client
        try(ServerSocket server= new ServerSocket(1500)){
            while(true){
                Socket client=server.accept();
                //System.out.println("Request arrived.");

                // avvio un nuovo RequestHandler per gestire la richiesta del client appena arrivato
                RequestHandler handler = new RequestHandler(client);
                // lo invio al pool di thread per l'esecuzione
                executors.submit(handler);
            }
        } catch (IOException e) {
            System.err.println("Error: "+ e.getMessage());
        } finally {
            executors.shutdown();
        }
    }

}

