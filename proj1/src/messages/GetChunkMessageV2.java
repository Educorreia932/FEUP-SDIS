package messages;

public class GetChunkMessageV2 extends Message {
    private final int port;
    protected int chunk_no;

    public GetChunkMessageV2(String version, int sender_id, String file_id, int chunk_no, int port) {
        super(version, "GETCHUNK", sender_id, file_id);
        this.port = port;
        this.chunk_no = chunk_no;
    }

    public GetChunkMessageV2(String[] header_fields) {
        super(header_fields);
        this.port = Integer.parseInt(header_fields[Fields.REP_DEG.ordinal()]);
        this.chunk_no = Integer.parseInt(header_fields[Fields.CHUNK_NO.ordinal()]);
    }

    @Override
    public String getHeader() {
        String content = String.format("%d %d", chunk_no, port);
        return super.getHeader(content);
    }

    @Override
    public String toString()
    {
        return super.toString() + String.format("%d %d", chunk_no, port);
    }

    public int getPort() {
        return port;
    }

    public int getChunk_no() {
        return chunk_no;
    }

    public void setChunkNo(int chunk_no) {
        this.chunk_no = chunk_no;
    }
}
