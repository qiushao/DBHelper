package net.qiushao.lib.dbhelper.annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * @return 指定列的顺序，如果不指定，则列的先后顺序为随机排序，
     * 如果指定列的顺序，则所有的列都要指定顺序
     */
	int index() default -1;

    /**
     * @return 列的名称，默认为变量名
     */
    String name() default "";

    /**
     * @return 该列是否作为主键， 默认为false
     */
    boolean primary() default false;

    /**
     * @return 该列是否作为ID， ID要求类型为整型，作为表的第一列，为主键，自动增长
     * 一个表只能有一个ID， 指定了ID之后，就不能再指定 primary
     */
    boolean ID() default false;
}
