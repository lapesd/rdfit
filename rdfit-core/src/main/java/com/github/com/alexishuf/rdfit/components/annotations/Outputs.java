package com.github.com.alexishuf.rdfit.components.annotations;

import com.github.com.alexishuf.rdfit.components.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Outputs {
    /**
     * The class of objects the {@link Component} outputs.
     * @return
     */
    Class<?> value();
}
