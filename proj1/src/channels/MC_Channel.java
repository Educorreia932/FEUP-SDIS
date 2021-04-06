package channels;

import messages.Fields;
import messages.Message;
import peer.Peer;
import utils.Pair;

import java.util.HashMap;
import java.util.concurrent.Semaphore;


public class MC_Channel extends Channel {

    public MC_Channel(String host, int port, Peer peer) {
        super(host, port, peer);
    }

    @Override
    protected void parseMessage(byte[] msg, int msg_len) {
        String[] header_fields = Message.getHeaderFields(msg);

        // Parse fields
        String type = header_fields[Fields.MSG_TYPE.ordinal()];
        int sender_id = Integer.parseInt(header_fields[Fields.SENDER_ID.ordinal()]);

        // Ignore message from itself
        if (sender_id == peer.id) return;

        switch (type){
            case "STORED":
                // Parse fields
                String file_id = header_fields[Fields.FILE_ID.ordinal()];
                int chunk_no = Integer.parseInt(header_fields[Fields.CHUNK_NO.ordinal()]);

                System.out.printf("> Peer %d | STORED \n", peer.id);
                peer.storage.incrementReplicationDegree(file_id, chunk_no, sender_id);

                break;

            case "GETCHUNK":
                System.out.printf("> Peer %d | GETCHUNK \n", peer.id);
                peer.getChunk(header_fields);
                break;

            case "DELETE":
                System.out.printf("> Peer %d | DELETE \n", peer.id);
                peer.storage.deleteFile(header_fields[Fields.FILE_ID.ordinal()]);
                break;
        }
    }
}
