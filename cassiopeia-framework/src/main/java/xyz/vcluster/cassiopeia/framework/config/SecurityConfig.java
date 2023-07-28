package xyz.vcluster.cassiopeia.framework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.filter.CorsFilter;
import xyz.vcluster.cassiopeia.authentication.constant.AuthenticationConstant;
import xyz.vcluster.cassiopeia.authentication.filter.CassiopeiaAuthenticationTokenFilter;
import xyz.vcluster.cassiopeia.authentication.manager.CassiopeiaExternalAuthenticatorManager;
import xyz.vcluster.cassiopeia.authentication.provider.CassiopeiaAuthenticationProvider;
import xyz.vcluster.cassiopeia.authentication.service.TokenService;
import xyz.vcluster.cassiopeia.authentication.service.UserDetailsService;
import xyz.vcluster.cassiopeia.common.config.CassiopeiaConfig;
import xyz.vcluster.cassiopeia.framework.security.handle.StandardAuthenticationEntryPoint;
import xyz.vcluster.cassiopeia.framework.security.handle.StandardLogoutSuccessHandler;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * spring security配置
 *
 * @author cassiopeia
 */
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 系统基础配置
     */
    @Autowired
    private CassiopeiaConfig cassiopeiaConfig;

    /**
     * 令牌服务
     */
    @Autowired
    private TokenService tokenService;

    /**
     * 标准用户服务
     */
    @Resource(name = "standardUserDetailsService")
    private UserDetailsService userDetailsService;

    /**
     * 认证失败处理类
     */
    @Autowired
    private StandardAuthenticationEntryPoint unauthorizedHandler;

    /**
     * 退出处理类
     */
    @Autowired
    private StandardLogoutSuccessHandler logoutSuccessHandler;

    /**
     * 跨域过滤器
     */
    @Autowired
    private CorsFilter corsFilter;

    @Autowired
    private CassiopeiaExternalAuthenticatorManager externalAuthenticatorManager;

    /**
     * 认证管理类
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * anyRequest          |   匹配所有请求路径
     * access              |   SpringEl表达式结果为true时可以访问
     * anonymous           |   匿名可以访问
     * denyAll             |   用户不能访问
     * fullyAuthenticated  |   用户完全认证可以访问（非remember-me下自动登录）
     * hasAnyAuthority     |   如果有参数，参数表示权限，则其中任何一个权限可以访问
     * hasAnyRole          |   如果有参数，参数表示角色，则其中任何一个角色可以访问
     * hasAuthority        |   如果有参数，参数表示权限，则其权限可以访问
     * hasIpAddress        |   如果有参数，参数表示IP地址，如果用户IP和参数匹配，则可以访问
     * hasRole             |   如果有参数，参数表示角色，则其角色可以访问
     * permitAll           |   用户可以任意访问
     * rememberMe          |   允许通过remember-me登录的用户访问
     * authenticated       |   用户登录后可访问
     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        List<String> anonymousUrls = new LinkedList<>();;
        anonymousUrls.add(AuthenticationConstant.SECURITY_ENDPOINT_DEFINITION);
        anonymousUrls.add(AuthenticationConstant.SECURITY_ENDPOINT_AUTHORIZE);
        anonymousUrls.add(AuthenticationConstant.SECURITY_ENDPOINT_AUTHENTICATE);
        anonymousUrls.add(AuthenticationConstant.SECURITY_ENDPOINT_TOKEN);
        anonymousUrls.add("/captchaImage");
        anonymousUrls.add("/register");
        anonymousUrls.add("/common/download**");
        anonymousUrls.add("/common/resource**");
        anonymousUrls.add("/common/resource/**");
        anonymousUrls.add("/swagger-ui.html");
        anonymousUrls.add("/swagger-resources/**");
        anonymousUrls.add("/webjars/**");
        anonymousUrls.add("/*/api-docs");
        anonymousUrls.addAll(cassiopeiaConfig.getAnonymousUrls());

        httpSecurity
                // CSRF禁用，因为不使用session
                .csrf().disable()
                // 认证失败处理类
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                // 基于token，所以不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // 过滤请求
                .authorizeRequests()
                .antMatchers(anonymousUrls.toArray(new String[]{})).anonymous()
                .antMatchers(HttpMethod.GET,
                        "/",
                        "/*.html",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/druid/**",
                        "/profile/**").permitAll()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated()
                .and()
                .headers().frameOptions().disable();
        logoutSuccessHandler.setTokenService(tokenService);
        logoutSuccessHandler.setExternalAuthenticatorManager(externalAuthenticatorManager);
        httpSecurity
                .logout()
                .logoutUrl(AuthenticationConstant.SECURITY_ENDPOINT_LOGOUT)
                .logoutSuccessHandler(logoutSuccessHandler);

        // 添加JWT filter
        httpSecurity.addFilterBefore(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        // 添加CORS filter
        httpSecurity.addFilterBefore(corsFilter, CassiopeiaAuthenticationTokenFilter.class);
        httpSecurity.addFilterBefore(corsFilter, LogoutFilter.class);
    }

    /**
     * 身份认证提供商
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProvider());
    }

    /**
     * 令牌过滤器
     */
    @Bean
    public CassiopeiaAuthenticationTokenFilter authenticationTokenFilter() {
        CassiopeiaAuthenticationTokenFilter authenticationTokenFilter = new CassiopeiaAuthenticationTokenFilter();
        authenticationTokenFilter.setTokenService(tokenService);

        return authenticationTokenFilter;
    }

    /**
     * 认证处理提供类
     */
    @Bean
    public CassiopeiaAuthenticationProvider authenticationProvider() {
        CassiopeiaAuthenticationProvider authenticationProvider = new CassiopeiaAuthenticationProvider();
        authenticationProvider.registryExternalAuthenticatorManager(externalAuthenticatorManager);

        // for default authenticate mode: password
        authenticationProvider.registryAuthenticationMode(AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_PASSWORD);
        authenticationProvider.registryPasswordEncoder(AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_PASSWORD,
                passwordEncoder());
        authenticationProvider.registryUserDetailsService(AuthenticationConstant.SECURITY_AUTHENTICATION_MODE_PASSWORD,
                userDetailsService);

        return authenticationProvider;
    }

    /**
     * 强散列哈希加密实现
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
