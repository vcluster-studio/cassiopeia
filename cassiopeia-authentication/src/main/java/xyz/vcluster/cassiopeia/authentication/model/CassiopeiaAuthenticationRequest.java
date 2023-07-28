package xyz.vcluster.cassiopeia.authentication.model;

/**
 * 认证请求类.
 */
public class CassiopeiaAuthenticationRequest {

    /**
     * 认证模式
     */
    private String authenticationMode;

    /**
     * 认证模式源
     */
    private String authenticationSource;

    /**
     * 用户标识
     */
    private String principal;

    /**
     * 用户凭证
     */
    private String credentials;

    /**
     * 图形验证码标识
     */
    private String captchaUUID;

    /**
     * captchaCode
     */
    private String captchaCode;

    public String getAuthenticationMode() {
        return authenticationMode;
    }

    public void setAuthenticationMode(String authenticationMode) {
        this.authenticationMode = authenticationMode;
    }

    public String getAuthenticationSource() {
        return authenticationSource;
    }

    public void setAuthenticationSource(String authenticationSource) {
        this.authenticationSource = authenticationSource;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public String getCaptchaUUID() {
        return captchaUUID;
    }

    public void setCaptchaUUID(String captchaUUID) {
        this.captchaUUID = captchaUUID;
    }

    public String getCaptchaCode() {
        return captchaCode;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }
}
