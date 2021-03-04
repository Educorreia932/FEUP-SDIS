import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Client {
    public static void main(String[] args) throws IOException {
        // Parse line arguments
        InetAddress mcast_addr = InetAddress.getByName(args[0]);
        int mcast_port = Integer.parseInt(args[1]);
        String oper = args[2];
        String opnd = null;

        // Listen to multicast messages
        MulticastSocket multicastSocket = new MulticastSocket(mcast_port);
        multicastSocket.joinGroup(mcast_addr);

        byte[] buffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        multicastSocket.receive(packet);

        String[] req = new String(packet.getData()).trim().split(" ");

        DatagramSocket socket = new DatagramSocket();

        // Perform operation
        byte[] sbuf = opnd.getBytes();

        InetAddress address = InetAddress.getByName(host);
        DatagramPacket sentPacket = new DatagramPacket(sbuf, sbuf.length, address, mcast_port);

        socket.send(sentPacket);
    }
}
