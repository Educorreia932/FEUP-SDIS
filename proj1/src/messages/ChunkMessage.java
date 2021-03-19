package messages;

public class ChunkMessage extends Message{
    private int chunk_no;

    public ChunkMessage(String version, String sender_id, String file_id, int chunk_no){
        super(version, "CHUNK", sender_id, file_id);
        this.chunk_no = chunk_no;
    }

    @Override
    public String toString() {
        String content = String.format("%d", chunk_no);
        return super.generateHeader(content);
    }
}
