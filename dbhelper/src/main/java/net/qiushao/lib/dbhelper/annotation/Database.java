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

    /**
     * if timestamp is true, database name will add timestamp info, for example : person2015-09-24.db
     * in most case, you no need set timestamp.
     */
    boolean timestamp() default false;

    /**
     * table name, default is class simple name
     */
    String tableName() default "";

    /**
     * table version
     */
    int tableVersion() default 1;

    /**
     * if add an column _id as the primary key and autoincrement
     * if true, column _id's index will be 0, so customer column index should start with 1
     */
    boolean addID() default false;
}
