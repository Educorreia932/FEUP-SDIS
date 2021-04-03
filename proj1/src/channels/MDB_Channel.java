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

        String header_string = new String(header);
        String[] header_fields = header_string.split(" "); // Split header by spaces

        // Ignore message from itself
        int sender_id = Integer.parseInt(header_fields[Fields.SENDER_ID.ordinal()]);
        if (sender_id == peer.id) return;

        String type = header_fields[1];
        if(type.equals("PUTCHUNK")){
            String chunk_no = header_fields[4];
            System.out.printf("> Peer %d | %d bytes | PUTCHUNK %s\n", peer.id, msg_len, chunk_no);
            peer.putChunk(header_fields, body, msg_len);
        }
    }
}
