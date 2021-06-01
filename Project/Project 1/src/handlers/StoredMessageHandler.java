package handlers;

import messages.StoredMessage;
import peer.Peer;

public class StoredMessageHandler extends MessageHandler{
    private final int chunk_no;
    private final int sender_id;
    private final Peer peer;

    public StoredMessageHandler(StoredMessage stored_msg, Peer peer){
        super(stored_msg.getFile_id(), peer.storage);
        chunk_no = stored_msg.getChunk_no();
        sender_id = stored_msg.getSender_id();
        this.peer = peer;
    }

    @Override
    public void run() {
        // Increment RP
        storage.updateReplicationDegree(file_id, chunk_no, sender_id, true);
        peer.saveStorage(); // Update storage
    }
}
