import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Client {
    public static void main(String[] args) throws IOException {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String oper = args[2];
        String opnd = null;

        if (oper.equals("register"))
            opnd = String.format("REGISTER %s %s", args[3], args[4]);

        else if (oper.equals("lookup"))
            opnd = String.format("LOOKUP %s", args[3]);

        DatagramSocket socket = new DatagramSocket();

        // Send packet

        byte[] sbuf = opnd.getBytes();

        InetAddress address = InetAddress.getByName(host);
        DatagramPacket packet = new DatagramPacket(sbuf, sbuf.length, address, port);

        socket.send(packet);

        // Wait for reply

        byte[] rbuf = new byte[256];

        DatagramPacket replyPacket = new DatagramPacket(rbuf, rbuf.length);
        socket.receive(replyPacket);

        String result = new String(replyPacket.getData());

        System.out.println(result);
        System.out.printf("Client: %s : %s", opnd, result);

        socket.close();
    }
}
