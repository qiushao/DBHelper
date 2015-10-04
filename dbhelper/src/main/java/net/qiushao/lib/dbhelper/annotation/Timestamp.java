package net.qiushao.lib.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 如果同时使用了@Database 和 @Timestamp 注解，
 * 则DBHelper会在数据库的名称中加上时间戳信息。
 * 例如本来数据库是"person.db"，加上@Timestamp注解后，
 * 数据库名就会变成"person2015-10-04.db",其中的日期为当前的日期，日期的格式可以通过format指定。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Timestamp {
    /**
     * @return 日期的格式
     */
    String format() default "yyyy-MM-dd";
}
