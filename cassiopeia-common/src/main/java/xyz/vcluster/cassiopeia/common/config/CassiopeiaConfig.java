package xyz.vcluster.cassiopeia.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 读取项目相关配置
 *
 * @author cassiopeia
 */
@Component
@ConfigurationProperties(prefix = "cassiopeia")
public class CassiopeiaConfig {
    /**
     * 项目名称
     */
    private String name;

    /**
     * 版本
     */
    private String version;

    /**
     * 版权年份
     */
    private String copyrightYear;

    /**
     * 实例演示开关
     */
    private boolean demoEnabled;

    /**
     * 存储类型
     */
    private static String storageType;

    /**
     * 存储路径
     */
    private static String profile;

    /**
     * 获取地址开关
     */
    private static boolean addressEnabled;

    /**
     * 匿名访问地址
     */
    private List<String> anonymousUrls;

    /**
     * 网络图片本地化忽略域名
     */
    private List<String> ignoreNetImageCatchDomains;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCopyrightYear() {
        return copyrightYear;
    }

    public void setCopyrightYear(String copyrightYear) {
        this.copyrightYear = copyrightYear;
    }

    public boolean isDemoEnabled() {
        return demoEnabled;
    }

    public void setDemoEnabled(boolean demoEnabled) {
        this.demoEnabled = demoEnabled;
    }

    public static String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        CassiopeiaConfig.storageType = storageType;
    }

    public static String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        CassiopeiaConfig.profile = profile;
    }

    public static boolean isAddressEnabled() {
        return addressEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled) {
        CassiopeiaConfig.addressEnabled = addressEnabled;
    }

    public List<String> getAnonymousUrls() {
        return anonymousUrls;
    }

    public void setAnonymousUrls(List<String> anonymousUrls) {
        this.anonymousUrls = anonymousUrls;
    }

    public List<String> getIgnoreNetImageCatchDomains() {
        return ignoreNetImageCatchDomains;
    }

    public void setIgnoreNetImageCatchDomains(List<String> ignoreNetImageCatchDomains) {
        this.ignoreNetImageCatchDomains = ignoreNetImageCatchDomains;
    }

    /**
     * 获取导入上传路径
     */
    public static String getImportPath() {
        return getProfile() + "/import";
    }

    /**
     * 获取头像上传路径
     */
    public static String getAvatarPath() {
        return getProfile() + "/avatar";
    }

    /**
     * 获取下载路径
     */
    public static String getDownloadPath() {
        return getProfile() + "/download";
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath() {
        return getProfile() + "/upload";
    }

    /**
     * 获取临时路径
     */
    public static String getTemporaryPath() {
        return getProfile() + "/temporary";
    }
}
