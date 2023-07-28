package xyz.vcluster.cassiopeia.framework.security.authenticator;

import org.springframework.stereotype.Service;
import xyz.vcluster.cassiopeia.authentication.authenticator.CassiopeiaExternalAuthenticator;
import xyz.vcluster.cassiopeia.authentication.constant.AuthenticationConstant;
import xyz.vcluster.cassiopeia.authentication.model.CassiopeiaUserDetails;
import xyz.vcluster.cassiopeia.authentication.provider.CassiopeiaAuthenticationToken;
import xyz.vcluster.cassiopeia.common.utils.StringUtils;
import xyz.vcluster.cassiopeia.common.utils.http.HttpHelper;

import java.util.LinkedList;
import java.util.List;

/**
 * 标准外部认证器
 *
 * @author cassiopeia
 */
@Service("standardExternalAuthenticator")
public class StandardExternalAuthenticator implements CassiopeiaExternalAuthenticator {

    private final static String ENDPOINT_AUTHORIZE = AuthenticationConstant.SECURITY_ENDPOINT_LOGIN;

    private final static String ENDPOINT_TOKEN = AuthenticationConstant.SECURITY_ENDPOINT_TOKEN;

    private final static String ENDPOINT_LOGOUT = AuthenticationConstant.SECURITY_ENDPOINT_LOGIN;

    /**
     * 外部认证模式.
     *
     * @return 认证模式
     */
    public String mode() {
        return AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_PASSWORD;
    }

    /**
     * 外部认证源.
     *
     * @return 认证源
     */
    public String source() {
        return AuthenticationConstant.SECURITY_AUTHENTICATION_SOURCE_LOCAL;
    }

    /**
     * 授权端点.
     *
     * @return 授权端点
     */
    public String authorizeEndpoint() {
        return authorizeEndpoint(null);
    }


    /**
     * 授权端点.
     *
     * @return 授权端点
     */
    public String authorizeEndpoint(String state) {

        List<HttpHelper.RequestParameter> requestParameters = new LinkedList<>();
        requestParameters.add(new HttpHelper.RequestParameter(
                AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_KEY,
                AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_PASSWORD));
        requestParameters.add(new HttpHelper.RequestParameter(
                AuthenticationConstant.SECURITY_AUTHENTICATION_SOURCE_KEY,
                AuthenticationConstant.SECURITY_AUTHENTICATION_SOURCE_LOCAL));
        if (StringUtils.isNotEmpty(state)) {
            requestParameters.add(new HttpHelper.RequestParameter(
                    AuthenticationConstant.SECURITY_AUTHENTICATION_STATE_KEY,
                    state));
        }

        return HttpHelper.encodeUrl(ENDPOINT_AUTHORIZE, requestParameters);
    }

    /**
     * 令牌端点.
     *
     * @return 令牌端点
     */
    public String tokenEndpoint() {
        return ENDPOINT_TOKEN;
    }

    /**
     * 登出端点.
     *
     * @return 登出端点
     */
    public String logoutEndpoint(String redirectUri) {
        List<HttpHelper.RequestParameter> requestParameters = new LinkedList<>();
        requestParameters.add(new HttpHelper.RequestParameter(
                AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_KEY,
                AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_PASSWORD));
        requestParameters.add(new HttpHelper.RequestParameter(
                AuthenticationConstant.SECURITY_AUTHENTICATION_SOURCE_KEY,
                AuthenticationConstant.SECURITY_AUTHENTICATION_SOURCE_LOCAL));
        if (StringUtils.isNotEmpty(redirectUri)) {
            requestParameters.add(new HttpHelper.RequestParameter(
                    AuthenticationConstant.SECURITY_AUTHENTICATION_REDIRECT_URI_KEY,
                    redirectUri));
        }

        return HttpHelper.encodeUrl(ENDPOINT_LOGOUT, requestParameters);
    }

    /**
     * 登出端点.
     *
     * @return 登出端点
     */
    public String logoutEndpoint() {
        return ENDPOINT_LOGOUT;
    }

    /**
     * 根据认证要求信息进行预认证.
     * 校验通过后，按照标识的匹配规则进行用户数据持久化.
     * 用户数据已在本地持久化，无需特别操作
     *
     * @param authentication     认证要求信息
     * @param authenticationMode 认证模式
     * @return 预认证令牌
     */
    @Override
    public CassiopeiaAuthenticationToken doAuthenticate(CassiopeiaAuthenticationToken authentication, String authenticationMode) {

        return authentication;
    }

    /**
     * 认证用户信息进行装饰.
     *
     * @param userDetails        认证用户信息
     * @param authenticationMode 认证模式
     * @return 认证用户信息
     */
    @Override
    public CassiopeiaUserDetails decorate(CassiopeiaUserDetails userDetails, String authenticationMode) {

        // 标准认证不使用外部源的用户标识进行认证用户的缓存
        userDetails.setPrincipal(null);

        return userDetails;
    }

    /**
     * 根据认证令牌取消认证.
     * 校验通过后，按照标识的匹配规则取消用户数据持久化.
     * 用户数据已在本地持久化，无需特别操作
     *
     * @param authentication     认证令牌
     * @param authenticationMode 认证模式
     * @return 认证令牌
     */
    @Override
    public CassiopeiaAuthenticationToken cancelAuthenticate(CassiopeiaAuthenticationToken authentication, String authenticationMode) {

        return authentication;
    }

}
