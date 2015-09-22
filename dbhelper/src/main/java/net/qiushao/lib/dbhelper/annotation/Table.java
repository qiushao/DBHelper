package net.qiushao.lib.dbhelper.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
	/**
	 * table version
	 * @return
	 */
	int version() default 1;
	
	/**
	 * table name
	 * @return
	 */
	String name();
}
