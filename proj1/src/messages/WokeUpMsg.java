package messages;

public class WokeUpMsg extends Message {
    public WokeUpMsg(int sender_id){
        super("2.0", "WOKEUP", sender_id, "");
    }

    public WokeUpMsg(String[] header_fields){
        super("2.0", "WOKEUP",
               Integer.parseInt(header_fields[Fields.SENDER_ID.ordinal()]), "");
    }
}
