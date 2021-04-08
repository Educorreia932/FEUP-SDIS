package channels;

import messages.Fields;
import messages.Message;
import peer.Peer;

public class MC_Channel extends Channel {
    public MC_Channel(String host, int port, Peer peer) {
        super(host, port, peer);
    }

    @Override
    protected void parseMessage(byte[] msg, int msg_len) {
        String[] header_fields = Message.getHeaderFields(msg);

        // Parse fields
        String type = header_fields[Fields.MSG_TYPE.ordinal()], file_id;
        int sender_id = Integer.parseInt(header_fields[Fields.SENDER_ID.ordinal()]),
        chunk_no;

        // Ignore message from itself
        if (sender_id == peer.id) return;

        switch (type){
            case "STORED":
                chunk_no = Integer.parseInt(header_fields[Fields.CHUNK_NO.ordinal()]);
                file_id = header_fields[Fields.FILE_ID.ordinal()];
                // Log
                System.out.printf("> Peer %d Received | %s %d | FROM Peer %d \n", peer.id, type, chunk_no, sender_id);

                // Increment RP
                peer.storage.updateReplicationDegree(file_id, chunk_no, sender_id, true);
                peer.saveStorage(); // Update storage
                break;

            case "GETCHUNK":
                chunk_no = Integer.parseInt(header_fields[Fields.CHUNK_NO.ordinal()]);
                // Log
                System.out.printf("> Peer %d Received | %s %d | FROM Peer %d \n", peer.id, type, chunk_no, sender_id);

                peer.getChunk(header_fields);
                break;

            case "DELETE":
                // Log
                System.out.printf("> Peer %d Received | %s | FROM Peer %d \n", peer.id, type, sender_id);

                peer.storage.deleteFile(header_fields[Fields.FILE_ID.ordinal()]);
                peer.saveStorage(); // Update storage
                break;

            case "REMOVED":
                chunk_no = Integer.parseInt(header_fields[Fields.CHUNK_NO.ordinal()]);
                file_id = header_fields[Fields.FILE_ID.ordinal()];

                // Log
                System.out.printf("> Peer %d Received | %s %d | FROM Peer %d \n", peer.id, type, chunk_no, sender_id);

                // Decrement RP
                peer.storage.updateReplicationDegree(file_id, chunk_no, sender_id, false);
                peer.updateChunkRP(file_id, chunk_no); // Check if perceived RP < Desired RP
                peer.saveStorage(); // Update storage
                break;
        }
    }
}
