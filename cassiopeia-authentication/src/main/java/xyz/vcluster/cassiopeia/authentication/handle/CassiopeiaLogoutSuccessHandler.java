package xyz.vcluster.cassiopeia.authentication.handle;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import xyz.vcluster.cassiopeia.authentication.service.TokenService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 自定义退出处理抽象类
 *
 * @author cassiopeia
 */
public abstract class CassiopeiaLogoutSuccessHandler implements LogoutSuccessHandler {

    /**
     * 令牌服务
     */
    private TokenService tokenService;

    /**
     * 退出处理
     */
    @Override
    public abstract void onLogoutSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication)
            throws IOException, ServletException;

    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    protected TokenService getTokenService() {
        return this.tokenService;
    }
}
