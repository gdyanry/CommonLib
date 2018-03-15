package lib.common.model.resourceaccess;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import lib.common.util.StringUtil;

/**
 * Created by yanry on 2015/8/15.
 */
public abstract class FileHashMapper {
    private File root;
    private int folderLimit;
    private Map<String, File> cache;

    public FileHashMapper(File root, int folderLimit) {
        this.root = root;
        this.folderLimit = folderLimit;
        this.cache = new HashMap<String, File>();
        if (!root.exists()) {
        	root.mkdirs();
        }
    }

    public File getFile(String key) {
        File f = cache.get(key);
    	if (f != null) {
    		return f;
    	}
        int folderNum = key.hashCode() % folderLimit;
        File folder = new File(root, folderNum + "");
        if (!folder.exists()) {
        	folder.mkdirs();
        }
        f = new File(folder, getFileName(key));
        cache.put(key, f);
        return f;
    }

    public String getMD5FileName(String key, String fileSubfix) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String fileName = StringUtil.encrypt(key, "UTF-8", "MD5");
        if (fileSubfix != null && fileSubfix.length() > 0) {
            fileName = fileName + "." + fileSubfix;
        }
        return fileName;
    }
    
    public File getRootDir() {
    	return root;
    }

    protected abstract String getFileName(String key);
}
