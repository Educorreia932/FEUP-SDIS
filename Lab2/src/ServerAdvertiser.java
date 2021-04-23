import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class ServerAdvertiser implements Runnable {
    private final InetAddress mcast_addr;
    private final int mcast_port;
    private final InetAddress srvc_addr;
    private final int srvc_port;

    public ServerAdvertiser(InetAddress mcast_addr, int mcast_port, InetAddress srvc_addr, int srvc_port) {
        this.mcast_addr = mcast_addr;
        this.mcast_port = mcast_port;
        this.srvc_addr = srvc_addr;
        this.srvc_port = srvc_port;
    }

    @Override
    public void run() {
        MulticastSocket multicastSocket = null;

        try {
            multicastSocket = new MulticastSocket(mcast_port);
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        String message = srvc_addr + " " + srvc_port;

        DatagramPacket advertisement = new DatagramPacket(
                message.getBytes(),
                message.getBytes().length,
                mcast_addr,
                mcast_port
        );

        try {
            multicastSocket.send(advertisement);
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.printf("Multicast: %s %s: %s %s", mcast_addr, mcast_port, srvc_addr, srvc_port);
    }
}
