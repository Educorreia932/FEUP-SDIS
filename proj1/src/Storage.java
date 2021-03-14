import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Storage {
    private HashMap<Integer, String> filesMap = new HashMap<Integer, String>();
    private static Storage instance;
    public Storage(){
    }

    public static Storage getInstance() {
        if(instance == null){
            instance = new Storage();
        }
        return instance;
    }

    public File getFile(String file_pathname){
        File file = new File(file_pathname.trim());
        if (file.exists() && !file.isDirectory()){
            return file;
        }
        return null;
    }

    public void makeDirectories(int peer_id){
        Path peer_path = Paths.get("peer" + peer_id + '/');
        Path peer_backup_path = Paths.get("peer" + peer_id + '/' + "backup/");
        try {
            Files.createDirectory(peer_path);
            Files.createDirectory(peer_backup_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
