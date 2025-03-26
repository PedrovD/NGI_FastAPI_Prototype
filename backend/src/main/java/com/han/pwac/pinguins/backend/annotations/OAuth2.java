package com.han.pwac.pinguins.backend.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)  // Specifies that this annotation can be applied to methods
@Retention(RetentionPolicy.RUNTIME)
public @interface OAuth2 {
    boolean required() default true;
}
