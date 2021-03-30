package peer;

import peer.storage.Storage;
import channels.*;
import messages.*;
import peer.storage.StorageThread;
import subprotocols.*;

import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Peer implements RMI {
    private int id;
    private String version;
    private String access_point;
    private MC_Channel mc_channel;
    private MDB_Channel mdb_channel;
    private MDR_Channel mdr_channel;
    private final Storage storage;

    public static void main(String[] args) {
        if (args.length != 9) {
            usage();
            System.exit(1);
        }

        Peer peer_obj = new Peer(args); // create peer

        try {
            //RMI
            RMI RMI_stub = (RMI) UnicastRemoteObject.exportObject(peer_obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(peer_obj.access_point, RMI_stub);
        }

        catch (RemoteException | AlreadyBoundException e) {
            System.err.println("ERROR: Failed to bind peer object in the registry.\n Aborting...");
            System.exit(-1);
        }

        // Start listening on channels
        peer_obj.mdb_channel.run();
    }

    public Peer(String[] args) {
        try {
            version = args[0];
            id = Integer.parseInt(args[1]);
            access_point = args[2];
            mc_channel = new MC_Channel(args[3], Integer.parseInt(args[4]), this);
            mdb_channel = new MDB_Channel(args[5], Integer.parseInt(args[6]), this);
            mdr_channel = new MDR_Channel(args[7], Integer.parseInt(args[8]), this);
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

    @Override
    public void backupFile(String file_pathname, int replication_degree) {
        File file = storage.getFile(file_pathname, id);

        if (file == null)
            System.err.println("ERROR: File to backup does not exist. Aborting.");

        else {
            String file_id = storage.addBackedUpFile(file.toPath());
            new Backup(this.id, version, file, file_id, replication_degree, mdb_channel).run();
        }
    }

    @Override
    public void restoreFile(String file_pathname) {
        System.out.println("Not implemented yet");
    }

    @Override
    public void deleteFile(String file_pathname) {
        System.out.println("Not implemented yet");
    }

    @Override
    public void reclaim(int max_space) {
        System.out.println("Not implemented yet");
    }

    @Override
    public void getStateInformation() {
        System.out.println("Not implemented yet");
    }

    public void parseMessage(byte[] message_bytes) {
        byte[] header = Message.getHeaderBytes(message_bytes);
        byte[] body = Message.getBodyBytes(message_bytes, header.length);

        String header_string = new String(header);
        String[] header_fields = header_string.split(" "); // Split header by spaces

        int sender_id = Integer.parseInt(header_fields[2]);

        // Ignore message from itself
        if (sender_id == id)
            return;

        System.out.println("< Peer " + id + " received " + (message_bytes.length) + " bytes\n");

        new StorageThread(header_fields, body, mc_channel, storage, id).run();
    }

    private static void usage() {
        System.out.println("Usage: <protocol version> <peer ID> <service access point> <MC> <MDB> <MDR>");
    }
}
