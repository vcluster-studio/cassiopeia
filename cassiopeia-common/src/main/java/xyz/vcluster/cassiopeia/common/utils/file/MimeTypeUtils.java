package xyz.vcluster.cassiopeia.common.utils.file;

import org.springframework.http.MediaType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 媒体类型工具类
 *
 * @author cassiopeia
 */
public class MimeTypeUtils {

    static Map<String, String> contentTypeMaps = new LinkedHashMap<>();
    static Map<String, String> extensionMaps = new LinkedHashMap<>();

    static {
        contentTypeMaps.put("bmp", "image/bmp");
        extensionMaps.put("image/bmp", "bmp");
        contentTypeMaps.put("gif", "image/gif");
        extensionMaps.put("image/gif", "gif");
        contentTypeMaps.put("jpg", "image/jpg");
        extensionMaps.put("image/jpg", "jpg");
        contentTypeMaps.put("jpeg", "image/jpeg");
        extensionMaps.put("image/jpeg", "jpeg");
        contentTypeMaps.put("png", "image/png");
        extensionMaps.put("image/png", "png");
        contentTypeMaps.put("webp", "image/webp");
        extensionMaps.put("image/webp", "webp");
        contentTypeMaps.put("svg", "image/svg+xml");
        extensionMaps.put("image/svg+xml", "svg");
        contentTypeMaps.put("pdf", "application/pdf");
        extensionMaps.put("application/pdf", "pdf");
        contentTypeMaps.put("doc", "application/msword");
        extensionMaps.put("application/msword", "doc");
        contentTypeMaps.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        extensionMaps.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
        contentTypeMaps.put("xls", "application/vnd.ms-excel");
        extensionMaps.put("application/vnd.ms-excel", "xls");
        contentTypeMaps.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        extensionMaps.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
        contentTypeMaps.put("ppt", "application/vnd.ms-powerpoint");
        extensionMaps.put("application/vnd.ms-powerpoint", "ppt");
        contentTypeMaps.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        extensionMaps.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx");
        contentTypeMaps.put("html", "text/html");
        extensionMaps.put("text/html", "html");
        contentTypeMaps.put("htm", "text/html");
        contentTypeMaps.put("txt", "text/plain");
        extensionMaps.put("text/plain", "txt");
        contentTypeMaps.put("json", "application/json");
        extensionMaps.put("application/json", "json");
        contentTypeMaps.put("rar", "application/x-rar");
        extensionMaps.put("application/x-rar", "rar");
        contentTypeMaps.put("zip", "application/zip");
        extensionMaps.put("application/zip", "zip");
        contentTypeMaps.put("gz", "application/x-gzip");
        extensionMaps.put("application/x-gzip", "gz");
        contentTypeMaps.put("bz2", "application/x-bzip");
        extensionMaps.put("application/x-bzip", "bz2");
        contentTypeMaps.put("mp4", "video/mpeg4");
        extensionMaps.put("video/mpeg4", "mp4");
        contentTypeMaps.put("avi", "video/avi");
        extensionMaps.put("video/avi", "avi");
        contentTypeMaps.put("rmvb", "application/vnd.rn-realmedia-vbr");
        extensionMaps.put("application/vnd.rn-realmedia-vbr", "rmvb");
        contentTypeMaps.put("swf", "application/x-shockwave-flash");
        extensionMaps.put("application/x-shockwave-flash", "swf");
        contentTypeMaps.put("flv", "video/x-flv");
        extensionMaps.put("video/x-flv", "flv");
        contentTypeMaps.put("mp3", "audio/mp3");
        extensionMaps.put("audio/mp3", "mp3");
        contentTypeMaps.put("wav", "audio/wav");
        extensionMaps.put("audio/wav", "wav");
        contentTypeMaps.put("wma", "audio/x-ms-wma");
        extensionMaps.put("audio/x-ms-wma", "wma");
        contentTypeMaps.put("wmv", "video/x-ms-wmv");
        extensionMaps.put("video/x-ms-wmv", "wmv");
        contentTypeMaps.put("mid", "audio/mid");
        extensionMaps.put("audio/mid", "mid");
        contentTypeMaps.put("mpg", "video/mpg");
        extensionMaps.put("video/mpg", "mpg");
        contentTypeMaps.put("asf", "video/x-ms-asf");
        extensionMaps.put("video/x-ms-asf", "asf");
        contentTypeMaps.put("rm", "application/vnd.rn-realmedia");
        extensionMaps.put("application/vnd.rn-realmedia", "rm");
    }

    public static final String[] IMAGE_EXTENSION = {"bmp", "gif", "jpg", "jpeg", "png", "webp", "svg"};

    public static final String[] DOCUMENT_EXTENSION = {"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt", "json"};

    public static final String[] COMPRESS_EXTENSION = {"rar", "zip", "gz", "bz2"};

    public static final String[] VIDEO_EXTENSION = {"mp4", "avi", "rmvb"};

    public static final String[] FLASH_EXTENSION = {"swf", "flv"};

    public static final String[] MEDIA_EXTENSION = {"swf", "flv", "mp3", "wav", "wma", "wmv", "mid", "avi", "mpg",
            "asf", "rm", "rmvb"};

    public static final String[] DEFAULT_ALLOWED_EXTENSION = {
            // 图片
            "bmp", "gif", "jpg", "jpeg", "png", "webp", "svg",
            // pdf word excel powerpoint
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "html", "htm", "txt", "json",
            // 压缩文件
            "rar", "zip", "gz", "bz2",
            // 视频格式
            "mp4", "avi", "rmvb",
            // FLASH
            "swf", "flv",
            // 媒体
            "mp3", "wav", "wma", "wmv", "mid", "mpg", "asf", "rm"
    };

    public static String getExtension(String contentType) {
        String extension = extensionMaps.get(contentType);
        return extension == null ? "" : extension;
    }

    public static String getContentType(String extension) {
        String contentType = contentTypeMaps.get(extension);
        return contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType;
    }
}
