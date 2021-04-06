package peer;

import peer.storage.BackedUpChunk;
import peer.storage.BackedUpFile;
import utils.Pair;

import java.util.HashMap;
import java.util.Set;

public class PeerState {
    private Set<BackedUpChunk> backed_up_chunks;
    private HashMap<String, BackedUpFile> backed_up_files;
    private HashMap<Pair<String, Integer>, Integer> stored_chunks;

    public PeerState(HashMap<String, BackedUpFile> backed_up_files, HashMap<Pair<String, Integer>, Integer> stored_chunks,
                     Set<BackedUpChunk> backed_up_chunks) {
        this.backed_up_files = backed_up_files;
        this.stored_chunks = stored_chunks;
        this.backed_up_chunks = backed_up_chunks;
    }

    private String getBackedUpFilesInfo(){
        String result = "----------------- \n BACKED UP FILES\n----------------- \n\n";
        for(String path : backed_up_files.keySet()){
            BackedUpFile file = backed_up_files.get(path);
            result +=   "PATH: " + path + '\n'+
                        "ID: " + file.getId() + '\n' +
                        "Desired Replication Degree: " + file.getReplication_degree() + '\n';

            for(int chunk_no = 0; chunk_no < file.getNumberOfChunks(); chunk_no++){
                int rep_deg = stored_chunks.get(Pair.create(path, chunk_no)); //TODO: Check exists
                result += "CHUNK: " + chunk_no + " - Perceived RP: " + rep_deg + "\n";
            }
            result += '\n';
        }
        return result;
    }

    private String getBackedUpChunksInfo(){
        String result = "------------------ \n BACKED UP CHUNKS\n------------------ \n\n";
        for (BackedUpChunk chunk : backed_up_chunks){
            result += "ID: " + chunk.getFile_id() + '/' + chunk.getChunk_no()
                    + "\nSIZE: " + chunk.getSize()
                    + "\nDESIRED REPLICATION DEGREE: " + chunk.getDesired_rep_deg()
                    + "\nPERCEIVED REPLICATION DEGREE: " + "TODO" + '\n'; // TODO: Add: desired
        }
        return result;
    }

    @Override
    public String toString() {
        return getBackedUpFilesInfo() + getBackedUpChunksInfo();
    }
}
