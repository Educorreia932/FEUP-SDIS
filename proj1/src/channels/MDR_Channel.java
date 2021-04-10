package channels;

import handlers.ChunkMessageHandler;
import messages.ChunkMessage;
import messages.Fields;
import messages.Message;
import peer.Peer;
import utils.Pair;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MDR_Channel extends Channel {
    public ConcurrentHashMap<Pair<String, Integer>, byte[]> received_chunks;
    public AtomicBoolean received_chunk_msg;

    public MDR_Channel(String host, int port, Peer peer) {
        super(host, port, peer);
        received_chunks = new ConcurrentHashMap<>();
        this.received_chunk_msg = new AtomicBoolean(false);
    }

    @Override
    protected void parseMessage(byte[] msg, int msg_len) {
        byte[] header = Message.getHeaderBytes(msg);
        String[] header_fields = Message.getHeaderFields(msg);

        int sender_id = Integer.parseInt(header_fields[Fields.SENDER_ID.ordinal()]);
        String type = header_fields[Fields.MSG_TYPE.ordinal()];

        // Ignore message from itself
        if (sender_id == peer.id) return;

        if (type.equals("CHUNK")) {
            this.received_chunk_msg.set(true); // Received chunk message => true

            ChunkMessage chunk_msg = new ChunkMessage(header_fields);
            byte[] body = Message.getBodyBytes(msg, msg_len, header.length);
            if(body == null) body = new byte[0]; // Empty chunk
            // Log
            System.out.printf("< Peer %d received: %s\n", peer.id, chunk_msg.toString());
            // Chunk message handler
            pool.execute(new ChunkMessageHandler(chunk_msg, peer, body));
        }
    }
}
