package channels;

import peer.Peer;
import java.net.InetAddress;

public abstract class Channel {
    protected boolean running;
    protected String host;
    protected int port;
    protected Peer peer;
    protected InetAddress group;
    protected final static int MAX_SIZE = 66000;
    protected byte[] buf = new byte[MAX_SIZE];

    public Channel(String host, int port, Peer peer) {
        this.host = host;
        this.port = port;
        this.peer = peer;
    }

    public abstract int start();
    public abstract void stop();
    public abstract void send(byte[] buffer);
}
