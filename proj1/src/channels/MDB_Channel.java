package channels;

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

                peer.parseMessage(packetData);
            }

            catch (IOException e) {
                System.err.println("ERROR: Failed to receive packet.");
            }
        }
    }
}
