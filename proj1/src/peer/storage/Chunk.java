package peer.storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Chunk implements Serializable {
    private final String file_id;
    private final int chunk_no;
    private int size;
    private final int desired_rep_deg;
    private final List<Integer> peers;

    public Chunk(String file_id, int chunk_no, int size, int desired_rep_deg, int sender_id) {
        this.file_id = file_id;
        this.chunk_no = chunk_no;
        this.size = size;
        this.desired_rep_deg = desired_rep_deg;
        this.peers = new ArrayList<>(Collections.singletonList(sender_id));
    }

    public Chunk(String file_id, int chunk_no, int desired_rep_deg, int sender_id) {
        this.file_id = file_id;
        this.chunk_no = chunk_no;
        this.desired_rep_deg = desired_rep_deg;
        this.peers = new ArrayList<>(Collections.singletonList(sender_id));
    }

    public String getFile_id() {
        return file_id;
    }

    public int getSize() {
        return size;
    }

    public int getDesired_rep_deg() {
        return desired_rep_deg;
    }

    public int getChunk_no() {
        return chunk_no;
    }

    public synchronized void incrementPerceivedRepDegree(int sender_id) {
        if(!peers.contains(sender_id)){ // Check if sender already sent stored msg
            peers.add(sender_id); // Add sender
        }
    }

    public synchronized void decrementPerceivedRepDegree(int sender_id) {
        for(int i = 0; i < peers.size(); i++){
            if(peers.get(i) == sender_id){ // If peer is registered
                peers.remove(i); // Remove peer from list
                break; // Stop search
            }
        }
    }

    public synchronized int getPerceivedRP() {
        return peers.size();
    }

    public boolean needsBackUp() {
        return peers.size() < desired_rep_deg;
    }
}
