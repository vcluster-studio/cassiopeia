package xyz.vcluster.cassiopeia.authentication.exception;

/**
 * 认证模式异常
 *
 * @author cassiopeia
 */
public class AuthenticationModeUnsupportedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 认证模式
     */
    private String authenticationMode;

    public AuthenticationModeUnsupportedException(String authenticationMode, String message, Throwable cause) {
        super(message, cause);
        this.authenticationMode = authenticationMode;
    }

    public AuthenticationModeUnsupportedException(String authenticationMode, String message) {
        this(authenticationMode, message, null);
    }

    public AuthenticationModeUnsupportedException(String authenticationMode, Throwable cause) {
        this(authenticationMode, cause.getMessage(), cause);
    }

    public AuthenticationModeUnsupportedException(String authenticationMode) {
        this(authenticationMode, null, null);
    }

    public String getAuthenticationMode() {
        return authenticationMode;
    }

    public void setAuthenticationMode(String authenticationMode) {
        this.authenticationMode = authenticationMode;
    }

}