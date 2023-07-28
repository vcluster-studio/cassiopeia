package xyz.vcluster.cassiopeia.common.annotation;

import org.apache.poi.ss.usermodel.*;

import java.lang.annotation.*;

/**
 * 导出Excel数据格式
 *
 * @author cassiopeia
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface CellStyleDescribe {

    /**
     * 导出字段：高度 单位为字符
     */
    double height() default 14;

    /**
     * 导出字段：宽 单位为字符
     */
    double width() default 16;

    /**
     * 导出字段：水平对齐方式
     */
    HorizontalAlignment horizontalAlignment() default HorizontalAlignment.CENTER;

    /**
     * 导出字段：垂直对齐方式
     */
    VerticalAlignment verticalAlignment() default VerticalAlignment.CENTER;

    /**
     * 导出字段：填充模式
     */
    FillPatternType fillPattern() default FillPatternType.SOLID_FOREGROUND;

    /**
     * 导出字段：填充前景颜色
     */
    IndexedColors fillForegroundColor() default IndexedColors.WHITE1;

    /**
     * 导出字段：填充背景颜色
     */
    IndexedColors fillBackgroundColor() default IndexedColors.WHITE1;

    /**
     * 导出字段：右边框样式
     */
    BorderStyle rightBorderStyle() default BorderStyle.THIN;

    /**
     * 导出字段：右边框颜色
     */
    IndexedColors rightBorderColor() default IndexedColors.GREY_50_PERCENT;

    /**
     * 导出字段：左边框样式
     */
    BorderStyle leftBorderStyle() default BorderStyle.THIN;

    /**
     * 导出字段：左边框颜色
     */
    IndexedColors leftBorderColor() default IndexedColors.GREY_50_PERCENT;

    /**
     * 导出字段：上边框样式
     */
    BorderStyle topBorderStyle() default BorderStyle.THIN;

    /**
     * 导出字段：上边框颜色
     */
    IndexedColors topBorderColor() default IndexedColors.GREY_50_PERCENT;

    /**
     * 导出字段：下边框样式
     */
    BorderStyle bottomBorderStyle() default BorderStyle.THIN;

    /**
     * 导出字段：下边框样式
     */
    IndexedColors bottomBorderColor() default IndexedColors.GREY_50_PERCENT;

    /**
     * 导出字段：文字自动换行
     */
    boolean warpText() default false;

    /**
     * 导出字段：单元格数据格式
     */
    String dataFormat() default "General";

    /**
     * 导出字段：文字字体
     */
    String fontName() default "Arial";

    /**
     * 导出字段：文字字号
     */
    short fontHeight() default 10;

    /**
     * 导出字段：文字颜色
     */
    IndexedColors fontColor() default IndexedColors.BLACK;

    /**
     * 导出字段：文字加粗
     */
    boolean fontBold() default false;

    /**
     * 导出字段：文字倾斜
     */
    boolean fontItalic() default false;

    /**
     * 导出字段：文字下划线
     */
    byte fontUnderline() default 0x0;
}