package peer;

import messages.ChunkMessage;
import messages.Fields;
import messages.StoredMessage;
import channels.*;
import peer.storage.BackedUpFile;
import peer.storage.Storage;
import subprotocols.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Peer implements RMI {
    public int id;
    private String version;
    private String access_point;
    private MC_Channel control_channel;
    private MDB_Channel backup_channel;
    private MDR_Channel restore_channel;
    private ExecutorService pool;
    public final Storage storage;

    public static void main(String[] args) {
        if (args.length != 9) {
            usage();
            System.exit(1);
        }

        Peer peer = new Peer(args); // create peer

        try {
            //RMI
            RMI RMI_stub = (RMI) UnicastRemoteObject.exportObject(peer, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(peer.access_point, RMI_stub);
        }

        catch (RemoteException | AlreadyBoundException e) {
            System.err.println("ERROR: Failed to bind peer object in the registry.\n Aborting...");
            System.exit(-1);
        }

        peer.pool = Executors.newCachedThreadPool();
        // Start listening on channels
        peer.pool.execute(peer.backup_channel);
        peer.pool.execute(peer.control_channel);
        peer.pool.execute(peer.restore_channel);
    }

    public Peer(String[] args) {
        try {
            version = args[0];
            id = Integer.parseInt(args[1]);
            access_point = args[2];
            control_channel = new MC_Channel(args[3], Integer.parseInt(args[4]), this);
            backup_channel = new MDB_Channel(args[5], Integer.parseInt(args[6]), this);
            restore_channel = new MDR_Channel(args[7], Integer.parseInt(args[8]), this);
        }

        catch (NumberFormatException e) {
            System.err.println("ERROR: Bad input. Check number arguments.");
            usage();
            System.exit(-1);
        }

        // Set up storage
        storage = new Storage(id);
        storage.makeDirectories();
    }

    public void getChunk(String[] header){
        String file_id = header[Fields.FILE_ID.ordinal()];
        int chunk_no = Integer.parseInt(header[Fields.CHUNK_NO.ordinal()]);
        File chunk = storage.getStoredChunk(file_id, chunk_no);

        if(chunk == null) return; // Chunk is not stored

        int read_bytes;
        byte[] body = new byte[Storage.MAX_CHUNK_SIZE], message_bytes = null;
        ChunkMessage message = new ChunkMessage(version, id, file_id, chunk_no);

        try { // Create Message
            FileInputStream inputStream = new FileInputStream(chunk.getPath());
            read_bytes = inputStream.read(body); // TODO: Check -1
            message_bytes = message.getBytes(body, read_bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Send CHUNK msg
        restore_channel.send(message_bytes);
        System.out.printf("< Peer %d | bytes %d | CHUNK %d\n", id, message_bytes.length, chunk_no);
    }

    /**
     * Stores chunk received from the MDB channel
     * @param header Header of the message received
     * @param body Chunk to store
     * @param msg_len Length of the message received
     */
    public void putChunk(String[] header, byte[] body, int msg_len){
        // Parse fields
        int chunk_no = Integer.parseInt(header[Fields.CHUNK_NO.ordinal()]);
        String version = header[Fields.VERSION.ordinal()],
                file_id = header[Fields.FILE_ID.ordinal()];

        // If store was successful send STORED
        if(storage.putChunk(file_id, chunk_no, body)){
            try { // Sleep (0-400ms)
                Thread.sleep(new Random().nextInt(400));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Send STORED msg
            StoredMessage store_msg = new StoredMessage(version, id, file_id, chunk_no);
            control_channel.send(store_msg.getBytes(null, 0));
            System.out.printf("< Peer %d | STORED %d\n", id, chunk_no);
        }
    }

    @Override
    public void backupFile(String file_pathname, int replication_degree) {
        File file = storage.getFile(file_pathname, id);

        if (file == null)
            System.err.println("ERROR: File to backup does not exist. Aborting.");

        else {
            BackedUpFile file_info = storage.addBackedUpFile(file.toPath());
            Runnable task = new Backup(id, version, file, file_info.getId(), file_info.getNumberOfChunks(), replication_degree,
                    backup_channel, control_channel);
            pool.execute(task);
        }
    }

    @Override
    public void restoreFile(String file_path) {
        BackedUpFile file = storage.getFileInfo(file_path);
        if(file == null){
            System.out.println("File to restore needs to be backed up first. Aborting...");
            return;
        }
        Runnable task = new Restore(id, version, file.getId(), file.getNumberOfChunks(), restore_channel, control_channel);
        pool.execute(task);
    }

    @Override
    public void deleteFile(String file_path) {
        BackedUpFile file = storage.getFileInfo(file_path);
        if(file == null){
            System.out.println("File to delete needs to be backed up first. Aborting...");
            return;
        }
        storage.removeBackedUpFile(file); // Remove from backed up files
        Delete task = new Delete(version, id, file.getId(), control_channel);
        pool.execute(task);
    }

    @Override
    public void reclaim(int max_space) {
        System.out.println("Not implemented yet");
    }

    @Override
    public void getStateInformation() {
        System.out.println("Not implemented yet");
    }

    private static void usage() {
        System.out.println("Usage: <protocol version> <peer ID> <service access point> <MC> <MDB> <MDR>");
    }
}
