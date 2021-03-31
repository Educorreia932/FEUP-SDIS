package channels;

import messages.Message;
import peer.Peer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MDB_Channel extends Channel implements Runnable{
    private MulticastSocket socket;

    public MDB_Channel(String host, int port, Peer peer) {
        super(host, port, peer);
    }

    public int start() {
        try {
            socket = new MulticastSocket(port);
            group = InetAddress.getByName(host);
            socket.joinGroup(group);
        }

        catch (IOException e) {
            System.err.println("ERROR: Failed to start channel.");

            return -1;
        }

        return 0;
    }

    public void stop() {
        running = false;

        try {
            socket.leaveGroup(group);
        }

        catch (IOException e) {
            System.err.println("ERROR: Failed to leave group.");
        }

        socket.close();
    }

    @Override
    public void run() {
        if (start() != 0)
            return;

        running = true;

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet); // Receive packet
                parseMessage(packet.getData(), packet.getLength());
            }

            catch (IOException e) {
                System.err.println("ERROR: Failed to receive packet.");
            }
        }
    }

    public void parseMessage(byte[] msg, int msg_len) {
        byte[] header = Message.getHeaderBytes(msg);
        byte[] body = Message.getBodyBytes(msg, msg_len, header.length);

        String header_string = new String(header);
        String[] header_fields = header_string.split(" "); // Split header by spaces

        // Parse fields
        int sender_id = Integer.parseInt(header_fields[2]);
        String file_id = header_fields[3];
        int chunk_no = Integer.parseInt(header_fields[4]);

        // Ignore message from itself
        if (sender_id == peer.id) return;

        System.out.printf("< Peer %d | %d bytes | Chunk number %d\n", peer.id, msg_len, chunk_no);

        peer.storage.putChunk(file_id, chunk_no, body);
    }

    public void send(byte[] buffer) {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);

        try {
            socket.send(packet);
        }

        catch (IOException e) {
            System.err.println("ERROR: Failed to send packet.");
        }
    }
}
