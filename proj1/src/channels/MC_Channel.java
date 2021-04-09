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
                // Parse fields
                chunk_no = Integer.parseInt(header_fields[Fields.CHUNK_NO.ordinal()]);
                file_id = header_fields[Fields.FILE_ID.ordinal()];

                // Log
                System.out.printf("> Peer %d Received | %s %d | FROM Peer %d \n", peer.id, type, chunk_no, sender_id);

                // Stored Message Handler
                peer.storedMessageHandler(file_id, chunk_no, sender_id);
                break;

            case "GETCHUNK":
                // Parse fields
                chunk_no = Integer.parseInt(header_fields[Fields.CHUNK_NO.ordinal()]);

                // Log
                System.out.printf("> Peer %d Received | %s %d | FROM Peer %d \n", peer.id, type, chunk_no, sender_id);

                // GetChunk Message Handler
                peer.getChunkMessageHandler(header_fields);
                break;

            case "DELETE":
                // Parse fields
                file_id = header_fields[Fields.FILE_ID.ordinal()];

                // Log
                System.out.printf("> Peer %d Received | %s | FROM Peer %d \n", peer.id, type, sender_id);

                // Delete Message Handler
                peer.deleteMessageHandler(file_id);
                break;

            case "REMOVED":
                // Parse fields
                chunk_no = Integer.parseInt(header_fields[Fields.CHUNK_NO.ordinal()]);
                file_id = header_fields[Fields.FILE_ID.ordinal()];

                // Log
                System.out.printf("> Peer %d Received | %s %d | FROM Peer %d \n", peer.id, type, chunk_no, sender_id);

                // Removed Message Handler
                peer.removedMessageHandler(file_id, chunk_no, sender_id);
                break;
        }
    }
}
