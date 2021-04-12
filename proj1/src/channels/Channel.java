package channels;

import handlers.MessageHandler;
import handlers.RemovedMessageHandler;
import peer.Peer;
import utils.Observer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Channel implements Runnable {
    protected MulticastSocket socket;
    protected boolean running;
    protected String host;
    protected int port;
    protected Peer peer;
    protected InetAddress group;
    protected final static int MAX_SIZE = 66000;
    protected byte[] buf = new byte[MAX_SIZE];
    protected final ExecutorService pool;
    private final Set<Observer> observers;

    public Channel(String host, int port, Peer peer) {
        this.host = host;
        this.port = port;
        this.peer = peer;
        pool = Executors.newCachedThreadPool();
        observers = ConcurrentHashMap.newKeySet();
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
                System.err.println("No more messages.");
                stop();
            }
        }
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

    protected abstract void parseMessage(byte[] msg, int msg_len);

    void notifyObserver(String file_id, int chunk_no) {
        for (Observer observer : observers)
            observer.notify(file_id, chunk_no);
    }

    public void subscribe(Observer subscriber){
        observers.add(subscriber);
    }

    public void unsubscribe(Observer subscriber){
        observers.remove(subscriber);
    }
}
