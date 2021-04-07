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
        String type = header_fields[Fields.MSG_TYPE.ordinal()];
        int sender_id = Integer.parseInt(header_fields[Fields.SENDER_ID.ordinal()]),
        chunk_no = Integer.parseInt(header_fields[Fields.CHUNK_NO.ordinal()]);

        // Ignore message from itself
        if (sender_id == peer.id) return;
        // Log
        System.out.printf("> Peer %d Received | %s %d | FROM Peer %d \n", peer.id, type, chunk_no, sender_id);

        switch (type){
            case "STORED":
                String file_id = header_fields[Fields.FILE_ID.ordinal()];
                peer.storage.incrementReplicationDegree(file_id, chunk_no, sender_id);
                peer.saveStorage();
                break;

            case "GETCHUNK":
                peer.getChunk(header_fields);
                break;

            case "DELETE":
                peer.storage.deleteFile(header_fields[Fields.FILE_ID.ordinal()]);
                break;

            case "REMOVED":
                // TODO
                break;
        }
    }
}
