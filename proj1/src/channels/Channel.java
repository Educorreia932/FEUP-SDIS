package channels;

import messages.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class Channel implements Runnable{
    private boolean running;
    private String host;
    private int port;
    private MulticastSocket socket;
    private InetAddress group;
    private byte[] buf = new byte[256];
    private final static int MAX_SIZE = 70000; // TODO: what is the best value

    public Channel(String host, int port){
        this.host = host;
        this.port = port;
        running = false;
    }

    @Override
    public void run() {
        start();    // Start channel
        running = true;
        try {
            while (running) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet); // Receive packet
            }
            stop(); // stop channel
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stop() throws IOException {
        socket.leaveGroup(group);
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
