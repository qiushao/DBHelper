package net.qiushao.lib.dbhelper.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	/**
	 * column index in table
	 */
	int index() default -1;

    /**
     * column name, default is field name
     */
    String name() default "";

    /**
     * if this column is primary key
     */
    boolean primary() default false;
}
