package net.qiushao.lib.dbhelper.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	/**
	 * index start with 1, because index 0 already use _id as primary key.
	 * @return
	 */
	int index();
}
