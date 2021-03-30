package peer;

import peer.storage.Storage;
import channels.*;
import subprotocols.*;

import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Peer implements RMI {
    public int id;
    private String version;
    private String access_point;
    private MC_Channel communication_channel;
    private MDB_Channel backup_channel;
    private MDR_Channel restore_channel;
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

        // Start listening on channels
        peer.backup_channel.run();
    }

    public Peer(String[] args) {
        try {
            version = args[0];
            id = Integer.parseInt(args[1]);
            access_point = args[2];
            communication_channel = new MC_Channel(args[3], Integer.parseInt(args[4]), this);
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

    @Override
    public void backupFile(String file_pathname, int replication_degree) {
        File file = storage.getFile(file_pathname, id);

        if (file == null)
            System.err.println("ERROR: File to backup does not exist. Aborting.");

        else {
            String file_id = storage.addBackedUpFile(file.toPath());
            new Backup(this.id, version, file, file_id, replication_degree, backup_channel).run();
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

    private static void usage() {
        System.out.println("Usage: <protocol version> <peer ID> <service access point> <MC> <MDB> <MDR>");
    }
}
