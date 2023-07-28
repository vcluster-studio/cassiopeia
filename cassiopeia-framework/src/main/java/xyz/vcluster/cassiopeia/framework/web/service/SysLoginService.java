package xyz.vcluster.cassiopeia.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xyz.vcluster.cassiopeia.authentication.constant.AuthenticationConstant;
import xyz.vcluster.cassiopeia.authentication.exception.ExternalAuthenticationException;
import xyz.vcluster.cassiopeia.authentication.provider.CassiopeiaAuthenticationToken;
import xyz.vcluster.cassiopeia.common.constant.Constants;
import xyz.vcluster.cassiopeia.common.core.domain.entity.SysUser;
import xyz.vcluster.cassiopeia.common.core.domain.model.LoginUser;
import xyz.vcluster.cassiopeia.common.core.redis.RedisCache;
import xyz.vcluster.cassiopeia.common.exception.user.CaptchaException;
import xyz.vcluster.cassiopeia.common.exception.user.CaptchaExpireException;
import xyz.vcluster.cassiopeia.common.exception.user.UserPasswordNotMatchException;
import xyz.vcluster.cassiopeia.common.utils.DateUtils;
import xyz.vcluster.cassiopeia.common.utils.MessageUtils;
import xyz.vcluster.cassiopeia.common.utils.ServletUtils;
import xyz.vcluster.cassiopeia.common.utils.ip.IpUtils;
import xyz.vcluster.cassiopeia.framework.manager.AsyncManager;
import xyz.vcluster.cassiopeia.framework.manager.factory.AsyncFactory;
import xyz.vcluster.cassiopeia.framework.security.service.RedisCacheTokenService;
import xyz.vcluster.cassiopeia.system.service.ISysConfigService;
import xyz.vcluster.cassiopeia.system.service.ISysUserService;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 登录校验方法
 *
 * @author cassiopeia
 */
@Service
public class SysLoginService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedisCacheTokenService tokenService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 指定认证模式认证
     *
     * @param principal            用户标识
     * @param credentials          用户凭证
     * @param authenticationMode   认证模式
     * @param authenticationSource 认证源
     * @param captchaUUID          图形验证码标识
     * @param captchaCode          图形验证码内容
     * @return 认证令牌
     */
    public String authenticate(String principal,
                               String credentials,
                               String authenticationMode,
                               String authenticationSource,
                               String captchaUUID,
                               String captchaCode) {
        switch (authenticationMode) {
            case AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_PASSWORD:
                boolean captchaOnOff = configService.selectCaptchaOnOff();
                if (captchaOnOff) {
                    validateCaptcha(principal, captchaUUID, captchaCode);
                }
                break;
        }

        // 用户验证
        Authentication authentication;
        try {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put(AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_KEY, authenticationMode);
            details.put(AuthenticationConstant.SECURITY_AUTHENTICATION_SOURCE_KEY, authenticationSource);
            CassiopeiaAuthenticationToken authenticationToken =
                    new CassiopeiaAuthenticationToken(principal, credentials);
            authenticationToken.setAuthenticationMode(authenticationMode);
            authenticationToken.setAuthenticationSource(authenticationSource);
            authenticationToken.setDetails(details);
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (ExternalAuthenticationException | InternalAuthenticationServiceException e) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(principal,
                    Constants.LOGIN_FAIL, e.getMessage()));
            throw e;
        } catch (UsernameNotFoundException e) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(principal,
                    Constants.LOGIN_FAIL, MessageUtils.message("user.not.exists")));
            throw new UserPasswordNotMatchException();
        } catch (BadCredentialsException e) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(principal,
                    Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        } catch (Exception e) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(principal,
                    Constants.LOGIN_FAIL, e.getMessage()));
            throw new InternalAuthenticationServiceException(e.getMessage());
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(principal,
                Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        if (loginUser.getUser() != null) {
            recordLoginInfo(loginUser.getUser());
        }

        return tokenService.createToken(loginUser);
    }

    /**
     * 校验验证码
     *
     * @param username    用户名
     * @param captchaUUID 图形验证码标识
     * @param captchaCode 图形验证码内容
     */
    public void validateCaptcha(String username,
                                String captchaUUID,
                                String captchaCode) {
        String verifyKey = Constants.CAPTCHA_CODE_KEY + captchaUUID;
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
            throw new CaptchaExpireException();
        }
        if (!captchaCode.equalsIgnoreCase(captcha)) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
    }

    /**
     * 记录登录信息
     */
    public void recordLoginInfo(SysUser user) {

        user.setLoginIp(IpUtils.getIpAddress(ServletUtils.getRequest()));
        user.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(user);
    }
}
