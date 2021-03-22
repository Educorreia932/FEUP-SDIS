package channels;

import Peer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Channel implements Runnable{
    private boolean running;
    private String host;
    private int port;
    private Peer peer;
    private MulticastSocket socket;
    private InetAddress group;
    private byte[] buf = new byte[256];
    private final static int MAX_SIZE = 70000; // TODO: what is the best value

    public Channel(String host, int port, Peer peer){
        this.host = host;
        this.port = port;
        this.peer = peer;
    }

    @Override
    public void run() {
        start();    // Start channel
        running = true;
        try {
            while(running){
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet); // Receive packet
                peer.parseMsg(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        try {
            socket.leaveGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket.close();
    }

    private void start(){
        try {
            socket= new MulticastSocket(port);
            group = InetAddress.getByName(host);
            socket.joinGroup(group);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(byte[] buffer){
        DatagramPacket packet
                = new DatagramPacket(buffer, buffer.length, group, port);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
