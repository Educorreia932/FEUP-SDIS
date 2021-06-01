package messages;

public class GetChunkMessage extends Message {
    protected int chunk_no;

    public GetChunkMessage(String version, int sender_id, String file_id, int chunk_no) {
        super(version, "GETCHUNK", sender_id, file_id);
        this.chunk_no = chunk_no;
    }

    public GetChunkMessage(String[] header_fields){
        super(header_fields);
        this.chunk_no = Integer.parseInt(header_fields[Fields.CHUNK_NO.ordinal()]);
    }

    @Override
    public String getHeader() {
        String content = String.format("%d", chunk_no);
        return super.getHeader(content);
    }

    @Override
    public String toString(){
        return super.toString() + String.format("%d", chunk_no);
    }

    public void setChunkNo(int chunk_no) {
        this.chunk_no = chunk_no;
    }

    public int getChunk_no() {
        return chunk_no;
    }

}
