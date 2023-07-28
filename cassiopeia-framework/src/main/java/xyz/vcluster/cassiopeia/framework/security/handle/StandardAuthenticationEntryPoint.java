package xyz.vcluster.cassiopeia.framework.security.handle;

import com.alibaba.fastjson.JSON;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import xyz.vcluster.cassiopeia.authentication.handle.CassiopeiaAuthenticationEntryPoint;
import xyz.vcluster.cassiopeia.common.constant.HttpStatus;
import xyz.vcluster.cassiopeia.common.core.domain.AjaxResult;
import xyz.vcluster.cassiopeia.common.utils.MessageUtils;
import xyz.vcluster.cassiopeia.common.utils.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证失败处理类 返回未授权
 *
 * @author cassiopeia
 */
@Component
public class StandardAuthenticationEntryPoint extends CassiopeiaAuthenticationEntryPoint {
    private static final long serialVersionUID = -8970718410437077606L;

    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException {
        int code = HttpStatus.UNAUTHORIZED;
        String msg = MessageUtils.message("auth.request.error", request.getRequestURI());
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.error(code, msg)));
    }
}
