package xyz.vcluster.cassiopeia.common.enums;

/**
 * 通用枚举
 *
 * @author cassiopeia
 */
public enum CommonEnum {
    SYSTEM_BUILTIN_YES(1, "Y", "系统内置"),
    SYSTEM_BUILTIN_NO(0, "N", "非系统内置"),
    ;

    private final Integer code;
    private final String value;
    private final String description;

    CommonEnum(Integer code, String value, String description) {
        this.code = code;
        this.value = value;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
