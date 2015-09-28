package net.qiushao.lib.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * if an java bean is annotated by @Database and @Timestamp,
 * then DBHelper will auto add timestamp to the database name
 * such as Person2015-09-28.db
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Timestamp {
    String format() default "yy-MM-dd";
}
