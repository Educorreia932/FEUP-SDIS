package peer;

import messages.*;
import channels.*;
import peer.storage.BackedUpFile;
import peer.storage.Chunk;
import peer.storage.Storage;
import subprotocols.*;

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

        try {
            loadStorage(); // Load storage if exists
        }

        catch (IOException | ClassNotFoundException e) {
            // Set up storage
            storage = new Storage(id);
            storage.makeDirectories();
        }
    }

    public void getChunk(String[] header) {
        String file_id = header[Fields.FILE_ID.ordinal()];
        int chunk_no = Integer.parseInt(header[Fields.CHUNK_NO.ordinal()]);
        File chunk = storage.getChunkFile(file_id, chunk_no);

        if (chunk == null) {
            System.err.printf("Chunk %d is not stored \n", chunk_no);
            return; // Chunk is not stored
        }

        int read_bytes = 0;
        byte[] body = new byte[Storage.MAX_CHUNK_SIZE], message_bytes;
        ChunkMessage message = new ChunkMessage(version, id, file_id, chunk_no);

        // Create Message
        try {
            // Read chunk
            FileInputStream inputStream = new FileInputStream(chunk.getPath());

            if(inputStream.available() > 0) // Check if empty
                read_bytes = inputStream.read(body); // Read chunk

            // Get message byte array
            message_bytes = message.getBytes(body, read_bytes);

            restore_channel.received_chunk_msg.set(false);
            Thread.sleep(new Random().nextInt(400)); // Sleep (0-400)ms
            if(restore_channel.received_chunk_msg.get()) return; // Abort if received chunk message

            restore_channel.send(message_bytes); // Send message
            System.out.printf("< Peer %d sent | bytes %d | CHUNK %d\n", id, message_bytes.length, chunk_no);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stores chunk received from the MDB channel
     *
     * @param header Header of the message received
     * @param body   Chunk to store
     */
    public void putChunk(String[] header, byte[] body) {
        // Parse fields
        int chunk_no = Integer.parseInt(header[Fields.CHUNK_NO.ordinal()]),
        replication_degree = Integer.parseInt(header[Fields.REP_DEG.ordinal()]);
        String version = header[Fields.VERSION.ordinal()],
                file_id = header[Fields.FILE_ID.ordinal()];

        // If store was successful send STORED
        if (storage.putChunk(file_id, chunk_no, body, replication_degree)) {
            try { // Sleep (0-400ms)
                Thread.sleep(new Random().nextInt(400));
            }

            catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Send STORED msg
            StoredMessage store_msg = new StoredMessage(version, id, file_id, chunk_no);
            control_channel.send(store_msg.getBytes(null, 0));
            System.out.printf("< Peer %d sent | STORED %d\n", id, chunk_no);
        }
    }

    @Override
    public void backupFile(String file_pathname, int replication_degree) {
        // TODO: Verificar se sofreu alteraçoes
        File file = storage.getFile(file_pathname, id);

        if (file == null)
            System.err.println("ERROR: File to backup does not exist. Aborting.");

        else {
            BackedUpFile file_info = storage.addBackedUpFile(file.toPath(), replication_degree);
            Runnable task = new Backup(this, version, file, file_info.getId(), file_info.getNumberOfChunks(), replication_degree,
                    backup_channel, control_channel);
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
    public void reclaim(int max_space) {
        if(max_space < 0) return; // Ignore negative values
        while(storage.getUsedSpace().get() > max_space){
            Chunk chunk = storage.removeRandomChunk(); // Remove a chunk

            if(chunk == null){
                System.out.println("No chunks to delete. Aborting...");
                return;
            }

            // Send REMOVED msg
            RemovedMessage message = new RemovedMessage(version, id, chunk.getFile_id(), chunk.getChunk_no());
            control_channel.send(message.getBytes(null, 0));
            System.out.printf("< Peer %d sent | REMOVED %d\n", id, chunk.getChunk_no());
        }
        System.out.println("Finished Reclaim.");
    }

    @Override
    public String getStateInformation() {
        return "----------------- \n BACKED UP FILES\n----------------- \n" +
                storage.getBackedUpFilesInfo() +
                "------------------ \n BACKED UP CHUNKS\n------------------ \n" +
                storage.getBackedUpChunksInfo();
    }

    private static void usage() {
        System.out.println("Usage: <protocol version> <peer ID> <service access point> <MC> <MDB> <MDR>");
    }

    public void saveStorage() {
        FileOutputStream fileOutputStream;

        try {
            fileOutputStream = new FileOutputStream(Storage.FILESYSTEM_FOLDER + id + "/storageBackup.txt");
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
        String filePath = Storage.FILESYSTEM_FOLDER + id + "/storageBackup.txt";

        FileInputStream fileInputStream = new FileInputStream(filePath);
        ObjectInputStream objectInputStream  = new ObjectInputStream(fileInputStream);

        this.storage = (Storage) objectInputStream.readObject();
    }

    public void fixChunkRP(String file_id, int chunk_no) {
        Chunk chunk = storage.getStoredChunk(file_id, chunk_no);
        if(chunk != null && chunk.needsBackUp()){
            File chunk_file = storage.getChunkFile(file_id, chunk_no);

            if(chunk_file != null){
                int read_bytes = 0;
                byte[] body = new byte[Storage.MAX_CHUNK_SIZE], message_bytes;
                PutChunkMessage message = new PutChunkMessage(version, id, file_id, chunk.getDesired_rep_deg(), chunk_no);

                // Create Message
                try {
                    // Read chunk
                    FileInputStream inputStream = new FileInputStream(chunk_file.getPath());

                    if (inputStream.available() > 0)         // Check if empty
                        read_bytes = inputStream.read(body); // Read chunk

                    // Get message byte array
                    message_bytes = message.getBytes(body, read_bytes);

                    // TODO: Abort if received PUTCHUNK
                    Thread.sleep(new Random().nextInt(400)); // Sleep (0-400)ms

                    restore_channel.send(message_bytes); // Send message
                    System.out.printf("< Peer %d sent | bytes %d | PUTCHUNK %d\n", id, message_bytes.length, chunk_no);
                }
                catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
