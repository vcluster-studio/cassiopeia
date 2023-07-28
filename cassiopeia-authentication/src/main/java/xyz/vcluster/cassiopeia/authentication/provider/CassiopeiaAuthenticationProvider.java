package xyz.vcluster.cassiopeia.authentication.provider;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import xyz.vcluster.cassiopeia.authentication.manager.CassiopeiaExternalAuthenticatorManager;
import xyz.vcluster.cassiopeia.authentication.model.CassiopeiaUserDetails;
import xyz.vcluster.cassiopeia.authentication.service.UserDetailsService;

import java.util.*;

/**
 * 认证处理核心实现类.
 * 通过具体的认证模式匹配对应的UserDetailsService、PasswordEncoder、UserDetailsPasswordService，
 * 并基于上述服务识别用户身份。
 * <p>
 * 本认证仅处理CassiopeiaAuthenticationToken类型的认证令牌，所有的登录认证方式均需要抽象成此种认证令牌
 */
public class CassiopeiaAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private static final String USER_NOT_FOUND_PASSWORD = "userNotFoundPassword";

    private final List<String> authenticationModes = new LinkedList<>();

    private final Map<String, PasswordEncoder> passwordEncoderMap = new LinkedHashMap<>();

    private final Map<String, UserDetailsService> userDetailsServiceMap = new LinkedHashMap<>();

    private final Map<String, UserDetailsPasswordService> userDetailsPasswordServiceMap = new LinkedHashMap<>();

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private CassiopeiaExternalAuthenticatorManager externalAuthenticatorManager = new CassiopeiaExternalAuthenticatorManager();

    private volatile String userNotFoundEncodedPassword;

    public CassiopeiaAuthenticationProvider() {
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CassiopeiaAuthenticationToken authenticationToken = (CassiopeiaAuthenticationToken) authentication;
        String authenticationMode = authenticationToken.getAuthenticationMode();
        if (!this.authenticationModes.contains(authenticationMode)) {
            return null;
        }

        Authentication externalAuthenticate = externalAuthenticatorManager.doAuthenticate(authenticationToken,
                authenticationMode);

        return super.authenticate(externalAuthenticate);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        CassiopeiaAuthenticationToken authenticationToken = (CassiopeiaAuthenticationToken) authentication;

        if (authenticationToken.getCredentials() == null) {
            this.logger.debug("Authentication failed: no credentials provided");
            throw new BadCredentialsException(this.messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        } else {
            String authenticationMode = authenticationToken.getAuthenticationMode();
            String presentedPassword = authenticationToken.getCredentials().toString();
            if (!this.getPasswordEncoder(authenticationMode).matches(presentedPassword, userDetails.getPassword())) {
                this.logger.debug("Authentication failed: password does not match stored value");
                throw new BadCredentialsException(this.messages.getMessage(
                        "AbstractUserDetailsAuthenticationProvider.badCredentials",
                        "Bad credentials"));
            }
        }
    }

    @Override
    protected void doAfterPropertiesSet() {
        Assert.notNull(this.userDetailsServiceMap, "UserDetailsService Map must be set");
        Assert.notNull(this.passwordEncoderMap, "PasswordEncoder Map must be set");
    }

    @Override
    protected final UserDetails retrieveUser(String principal,
                                             UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        CassiopeiaAuthenticationToken authenticationToken = (CassiopeiaAuthenticationToken) authentication;

        this.prepareTimingAttackProtection();

        try {
            String authenticationMode = authenticationToken.getAuthenticationMode();
            CassiopeiaUserDetails loadedUser = this.getUserDetailsService(authenticationMode)
                    .loadUserByPrincipal(principal);
            if (loadedUser == null) {
                throw new InternalAuthenticationServiceException(
                        "UserDetailsService returned null, which is an interface contract violation");
            } else {
                loadedUser.setAuthenticationMode(authenticationMode);
                loadedUser.setAuthenticationSource(authenticationToken.getAuthenticationSource());
                loadedUser.setPrincipal((String) authenticationToken.getPrincipal());
                loadedUser.setCredentials((String) authenticationToken.getCredentials());

                loadedUser = externalAuthenticatorManager.decorate(loadedUser, authenticationMode);
                return loadedUser;
            }
        } catch (UsernameNotFoundException ex) {
            this.mitigateAgainstTimingAttack(authenticationToken);
            throw ex;
        } catch (InternalAuthenticationServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    protected Authentication createSuccessAuthentication(Object principal,
                                                         Authentication authentication,
                                                         UserDetails user) {
        CassiopeiaAuthenticationToken authenticationToken = (CassiopeiaAuthenticationToken) authentication;

        String authenticationMode = authenticationToken.getAuthenticationMode();
        boolean upgradeEncoding = this.getUserDetailsPasswordService(authenticationMode) != null
                && this.getPasswordEncoder(authenticationMode).upgradeEncoding(user.getPassword());
        if (upgradeEncoding) {
            String presentedPassword = authenticationToken.getCredentials().toString();
            String newPassword = this.getPasswordEncoder(authenticationMode).encode(presentedPassword);
            user = this.getUserDetailsPasswordService(authenticationMode).updatePassword(user, newPassword);
        }

        UsernamePasswordAuthenticationToken result = new CassiopeiaAuthenticationToken(
                principal, authenticationToken.getCredentials(),
                authoritiesMapper.mapAuthorities(user.getAuthorities()));
        result.setDetails(authenticationToken.getDetails());
        return result;
    }

    private void prepareTimingAttackProtection() {
        if (this.userNotFoundEncodedPassword == null) {
            this.userNotFoundEncodedPassword = this.getDefaultPasswordEncoder().encode(USER_NOT_FOUND_PASSWORD);
        }
    }

    private void mitigateAgainstTimingAttack(CassiopeiaAuthenticationToken authentication) {
        String authenticationMode = authentication.getAuthenticationMode();
        if (authentication.getCredentials() != null) {
            String presentedPassword = authentication.getCredentials().toString();
            this.getPasswordEncoder(authenticationMode).matches(presentedPassword, this.userNotFoundEncodedPassword);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (CassiopeiaAuthenticationToken.class
                .isAssignableFrom(authentication));
    }

    protected PasswordEncoder getDefaultPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    public void registryAuthenticationMode(String authenticationMode) {
        this.authenticationModes.add(authenticationMode);
    }

    public void registryAuthenticationModes(String... authenticationModes) {
        this.authenticationModes.addAll(Arrays.asList(authenticationModes));
    }

    public void registryPasswordEncoder(String authenticationMode, PasswordEncoder passwordEncoder) {
        Assert.notNull(authenticationMode, "authenticationMode cannot be null");
        Assert.notNull(passwordEncoder, "passwordEncoder cannot be null");
        this.passwordEncoderMap.put(authenticationMode, passwordEncoder);
        this.userNotFoundEncodedPassword = null;
    }

    protected PasswordEncoder getPasswordEncoder(String authenticationMode) {
        Assert.notNull(authenticationMode, "authenticationMode cannot be null");
        return passwordEncoderMap.get(authenticationMode);
    }

    public void registryUserDetailsService(String authenticationMode, UserDetailsService userDetailsService) {
        Assert.notNull(authenticationMode, "authenticationMode cannot be null");
        Assert.notNull(userDetailsService, "userDetailsService cannot be null");
        this.userDetailsServiceMap.put(authenticationMode, userDetailsService);
    }

    protected UserDetailsService getUserDetailsService(String authenticationMode) {
        Assert.notNull(authenticationMode, "authenticationMode cannot be null");
        return userDetailsServiceMap.get(authenticationMode);
    }

    public void registryUserDetailsPasswordService(String authenticationMode,
                                                   UserDetailsPasswordService userDetailsPasswordService) {
        Assert.notNull(authenticationMode, "authenticationMode cannot be null");
        Assert.notNull(userDetailsPasswordService, "userDetailsPasswordService cannot be null");
        this.userDetailsPasswordServiceMap.put(authenticationMode, userDetailsPasswordService);
    }

    protected UserDetailsPasswordService getUserDetailsPasswordService(String authenticationMode) {
        return userDetailsPasswordServiceMap.get(authenticationMode);
    }

    public void registryExternalAuthenticatorManager(CassiopeiaExternalAuthenticatorManager externalAuthenticatorManager) {
        this.externalAuthenticatorManager = externalAuthenticatorManager;
    }

    protected CassiopeiaExternalAuthenticatorManager getExternalAuthenticatorManager() {
        return externalAuthenticatorManager;
    }

    public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
        this.authoritiesMapper = authoritiesMapper;
    }
}
