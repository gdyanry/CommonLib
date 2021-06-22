package yanry.lib.java.model.schedule.extend;

import java.nio.CharBuffer;

import yanry.lib.java.model.schedule.Display;
import yanry.lib.java.model.schedule.ShowData;

/**
 * Created by yanry on 2020/1/1.
 */
public abstract class BufferedStringDisplay extends Display<ShowData> {
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
    protected void internalDismiss() {
        onFlush(buffer.flip().toString());
        buffer.clear();
    }

    @Override
    protected void show(ShowData data) {
        String content = data.getExtra().toString();
        if (content == null || content.length() == 0) {
            return;
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
    }
}
