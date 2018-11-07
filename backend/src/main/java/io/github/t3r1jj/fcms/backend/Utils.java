package io.github.t3r1jj.fcms.backend;

import java.util.function.Predicate;

public class Utils {
    public static <T> Predicate<T> not(Predicate<T> p) {
        return p.negate();
    }
}
