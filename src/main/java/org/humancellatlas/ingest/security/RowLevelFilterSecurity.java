package org.humancellatlas.ingest.security;

import java.lang.annotation.*;
import java.util.List;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RowLevelFilterSecurity {
    String value();
    Class<?>[] ignoreClasses() default {};
}
