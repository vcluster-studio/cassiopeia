package xyz.vcluster.cassiopeia.authentication.authenticator;

import xyz.vcluster.cassiopeia.authentication.model.CassiopeiaUserDetails;
import xyz.vcluster.cassiopeia.authentication.provider.CassiopeiaAuthenticationToken;

/**
 * 预认证器
 *
 * @author cassiopeia
 */
public interface CassiopeiaExternalAuthenticator {

    /**
     * 外部认证模式.
     *
     * @return 认证模式
     */
    String mode();

    /**
     * 外部认证源.
     *
     * @return 认证源
     */
    String source();

    /**
     * 授权端点.
     *
     * @return 授权端点
     */
    String authorizeEndpoint();

    /**
     * 授权端点.
     *
     * @return 授权端点
     */
    String authorizeEndpoint(String state);

    /**
     * 令牌端点.
     *
     * @return 令牌端点
     */
    String tokenEndpoint();

    /**
     * 登出端点.
     *
     * @return 登出端点
     */
    String logoutEndpoint();

    /**
     * 登出端点.
     *
     * @return 登出端点
     */
    String logoutEndpoint(String redirectUri);

    /**
     * 根据认证要求信息进行预认证.
     * 校验通过后，按照标识的匹配规则进行用户数据持久化.
     *
     * @param authentication     认证要求信息
     * @param authenticationMode 认证模式
     * @return 外部认证令牌
     */
    CassiopeiaAuthenticationToken doAuthenticate(CassiopeiaAuthenticationToken authentication, String authenticationMode);

    /**
     * 认证用户信息进行装饰.
     *
     * @param userDetails        认证用户信息
     * @param authenticationMode 认证模式
     * @return 认证用户信息
     */
    CassiopeiaUserDetails decorate(CassiopeiaUserDetails userDetails, String authenticationMode);

    /**
     * 根据认证令牌取消认证.
     * 校验通过后，按照标识的匹配规则取消用户数据持久化.
     *
     * @param authentication     认证令牌
     * @param authenticationMode 认证模式
     * @return 外部认证令牌
     */
    CassiopeiaAuthenticationToken cancelAuthenticate(CassiopeiaAuthenticationToken authentication, String authenticationMode);
}
