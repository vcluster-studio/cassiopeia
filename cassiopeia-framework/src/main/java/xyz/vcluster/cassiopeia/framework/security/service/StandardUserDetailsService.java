package xyz.vcluster.cassiopeia.framework.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xyz.vcluster.cassiopeia.authentication.model.CassiopeiaUserDetails;
import xyz.vcluster.cassiopeia.authentication.service.UserDetailsService;
import xyz.vcluster.cassiopeia.common.core.domain.entity.SysUser;
import xyz.vcluster.cassiopeia.common.core.domain.model.LoginUser;
import xyz.vcluster.cassiopeia.common.enums.UserStatus;
import xyz.vcluster.cassiopeia.common.utils.MessageUtils;
import xyz.vcluster.cassiopeia.common.utils.StringUtils;
import xyz.vcluster.cassiopeia.framework.web.service.SysPermissionService;
import xyz.vcluster.cassiopeia.system.service.ISysUserService;

/**
 * 标准用户服务
 *
 * @author cassiopeia
 */
@Service("standardUserDetailsService")
public class StandardUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(StandardUserDetailsService.class);

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysPermissionService permissionService;

    @Override
    public CassiopeiaUserDetails loadUserByPrincipal(String username) throws AuthenticationException {
        SysUser user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user)) {
            log.info("登录用户：{} 不存在.", username);
            throw new UsernameNotFoundException(MessageUtils.message("user.not.exists"));
        } else if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            log.info("登录用户：{} 已被删除.", username);
            throw new DisabledException(MessageUtils.message("user.account.blocked"));
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new LockedException(MessageUtils.message("user.account.locked"));
        }

        return createLoginUser(user);
    }

    private CassiopeiaUserDetails createLoginUser(SysUser user) {
        return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
    }
}
