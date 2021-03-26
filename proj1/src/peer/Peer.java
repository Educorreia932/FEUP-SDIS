package peer;

import peer.storage.Storage;
import channels.*;
import messages.*;
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

        try { //RMI
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

    public void parseMessage(byte[] message) {
        byte[] header = Message.getHeader(message); // get header
        byte[] body = Message.getBody(message, header.length); // get body

        String header_str = new String(header);
        String[] split_header = header_str.split(" "); // split header by spaces

        int sender_id = Integer.parseInt(split_header[2]);

        if (sender_id == id)
            return; // ignore msg from itself

        //String version = header[0];
        String msg_type = split_header[1];
        //String sender_id = header[2];
        String file_id = split_header[3];
        int chunkno, replication_degree;

        switch (msg_type) {
            case "PUTCHUNK":
                chunkno = Integer.parseInt(split_header[4]);
                //replication_degree = Integer.parseInt(header[5]);
                if(storage.putChunk(file_id, chunkno, body))
                    System.out.println("SEND STORED");
                break;
            case "STORED":
            case "GETCHUNK":
            case "CHUNK":
            case "REMOVED":
            case "DELETE":
                System.out.println("Not implemented");
                break;
        }
    }

    private static void usage() {
        System.out.println("Usage: <protocol version> <peer ID> <service access point> <MC> <MDB> <MDR>");
    }
}
