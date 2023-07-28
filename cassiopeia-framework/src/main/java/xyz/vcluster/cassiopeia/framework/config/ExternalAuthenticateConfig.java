package xyz.vcluster.cassiopeia.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.vcluster.cassiopeia.authentication.authenticator.CassiopeiaExternalAuthenticator;
import xyz.vcluster.cassiopeia.authentication.manager.CassiopeiaExternalAuthenticatorManager;

import javax.annotation.Resource;

/**
 * 标准外部认证配置
 *
 * @author cassiopeia
 */
@Configuration
public class ExternalAuthenticateConfig {

    /**
     * 标准外部认证器
     */
    @Resource(name = "standardExternalAuthenticator")
    private CassiopeiaExternalAuthenticator externalAuthenticator;

    /**
     * 外部认证管理器
     */
    @Bean
    public CassiopeiaExternalAuthenticatorManager externalAuthenticatorManager() {
        CassiopeiaExternalAuthenticatorManager externalAuthenticatorManager = new CassiopeiaExternalAuthenticatorManager();
        externalAuthenticatorManager.registryDefaultAuthenticator(externalAuthenticator);

        return externalAuthenticatorManager;
    }
}
