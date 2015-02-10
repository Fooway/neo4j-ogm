package org.springframework.data.neo4j.ogm.annotation.typeconversion;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface DateString {

    static final String CLASS = "org.springframework.data.neo4j.ogm.annotation.typeconversion.DateString";
    static final String FORMAT = "value";

    static final String ISO_8601 ="yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    String value() default ISO_8601;

}

