package util;

import java.io.File;

public class FileUtils {
    public static File getDownloadPath(String fileName) {
        String userHome = System.getProperty("user.home");
        File downloadDir = new File(userHome, "Downloads");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        return new File(downloadDir, fileName);
    }
}
