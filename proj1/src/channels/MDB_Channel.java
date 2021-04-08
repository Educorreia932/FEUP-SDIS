package channels;

import messages.Fields;
import messages.Message;
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

        // Ignore message from itself
        int sender_id = Integer.parseInt(header_fields[Fields.SENDER_ID.ordinal()]);
        if (sender_id == peer.id) return;

        String type = header_fields[Fields.MSG_TYPE.ordinal()];

        if(type.equals("PUTCHUNK")){
            String chunk_no = header_fields[Fields.CHUNK_NO.ordinal()];
            System.out.printf("> Peer %d received | %d bytes | PUTCHUNK %s | FROM Peer %d \n", peer.id, msg_len, chunk_no, sender_id);
            peer.putChunk(header_fields, body); // Store chunk
            peer.saveStorage(); // Update storage
        }
    }
}
