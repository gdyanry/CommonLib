package lib.common.model.resourceaccess;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import lib.common.util.StringUtil;

/**
 * Created by yanry on 2015/8/15.
 */
public class FileHashMapper {
    private File root;
    private int folderLimit;
    private String fileSubfix;
    private Map<Object, File> cache;

    public FileHashMapper(File root, int folderLimit, String fileSubfix) {
        this.root = root;
        this.folderLimit = folderLimit;
        this.fileSubfix = fileSubfix;
        this.cache = new HashMap<Object, File>();
        if (!root.exists()) {
        	root.mkdirs();
        }
    }

    public File getFile(Object key) {
    	File f = cache.get(key);
    	if (f != null) {
    		return f;
    	}
        int folderNum = key.hashCode() % folderLimit;
        File folder = new File(root, folderNum + "");
        if (!folder.exists()) {
        	folder.mkdirs();
        }
        String fileName;
		try {
			fileName = StringUtil.encrypt(key.toString(), "UTF-8", "MD5");
			if (fileSubfix != null && fileSubfix.length() > 0) {
				fileName = fileName + "." + fileSubfix;
			}
			f = new File(folder, fileName);
			cache.put(key, f);
			return f;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }
    
    public File getRootDir() {
    	return root;
    }
}
