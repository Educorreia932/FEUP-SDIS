package channels;

import peer.Peer;

public class MC_Channel extends Channel {
    public MC_Channel(String host, int port, Peer peer) {
        super(host, port, peer);
    }

    @Override
    public void run() {

    }
}
