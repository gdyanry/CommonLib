package yanry.lib.java.model;

import java.nio.CharBuffer;

import yanry.lib.java.model.schedule.ShowData;
import yanry.lib.java.model.schedule.SyncDisplay;

/**
 * Created by yanry on 2020/1/1.
 */
public abstract class BufferedStringDisplay extends SyncDisplay<ShowData, CharBuffer> {
    private CharBuffer buffer;
    private String segmentPrefix;
    private String contentSeparator;
    private boolean avoidBreak;

    public BufferedStringDisplay(int bufferSize, String segmentPrefix, String contentSeparator, boolean avoidBreak) {
        buffer = CharBuffer.allocate(bufferSize);
        buffer.put(segmentPrefix);
        this.segmentPrefix = segmentPrefix;
        this.contentSeparator = contentSeparator;
        this.avoidBreak = avoidBreak;
    }

    protected abstract void onFlush(String segment);

    @Override
    protected CharBuffer showData(CharBuffer currentView, ShowData data) {
        String content = data.getExtra().toString();
        if (content == null || content.length() == 0) {
            return buffer;
        }
        if (buffer.position() == 0 && segmentPrefix != null) {
            buffer.put(segmentPrefix);
        }
        int remainLen = content.length();
        int from = 0;
        int to;
        int remaining;
        while ((remaining = buffer.remaining()) < remainLen) {
            if (avoidBreak && buffer.position() > (segmentPrefix == null ? 0 : segmentPrefix.length())) {
                onFlush(buffer.flip().toString());
                buffer.clear();
                if (segmentPrefix != null) {
                    buffer.put(segmentPrefix);
                }
            } else {
                to = from + remaining;
                onFlush(buffer.put(content.substring(from, to)).flip().toString());
                if (segmentPrefix != null) {
                    buffer.put(segmentPrefix);
                }
                from = to;
                remainLen -= remaining;
            }
        }
        buffer.put(content.substring(from));
        if (contentSeparator != null && buffer.remaining() > contentSeparator.length()) {
            buffer.put(contentSeparator);
        } else {
            onFlush(buffer.flip().toString());
            buffer.clear();
        }
        return buffer;
    }

    @Override
    protected void dismiss(CharBuffer view) {
        onFlush(buffer.flip().toString());
        buffer.clear();
    }

    @Override
    protected boolean isShowing(CharBuffer view) {
        return true;
    }
}
