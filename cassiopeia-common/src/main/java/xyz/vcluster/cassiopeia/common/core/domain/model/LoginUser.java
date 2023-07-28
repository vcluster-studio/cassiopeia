package xyz.vcluster.cassiopeia.common.core.domain.model;

import xyz.vcluster.cassiopeia.authentication.constant.AuthenticationConstant;
import xyz.vcluster.cassiopeia.authentication.model.CassiopeiaUserDetails;
import xyz.vcluster.cassiopeia.common.core.domain.entity.SysUser;

import java.util.Set;

/**
 * 登录用户身份权限
 *
 * @author cassiopeia
 */
public class LoginUser extends CassiopeiaUserDetails {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 权限列表
     */
    private Set<String> permissions;

    /**
     * 用户信息
     */
    private SysUser user;

    public LoginUser() {
    }

    public LoginUser(SysUser user, Set<String> permissions) {
        this((user != null ? user.getUserId() : null), (user != null ? user.getDeptId() : null), user, permissions);
    }

    public LoginUser(Long userId, Long deptId, SysUser user, Set<String> permissions) {
        this(userId, deptId, user, permissions, AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_PASSWORD,
                AuthenticationConstant.SECURITY_AUTHENTICATION_SOURCE_LOCAL);
    }

    public LoginUser(String authenticationMode,
                     String authenticationSource) {
        this(null, null, null, null, authenticationMode, authenticationSource);
    }

    public LoginUser(Long userId,
                     Long deptId,
                     SysUser user,
                     Set<String> permissions,
                     String authenticationMode,
                     String authenticationSource) {

        this.setUsername(user != null ? user.getUserName() : null);
        this.setPassword(user != null ? user.getPassword() : null);
        this.setAuthenticationMode(authenticationMode);
        this.setAuthenticationSource(authenticationSource);

        this.userId = userId;
        this.deptId = deptId;
        this.user = user;
        this.permissions = permissions;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
    }

    public String getIpaddr() {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr) {
        this.ipaddr = ipaddr;
    }

    public String getLoginLocation() {
        return loginLocation;
    }

    public void setLoginLocation(String loginLocation) {
        this.loginLocation = loginLocation;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public SysUser getUser() {
        return user;
    }

    public void setUser(SysUser user) {
        this.user = user;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();

        if (this.user != null) {
            this.user.setPassword(null);
        }
    }
}
