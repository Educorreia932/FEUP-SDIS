package channels;

import messages.*;
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
        int sender_id = Integer.parseInt(header_fields[Fields.SENDER_ID.ordinal()]);

        // Ignore message from itself
        if (sender_id == peer.id) return;

        switch (type){
            case "STORED":
                StoredMessage stored_msg = new StoredMessage(header_fields);
                // Log
                System.out.printf("> Peer %d received: %s\n", peer.id, stored_msg.toString());
                // Stored Message Handler
                peer.storedMessageHandler(stored_msg.getFile_id(), stored_msg.getChunk_no(), sender_id);
                break;

            case "GETCHUNK":
                GetChunkMessage get_chunk_msg = new GetChunkMessage(header_fields);
                // Log
                System.out.printf("> Peer %d received: %s\n", peer.id, get_chunk_msg.toString());
                // GetChunk Message Handler
                peer.getChunkMessageHandler(get_chunk_msg);
                break;

            case "DELETE":
                DeleteMessage delete_msg = new DeleteMessage(header_fields);
                // Log
                System.out.printf("> Peer %d received: %s\n", peer.id, delete_msg.toString());
                // Delete Message Handler
                peer.deleteMessageHandler(delete_msg.getFile_id());
                break;

            case "REMOVED":
                RemovedMessage removed_msg = new RemovedMessage(header_fields);
                // Log
                System.out.printf("> Peer %d received: %s\n", peer.id, removed_msg.toString());
                // Removed Message Handler
                peer.removedMessageHandler(removed_msg.getFile_id(), removed_msg.getChunk_no(), sender_id);
                break;
        }
    }
}
