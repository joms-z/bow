package util;

import java.io.File;
import java.io.IOException;

/**
 * Created by Joms on 7/22/2016.
 */
public class FileUtils {

    public static File createFileIfNotExists(String fileName) throws IOException {
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        }
        catch (IOException ie) {
            ie.printStackTrace();
            throw ie;
        }
        return file;
    }
}
