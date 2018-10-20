/**
 *
 */
package lib.common.util;

import lib.common.model.log.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Set;

/**
 * @author yanry
 * <p>
 * 2015年5月12日 下午1:03:18
 */
public class FileUtil {

    public static long getDirSize(File rootDir, boolean clear, Set<File> exclusive) {
        long size = 0;
        File[] listFiles = rootDir.listFiles();
        if (listFiles != null) {
            for (File f : listFiles) {
                if (exclusive.add(f)) {
                    if (f.isFile()) {
                        size += f.length();
                    } else {
                        size += getDirSize(f, clear, exclusive);
                    }
                    if (clear) {
                        f.delete();
                    }
                }
            }
        }
        return size;
    }

    public static void copyAndRename(File srcDir, File destDir, RenameFunction function) throws IOException {
        HashMap<String, File> map = new HashMap<>();
        for (File file : srcDir.listFiles()) {
            String renameTo = function.rename(file);
            if (renameTo != null) {
                File conflictFile = map.put(renameTo, file);
                if (conflictFile == null) {
                    Files.copy(file.toPath(), Path.of(destDir.getAbsolutePath(), renameTo));
                } else {
                    Logger.getDefault().e("%s has conflict mapping: %s, %s", renameTo, conflictFile, file);
                }
            }
        }
    }

    public static void copyAndRename(File srcDir, File destDir, String encryptAlgorithm, String charset) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance(encryptAlgorithm);
        copyAndRename(srcDir, destDir, file -> {
            if (file.isFile()) {
                try {
                    return StringUtil.encrypt(file.getName(), charset, md);
                } catch (Exception e) {
                    Logger.getDefault().catches(e);
                }
            }
            return null;
        });
    }

    public interface RenameFunction {
        /**
         * @param file
         * @return return null means abort rename action.
         */
        String rename(File file);
    }
}
