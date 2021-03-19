package messages;

public abstract class Message {
    protected String version;
    protected String type;
    protected String sender_id;
    protected String file_id;
    protected final String CRLF = "\r\n";

    public Message(String version, String type, String sender_id, String file_id) {
        this.version = version;
        this.type = type;
        this.sender_id = sender_id;
        this.file_id = file_id;
    }

    public String generateHeader(String content) {
        return String.format("%s %s %s %s %s %s %s", version, type, sender_id, file_id, content, CRLF, CRLF);
    }
}
