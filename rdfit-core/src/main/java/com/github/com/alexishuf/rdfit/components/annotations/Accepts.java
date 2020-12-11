package com.github.com.alexishuf.rdfit.components.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Accepts {
    /**
     * The classes which the component accepts as input.
     */
    @SuppressWarnings("rawtypes") Class[] value();
}
