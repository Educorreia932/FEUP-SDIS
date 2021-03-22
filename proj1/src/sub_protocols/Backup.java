package sub_protocols;

import channels.MDB_Channel;
import messages.PutChunkMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Backup implements Runnable{
  private final File file;
  private final String file_id;
  private final String version;
  private final int replication_degree;
  private final int initiator_peer;
  private final int CHUNKSIZE = 64000;
  private final MDB_Channel mdb_channel;

  public Backup(Integer peer_id, String version, File file, String file_id,
                int replication_degree, MDB_Channel mdb_channel){
    this.initiator_peer = peer_id;
    this.version = version;
    this.file = file;
    this.file_id = file_id;
    this.replication_degree = replication_degree;
    this.mdb_channel = mdb_channel;
  }

  @Override
  public void run() {
    sendChunks();
  }

  // TODO: Send empty chunk
  private void sendChunks(){
    int read_bytes, chunk_no = 0, bytes_to_read = CHUNKSIZE;
    long file_size = file.length();

    try {
      FileInputStream inputStream = new FileInputStream(file.getPath());

      while(file_size > 0){
        if(file_size < CHUNKSIZE)
          bytes_to_read = (int)file_size;

        // Get header
        PutChunkMessage msg = new PutChunkMessage(version, initiator_peer, file_id, replication_degree, chunk_no);
        byte[] header = msg.toString().getBytes(StandardCharsets.UTF_8);

        // Set buffer
        byte[] buffer = new byte[bytes_to_read + header.length];
        System.arraycopy(header, 0, buffer, 0, header.length); // add header
        read_bytes = inputStream.read(buffer, header.length, bytes_to_read); // add chunk

        // Send buffer
        mdb_channel.send(buffer);

        // Update variables
        file_size -= read_bytes;  // update bytes left to read from file
        chunk_no++; // update chunk
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
