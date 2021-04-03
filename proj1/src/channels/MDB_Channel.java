package channels;

import messages.Message;
import peer.Peer;

public class MDB_Channel extends Channel implements Runnable{

    public MDB_Channel(String host, int port, Peer peer) {
        super(host, port, peer);
    }

    public void parseMessage(byte[] msg, int msg_len) {
        byte[] header = Message.getHeaderBytes(msg);
        byte[] body = Message.getBodyBytes(msg, msg_len, header.length);

        String header_string = new String(header);
        String[] header_fields = header_string.split(" "); // Split header by spaces

        // Ignore message from itself
        int sender_id = Integer.parseInt(header_fields[2]);
        if (sender_id == peer.id) return;

        peer.storeChunk(header_fields, body, msg_len);
    }


}
