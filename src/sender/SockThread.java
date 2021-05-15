package sender;

import message.Message;
import message.file.FileMessage;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SockThread implements Runnable {
    private static final int MAX_CONNS = 5;
    private static final String[] CYPHER_SUITES = new String[]{
            "SSL_RSA_WITH_RC4_128_MD5",
            "SSL_RSA_WITH_RC4_128_SHA",
            "SSL_RSA_WITH_NULL_MD5",
            "TLS_RSA_WITH_AES_128_CBC_SHA",
            "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
            "TLS_DHE_DSS_WITH_AES_128_CBC_SHA",
            "TLS_DH_anon_WITH_AES_128_CBC_SHA"
    };

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService threadPool =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final String name;
    private final ServerSocket serverSocket;
    private final InetAddress ip;
    private final Integer port;
    private MessageHandler handler;

    public SockThread(String name, InetAddress ip, Integer port) throws IOException {
        this.name = name;
        this.ip = ip;
        this.port = port;

        // this.serverSocket =
        //        (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port, MAX_CONNS, ip);
        // this.serverSocket.setEnabledCipherSuites(CYPHER_SUITES);
        this.serverSocket = new ServerSocket(port, MAX_CONNS, ip);
    }

    public String getName() {
        return this.name;
    }

    public void setHandler(MessageHandler handler) {
        this.handler = handler;
    }

    public void close() {
        this.threadPool.shutdown();

        try {
            this.serverSocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void interrupt() {
        running.set(false);
    }

    public void start() {
        Thread worker = new Thread(this);
        worker.start();
    }

    @Override
    public void run() {
        running.set(true);
        while (running.get()) {
            byte[] packetData = new byte[64000 + 1000];
            int n;
            // SSLSocket socket; TODO Use threads to accept connections concurrently?
            Socket socket;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Timed out while waiting for answer (Sock thread) " + this);
                try {
                    serverSocket.close();
                } catch (IOException ioException) {
                    System.err.println("Failed to close socket (ChunkTCP)");
                }
                continue;
            }

            Object obj;
            try {
                obj = new ObjectInputStream(socket.getInputStream()).readObject();
            } catch (SocketException e) {
                // happens if the blocking call is interrupted
                continue;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            }

            this.threadPool.execute(
                    () -> {
                        assert obj != null;
                        handler.handleMessage((Message) obj);
                    });
        }
    }

    public void send(Message message, InetAddress address, int port) {
        System.out.println("Sent: " + message);
        try {
            // SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(address, port);
            // socket.setEnabledCipherSuites(CYPHER_SUITES);
            Socket socket = new Socket(address, port);
            new ObjectOutputStream(socket.getOutputStream()).writeObject(message);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return ip + ":" + port + "\n";
    }
}