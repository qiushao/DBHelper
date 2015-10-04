package net.qiushao.lib.dbhelper.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Database {
	/**
	 * @return 数据库名，默认为包名（其中的'.' 会被替换为'_'） + ".db"
	 */
    String databaseName() default "";

    /**
     * @return 数据库存放的位置，默认是 '/data/data/包名/database'
     */
    String databaseDir() default "";

    /**
     * @return 表名，默认为简单类名
     */
    String tableName() default "";

    /**
     * @return 数据库表的版本，默认为1
     */
    int tableVersion() default 1;
}
