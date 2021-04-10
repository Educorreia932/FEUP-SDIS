package MessageHandlers;

import channels.MDR_Channel;
import messages.ChunkMessage;
import messages.GetChunkMessage;
import peer.Peer;
import peer.storage.Storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class GetChunkMessageHandler extends MessageHandler{
    private final int chunk_no;
    private final int peer_id;
    private final String version;
    private final MDR_Channel restore_channel;

    public GetChunkMessageHandler(GetChunkMessage get_chunk_msg, Peer peer){
        super(get_chunk_msg.getFile_id(), peer.storage);
        chunk_no = get_chunk_msg.getChunk_no();
        version = get_chunk_msg.getVersion();
        peer_id = peer.id;
        restore_channel = peer.getRestore_channel();
    }

    @Override
    public void run() {
        File chunk = storage.getFile(file_id, chunk_no);

        if (chunk == null)
            return; // Chunk is not stored

        int read_bytes = 0;
        byte[] body = new byte[Storage.MAX_CHUNK_SIZE], message_bytes;
        ChunkMessage message = new ChunkMessage(version, peer_id, file_id, chunk_no);

        // Create Message
        try {
            // Read chunk
            FileInputStream inputStream = new FileInputStream(chunk.getPath());

            if (inputStream.available() > 0) // Check if empty
                read_bytes = inputStream.read(body); // Read chunk

            // Get message byte array
            message_bytes = message.getBytes(body, read_bytes);

            restore_channel.received_chunk_msg.set(false); // TODO: many protocols at same time dont work ???

            // Sleep
            int sleep_time = new Random().nextInt(400); // Sleep (0-400)ms
            ScheduledThreadPoolExecutor scheduledPool = new ScheduledThreadPoolExecutor(1);
            scheduledPool.schedule(() -> {
                // Abort if received chunk message
                if (restore_channel.received_chunk_msg.get()) return;

                // Send message
                restore_channel.send(message_bytes);
                System.out.printf("< Peer %d Sent: %s\n", peer_id, message.toString()); // Log
            }, sleep_time, TimeUnit.MILLISECONDS);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
