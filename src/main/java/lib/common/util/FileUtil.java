/**
 * 
 */
package lib.common.util;

import java.io.File;
import java.util.Set;

/**
 * @author yanry
 *
 *         2015年5月12日 下午1:03:18
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
}
