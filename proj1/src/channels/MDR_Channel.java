package channels;

import messages.Fields;
import messages.Message;
import peer.Peer;
import utils.Pair;

import java.util.ArrayList;
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

        // Ignore message from itself
        int sender_id = Integer.parseInt(header_fields[Fields.SENDER_ID.ordinal()]);
        if (sender_id == peer.id) return;

        String type = header_fields[Fields.MSG_TYPE.ordinal()];

        if (type.equals("CHUNK")) {
            // Parse fields
            int chunk_no = Integer.parseInt(header_fields[Fields.CHUNK_NO.ordinal()]);
            String file_id = header_fields[Fields.FILE_ID.ordinal()];
            byte[] body = Message.getBodyBytes(msg, msg_len, header.length);

            if(body != null){
                this.received_chunk_msg.set(true);
                System.out.printf("< Peer %d received | %d bytes | CHUNK %d | FROM Peer %d\n", peer.id, body.length, chunk_no, sender_id);

                if(peer.storage.isFileBackedUp(file_id).get()) // Store chunk only if peer asked for it
                    received_chunks.putIfAbsent(Pair.create(file_id, chunk_no), body); // Store chunk
            }
        }
    }

    public void restoreFileChunks(String file_path, String file_id, int number_of_chunks) {
        ArrayList<byte[]> chunks = new ArrayList<>();

        for (int chunk_no = 0; chunk_no < number_of_chunks; chunk_no++)
            chunks.add(received_chunks.remove(Pair.create(file_id, chunk_no)));

        peer.storage.writeFile(file_path, chunks);
    }
}
