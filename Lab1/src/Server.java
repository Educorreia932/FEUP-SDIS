import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

public class Server {
    public static void main(String[] args) throws IOException {
        HashMap<String, String> table = new HashMap<>();

        int port = Integer.parseInt(args[0]);

        DatagramSocket socket = new DatagramSocket(port);

        byte[] rbuf = new byte[256];

        DatagramPacket packet = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(packet);
        String[] received = new String(packet.getData()).split(" ");

        if (received[0].equals("REGISTER")) {
            table.put(received[1], received[2]);

            System.out.printf("Server: %s %s", received[1], received[2]);

            byte[] sbuf = String.valueOf(table.size()).getBytes();

            InetAddress clientAddress = packet.getAddress();
            int clientPort = packet.getPort();

            DatagramPacket response = new DatagramPacket(sbuf, sbuf.length, clientAddress, clientPort);
            socket.send(response);
        }

        else if (received[0].equals("LOOKUP")) {
            System.out.printf("Server: %s", received[1]);
        }

        socket.close();
    }
}
