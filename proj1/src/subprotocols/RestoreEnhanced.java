package subprotocols;

import channels.MC_Channel;
import channels.MDR_Channel;
import messages.ChunkMessage;
import messages.GetChunkEnhancedMsg;
import messages.Message;
import peer.Peer;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class RestoreEnhanced extends Subprotocol {
    private final int number_of_chunks;
    private final String file_path;
    private GetChunkEnhancedMsg message;
    private ServerSocket socket;

    public RestoreEnhanced(Peer initiator_peer, String version, String file_path, String file_id, int number_of_chunks, MDR_Channel restore_channel, MC_Channel control_channel) {
        super(control_channel, version, initiator_peer);

        this.number_of_chunks = number_of_chunks;
        this.file_path = file_path;

        try {
            socket = new ServerSocket(0);
            socket.setSoTimeout(1000);
            message = new GetChunkEnhancedMsg(version, initiator_peer.id, file_id, 0,
                    socket.getLocalPort());
        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to open socket. Aborting backup...");
        }
    }

    @Override
    public void run() {
        if (socket.isClosed())
            return;

        int chunk_no = 0;
        byte[] msg = new byte[65000];
        Path path = Paths.get(file_path);
        int position = 0;

        try {
            AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE);

            while (chunk_no < number_of_chunks) {
                // Send message
                control_channel.send(message.getBytes(null, 0));
                System.out.printf("< Peer %d sent: %s\n", initiator_peer.id, message.toString());

                Thread.sleep(500); // Sleep

                // Open socket
                Socket clientSocket = socket.accept();
                DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

                // Receive message
                int msg_len = inputStream.read(msg);
                byte[] header = Message.getHeaderBytes(msg);
                String[] header_fields = Message.getHeaderFields(msg);

                // Log
                ChunkMessage chunk_msg = new ChunkMessage(header_fields);
                System.out.printf("< Peer %d received: %s\n", initiator_peer.id, chunk_msg);

                // Write to file
                byte[] body = Message.getBodyBytes(msg, msg_len, header.length);

                if (body != null) {
                    ByteBuffer buffer = ByteBuffer.allocate(body.length);
                    buffer.put(body);
                    buffer.flip();

                    Future<Integer> operation = fileChannel.write(buffer, position);
                    buffer.clear();

                    position += operation.get();
                }

                // Close connection
                clientSocket.close();

                // Update chunk
                message.setChunkNo(++chunk_no);
            }

            // Close server socket and file
            socket.close();
            fileChannel.close();
        }

        catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        System.out.println("RESTORE of " + file_path + " finished.");
    }
}
