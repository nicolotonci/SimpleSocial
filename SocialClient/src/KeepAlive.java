import java.net.*;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;
/**
 * Created by nico on 25/08/16.
 */
public class KeepAlive implements Runnable {
    private String token;
    private DatagramSocket socket;
    private DatagramPacket ackPacket;

    public KeepAlive(String token){
        this.token = token;
        try {
            this.socket = new DatagramSocket();
            this.ackPacket = new DatagramPacket(token.getBytes("UTF-8"), token.getBytes("UTF-8").length ,new InetSocketAddress(InetAddress.getLocalHost(), 1500)); // 32 byte is he lenght because the token is 32 characters
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // mi iscrivo al gruppo multicast e mi metto in ascolto
        try(MulticastSocket client = new MulticastSocket(2000)){
            client.joinGroup(InetAddress.getByName("225.1.1.1"));
            DatagramPacket packet= new DatagramPacket(new byte[512], 512);
            while(true){
                // ricevo il datagramma
                client.receive(packet);
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(
                        packet.getData(),packet.getOffset(),packet.getLength()));

                // verifico che sia una richiesta di KeepAlive, se si rispondo con il mio token
                if (in.readUTF().equals("KeepAlive")){
                    // rispondo al server con un datagramma unicast
                    socket.send(ackPacket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
