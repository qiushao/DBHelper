package net.qiushao.lib.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Database {
	/**
	 * database name, dbhelper will add ".db" suffix automatic,
     * default is package name
	 */
    String databaseName() default "";

    String databaseDir() default "";

    /**
     * table name, default is class simple name
     */
    String tableName() default "";

    /**
     * table version
     */
    int tableVersion() default 1;
}
