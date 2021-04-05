package subprotocols;

import channels.MC_Channel;
import peer.Peer;

public abstract class Subprotocol implements Runnable {
    private MC_Channel control_channel;
    private String version;
    private Peer initiator_peer;
    private int number_of_chunks;
    private String file_id;

    public Subprotocol(MC_Channel control_channel, String version, Peer initiator_peer, int number_of_chunks, String file_id) {
        this.control_channel = control_channel;
        this.version = version;
        this.initiator_peer = initiator_peer;
        this.number_of_chunks = number_of_chunks;
        this.file_id = file_id;
    }
}
