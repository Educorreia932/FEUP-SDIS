package messages;

public class GetChunkMessageV2 extends GetChunkMessage {
    private final int port;
    private final String address;

    public GetChunkMessageV2(String version, int sender_id, String file_id, int chunk_no, int port,
                             String address) {
        super(version, sender_id, file_id, chunk_no);
        this.port = port;
        this.address = address;
    }

    public GetChunkMessageV2(String[] header_fields) {
        super(header_fields);
        this.port = Integer.parseInt(header_fields[Fields.REP_DEG.ordinal()]);
        this.address = header_fields[Fields.REP_DEG.ordinal() + 1];
    }

    @Override
    public String getHeader() {
        String content = String.format("%d %d %s", super.chunk_no, port, address);
        return super.getHeader(content);
    }

    @Override
    public String toString(){
        return super.toString() + String.format(" %d %s", port, address);
    }

    public int getPort() {
        return port;
    }

    public String getAddress() {
        return address;
    }
}
