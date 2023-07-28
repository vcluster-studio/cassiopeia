package xyz.vcluster.cassiopeia.authentication.filter;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import xyz.vcluster.cassiopeia.authentication.model.CassiopeiaUserDetails;
import xyz.vcluster.cassiopeia.authentication.provider.CassiopeiaAuthenticationToken;
import xyz.vcluster.cassiopeia.authentication.service.TokenService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 令牌过滤器，验证令牌有效性
 *
 * @author cassiopeia
 */
public class CassiopeiaAuthenticationTokenFilter extends OncePerRequestFilter {

    /**
     * 令牌服务
     */
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        CassiopeiaUserDetails loginUser = tokenService.getLoginUser(request);
        if (loginUser != null && StringUtils.isEmpty(SecurityContextHolder.getContext().getAuthentication())) {
            tokenService.verifyToken(loginUser);
            CassiopeiaAuthenticationToken authenticationToken
                    = new CassiopeiaAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            authenticationToken.setAuthenticationMode(loginUser.getAuthenticationMode());
            authenticationToken.setAuthenticationSource(loginUser.getAuthenticationSource());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        chain.doFilter(request, response);

    }

    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    protected TokenService getTokenService() {
        return this.tokenService;
    }
}
