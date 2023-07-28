package xyz.vcluster.cassiopeia.common.utils.file;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import xyz.vcluster.cassiopeia.common.config.CassiopeiaConfig;
import xyz.vcluster.cassiopeia.common.constant.Constants;
import xyz.vcluster.cassiopeia.common.utils.DateUtils;
import xyz.vcluster.cassiopeia.common.utils.StringUtils;
import xyz.vcluster.cassiopeia.common.utils.uuid.IdUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件处理工具类
 *
 * @author cassiopeia
 */
public class FileUtils {
    public static String FILENAME_PATTERN = "[a-zA-Z0-9_\\-\\|\\.\\u4e00-\\u9fa5]+";

    /**
     * 读取指定文件
     *
     * @param fileNames 文件名称
     * @param fileNames 文件原始名称
     * @return 文件数据
     * @throws IOException IO异常
     */
    public static byte[] read(String[] fileNames, String[] realFileNames) throws IOException {
        byte[] data;

        if (fileNames == null || fileNames.length == 0) {
            throw new IOException("download.non.downloader.request");
        }

        if (realFileNames == null || (realFileNames.length > 0 && realFileNames.length != fileNames.length)) {
            throw new IOException("download.non.downloader.request1");
        }
        if (fileNames.length > 1) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(bos);
            zos.setMethod(ZipOutputStream.DEFLATED);
            zos.setLevel(7);

            Map<String, Integer> countMap = new HashMap<>();
            for (int i = 0; i < fileNames.length; i++) {
                String realFileName = realFileNames[i];

                Integer count = 0;
                if (countMap.containsKey(realFileName)) {
                    count = countMap.get(realFileName);
                }
                if (count > 0) {
                    String extension = getExtension(realFileName);
                    int index = realFileName.lastIndexOf(".");
                    String name;
                    if (index <= 0 || index > realFileName.length()) {
                        name = realFileName;
                    } else {
                        name = realFileName.substring(0, realFileName.lastIndexOf("."));
                    }
                    realFileName = String.format("%s(%d).%s", name, count, extension);
                }
                count++;
                countMap.put(realFileNames[i], count);

                ZipEntry ze = new ZipEntry(realFileName);
                zos.putNextEntry(ze);
                IOUtils.write(FileUtils.read(fileNames[i]), zos);
                zos.flush();
                zos.closeEntry();
            }
            IOUtils.closeQuietly(zos);
            data = bos.toByteArray();
            IOUtils.closeQuietly(bos);
        } else {
            data = FileUtils.read(fileNames[0]);
        }

