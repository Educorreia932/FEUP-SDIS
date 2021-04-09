package subprotocols;

import channels.MC_Channel;
import messages.DeleteMessage;
import peer.Peer;

public class Delete extends Subprotocol {
    private final Peer initiator_peer;
    private final DeleteMessage message;

    public Delete(Peer initiator_peer, String version, String file_id, MC_Channel control_channel) {
        super(control_channel, version, initiator_peer);
        this.initiator_peer = initiator_peer;
        this.message = new DeleteMessage(version, initiator_peer.id, file_id);
    }

    @Override
    public void run() {
        int MAX_TRIES = 3;
        byte[] message_bytes = message.getBytes(null, 0);

        for(int i = 0; i < MAX_TRIES; i++) {
            // Send message
            control_channel.send(message_bytes);
            System.out.printf("< Peer %d sent: %s\n", initiator_peer.id, message.toString());

            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("DELETE of " + message.getFile_id() + " finished.");
    }
}
