package subprotocols;

import channels.MC_Channel;
import peer.Peer;

public abstract class Subprotocol implements Runnable {
    protected MC_Channel control_channel;
    protected String version;
    protected Peer initiator_peer;

    public Subprotocol(MC_Channel control_channel, String version, Peer initiator_peer) {
        this.control_channel = control_channel;
        this.version = version;
        this.initiator_peer = initiator_peer;
    }
}
