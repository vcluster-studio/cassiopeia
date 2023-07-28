package xyz.vcluster.cassiopeia.authentication.handle;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;

/**
 * 认证失败处理抽象类
 *
 * @author cassiopeia
 */
public abstract class CassiopeiaAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    private static final long serialVersionUID = -8970718410437077606L;

    public abstract void commence(HttpServletRequest request,
                                  HttpServletResponse response,
                                  AuthenticationException e)
            throws IOException;
}
