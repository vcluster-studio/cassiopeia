package xyz.vcluster.cassiopeia.authentication.service;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import xyz.vcluster.cassiopeia.authentication.model.CassiopeiaUserDetails;

/**
 * 用户服务
 *
 * @author cassiopeia
 */
public interface UserDetailsService {

    /**
     * 通过用户标识获取用户信息
     */
    CassiopeiaUserDetails loadUserByPrincipal(String principal) throws AuthenticationException;
}
