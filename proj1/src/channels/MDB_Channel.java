package channels;

import handlers.PutChunkMessageHandler;
import handlers.RemovedMessageHandler;
import messages.Fields;
import messages.Message;
import messages.PutChunkMessage;
import peer.Peer;

public class MDB_Channel extends Channel implements Runnable{

    public MDB_Channel(String host, int port, Peer peer) {
        super(host, port, peer);
    }

    @Override
    protected void parseMessage(byte[] msg, int msg_len) {
        byte[] header = Message.getHeaderBytes(msg);
        byte[] body = Message.getBodyBytes(msg, msg_len, header.length);
        String[] header_fields = Message.getHeaderFields(msg);

        // Parse fields
        String type = header_fields[Fields.MSG_TYPE.ordinal()];
        int sender_id = Integer.parseInt(header_fields[Fields.SENDER_ID.ordinal()]);

        // Ignore message from itself
        if (sender_id == peer.id) return;

        if(type.equals("PUTCHUNK")){
            PutChunkMessage put_chunk_msg = new PutChunkMessage(header_fields);
            // Notify
            notifyObserver(put_chunk_msg.getFile_id(), put_chunk_msg.getChunk_no());
            //Log
            System.out.printf("> Peer %d received: %s\n", peer.id, put_chunk_msg.toString());
            // Putchunk Message Handler
            pool.execute(new PutChunkMessageHandler(put_chunk_msg, body, peer));
        }
    }
}
