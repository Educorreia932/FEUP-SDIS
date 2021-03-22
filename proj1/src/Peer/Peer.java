package Peer;

import channels.*;
import messages.Message;
import sub_protocols.Backup;

import java.io.File;
import java.net.DatagramPacket;
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
        } catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
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
            System.out.println("Exception: " + e.getMessage());
            System.exit(1);
        }
        // set up storage
        storage = Storage.getInstance();
        storage.makeDirectories(id);
    }

    @Override
    public void backupFile(String file_pathname, int replication_degree) {
        File file = storage.getFile(file_pathname, id);

        if (file == null) {
            System.out.println("File does not exist. Aborting.");
        }
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

    public void parseMsg(DatagramPacket packet){
        byte[] packet_data = packet.getData();  // get bytes
        byte[] header = Message.getHeader(packet_data); // get header
        byte[] body = Message.getBody(packet_data, header.length); // get body

        String header_str = new String(header);
        String[] split_header = header_str.split(" "); // split header by spaces

        int sender_id = Integer.parseInt(split_header[2]);
        if(sender_id == id) return; // ignore msg from itself

        storage.execute(split_header, body); // execute msg
    }

    private static void usage() {
        System.out.println("Usage: <protocol version> <peer ID> <service access point> <MC> <MDB> <MDR>");
    }
}
