package xyz.vcluster.cassiopeia.authentication.provider;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import xyz.vcluster.cassiopeia.authentication.model.CassiopeiaUserDetails;

import java.util.Collection;

public class CassiopeiaAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private String authenticationMode;

    private String authenticationSource;

    public CassiopeiaAuthenticationToken(Object principal, Object credentials) {
        this(principal, credentials, null);
    }

    public CassiopeiaAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
    }

    public String getAuthenticationMode() {
        return authenticationMode;
    }

    public void setAuthenticationMode(String authenticationMode) {
        this.authenticationMode = authenticationMode;
    }

    public String getAuthenticationSource() {
        return authenticationSource;
    }

    public void setAuthenticationSource(String authenticationSource) {
        this.authenticationSource = authenticationSource;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();

        Object principal = super.getPrincipal();
        if (principal instanceof CassiopeiaUserDetails) {
            ((CassiopeiaUserDetails) principal).eraseCredentials();
        }
    }
}
