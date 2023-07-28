package xyz.vcluster.cassiopeia.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * 路由配置信息
 *
 * @author cassiopeia
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RouterVo {
    /**
     * 路由名字
     */
    private String name;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 是否隐藏路由，当设置 true 的时候该路由不会再侧边栏出现
     */
    private boolean hidden;

    /**
     * 重定向地址，当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
     */
    private String redirect;

    /**
     * 组件地址
     */
    private String component;

    /**
     * 路由参数：如 {"id": 1, "name": "ry"}
     */
    private String query;

    /**
     * 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
     */
    private Boolean alwaysShow;

    /**
     * 其他元素
     */
    private MetaVo meta;

    /**
     * 子菜单来源
     * 0:菜单管理预置
     * 1:业务接口动态获取
     */
    private Integer subMenuSource = 0;

    /**
     * 子菜单来源业务接口
     */
    private String subMenuDataUrl;

    /**
     * 事件类型
     * 0:无事件
     * 1:左击事件
     * 2:右击事件
     */
    private Integer eventType=0;

    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 事件参数值
     */
    private String[] eventParam;

    /**
     * 事件图标
     */
    private String eventIcon;

    /**
     * 页面展现方式
     * 0:新标签页
     * 1:新选项卡
     */
    private Integer pageShowType=0;

    /**
     * 子路由
     */
    private List<RouterVo> children;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean getHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Boolean getAlwaysShow() {
        return alwaysShow;
    }

    public void setAlwaysShow(Boolean alwaysShow) {
        this.alwaysShow = alwaysShow;
    }

    public MetaVo getMeta() {
        return meta;
    }

    public void setMeta(MetaVo meta) {
        this.meta = meta;
    }

    public Integer getSubMenuSource() {
        return subMenuSource;
    }

    public void setSubMenuSource(Integer subMenuSource) {
        this.subMenuSource = subMenuSource;
    }

    public String getSubMenuDataUrl() {
        return subMenuDataUrl;
    }

    public void setSubMenuDataUrl(String subMenuDataUrl) {
        this.subMenuDataUrl = subMenuDataUrl;
    }

    public Integer getEventType() {
        return eventType;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String[] getEventParam() {
        return eventParam;
    }

    public void setEventParam(String[] eventParam) {
        this.eventParam = eventParam;
    }

    public String getEventIcon() {
        return eventIcon;
    }

    public void setEventIcon(String eventIcon) {
        this.eventIcon = eventIcon;
    }

    public Integer getPageShowType() {
        return pageShowType;
    }

    public void setPageShowType(Integer pageShowType) {
        this.pageShowType = pageShowType;
    }

    public List<RouterVo> getChildren() {
        return children;
    }

    public void setChildren(List<RouterVo> children) {
        this.children = children;
    }
}
