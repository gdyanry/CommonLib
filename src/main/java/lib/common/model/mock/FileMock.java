package lib.common.model.mock;

import lib.common.model.FileMonitor;
import lib.common.model.log.Logger;
import lib.common.util.FileUtil;
import lib.common.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;

public abstract class FileMock implements Executor {
    private File file;
    private Runnable loadFile;
    private String cachedMd5;

    public FileMock(String filePath, String charset) {
        super();
        file = new File(filePath);
        loadFile = () -> {
            try {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if (file.exists()) {
                    String md5 = FileUtil.getMD5(file);
                    if (md5 != null && !md5.equals(cachedMd5)) {
                        cachedMd5 = md5;
                        parseContent(IOUtil.fileToString(file, charset));
                    }
                }
            } catch (IOException | NoSuchAlgorithmException e) {
                Logger.getDefault().catches(e);
            }
        };
    }

    public void loadFile() {
        execute(loadFile);
    }

    public void monitor() throws IOException {
        FileMonitor fileMonitor = new FileMonitor();
        fileMonitor.monitor(new FileMonitor.WatchItem(file, StandardWatchEventKinds.ENTRY_MODIFY) {
            @Override
            protected void onEvent(WatchEvent<?> e) {
                execute(loadFile);
            }
        });
        execute(fileMonitor);
    }

    protected abstract void parseContent(String fileContent);
}
