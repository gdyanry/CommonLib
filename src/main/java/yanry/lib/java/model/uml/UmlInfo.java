package yanry.lib.java.model.uml;

import java.lang.reflect.AnnotatedElement;

public class UmlInfo {
    private static final UmlInfo defaultInclude = new UmlInfo(true, "");
    private static final UmlInfo defaultExclude = new UmlInfo(false, "");
    private boolean include;
    private String note;

    private UmlInfo(boolean include, String note) {
        this.include = include;
        this.note = note;
    }

    public static UmlInfo getDefault(boolean include) {
        return include ? defaultInclude : defaultExclude;
    }

    public static UmlInfo get(AnnotatedElement annotatedElement) {
        UmlElement annotation = annotatedElement.getAnnotation(UmlElement.class);
        if (annotation != null) {
            String note = annotation.note();
            if (note.length() > 0) {
                return new UmlInfo(annotation.include(), annotation.note());
            }
            return getDefault(annotation.include());
        }
        return null;
    }

    public boolean isInclude() {
        return include;
    }

    public String getNote() {
        return note;
    }
}
