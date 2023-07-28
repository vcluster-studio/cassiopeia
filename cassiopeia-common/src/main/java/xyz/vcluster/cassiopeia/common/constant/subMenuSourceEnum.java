package xyz.vcluster.cassiopeia.common.constant;

/**
 * 子菜单来源类型
 *
 * @author cassiopeia
 */
public enum subMenuSourceEnum {
    PRESET(0,"菜单管理预置"),
    BIZ_DYNAMIC_GET(1,"业务接口动态获取");

    private int key;
    private String desc;

    subMenuSourceEnum(int key, String desc) {
        this.key = key;
        this.desc = desc;
    }

}
