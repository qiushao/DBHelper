package net.qiushao.lib.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Database {
	/**
	 * database name, dbhelper will add ".db" suffix automatic
	 * @return
	 */
    String name();
    
    /**
     * if timestamp is true, database name will be "name+yy-mm-dd" format
     * @return
     */
    boolean timestamp() default false;
}
