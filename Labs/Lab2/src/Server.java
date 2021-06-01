import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Server {
    public static void main(String[] args) throws IOException {
        HashMap<String, String> lookupTable = new HashMap<>();

        // Parse line arguments
        int srvc_port = Integer.parseInt(args[0]);
        InetAddress mcast_addr = InetAddress.getByName(args[1]);
        int mcast_port = Integer.parseInt(args[2]);
        InetAddress srvc_addr = InetAddress.getLocalHost();

        // Schedule server advertisement
        ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(4);

        ServerAdvertiser advertiser;

        advertiser = new ServerAdvertiser(mcast_addr, mcast_port, srvc_addr, srvc_port);

        scheduledPool.scheduleWithFixedDelay(advertiser, 1, 1, TimeUnit.SECONDS);

        DatagramSocket socket = new DatagramSocket(srvc_port);

        while (true) {
            byte[] buffer = new byte[256];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

            socket.receive(receivedPacket);

            String[] request = new String(receivedPacket.getData()).trim().split(" ");

            byte[] sbuf = String.valueOf(lookupTable.size()).getBytes();

            switch (request[0]) {
                case "REGISTER":
                    lookupTable.put(request[1], request[2]);

                    System.out.printf("Server: %s %s", request[1], request[2]);

                    InetAddress clientAddress = receivedPacket.getAddress();
                    int clientPort = receivedPacket.getPort();

                    break;

                case "LOOKUP":
                    System.out.printf("Server: %s", request[1]);

                    break;
            }

            String response = "";

            DatagramPacket sentPacket = new DatagramPacket(
                response.getBytes(),
                response.getBytes().length,
                receivedPacket.getAddress(),
                receivedPacket.getPort()
            );

            socket.send(sentPacket);
        }
    }
}
