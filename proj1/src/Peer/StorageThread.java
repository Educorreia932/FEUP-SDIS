package Peer;

public class StorageThread implements Runnable{
    private String version;
    private String msg_type;
    private String sender_id;
    private String file_id;
    private int chunkno;
    private int replication_degree;

    public StorageThread(String version, String msg_type, String sender_id,
                         String file_id, int chunkno, int replication_degree){
        this.version = version;
        this.msg_type = msg_type;
        this.sender_id = sender_id;
        this.file_id = file_id;
        this.chunkno = chunkno;
        this.replication_degree = replication_degree;
    }

    public StorageThread(String version, String msg_type, String sender_id,
                         String file_id, int chunkno){
        this.version = version;
        this.msg_type = msg_type;
        this.sender_id = sender_id;
        this.file_id = file_id;
        this.chunkno = chunkno;
    }

    public StorageThread(String version, String msg_type, String sender_id,
                         String file_id){
        this.version = version;
        this.msg_type = msg_type;
        this.sender_id = sender_id;
        this.file_id = file_id;
    }

    @Override
    public void run() {
        switch (msg_type){
            case "PUTCHUNK":
                putchunk();
                break;
            case "STORED":
            case "GETCHUNK":
            case "CHUNK":
            case "REMOVED":
            case "DELETE":
                System.out.println("Not yet implemented");
                break;
            default:
                System.out.println("Unknown message type.");
        }
    }

    public void putchunk(){
        System.out.println("Saving chunk...");
    }
}
