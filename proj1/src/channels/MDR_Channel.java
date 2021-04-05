package channels;

import messages.Fields;
import messages.Message;
import peer.Peer;
import peer.storage.Chunk;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class MDR_Channel extends Channel {
    public Set<Chunk> received_chunks;
    public final Semaphore sem;

    public MDR_Channel(String host, int port, Peer peer) {
        super(host, port, peer);
        received_chunks = new HashSet<>();
        sem = new Semaphore(1);
    }

    @Override
    protected void parseMessage(byte[] msg, int msg_len) {
        byte[] header = Message.getHeaderBytes(msg);

        String header_string = new String(header);
        String[] header_fields = header_string.split(" "); // Split header by spaces

        // Ignore message from itself
        int sender_id = Integer.parseInt(header_fields[Fields.SENDER_ID.ordinal()]);
        if (sender_id == peer.id) return;

        String type = header_fields[Fields.MSG_TYPE.ordinal()];
        if(type.equals("CHUNK")){
            // Parse fields
            int chunk_no  = Integer.parseInt(header_fields[Fields.CHUNK_NO.ordinal()]);
            String file_id = header_fields[Fields.FILE_ID.ordinal()];
            byte[] body = Message.getBodyBytes(msg, msg_len, header.length);

            System.out.printf("< Peer %d | %d bytes | CHUNK %d\n", peer, body.length, chunk_no);

            try { // Store chunk
                sem.acquire();
                received_chunks.add(new Chunk(chunk_no, body, file_id));
                sem.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
