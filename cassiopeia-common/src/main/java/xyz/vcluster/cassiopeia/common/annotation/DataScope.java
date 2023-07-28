package xyz.vcluster.cassiopeia.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限过滤注解
 *
 * @author cassiopeia
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    /**
     * 部门表的别名
     */
    String deptAlias() default "";

    /**
     * 用户表的别名
     */
    String userAlias() default "";
}
