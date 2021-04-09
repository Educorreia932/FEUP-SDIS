package MessageHandlers;

import messages.StoredMessage;
import peer.Peer;
import peer.storage.Storage;

public class StoredMessageHandler implements Runnable{
    private final String file_id;
    private final int chunk_no;
    private final int sender_id;
    private final Storage storage;
    private final Peer peer;

    public StoredMessageHandler(StoredMessage stored_msg, Peer peer){
        file_id = stored_msg.getFile_id();
        chunk_no = stored_msg.getChunk_no();
        sender_id = stored_msg.getSender_id();
        storage = peer.storage;
        this.peer = peer;
    }

    @Override
    public void run() {
        // Increment RP
        storage.updateReplicationDegree(file_id, chunk_no, sender_id, true);
        peer.saveStorage(); // Update storage
    }
}
