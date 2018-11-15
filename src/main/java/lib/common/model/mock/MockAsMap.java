package lib.common.model.mock;

import lib.common.model.log.Logger;

import java.util.HashMap;

public abstract class MockAsMap extends FileMock {
    private String entrySplitter;
    private String keyValueSplitter;
    private HashMap<String, String> map;

    public MockAsMap(String filePath, String charset, String entrySplitter, String keyValueSplitter) {
        super(filePath, charset);
        this.entrySplitter = entrySplitter;
        this.keyValueSplitter = keyValueSplitter;
        map = new HashMap<>();
    }

    @Override
    protected synchronized void parseContent(String fileContent) {
        map.clear();
        String[] entries = fileContent.split(entrySplitter);
        for (String entry : entries) {
            String[] kv = entry.split(keyValueSplitter, 2);
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            } else {
                Logger.getDefault().ww("invalid entry: ", entry);
            }
        }
        onLoadComplete(map);
    }

    public String get(String key) {
        return map.get(key);
    }

    protected abstract void onLoadComplete(HashMap<String, String> map);
}
