package yanry.lib.java.util;

import yanry.lib.java.model.log.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
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

    public static String getMD5(File file) throws IOException, NoSuchAlgorithmException {
        byte[] buffer = new byte[8192];
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(file);
        int len;
        while ((len = fis.read(buffer)) != -1) {
            md.update(buffer, 0, len);
        }
        fis.close();
        return new BigInteger(1, md.digest()).toString(16);
    }

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
                    IOUtil.copyFile(file, new File(destDir, renameTo));
                } else {
                    Logger.getDefault().w("%s has conflict mapping: %s, %s", renameTo, conflictFile, file);
                }
            }
        }
    }

    public static void copyAndRename(File srcDir, File destDir, String encryptAlgorithm, String charset) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance(encryptAlgorithm);
        copyAndRename(srcDir, destDir, file -> {
            if (file.isFile()) {
                try {
                    return StringUtil.digest(file.getName(), charset, md);
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
