package channels;

import messages.Message;
import peer.Peer;
import java.io.IOException;
import java.net.DatagramPacket;

public class MDB_Channel extends Channel {
    public MDB_Channel(String host, int port, Peer peer) {
        super(host, port, peer);
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

                int packetLength = packet.getLength();
                byte[] packetData = new byte[packetLength];
                System.arraycopy(packet.getData(), 0, packetData, 0, packetLength);

                parseMessage(packetData);
            }

            catch (IOException e) {
                System.err.println("ERROR: Failed to receive packet.");
            }
        }
    }

    public void parseMessage(byte[] message_bytes) {
        byte[] header = Message.getHeaderBytes(message_bytes);
        byte[] body = Message.getBodyBytes(message_bytes, header.length);

        String header_string = new String(header);
        String[] header_fields = header_string.split(" "); // Split header by spaces

        int sender_id = Integer.parseInt(header_fields[2]);

        // Ignore message from itself
        if (sender_id == peer.id)
            return;

        String file_id = header_fields[3];
        int chunk_no = Integer.parseInt(header_fields[4]);

        System.out.printf("< Peer %d | %d bytes | Chunk number %d\n", peer.id, message_bytes.length, chunk_no);

        new Thread(() -> peer.storage.putChunk(file_id, chunk_no, body)).start();
    }
}
