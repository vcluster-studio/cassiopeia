package xyz.vcluster.cassiopeia.web.controller.system;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.vcluster.cassiopeia.common.core.domain.AjaxResult;
import xyz.vcluster.cassiopeia.common.core.domain.entity.SysMenu;
import xyz.vcluster.cassiopeia.common.core.domain.entity.SysUser;
import xyz.vcluster.cassiopeia.common.utils.SecurityUtils;
import xyz.vcluster.cassiopeia.framework.web.service.SysPermissionService;
import xyz.vcluster.cassiopeia.system.service.ISysMenuService;

import java.util.List;
import java.util.Set;

/**
 * 登录验证.
 *
 * @author cassiopeia
 */
@Api(tags = "登录接口")
@RestController
public class SysLoginController {

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    /**
     * 获取用户信息.
     *
     * @return 用户信息
     */
    @ApiOperation("获取用户信息")
    @GetMapping("getInfo")
    public AjaxResult getInfo() {
        AjaxResult ajax = AjaxResult.success();
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if (user != null) {
            // 角色集合
            Set<String> roles = permissionService.getRolePermission(user);
            // 权限集合
            Set<String> permissions = permissionService.getMenuPermission(user);

            ajax.put("user", user);
            ajax.put("roles", roles);
            ajax.put("permissions", permissions);
        }
        return ajax;
    }

    /**
     * 获取路由信息.
     *
     * @return 路由信息
     */
    @ApiOperation("获取路由信息")
    @GetMapping("getRouters")
    public AjaxResult getRouters() {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}
