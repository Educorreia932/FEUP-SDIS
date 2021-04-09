package peer;

import messages.*;
import channels.*;
import peer.storage.BackedUpFile;
import peer.storage.Chunk;
import peer.storage.Storage;
import subprotocols.*;
import utils.Pair;

import java.io.*;
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
    private String version; // TODO: Should the Peer store a version and not only the subprotocols?
    private String access_point;
    private MC_Channel control_channel;
    private MDB_Channel backup_channel;
    private MDR_Channel restore_channel;
    private ExecutorService pool;
    public Storage storage;

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
            pool = Executors.newCachedThreadPool();
        }

        catch (NumberFormatException e) {
            System.err.println("ERROR: Bad input. Check number arguments.");
            usage();
            System.exit(-1);
        }

        try {
            loadStorage(); // Load storage if exists
        }

        catch (IOException | ClassNotFoundException e) {
            // Set up storage
            storage = new Storage(id);
            storage.makeDirectories();
        }
    }

    /* Messages Handlers */

    public void storedMessageHandler(String file_id, int chunk_no, int sender_id){
        // Increment RP
        storage.updateReplicationDegree(file_id, chunk_no, sender_id, true);
        saveStorage(); // Update storage
    }

    public void deleteMessageHandler(String file_id){
        // Deletes all chunks from file
        storage.deleteAllChunksFromFile(file_id);
        saveStorage(); // Update storage
    }

    public void removedMessageHandler(String file_id, int chunk_no, int sender_id){
        // Decrement RP
        storage.updateReplicationDegree(file_id, chunk_no, sender_id, false);
        updateChunkRP(file_id, chunk_no); // Check if perceived RP < Desired RP
        saveStorage(); // Update storage
    }

    public void chunkMessageHandler(String file_id, int chunk_no, byte[] body){
        if(storage.isFileBackedUp(file_id).get()) // Store chunk only if peer has original file
            restore_channel.received_chunks.put(Pair.create(file_id, chunk_no), body);
    }

    /* RMI Interface */

    @Override
    public void backupFile(String filename, int replication_degree) {
        File file = storage.getFile(filename);

        if (file == null)
            System.err.println("ERROR: File to backup does not exist. Aborting.");

        else {
            BackedUpFile new_file = new BackedUpFile(file.toPath(), replication_degree);
            String old_file_id = storage.wasFileModified(file.getPath(), new_file.getId());
            if (old_file_id != null) { // File was modified
                Delete delete_task = new Delete(this, version, old_file_id, control_channel);
                pool.execute(delete_task);
            }

            storage.addBackedUpFile(new_file);
            Runnable task = new Backup(this, version, file, new_file.getId(), new_file.getNumberOfChunks(),
                    replication_degree, backup_channel, control_channel);
            pool.execute(task);
        }
    }

    @Override
    public void restoreFile(String file_path) {
        BackedUpFile file = storage.getFileInfo(file_path);

        if (file == null) {
            System.out.println("File to restore needs to be backed up first. Aborting...");
            return;
        }

        Runnable task = new Restore(this, version, file.getPath(), file.getId(), file.getNumberOfChunks(), restore_channel, control_channel);
        pool.execute(task);
    }

    @Override
    public void deleteFile(String file_path) {
        BackedUpFile file = storage.getFileInfo(file_path);

        if (file == null) {
            System.out.println("File to delete needs to be backed up first. Aborting...");
            return;
        }

        storage.removeBackedUpFile(file); // Remove from backed up files
        Delete task = new Delete(this, version, file.getId(), control_channel);
        pool.execute(task);
    }

    @Override
    public void reclaim(long max_space) {
        if (max_space < 0)
            return; // Ignore negative values

        Reclaim task = new Reclaim(control_channel, version, this, max_space);
        pool.execute(task);
    }

    @Override
    public String getStateInformation() {
        return "----------------- \n BACKED UP FILES\n----------------- \n" +
                storage.getBackedUpFilesState() +
                "------------------ \n BACKED UP CHUNKS\n------------------ \n" +
                storage.getBackedUpChunksState();
    }

    private static void usage() {
        System.out.println("Usage: <protocol version> <peer ID> <service access point> <MC> <MDB> <MDR>");
    }

    public void saveStorage() {
        FileOutputStream fileOutputStream;

        try {
            fileOutputStream = new FileOutputStream(Storage.getStoragePath(id));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(storage);
            objectOutputStream.flush();
            objectOutputStream.close();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadStorage() throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(Storage.getStoragePath(id));
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        this.storage = (Storage) objectInputStream.readObject();
    }

    public void updateChunkRP(String file_id, int chunk_no) {
        Chunk chunk = storage.getStoredChunk(file_id, chunk_no);

        if (chunk != null && chunk.needsBackUp()) {
            File chunk_file = storage.getFile(file_id, chunk_no);

            if (chunk_file != null) {
                try {
                    // TODO: Abort if received PUTCHUNK
                    Thread.sleep(new Random().nextInt(400)); // Sleep (0-400)ms

                    Backup task = new Backup(this, version, chunk_file, chunk.getFile_id(),
                            1, chunk.getDesired_rep_deg(), backup_channel, control_channel);
                    pool.execute(task);
                }

                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* Getters */

    public MC_Channel getControl_channel() {
        return control_channel;
    }

    public MDB_Channel getBackup_channel() {
        return backup_channel;
    }

    public MDR_Channel getRestore_channel() {
        return restore_channel;
    }
}
