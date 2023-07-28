package xyz.vcluster.cassiopeia.authentication.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 外部认证源凭证失效
 *
 * @author cassiopeia
 */
public class ExternalAuthenticationException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    /**
     * 认证模式
     */
    private String authenticationMode;

    /**
     * 认证源
     */
    private String authenticationSource;

    public ExternalAuthenticationException(String authenticationMode,
                                           String authenticationSource,
                                           String message,
                                           Throwable cause) {
        super(message, cause);
        this.authenticationMode = authenticationMode;
        this.authenticationSource = authenticationSource;
    }

    public ExternalAuthenticationException(String authenticationMode,
                                           String authenticationSource,
                                           String message) {
        this(authenticationMode, authenticationSource, message, null);
    }

    public ExternalAuthenticationException(String authenticationMode,
                                           String authenticationSource,
                                           Throwable cause) {
        this(authenticationMode, authenticationSource, cause.getMessage(), cause);
    }

    public ExternalAuthenticationException(String authenticationMode, String authenticationSource) {
        this(authenticationMode, authenticationSource, null, null);
    }

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
}