package yanry.lib.java.util.console.query;

import java.util.Collection;
import java.util.Iterator;

public class Select<T> extends OptionsMap<T> {
    public Select(T... options) {
        for (int i = 0; i < options.length; i++) {
            T option = options[i];
            appendMapping(String.valueOf(i), option);
        }
    }

    public Select(Collection<T> options) {
        Iterator<T> iterator = options.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            appendMapping(String.valueOf(i++), iterator.next());
        }
    }
}
