package subprotocols;

import channels.MC_Channel;
import messages.RemovedMessage;
import peer.Peer;
import peer.storage.Chunk;

public class Reclaim extends Subprotocol {
    private final long max_space;

    public Reclaim(MC_Channel control_channel, String version, Peer initiator_peer, long max_space) {
        super(control_channel, version, initiator_peer);

        this.max_space = max_space;
    }

    @Override
    public void run() {
        // Store
        initiator_peer.storage.setMaxSpace(max_space);

        while(initiator_peer.storage.getUsedSpace().get() > max_space) {
            Chunk chunk = initiator_peer.storage.removeRandomChunk(); // Remove a chunk

            if (chunk == null){
                System.out.println("No more chunks to delete. Aborting reclaim...");
                return;
            }

            // Send REMOVED msg
            RemovedMessage message = new RemovedMessage(version, initiator_peer.id, chunk.getFile_id(), chunk.getChunk_no());
            control_channel.send(message.getBytes(null, 0));
            System.out.printf("< Peer %d sent | REMOVED %d\n", initiator_peer.id, chunk.getChunk_no());
        }

        System.out.println("Finished Reclaim.");
    }
}
