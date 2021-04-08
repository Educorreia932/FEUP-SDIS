package messages;

public class ChunkMessage extends Message{
    private final int chunk_no;

    public ChunkMessage(String version, int sender_id, String file_id, int chunk_no){
        super(version, "CHUNK", sender_id, file_id);
        this.chunk_no = chunk_no;
    }

    @Override
    public String toString() {
        String content = String.format("%d", chunk_no);
        return super.getHeader(content);
    }
}
