package io.github.konohiroaki.annotationprocessor.timer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Timer {

    // Statement index of Start and End for measurement.
    int[] value() default {
        /* inclusive */ 0,
        /* inclusive */ Integer.MAX_VALUE
    };
}