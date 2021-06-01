package handlers;

import channels.MDR_Channel;
import messages.ChunkMessage;
import peer.Peer;
import utils.Pair;

public class ChunkMessageHandler extends MessageHandler {
    private final int chunk_no;
    private final byte[] body;
    private final MDR_Channel restore_channel;

    public ChunkMessageHandler(ChunkMessage chunk_msg, Peer peer, byte[] body) {
        super(chunk_msg.getFile_id(), peer.storage);
        chunk_no = chunk_msg.getChunk_no();
        this.body = body;
        restore_channel = peer.getRestore_channel();
    }

    @Override
    public void run() {
        if (storage.isFileBackedUp(file_id).get()) // Store body only if peer has original file
            restore_channel.received_chunks.put(Pair.create(file_id, chunk_no), body);

        else
            restore_channel.received_chunks.put(Pair.create(file_id, chunk_no), new byte[0]);
    }
}
