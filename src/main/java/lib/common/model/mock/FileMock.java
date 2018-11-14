package lib.common.model.mock;

import lib.common.model.FileMonitor;
import lib.common.model.log.Logger;
import lib.common.util.IOUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.concurrent.Executor;

public abstract class FileMock extends FileMonitor implements Executor {
    private Runnable loadFile;

    public FileMock(String filePath, String charset) throws IOException {
        super();
        File file = new File(filePath);
        monitor(new WatchItem(file, StandardWatchEventKinds.ENTRY_MODIFY) {
            @Override
            protected void onEvent(WatchEvent<?> e) throws IOException {
                execute(loadFile);
            }
        });
        loadFile = () -> {
            try {
                loadFile(file, charset);
            } catch (IOException e) {
                Logger.getDefault().catches(e);
            }
        };
    }

    public void start() {
        execute(loadFile);
        execute(this);
    }

    private void loadFile(File file, String charset) throws IOException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        parseContent(IOUtil.fileToString(file, charset));
    }

    protected abstract void parseContent(String fileContent);
}
