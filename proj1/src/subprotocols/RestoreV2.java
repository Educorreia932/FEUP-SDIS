package subprotocols;

import channels.MC_Channel;
import channels.MDR_Channel;
import messages.ChunkMessage;
import messages.GetChunkMessageV2;
import messages.Message;
import peer.Peer;
import utils.Pair;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class RestoreV2 extends Subprotocol {
    private final MDR_Channel restore_channel;
    private final int number_of_chunks;
    private final String file_id;
    private final String file_path;
    private GetChunkMessageV2 message;
    private ServerSocket socket;

    public RestoreV2(Peer initiator_peer, String version, String file_path, String file_id, int number_of_chunks, MDR_Channel restore_channel, MC_Channel control_channel) {
        super(control_channel, version, initiator_peer);

        this.restore_channel = restore_channel;
        this.number_of_chunks = number_of_chunks;
        this.file_id = file_id;
        this.file_path = file_path;

        try {
            socket = new ServerSocket(0);
            // socket.setSoTimeout(999999999);
            message = new GetChunkMessageV2(version, initiator_peer.id, file_id, 0,
                    socket.getLocalPort());
        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to open socket. Aborting backup...");
        }
    }

    @Override
    public void run() {
        if (socket.isClosed()) return;

        int chunk_no = 0;
        byte[] msg = new byte[65000];
        ArrayList<byte[]> chunks = new ArrayList<>(); // Array of chunks by order

        while (chunk_no < number_of_chunks) {
            // Send message
            control_channel.send(message.getBytes(null, 0));
            System.out.printf("< Peer %d sent: %s\n", initiator_peer.id, message.toString());

            try {
                Thread.sleep(500); // Sleep

                // Open socket
                Socket clientSocket = socket.accept();
                DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

                // Receive message
                int msg_len = inputStream.read(msg);
                byte[] header = Message.getHeaderBytes(msg);
                String[] header_fields = Message.getHeaderFields(msg);

                ChunkMessage chunk_msg = new ChunkMessage(header_fields);
                byte[] body = Message.getBodyBytes(msg, msg_len, header.length);
                System.out.printf("< Peer %d received: %s\n", initiator_peer.id, chunk_msg.toString());

                chunks.add(body);
                clientSocket.close();

                message.setChunkNo(++chunk_no);
            }
            catch (InterruptedException | IOException e) {
                e.printStackTrace();
                System.out.println("Failed to restore files of " + file_path);
                return;
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(restoreFileChunks(chunks))
            System.out.println("RESTORE of " + file_path + " finished.");
        else System.out.println("RESTORE of " + file_path + " failed.");

    }

    private boolean restoreFileChunks(ArrayList<byte[]> chunks) {

        try {
            FileOutputStream stream = new FileOutputStream(file_path);

            for (byte[] chunk : chunks)
                stream.write(chunk);

            stream.flush();
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
