package peer;

import peer.storage.BackedUpFile;
import utils.Pair;

import java.io.Serializable;
import java.util.HashMap;

public class PeerState {
    private HashMap<String, BackedUpFile> backed_up_files;
    private HashMap<Pair<String, Integer>, Integer> stored_chunks;

    public PeerState(HashMap<String, BackedUpFile> backed_up_files, HashMap<Pair<String, Integer>, Integer> stored_chunks) {
        this.backed_up_files = backed_up_files;
        this.stored_chunks = stored_chunks;
    }

    private String getBackedUpFilesInfo(){
        String result = "--------------- \n BACKED UP FILES\n --------------- \n\n";
        for(String path : backed_up_files.keySet()){
            BackedUpFile file = backed_up_files.get(path);
            result += "PATH: " + path + '\n';
            result += "ID: " + file.getId() + '\n';
            result += "Desired Replication Degree: " + file.getReplication_degree() + '\n';

            for(int chunk_no = 0; chunk_no < file.getNumberOfChunks(); chunk_no++){
                int rep_deg = stored_chunks.get(Pair.create(path, chunk_no)); //TODO: Check exists
                result += "CHUNK: " + chunk_no + " - Perceived RP: " + rep_deg + "\n";
            }
            result += '\n';
        }
        return result;
    }

    @Override
    public String toString() {
        return getBackedUpFilesInfo();
    }
}
