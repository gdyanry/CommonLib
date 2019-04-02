package yanry.lib.java.util.object;

import java.lang.annotation.*;

/**
 * Created by rongyu.yan on 3/7/2017.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
public @interface Visible {
}
