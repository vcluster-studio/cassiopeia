package xyz.vcluster.cassiopeia.framework.security.service;

import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.vcluster.cassiopeia.authentication.constant.AuthenticationConstant;
import xyz.vcluster.cassiopeia.authentication.model.CassiopeiaUserDetails;
import xyz.vcluster.cassiopeia.authentication.service.TokenService;
import xyz.vcluster.cassiopeia.common.config.TokenConfig;
import xyz.vcluster.cassiopeia.common.constant.Constants;
import xyz.vcluster.cassiopeia.common.core.domain.model.LoginUser;
import xyz.vcluster.cassiopeia.common.core.redis.RedisCache;
import xyz.vcluster.cassiopeia.common.utils.ServletUtils;
import xyz.vcluster.cassiopeia.common.utils.StringUtils;
import xyz.vcluster.cassiopeia.common.utils.ip.AddressUtils;
import xyz.vcluster.cassiopeia.common.utils.ip.IpUtils;
import xyz.vcluster.cassiopeia.common.utils.uuid.IdUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 使用redis缓存的令牌服务
 *
 * @author cassiopeia
 */
@Service
public class RedisCacheTokenService implements TokenService {

    private static final long MILLIS_SECOND = 1000;

    private static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private static final Long MILLIS_MINUTE_TEN = 20 * 60 * 1000L;

    /**
     * 令牌相关设定
     */
    @Autowired
    private TokenConfig tokenConfig;

    @Autowired
    private RedisCache redisCache;

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    @Override
    public CassiopeiaUserDetails getLoginUser(HttpServletRequest request) {
        // 获取请求携带的令牌
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token)) {
            try {
                Claims claims = parseToken(token);
                String tokenId = (String) claims.get(Constants.LOGIN_USER_KEY);
                String tokenKey = getTokenKey(tokenId);
                return redisCache.getCacheObject(tokenKey);
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * 设置用户身份信息
     */
    @Override
    public void setLoginUser(CassiopeiaUserDetails userDetails) {

        if (userDetails != null && StringUtils.isNotEmpty(userDetails.getTokenId())) {
            refreshToken(userDetails);
        }
    }

    /**
     * 删除用户身份信息
     */
    @Override
    public void delLoginUserByTokenId(String tokenId) {
        if (StringUtils.isNotEmpty(tokenId)) {
            String tokenKey = getTokenKey(tokenId);
            CassiopeiaUserDetails userDetails = redisCache.getCacheObject(tokenKey);
            if (userDetails != null) {
                String principal = userDetails.getPrincipal();
                if (StringUtils.isNotEmpty(principal)) {
                    String principalKey = getPrincipalKey(principal);
                    redisCache.deleteObject(principalKey);
                }
            }
            redisCache.deleteObject(tokenKey);
        }
    }

    /**
     * 删除用户身份信息
     */
    @Override
    public void delLoginUserByPrincipal(String principal) {
        if (StringUtils.isNotEmpty(principal)) {
            String principalKey = getPrincipalKey(principal);
            CassiopeiaUserDetails userDetails = redisCache.getCacheObject(principalKey);
            if (userDetails != null) {
                String tokenId = userDetails.getTokenId();
                if (StringUtils.isNotEmpty(tokenId)) {
                    String tokenKey = getTokenKey(tokenId);
                    redisCache.deleteObject(tokenKey);
                }
            }
            redisCache.deleteObject(principalKey);
        }
    }

    /**
     * 创建令牌
     *
     * @param userDetails 用户信息
     * @return 令牌
     */
    @Override
    public String createToken(CassiopeiaUserDetails userDetails) {

        String tokenId = IdUtils.simpleUUID();
        userDetails.setTokenId(tokenId);
        setUserAgent(userDetails);
        refreshToken(userDetails);

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, tokenId);
        claims.put(AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_KEY, userDetails.getAuthenticationMode());
        claims.put(AuthenticationConstant.SECURITY_AUTHENTICATION_SOURCE_KEY, userDetails.getAuthenticationSource());
        claims.put(AuthenticationConstant.SECURITY_AUTHENTICATION_PRINCIPAL_KEY, userDetails.getPrincipal());
        claims.put(AuthenticationConstant.SECURITY_FORM_PRINCIPAL_KEY, userDetails.getUsername());
        return createToken(claims);
    }

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     *
     * @param userDetails 登录信息
     * @return 令牌
     */
    @Override
    public void verifyToken(CassiopeiaUserDetails userDetails) {
        LoginUser loginUser = getLoginUser(userDetails);
        if (loginUser != null) {
            long expireTime = loginUser.getExpireTime();
            long currentTime = System.currentTimeMillis();
            if (expireTime - currentTime <= MILLIS_MINUTE_TEN) {
                refreshToken(userDetails);
            }
        }
    }

    /**
     * 刷新令牌有效期
     *
     * @param userDetails 登录信息
     */
    @Override
    public void refreshToken(CassiopeiaUserDetails userDetails) {

        LoginUser loginUser = getLoginUser(userDetails);
        if (loginUser != null) {
            loginUser.setLoginTime(System.currentTimeMillis());
            loginUser.setExpireTime(loginUser.getLoginTime() + tokenConfig.getExpireTime() * MILLIS_MINUTE);
            String tokenKey = getTokenKey(loginUser.getTokenId());
            redisCache.setCacheObject(tokenKey, loginUser, tokenConfig.getExpireTime(), TimeUnit.MINUTES);
            if (StringUtils.isNotEmpty(loginUser.getPrincipal())) {
                String principalKey = getPrincipalKey(loginUser.getPrincipal());
                redisCache.setCacheObject(principalKey, loginUser, tokenConfig.getExpireTime(), TimeUnit.MINUTES);
            }
        }
    }

    private LoginUser getLoginUser(CassiopeiaUserDetails userDetails) {
        if (userDetails instanceof LoginUser) {
            return (LoginUser) userDetails;
        }
        return null;
    }

    private void setUserAgent(CassiopeiaUserDetails userDetails) {

        LoginUser loginUser = getLoginUser(userDetails);
        if (loginUser != null) {
            UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
            String ip = IpUtils.getIpAddress(ServletUtils.getRequest());
            loginUser.setIpaddr(ip);
            loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
            loginUser.setBrowser(userAgent.getBrowser().getName());
            loginUser.setOs(userAgent.getOperatingSystem().getName());
        }
    }

    private String getToken(HttpServletRequest request) {

        String token = request.getHeader(tokenConfig.getHeader());
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    private String createToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, tokenConfig.getSecret())
                .compact();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(tokenConfig.getSecret())
                .parseClaimsJws(token)
                .getBody();
    }

    private String getTokenKey(String tokenId) {
        return Constants.LOGIN_TOKEN_KEY + tokenId;
    }

    private String getPrincipalKey(String principal) {
        return Constants.LOGIN_PRINCIPAL_KEY + principal;
    }
}
