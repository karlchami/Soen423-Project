package replica.replica_nick.utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StoreLogger {
    private String storePrefix;

    public StoreLogger(String storePrefix) {
        this.storePrefix = storePrefix;
    }

    public void log(String userID, String message) {
        String timeStamp = getTimestamp();
        String storeLogEntry = timeStamp + "\t" + userID + " - " + message;
        String userLogEntry = timeStamp + "\t" + message;

        logEntry(storeLogEntry, getStoreLogPath());
        logEntry(userLogEntry, getUserLogPath(userID));
    }

    private void logEntry(String message, String path) {
        File f = new File(path);

        try {
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }

            PrintWriter out = new PrintWriter(new FileWriter(f, true), true);
            out.println(message);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getStoreLogPath() {
        return "logs\\" + storePrefix + "_store.txt";
    }

    private String getUserLogPath(String userID) {
        return "logs\\" + userID + ".txt";
    }

    private String getTimestamp() {
        LocalDateTime timestamp = LocalDateTime.now();
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
