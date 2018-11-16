package lib.common.model.mock;

import lib.common.model.FileMonitor;
import lib.common.model.log.Logger;
import lib.common.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.concurrent.Executor;

public abstract class FileMock implements Executor {
    private File file;
    private Runnable loadFile;

    public FileMock(String filePath, String charset) {
        super();
        file = new File(filePath);
        loadFile = () -> {
            try {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                if (file.exists()) {
                    parseContent(IOUtil.fileToString(file, charset));
                }
            } catch (IOException e) {
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
