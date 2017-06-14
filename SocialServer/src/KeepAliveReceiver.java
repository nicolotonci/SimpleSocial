import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.Vector;
/**
 * Created by nico on 25/08/16.
 */
public class KeepAliveReceiver implements Runnable {
    private Vector<String> online_lst;

    public KeepAliveReceiver(Vector<String> online_user) {
        this.online_lst = online_user;
    }

    @Override
    public void run() {
        // mi metto in attesa di datagrammi UDP di risposta dai client
        try (DatagramSocket server= new DatagramSocket(1500)) {
            DatagramPacket request = new DatagramPacket(new byte[512], 512);
            while(true){
                // ricevo il token di un utente che ha risposto al messaggio di KeepAlive
                server.receive(request);
                String tok = new String(request.getData(),0,request.getLength(),"UTF-8");

                // se il token non è nella lista degli utenti online lo inserisco
                if (!online_lst.contains(tok))
                    online_lst.addElement(tok);
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
