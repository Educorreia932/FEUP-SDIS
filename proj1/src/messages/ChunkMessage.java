package messages;

import java.nio.charset.StandardCharsets;

public class ChunkMessage extends Message{
    private int chunk_no;

    public ChunkMessage(String version, int sender_id, String file_id, int chunk_no){
        super(version, "CHUNK", sender_id, file_id);
        this.chunk_no = chunk_no;
    }

    @Override
    public String toString() {
        String content = String.format("%d", chunk_no);
        return super.getHeader(content);
    }

    /**
     * Returns full message byte array
     *
     * @param body to include in byte array
     * @return Message byte array
     */
    @Override
    public byte[] getBytes(byte[] body, int body_len) {
        byte[] header = toString().getBytes(StandardCharsets.UTF_8);
        byte[] message = new byte[header.length + body_len];

        // Copy contents to msg array
        System.arraycopy(header, 0, message, 0, header.length);
        System.arraycopy(body, 0, message, header.length, body_len);

        return message;
    }
}
