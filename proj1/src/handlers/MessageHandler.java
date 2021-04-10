package handlers;

import peer.storage.Storage;

public abstract class MessageHandler implements Runnable{
    protected final String file_id;
    protected final Storage storage;

    public MessageHandler(String file_id, Storage storage){
        this.file_id = file_id;
        this.storage = storage;
    }

}
