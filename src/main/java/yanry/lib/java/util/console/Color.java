package yanry.lib.java.util.console;

public enum Color {

    Black(0), Red(1), Green(2), Yellow(3), Blue(4), Magenta(5), Cyan(6), White(7), Default(9);

    int code;

    Color(int code) {
        this.code = code;
    }
}
