package yanry.lib.java.model.schedule;

public interface OnReleaseListener {
    /**
     * @param type can be one of {@link ShowData#DISMISS_CANCELLED}, {@link ShowData#DISMISS_EXPELLED},
     *             {@link ShowData#DISMISS_MANUAL}, {@link ShowData#DISMISS_NOTIFIED}, {@link ShowData#DISMISS_TIMEOUT}
     */
    void onRelease(String type);
}
