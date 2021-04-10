package handlers;

import messages.RemovedMessage;
import peer.Peer;
import peer.storage.Chunk;
import subprotocols.Backup;

import java.io.File;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RemovedMessageHandler extends MessageHandler {
    private final int chunk_no;
    private final int sender_id;
    private final String version;
    private final Peer peer;

    public RemovedMessageHandler(RemovedMessage removed_msg, Peer peer) {
        super(removed_msg.getFile_id(), peer.storage);
        chunk_no = removed_msg.getChunk_no();
        sender_id = removed_msg.getSender_id();
        version = removed_msg.getVersion();
        this.peer = peer;
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

                //Sleep
                int sleep_time = new Random().nextInt(400); // Sleep (0-400)ms
                ScheduledThreadPoolExecutor scheduledPool = new ScheduledThreadPoolExecutor(1);
                scheduledPool.schedule(() -> {
                    // TODO: Abort if received PUTCHUNK
                    Backup task = new Backup(peer, version, chunk_file, chunk.getFile_id(),
                            1, chunk.getDesired_rep_deg(), peer.getBackup_channel(),
                            peer.getControl_channel());
                    peer.pool.execute(task);
                }, sleep_time, TimeUnit.MILLISECONDS);
            }
        }
    }
}
