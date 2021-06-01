package handlers;

import channels.MC_Channel;
import messages.WokeUpMsg;
import peer.Peer;
import subprotocols.Delete;

import java.util.Set;
import java.util.concurrent.*;

public class WokeUpMessageHandler implements Runnable{
    private Set<String> deleted_files;
    private ExecutorService pool;
    private String version;
    private Peer peer;
    private MC_Channel mc_channel;

    public WokeUpMessageHandler(WokeUpMsg woke_up_msg, Peer peer) {
        deleted_files = peer.storage.getDeletedFiles();
        pool = Executors.newCachedThreadPool();
        version = woke_up_msg.getVersion();
        this.peer = peer;
        mc_channel = peer.getControl_channel();
    }

    @Override
    public void run() {
        for(String file_id: deleted_files)
            pool.execute(new Delete(peer, version, file_id, mc_channel));
    }
}
