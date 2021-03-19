package channels;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Channel{
    private String host;
    private int port;
    private MulticastSocket socket;

    public Channel(String host, int port){
        this.host = host;
        this.port = port;

        try {
            socket= new MulticastSocket(port);
            InetAddress address = InetAddress.getByName(host);
            socket.joinGroup(address);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
