package xyz.vcluster.cassiopeia.common.utils.file;

import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.vcluster.cassiopeia.common.config.CassiopeiaConfig;
import xyz.vcluster.cassiopeia.common.constant.Constants;
import xyz.vcluster.cassiopeia.common.utils.StringUtils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * 图片处理工具类
 *
 * @author cassiopeia
 */
public class ImageUtils {
    private static final Logger log = LoggerFactory.getLogger(ImageUtils.class);

    /**
     * 读取文件为字节数据
     *
     * @param url 地址
     * @return 字节数据
     */
    public static byte[] read(String url) {
        byte[] data = null;
        InputStream in = null;
        try {
            if (url.startsWith("http")) {
                // 网络地址
                URL urlObj = new URL(url);
                URLConnection urlConnection = urlObj.openConnection();
                urlConnection.setConnectTimeout(30 * 1000);
                urlConnection.setReadTimeout(60 * 1000);
                urlConnection.setDoInput(true);
                in = urlConnection.getInputStream();
                data = IOUtils.toByteArray(in);
            } else {
                // 本机地址
                String localPath = CassiopeiaConfig.getProfile();
                String downloadPath = localPath + StringUtils.substringAfter(url, Constants.RESOURCE_PREFIX);
                data = FileUtils.read(downloadPath);
            }
            return data;
        } catch (Exception e) {
            log.error("获取文件路径异常 {}", e);
            return null;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
