package handlers;

import channels.MDB_Channel;
import messages.RemovedMessage;
import peer.Peer;
import peer.storage.Chunk;
import subprotocols.Backup;

import java.io.File;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class RemovedMessageHandler extends MessageHandler {
    private final int chunk_no;
    private final int sender_id;
    private final String version;
    private final Peer peer;
    private final MDB_Channel backup_channel;
    private final AtomicBoolean abort;

    public RemovedMessageHandler(RemovedMessage removed_msg, Peer peer) {
        super(removed_msg.getFile_id(), peer.storage);
        chunk_no = removed_msg.getChunk_no();
        sender_id = removed_msg.getSender_id();
        version = removed_msg.getVersion();
        this.peer = peer;
        backup_channel = peer.getBackup_channel();
        abort = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        // Decrement RP
        storage.updateReplicationDegree(file_id, chunk_no, sender_id, false);
        achieveDesiredChunkRP(); // Check if perceived RP < Desired RP
        peer.saveStorage(); // Update storage
    }

    private void achieveDesiredChunkRP() {
        Chunk chunk = storage.getStoredChunk(file_id, chunk_no);

        if (chunk != null && chunk.needsBackUp()) { // Initiate backup if perceived RP < Desired RP
            File chunk_file = storage.getFile(file_id, chunk_no);

            if (chunk_file != null) {
                // Subscribe
                backup_channel.subscribe(this);

                //Sleep
                int sleep_time = new Random().nextInt(400); // Sleep (0-400)ms
                try {
                    Thread.sleep(sleep_time);
                    // Unsubscribe
                    backup_channel.unsubscribe(this);
                    // Abort
                    if(abort.get()) return;

                    Backup task = new Backup(peer, version, chunk_file, chunk.getFile_id(),
                            1, chunk_no, chunk.getDesired_rep_deg(), peer.getBackup_channel(),
                            peer.getControl_channel());
                    peer.pool.execute(task);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void notify(String file_id, int chunk_no){
        abort.set(file_id.equals(this.file_id) && chunk_no == this.chunk_no);
    }
}
