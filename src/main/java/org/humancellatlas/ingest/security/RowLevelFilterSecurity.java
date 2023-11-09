package org.humancellatlas.ingest.security;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RowLevelFilterSecurity {
    String expression();
    Class<?>[] ignoreClasses() default {};
}
