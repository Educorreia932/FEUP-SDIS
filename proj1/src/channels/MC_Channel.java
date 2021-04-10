package channels;

import handlers.DeleteMessageHandler;
import handlers.GetChunkMessageHandler;
import handlers.RemovedMessageHandler;
import handlers.StoredMessageHandler;
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
                pool.execute(new StoredMessageHandler(stored_msg, peer));
                break;

            case "GETCHUNK":
                GetChunkMessage get_chunk_msg = new GetChunkMessage(header_fields);
                // Log
                System.out.printf("> Peer %d received: %s\n", peer.id, get_chunk_msg.toString());
                // GetChunk Message Handler
                pool.execute(new GetChunkMessageHandler(get_chunk_msg, peer));
                break;

            case "DELETE":
                DeleteMessage delete_msg = new DeleteMessage(header_fields);
                // Log
                System.out.printf("> Peer %d received: %s\n", peer.id, delete_msg.toString());
                // Delete Message Handler
                pool.execute(new DeleteMessageHandler(delete_msg, peer));
                break;

            case "REMOVED":
                RemovedMessage removed_msg = new RemovedMessage(header_fields);
                // Log
                System.out.printf("> Peer %d received: %s\n", peer.id, removed_msg.toString());
                // Removed Message Handler
                pool.execute(new RemovedMessageHandler(removed_msg, peer));
                break;
        }
    }
}
