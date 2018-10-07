package lib.common.t;

import java.util.stream.Stream;

public class StreamTest {
    public static void main(String... args) {
        Stream.of("", "a", "b", "", "c").map(s -> {
            System.out.println("map: " + s);
            return s.length() > 0 ? s : null;
        }).filter(s -> s != null).findAny().ifPresent(s -> System.out.println("any: " + s));
    }
}
