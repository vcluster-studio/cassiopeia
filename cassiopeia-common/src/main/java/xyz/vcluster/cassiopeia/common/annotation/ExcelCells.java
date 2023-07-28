package xyz.vcluster.cassiopeia.common.annotation;

import java.lang.annotation.*;

/**
 * Excel注解集
 *
 * @author cassiopeia
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelCells {
    ExcelCell[] value();
}