        return data;
    }

    /**
     * 读取指定文件
     *
     * @param fileName 文件名称
     * @return 文件数据
     * @throws IOException IO异常
     */
    public static byte[] read(String fileName) throws IOException {
        byte[] data = null;

        switch (CassiopeiaConfig.getStorageType()) {
            case "local":
                data = LocalFileUtils.read(fileName);
                break;
            default:
                data = LocalFileUtils.read(fileName);
        }

        return data;
    }

    /**
     * 写数据到文件中
     *
     * @param data 数据
     * @param path 目标路径
     * @return 目标文件
     * @throws IOException IO异常
     */
    public static String writeAppointPath(byte[] data, String path) throws IOException {
        String extension = getFileExtendName(data);
        String fileName = DateUtils.datePath() + "/" + IdUtils.simpleUUID() + "." + extension;

        write(data, path + "/" + fileName);
        return getPathFileName(path, fileName);
    }

    /**
     * 写数据到文件中
     *
     * @param data     数据
     * @param fileName 目标文件
     * @return 目标文件
     * @throws IOException IO异常
     */
    public static String write(byte[] data, String fileName) throws IOException {

        switch (CassiopeiaConfig.getStorageType()) {
            case "local":
                fileName = LocalFileUtils.write(data, fileName);
                break;
            default:
                fileName = LocalFileUtils.write(data, fileName);
        }

        return fileName;
    }

    /**
     * 文件是否存在
     *
     * @param fileName 文件名称
     * @return 是否存在
     * @throws IOException IO异常
     */
    public static boolean exists(String fileName) {
        boolean exists;

        switch (CassiopeiaConfig.getStorageType()) {
            case "local":
                exists = LocalFileUtils.exists(fileName);
                break;
            default:
                exists = LocalFileUtils.exists(fileName);
        }

        return exists;
    }

    /**
     * 获取子文件列表
     *
     * @param path 文件路径
     * @return 子文件列表
     */
    public static String[] list(String path) {
        String[] subFileName;

        switch (CassiopeiaConfig.getStorageType()) {
            case "local":
                subFileName = LocalFileUtils.list(path);
                break;
            default:
                subFileName = LocalFileUtils.list(path);
        }

        return subFileName;
    }

    /**
     * 删除文件
     *
     * @param filePath 文件
     * @return
     */
    public static boolean delete(String filePath) {
        boolean flag = false;

        switch (CassiopeiaConfig.getStorageType()) {
            case "local":
                flag = LocalFileUtils.delete(filePath);
                break;
            default:
                flag = LocalFileUtils.delete(filePath);
        }

        return flag;
    }

    public static final String getPathFileName(String uploadDir, String fileName) {
        int dirLastIndex = CassiopeiaConfig.getProfile().length() + 1;
        String currentDir = StringUtils.substring(uploadDir, dirLastIndex);
        return Constants.RESOURCE_PREFIX + "/" + currentDir + "/" + fileName;
    }

    /**
     * 文件名称验证
     *
     * @param filename 文件名称
     * @return true 正常 false 非法
     */
    public static boolean isValidFilename(String filename) {
        return filename.matches(FILENAME_PATTERN);
    }

    /**
     * 检查文件是否可下载
     *
     * @param resource 需要下载的文件
     * @return true 正常 false 非法
     */
    public static boolean checkAllowDownload(String resource) {
        // 禁止目录上跳级别
        if (StringUtils.contains(resource, "..")) {
            return false;
        }

        // 检查允许下载的文件规则
        if (ArrayUtils.contains(MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION, FileTypeUtils.getFileType(resource))) {
            return true;
        }

        // 不在允许下载的文件规则
        return false;
    }

    /**
     * 下载文件名重新编码
     *
     * @param request  请求对象
     * @param fileName 文件名
     * @return 编码后的文件名
     */
    public static String setFileDownloadHeader(HttpServletRequest request, String fileName) throws UnsupportedEncodingException {
        final String agent = request.getHeader("USER-AGENT");
        String filename = fileName;
        if (agent.contains("MSIE")) {
            // IE浏览器
            filename = URLEncoder.encode(filename, "utf-8");
            filename = filename.replace("+", " ");
        } else if (agent.contains("Firefox")) {
            // 火狐浏览器
            filename = new String(fileName.getBytes(), "ISO8859-1");
        } else if (agent.contains("Chrome")) {
            // google浏览器
            filename = URLEncoder.encode(filename, "utf-8");
        } else {
            // 其它浏览器
            filename = URLEncoder.encode(filename, "utf-8");
        }
        return filename;
    }

    /**
     * 下载文件重新编码
     *
     * @param response     响应对象
     * @param realFileName 真实文件名
     * @return
     */
    public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName) throws UnsupportedEncodingException {
        String percentEncodedFileName = percentEncode(realFileName);

        String contentDispositionValue = "attachment; filename=" +
                percentEncodedFileName +
                ";" +
                "filename*=" +
                "utf-8''" +
                percentEncodedFileName;
        response.setHeader("Content-disposition", contentDispositionValue);
    }

    /**
     * 查看文件重新编码
     *
     * @param response     响应对象
     * @param realFileName 真实文件名
     * @return
     */
    public static void setInlineResponseHeader(HttpServletResponse response, String realFileName) throws UnsupportedEncodingException {
        String percentEncodedFileName = percentEncode(realFileName);

        String contentDispositionValue = "inline; filename=" +
                percentEncodedFileName +
                ";" +
                "filename*=" +
                "utf-8''" +
                percentEncodedFileName;
        response.setHeader("Content-disposition", contentDispositionValue);
    }

    /**
     * 百分号编码工具方法
     *
     * @param s 需要百分号编码的字符串
     * @return 百分号编码后的字符串
     */
    public static String percentEncode(String s) throws UnsupportedEncodingException {
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        return encode.replaceAll("\\+", "%20");
    }

    /**
     * 获取图像后缀
     *
     * @param photoByte 图像数据
     * @return 后缀名
     */
    public static String getFileExtendName(byte[] photoByte) {
        String strFileExtendName = "jpg";
        if ((photoByte[0] == 71) && (photoByte[1] == 73) && (photoByte[2] == 70) && (photoByte[3] == 56)
                && ((photoByte[4] == 55) || (photoByte[4] == 57)) && (photoByte[5] == 97)) {
            strFileExtendName = "gif";
        } else if ((photoByte[6] == 74) && (photoByte[7] == 70) && (photoByte[8] == 73) && (photoByte[9] == 70)) {
            strFileExtendName = "jpg";
        } else if ((photoByte[0] == 66) && (photoByte[1] == 77)) {
            strFileExtendName = "bmp";
        } else if ((photoByte[1] == 80) && (photoByte[2] == 78) && (photoByte[3] == 71)) {
            strFileExtendName = "png";
        }
        return strFileExtendName;
    }

    /**
     * 编码文件名
     */
    public static final String extractFilename(String fileName) {
        return DateUtils.datePath() + "/" + IdUtils.simpleUUID() + "_" + fileName;
    }

    /**
     * 获取文件名的后缀
     *
     * @param fileName 文件名称
     * @return 后缀名
     */
    public static final String getExtension(String fileName) {

        return FilenameUtils.getExtension(fileName).toLowerCase();
    }

    /**
     * 输入流转字节数组
     * @param input
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024*4];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }
}
