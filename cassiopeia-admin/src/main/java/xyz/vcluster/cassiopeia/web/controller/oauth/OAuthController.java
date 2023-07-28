package xyz.vcluster.cassiopeia.web.controller.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xyz.vcluster.cassiopeia.authentication.authenticator.CassiopeiaExternalAuthenticator;
import xyz.vcluster.cassiopeia.authentication.constant.AuthenticationConstant;
import xyz.vcluster.cassiopeia.authentication.manager.CassiopeiaExternalAuthenticatorManager;
import xyz.vcluster.cassiopeia.authentication.model.CassiopeiaAuthenticationRequest;
import xyz.vcluster.cassiopeia.authentication.model.CassiopeiaUserDetails;
import xyz.vcluster.cassiopeia.common.config.TokenConfig;
import xyz.vcluster.cassiopeia.common.constant.Constants;
import xyz.vcluster.cassiopeia.common.core.domain.AjaxResult;
import xyz.vcluster.cassiopeia.common.core.redis.RedisCache;
import xyz.vcluster.cassiopeia.common.utils.StringUtils;
import xyz.vcluster.cassiopeia.common.utils.http.HttpHelper;
import xyz.vcluster.cassiopeia.common.utils.uuid.IdUtils;
import xyz.vcluster.cassiopeia.framework.security.service.RedisCacheTokenService;
import xyz.vcluster.cassiopeia.framework.web.service.SysLoginService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 通用请求处理.
 *
 * @author cassiopeia
 */
@RestController
@RequestMapping("/oauth")
public class OAuthController {

    private static final Logger log = LoggerFactory.getLogger(OAuthController.class);

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedisCacheTokenService tokenService;

    @Autowired
    private TokenConfig tokenConfig;

    @Autowired
    private SysLoginService loginService;

    /**
     * 外部认证管理器
     */
    @Autowired
    private CassiopeiaExternalAuthenticatorManager externalAuthenticatorManager;

    /**
     * 标准外部认证器
     */
    @Resource(name = "standardExternalAuthenticator")
    private CassiopeiaExternalAuthenticator standardExternalAuthenticator;

