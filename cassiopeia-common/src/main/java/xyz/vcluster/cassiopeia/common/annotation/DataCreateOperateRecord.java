package xyz.vcluster.cassiopeia.common.annotation;

import java.lang.annotation.*;

/**
 * 数据创建操作记录注解
 *
 * @author cassiopeia
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface DataCreateOperateRecord {

}
