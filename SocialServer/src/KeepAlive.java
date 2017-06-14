import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Vector;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;

/**
 * Created by nico on 25/08/16.
 */
public class KeepAlive implements Runnable {

    private Vector<String> online_list = null;
    private Vector<String> new_online_list = null;

    public KeepAlive(Vector<String> online_user, Vector<String> next_online_user){
        this.online_list = online_user;
        this.new_online_list = next_online_user;

        // avvio il server per ricevere le risposte del KeepAlive
        new Thread(new KeepAliveReceiver(this.new_online_list)).start();

    }

    @Override
    public void run() {

        try {
            // inizializzo il socket multicast e i dati da inviare ogni 10 secondi
            MulticastSocket senderSocket = new MulticastSocket(2000);
            senderSocket.setTimeToLive(1);
            senderSocket.setLoopbackMode(false);
            senderSocket.setReuseAddress(true);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(byteStream);
            out.writeUTF("KeepAlive");
            byte[] data = byteStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length,
                    InetAddress.getByName("225.1.1.1"), 2000);


            while (true) {

                // svuoto la lista degli utenti online dello slot precedente
                online_list.clear();

                // popolo la lista dello slot precedente con la lista in scrittura
                online_list.addAll(new_online_list);

                // svuoto la lista in scrittura dello slot corrente
                new_online_list.clear();

                // invio il datgramma di KeepAlive
                senderSocket.send(packet);

                // stampo la lista degli utenti online degli scorsi 10 secondi (slot precedente)
                PrintOnline_tokens();

                // mi addormento per 10 secondi e ricomincio
                Thread.sleep(10000);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void PrintOnline_tokens(){
        System.out.println("Currently online:");
        for (String s : online_list)
            System.out.println(" - " + Helper.token2username(s.trim()));
        System.out.println("\n");
    }

}
