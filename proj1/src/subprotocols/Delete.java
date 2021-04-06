package subprotocols;

import channels.MC_Channel;
import messages.DeleteMessage;
import peer.Peer;

public class Delete extends Subprotocol {
    private Peer initiator_peer;
    private DeleteMessage message;
    private final int MAX_TRIES = 3;

    public Delete(Peer initiator_peer, String version, String file_id, MC_Channel control_channel) {
        super(control_channel, version, initiator_peer, file_id);

        this.initiator_peer = initiator_peer;
        this.message = new DeleteMessage(version, initiator_peer.id, file_id);
    }


    @Override
    public void run() {
        for(int i = 0; i < MAX_TRIES; i++) {
            byte[] message_bytes = message.getBytes(null, 0);
            control_channel.send(message_bytes);
            System.out.printf("< Peer %d | %d bytes | DELETE \n", initiator_peer.id, message_bytes.length);

            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
