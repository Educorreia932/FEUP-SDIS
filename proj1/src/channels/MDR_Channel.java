package channels;

import messages.Fields;
import messages.Message;
import peer.Peer;
import utils.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class MDR_Channel extends Channel {
    public HashMap<Pair<String, Integer>, byte[]> received_chunks;
    public final Semaphore sem;

    public MDR_Channel(String host, int port, Peer peer) {
        super(host, port, peer);
        received_chunks = new HashMap<>();
        sem = new Semaphore(1);
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

            System.out.printf("< Peer %d | %d bytes | CHUNK %d\n", peer.id, body.length, chunk_no);

            // Store chunk
            try {
                sem.acquire();
                received_chunks.put(Pair.create(file_id, chunk_no), body);
                sem.release();
            }

            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void restoreFileChunks(String file_path, String file_id, int number_of_chunks) {
        ArrayList<byte[]> chunks = new ArrayList<>();

        try {
            this.sem.acquire();
        }

        catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int chunk_no = 0; chunk_no < number_of_chunks; chunk_no++)
            chunks.add(received_chunks.remove(Pair.create(file_id, chunk_no)));

        this.sem.release();

        peer.storage.writeFile(file_path, chunks);
    }
}
