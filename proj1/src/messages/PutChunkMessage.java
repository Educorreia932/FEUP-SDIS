package messages;

import java.nio.charset.StandardCharsets;

public class PutChunkMessage extends Message {
    private final int replication_degree;
    private int chunk_no;

    public PutChunkMessage(String version, int sender_id, String file_id, int replication_degree,
                           int chunk_no){
        super(version, "PUTCHUNK", sender_id, file_id);

        this.replication_degree = replication_degree;
        this.chunk_no = chunk_no;
    }

    @Override
    public String toString() {
        String content = String.format("%d %d", chunk_no, replication_degree);
        return super.getHeader(content);
    }

    public void setChunkNo(int chunk_no){
        this.chunk_no = chunk_no;
    }

    /**
     * Returns full message byte array
     * @param body to include in byte array
     * @return message byte array
     */
    public byte[] getMessage(byte[] body){
        byte[] header = toString().getBytes(StandardCharsets.UTF_8),
        msg = new byte[header.length + body.length];

        // Copy contents to msg array
        System.arraycopy(header, 0, msg, 0, header.length);
        System.arraycopy(body, 0, msg, header.length, body.length);
        return msg;
    }
}
