package channels;

import peer.Peer;

public class MC_Channel extends Channel implements Runnable{
    public MC_Channel(String host, int port, Peer peer) {
        super(host, port, peer);
    }

    @Override
    public int start() {
        return 0;
    }

    @Override
    public void stop() {

    }

    @Override
    public void send(byte[] buffer) {

    }

    @Override
    public void run() {

    }
}
