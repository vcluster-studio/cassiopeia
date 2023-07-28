package xyz.vcluster.cassiopeia.framework.security.handle;

import com.alibaba.fastjson.JSON;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import xyz.vcluster.cassiopeia.authentication.authenticator.CassiopeiaExternalAuthenticator;
import xyz.vcluster.cassiopeia.authentication.constant.AuthenticationConstant;
import xyz.vcluster.cassiopeia.authentication.handle.CassiopeiaLogoutSuccessHandler;
import xyz.vcluster.cassiopeia.authentication.manager.CassiopeiaExternalAuthenticatorManager;
import xyz.vcluster.cassiopeia.authentication.model.CassiopeiaUserDetails;
import xyz.vcluster.cassiopeia.authentication.provider.CassiopeiaAuthenticationToken;
import xyz.vcluster.cassiopeia.common.constant.Constants;
import xyz.vcluster.cassiopeia.common.core.domain.AjaxResult;
import xyz.vcluster.cassiopeia.common.utils.ServletUtils;
import xyz.vcluster.cassiopeia.common.utils.StringUtils;
import xyz.vcluster.cassiopeia.common.utils.http.HttpHelper;
import xyz.vcluster.cassiopeia.framework.manager.AsyncManager;
import xyz.vcluster.cassiopeia.framework.manager.factory.AsyncFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 自定义退出处理类 返回成功
 *
 * @author cassiopeia
 */
@Configuration
public class StandardLogoutSuccessHandler extends CassiopeiaLogoutSuccessHandler {

    private CassiopeiaExternalAuthenticatorManager externalAuthenticatorManager;

    /**
     * 退出处理
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        String redirectUri = request.getParameter(AuthenticationConstant.SECURITY_AUTHENTICATION_REDIRECT_URI_KEY);
        CassiopeiaUserDetails userDetails = this.getTokenService().getLoginUser(request);

        String logoutEndpoint;
        if (StringUtils.isNotNull(userDetails)) {
            if (authentication instanceof CassiopeiaAuthenticationToken) {
                externalAuthenticatorManager.cancelAuthenticate((CassiopeiaAuthenticationToken) authentication, userDetails.getAuthenticationMode());
            }

            String userName = userDetails.getUsername();
            // 删除用户缓存记录
            this.getTokenService().delLoginUserByTokenId(userDetails.getTokenId());
            // 记录用户退出日志
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(userName, Constants.LOGOUT, "退出成功", request));
        }

        CassiopeiaExternalAuthenticator authenticator = externalAuthenticatorManager.getDefaultAuthenticator();
        logoutEndpoint = authenticator.logoutEndpoint(redirectUri);

        if (Objects.equals(request.getMethod(), HttpMethod.GET.name())) {
            response.sendRedirect(HttpHelper.buildUrl(request, logoutEndpoint));
        } else {
            Map<String, String> data = new HashMap<>();
            data.put(AuthenticationConstant.SECURITY_AUTHENTICATION_REDIRECT_URI_KEY, HttpHelper.buildUrl(request, logoutEndpoint));
            ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success("退出成功", data)));
        }
    }

    public void setExternalAuthenticatorManager(CassiopeiaExternalAuthenticatorManager externalAuthenticatorManager) {
        this.externalAuthenticatorManager = externalAuthenticatorManager;
    }
}
