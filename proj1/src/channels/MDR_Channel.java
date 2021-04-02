package channels;

import peer.Peer;

public class MDR_Channel extends Channel {
    public MDR_Channel(String host, int port, Peer peer) {
        super(host, port, peer);
    }

    @Override
    public void parseMessage(byte[] msg, int msg_len) {

    }
}
