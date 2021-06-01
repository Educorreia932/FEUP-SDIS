package utils;

public interface Observer {
    void notify(String file_id, int chunk_no);
}
