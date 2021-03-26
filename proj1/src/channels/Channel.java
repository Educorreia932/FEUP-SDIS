package channels;

import peer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Channel implements Runnable {
    private boolean running;
    private String host;
    private int port;
    private Peer peer;
    private MulticastSocket socket;
    private InetAddress group;
    private final static int MAX_SIZE = 66000;
    private byte[] buf = new byte[MAX_SIZE];

    public Channel(String host, int port, Peer peer) {
        this.host = host;
        this.port = port;
        this.peer = peer;
    }

    @Override
    public void run() {
        if (start() != 0) return;
        running = true;

        while (running) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, MAX_SIZE);
                socket.receive(packet); // Receive packet
                System.out.println("Received packet with " + packet.getData().length + " bytes.");
                peer.parseMessage(packet.getData());
            }

            catch (IOException e) {
                System.err.println("ERROR: Failed to receive packet.");
            }
        }
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

    private int start() {
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
