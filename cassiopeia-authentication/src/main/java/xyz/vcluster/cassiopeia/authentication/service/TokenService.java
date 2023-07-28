package xyz.vcluster.cassiopeia.authentication.service;

import xyz.vcluster.cassiopeia.authentication.model.CassiopeiaUserDetails;

import javax.servlet.http.HttpServletRequest;

/**
 * 令牌服务
 *
 * @author cassiopeia
 */
public interface TokenService {

    /**
     * 获取登录用户信息
     */
    CassiopeiaUserDetails getLoginUser(HttpServletRequest request);

    /**
     * 设置登录用户信息
     */
    void setLoginUser(CassiopeiaUserDetails userDetails);

    /**
     * 删除登录用户信息
     */
    void delLoginUserByTokenId(String tokenId);

    /**
     * 删除登录用户信息
     */
    void delLoginUserByPrincipal(String principal);

    /**
     * 创建令牌
     */
    String createToken(CassiopeiaUserDetails userDetails);

    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     */
    void verifyToken(CassiopeiaUserDetails userDetails);

    /**
     * 刷新令牌有效期
     */
    void refreshToken(CassiopeiaUserDetails userDetails);
}