    @GetMapping(value = "/definition")
    public AjaxResult definition() {

        String defaultAuthenticationMode = standardExternalAuthenticator.mode();
        String defaultAuthenticationSource = standardExternalAuthenticator.source();
        List<HttpHelper.RequestParameter> requestParameters = new LinkedList<>();
        requestParameters.add(new HttpHelper.RequestParameter(
                AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_KEY,
                defaultAuthenticationMode));
        requestParameters.add(new HttpHelper.RequestParameter(
                AuthenticationConstant.SECURITY_AUTHENTICATION_SOURCE_KEY,
                defaultAuthenticationSource));

        String defaultAuthorizeEndpoint = HttpHelper.encodeUrl(AuthenticationConstant.SECURITY_ENDPOINT_AUTHORIZE, requestParameters);
        String defaultTokenEndpoint = AuthenticationConstant.SECURITY_ENDPOINT_TOKEN;
        String defaultLogoutEndpoint = AuthenticationConstant.SECURITY_ENDPOINT_LOGOUT;

        CassiopeiaExternalAuthenticator externalAuthenticator = externalAuthenticatorManager.getDefaultAuthenticator();
        if (externalAuthenticator != null) {
            defaultAuthenticationMode = externalAuthenticator.mode();
            defaultAuthenticationSource = externalAuthenticator.source();
            requestParameters = new LinkedList<>();
            requestParameters.add(new HttpHelper.RequestParameter(
                    AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_KEY,
                    defaultAuthenticationMode));
            requestParameters.add(new HttpHelper.RequestParameter(
                    AuthenticationConstant.SECURITY_AUTHENTICATION_SOURCE_KEY,
                    defaultAuthenticationSource));

            defaultAuthorizeEndpoint = HttpHelper.encodeUrl(AuthenticationConstant.SECURITY_ENDPOINT_AUTHORIZE, requestParameters);
        }

        List<String> allowedAuthenticationModes = externalAuthenticatorManager.getAuthenticationModes();
        List<String> allowedAuthenticationSources = new LinkedList<>();
        Map<String, Map<String, Object>> authenticationDefinitions = new HashMap<>();
        Map<String, Object> definition;
        for (String authenticationMode : allowedAuthenticationModes) {
            externalAuthenticator = externalAuthenticatorManager.getAuthenticator(authenticationMode);
            if (externalAuthenticator != null) {
                definition = new HashMap<>();
                definition.put("authentication_mode", externalAuthenticator.mode());
                definition.put("authentication_source", externalAuthenticator.source());
                requestParameters = new LinkedList<>();
                requestParameters.add(new HttpHelper.RequestParameter(
                        AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_KEY,
                        externalAuthenticator.mode()));
                requestParameters.add(new HttpHelper.RequestParameter(
                        AuthenticationConstant.SECURITY_AUTHENTICATION_SOURCE_KEY,
                        externalAuthenticator.source()));
                definition.put("authorize_endpoint", HttpHelper.encodeUrl(AuthenticationConstant.SECURITY_ENDPOINT_AUTHORIZE, requestParameters));
                definition.put("token_endpoint", defaultTokenEndpoint);
                definition.put("logout_endpoint", defaultLogoutEndpoint);
                allowedAuthenticationSources.add(externalAuthenticator.source());
                authenticationDefinitions.put(externalAuthenticator.source(), definition);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("default_authentication_mode", defaultAuthenticationMode);
        data.put("default_authentication_source", defaultAuthenticationSource);
        data.put("default_authorize_endpoint", defaultAuthorizeEndpoint);
        data.put("default_token_endpoint", defaultTokenEndpoint);
        data.put("default_logout_endpoint", defaultLogoutEndpoint);
        data.put("allowed_authentication_modes", allowedAuthenticationModes);
        data.put("allowed_authentication_sources", allowedAuthenticationSources);
        data.put("authentication_definitions", authenticationDefinitions);

        return AjaxResult.success(data);
    }

    @GetMapping(value = "/authorize")
    public void authorize(HttpServletRequest request,
                          HttpServletResponse response,
                          @RequestParam("authentication_mode") String authenticationMode,
                          @RequestParam("authentication_source") String authenticationSource,
                          @RequestParam(value = "redirect_uri", required = false) String redirectUri
    ) throws IOException {
        HttpHelper.dump(request);
        CassiopeiaUserDetails userDetails = tokenService.getLoginUser(request);
        if (userDetails != null) {
            // 当前请求有认证令牌
            response.sendRedirect(HttpHelper.buildUrl(request, redirectUri));
        } else {
            // 当前请求无认证令牌
            String state = IdUtils.simpleUUID();

            Map<String, String> data = new HashMap<>();
            data.put(AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_KEY, authenticationMode);
            data.put(AuthenticationConstant.SECURITY_AUTHENTICATION_SOURCE_KEY, authenticationSource);
            data.put(AuthenticationConstant.SECURITY_AUTHENTICATION_REDIRECT_URI_KEY, redirectUri);
            redisCache.setCacheObject(Constants.AUTHORIZE_STATE_KEY + state, data, tokenConfig.getExpireTime(), TimeUnit.MINUTES);

            CassiopeiaExternalAuthenticator externalAuthenticator = externalAuthenticatorManager.getAuthenticator(authenticationMode);
            String authorizeEndpoint = externalAuthenticator.authorizeEndpoint(state);
            response.sendRedirect(HttpHelper.buildUrl(request, authorizeEndpoint));
        }
    }

    @GetMapping(value = "/authenticate", params = "ticket")
    public void authenticate(HttpServletRequest request,
                             HttpServletResponse response,
                             @RequestParam String state,
                             @RequestParam("ticket") String ticket
    ) throws IOException {
        HttpHelper.dump(request);
        List<HttpHelper.RequestParameter> requestParameters = new LinkedList<>();

        Map<String, String> data = redisCache.getCacheObject(Constants.AUTHORIZE_STATE_KEY + state);
        if (data != null) {
            redisCache.deleteObject(Constants.AUTHORIZE_STATE_KEY + state);
            String authenticationMode = data.get(AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_KEY);
            String authenticationSource = data.get(AuthenticationConstant.SECURITY_AUTHENTICATION_SOURCE_KEY);
            String redirectUri = data.get(AuthenticationConstant.SECURITY_AUTHENTICATION_REDIRECT_URI_KEY);

            requestParameters.add(new HttpHelper.RequestParameter(
                    AuthenticationConstant.SECURITY_FORM_PRINCIPAL_KEY,
                    state));
            requestParameters.add(new HttpHelper.RequestParameter(
                    AuthenticationConstant.SECURITY_FORM_CREDENTIAL_KEY,
                    ticket));
            requestParameters.add(new HttpHelper.RequestParameter(
                    AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_KEY,
                    authenticationMode));
            requestParameters.add(new HttpHelper.RequestParameter(
                    AuthenticationConstant.SECURITY_AUTHENTICATION_SOURCE_KEY,
                    authenticationSource));
            if (StringUtils.isNotEmpty(redirectUri)) {
                requestParameters.add(new HttpHelper.RequestParameter(
                        AuthenticationConstant.SECURITY_AUTHENTICATION_REDIRECT_URI_KEY,
                        redirectUri));
            }
        }

        String loginEndpoint = HttpHelper.encodeUrl(AuthenticationConstant.SECURITY_ENDPOINT_LOGIN, requestParameters);

        response.sendRedirect(HttpHelper.buildUrl(request, loginEndpoint));
    }

    @PostMapping(value = "/token")
    public AjaxResult token(@RequestBody CassiopeiaAuthenticationRequest authenticationRequest) {

        AjaxResult ajax = AjaxResult.success();
        ajax.put(Constants.TOKEN, loginService.authenticate(
                authenticationRequest.getPrincipal(),
                authenticationRequest.getCredentials(),
                authenticationRequest.getAuthenticationMode(),
                authenticationRequest.getAuthenticationSource(),
                authenticationRequest.getCaptchaUUID(),
                authenticationRequest.getCaptchaCode()
        ));
        return ajax;
    }
}
