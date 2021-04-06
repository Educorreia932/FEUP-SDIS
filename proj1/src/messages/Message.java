package messages;

import java.nio.charset.StandardCharsets;

public abstract class Message {
    protected String version;
    protected String type;
    protected int sender_id;
    protected String file_id;
    private static final String CRLF = "\r\n";

    public Message(String version, String type, int sender_id, String file_id) {
        this.version = version;
        this.type = type;
        this.sender_id = sender_id;
        this.file_id = file_id;
    }

    protected String getHeader(String content) {
        return String.format("%s %s %d %s %s %s %s", version, type, sender_id, file_id, content, CRLF, CRLF);
    }

    public static byte[] getHeaderBytes(byte[] message) {
        int header_length = getCRLFIndex(message) + 5;
        byte[] header = new byte[header_length];

        System.arraycopy(message, 0, header, 0, header_length);

        return header;
    }

    public static byte[] getBodyBytes(byte[] message_bytes, int msg_len, int header_len) {
        int body_length = msg_len - header_len;

        if (body_length == 0)
            return null;

        byte[] body = new byte[body_length];
        System.arraycopy(message_bytes, header_len, body, 0, body_length);

        return body;
    }

    private static int getCRLFIndex(byte[] msg) {
        byte CR = (byte) 0xD;
        byte LF = (byte) 0xA;

        for (int i = 0; i < msg.length; i++)
            if (msg[i] == CR)
                if (msg[i + 1] == LF)
                    return i;

        return -1;
    }

    /**
     * Returns full message byte array
     *
     * @param body to include in byte array
     * @return Message byte array
     */
    public byte[] getBytes(byte[] body, int body_length) {
        byte[] header = toString().getBytes(StandardCharsets.UTF_8);
        byte[] message = new byte[header.length + body_length];

        // Copy contents to message array
        System.arraycopy(header, 0, message, 0, header.length);

        if (body != null)
            System.arraycopy(body, 0, message, header.length, body_length);

        return message;
    }

    public static String[] getHeaderFields(byte[] message_bytes) {
        String header_string = new String(getHeaderBytes(message_bytes));

        return header_string.split("\\s+"); // Split header by spaces
    }
}
