package xyz.vcluster.cassiopeia.authentication.manager;

import org.springframework.util.Assert;
import xyz.vcluster.cassiopeia.authentication.authenticator.CassiopeiaExternalAuthenticator;
import xyz.vcluster.cassiopeia.authentication.exception.AuthenticationModeUnsupportedException;
import xyz.vcluster.cassiopeia.authentication.model.CassiopeiaUserDetails;
import xyz.vcluster.cassiopeia.authentication.provider.CassiopeiaAuthenticationToken;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 外部认证管理器
 *
 * @author cassiopeia
 */
public class CassiopeiaExternalAuthenticatorManager {

    private CassiopeiaExternalAuthenticator defaultAuthenticator;

    private final List<String> authenticationModes = new LinkedList<>();

    private final Map<String, CassiopeiaExternalAuthenticator> authenticatorMap = new LinkedHashMap<>();

    public void registryDefaultAuthenticator(CassiopeiaExternalAuthenticator authenticator) {
        Assert.notNull(authenticator, "authenticator cannot be null");

        this.defaultAuthenticator = authenticator;

        if (!authenticationModes.contains(authenticator.mode())) {
            authenticationModes.add(authenticator.mode());
            authenticatorMap.put(authenticator.mode(), authenticator);
        }
    }

    public void registryAuthenticator(CassiopeiaExternalAuthenticator authenticator) {
        Assert.notNull(authenticator, "authenticator cannot be null");

        authenticationModes.add(authenticator.mode());
        authenticatorMap.put(authenticator.mode(), authenticator);
    }

    public CassiopeiaExternalAuthenticator getDefaultAuthenticator() {
        return defaultAuthenticator;
    }

    public List<String> getAuthenticationModes() {
        return authenticationModes;
    }


    public Map<String, CassiopeiaExternalAuthenticator> getAuthenticators() {
        return authenticatorMap;
    }

    public CassiopeiaExternalAuthenticator getAuthenticator(String authenticationMode) {
        return authenticatorMap.get(authenticationMode);
    }

    /**
     * 外部认证
     *
     * @param authentication     认证要求信息
     * @param authenticationMode 认证模式
     * @return 预认证令牌
     */
    public CassiopeiaAuthenticationToken doAuthenticate(CassiopeiaAuthenticationToken authentication, String authenticationMode) {
        if (!authenticationModes.contains(authenticationMode)) {
            throw new AuthenticationModeUnsupportedException(authenticationMode);
        }

        CassiopeiaExternalAuthenticator authenticator = authenticatorMap.get(authenticationMode);
        if (authenticator == null) {
            throw new AuthenticationModeUnsupportedException(authenticationMode);
        }

        return authenticator.doAuthenticate(authentication, authenticationMode);
    }

    /**
     * 外部用户装饰
     *
     * @param userDetails        认证用户信息
     * @param authenticationMode 认证模式
     * @return 认证用户信息
     */
    public CassiopeiaUserDetails decorate(CassiopeiaUserDetails userDetails, String authenticationMode) {
        if (!authenticationModes.contains(authenticationMode)) {
            throw new AuthenticationModeUnsupportedException(authenticationMode);
        }

        CassiopeiaExternalAuthenticator authenticator = authenticatorMap.get(authenticationMode);
        if (authenticator == null) {
            throw new AuthenticationModeUnsupportedException(authenticationMode);
        }

        return authenticator.decorate(userDetails, authenticationMode);
    }

    /**
     * 外部认证取消
     *
     * @param authentication     认证令牌
     * @param authenticationMode 认证模式
     * @return 认证令牌
     */
    public CassiopeiaAuthenticationToken cancelAuthenticate(CassiopeiaAuthenticationToken authentication, String authenticationMode) {
        if (!authenticationModes.contains(authenticationMode)) {
            throw new AuthenticationModeUnsupportedException(authenticationMode);
        }

        CassiopeiaExternalAuthenticator authenticator = authenticatorMap.get(authenticationMode);
        if (authenticator == null) {
            throw new AuthenticationModeUnsupportedException(authenticationMode);
        }

        return authenticator.cancelAuthenticate(authentication, authenticationMode);
    }
}
