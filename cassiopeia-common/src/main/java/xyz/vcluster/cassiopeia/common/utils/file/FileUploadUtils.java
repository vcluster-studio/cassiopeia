package xyz.vcluster.cassiopeia.common.utils.file;

import org.springframework.web.multipart.MultipartFile;
import xyz.vcluster.cassiopeia.common.config.CassiopeiaConfig;
import xyz.vcluster.cassiopeia.common.exception.file.FileNameLengthLimitExceededException;
import xyz.vcluster.cassiopeia.common.exception.file.FileSizeLimitExceededException;
import xyz.vcluster.cassiopeia.common.exception.file.InvalidExtensionException;
import xyz.vcluster.cassiopeia.common.utils.DateUtils;
import xyz.vcluster.cassiopeia.common.utils.StringUtils;
import xyz.vcluster.cassiopeia.common.utils.uuid.IdUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * 文件上传工具类
 *
 * @author cassiopeia
 */
public class FileUploadUtils {
    /**
     * 默认大小 5000M
     */
    public static final long DEFAULT_MAX_SIZE = 10737418240L;

    /**
     * 默认的文件名最大长度 100
     */
    public static final int DEFAULT_FILE_NAME_LENGTH = 100;

    /**
     * 默认上传的地址
     */
    private static String defaultBaseDir = CassiopeiaConfig.getProfile();

    public static void setDefaultBaseDir(String defaultBaseDir) {
        FileUploadUtils.defaultBaseDir = defaultBaseDir;
    }

    public static String getDefaultBaseDir() {
        return defaultBaseDir;
    }

    /**
     * 以默认配置进行文件上传
     *
     * @param file 上传的文件
     * @return 文件名称
     * @throws Exception
     */
    public static final String upload(MultipartFile file) throws IOException {
        return upload(getDefaultBaseDir(), file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    }

    /**
     * 以默认配置进行文件上传
     *
     * @param file             上传的文件
     * @param originalFilename 文件名称
     * @param contentType      文件类型
     * @return 文件名称
     * @throws Exception
     */
    public static final String upload(byte[] file,
                                      String originalFilename,
                                      String contentType) throws IOException {
        return upload(getDefaultBaseDir(), file, originalFilename, contentType, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    }

    /**
     * 根据文件路径上传
     *
     * @param baseDir 相对应用的基目录
     * @param file    上传的文件
     * @return 文件名称
     * @throws IOException
     */
    public static final String upload(String baseDir, MultipartFile file) throws IOException {
        return upload(baseDir, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    }

    /**
     * 根据文件路径上传
     *
     * @param baseDir          相对应用的基目录
     * @param file             上传的文件
     * @param originalFilename 文件名称
     * @param contentType      文件类型
     * @return 文件名称
     * @throws IOException
     */
    public static final String upload(String baseDir,
                                      byte[] file,
                                      String originalFilename,
                                      String contentType) throws IOException {
        return upload(baseDir, file, originalFilename, contentType, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    }

    /**
     * 文件上传
     *
     * @param baseDir           相对应用的基目录
     * @param file              上传的文件
     * @param isExtractFilename 是否扩展文件名称
     * @return 返回上传成功的文件名
     * @throws IOException
     */
    public static final String upload(String baseDir, MultipartFile file, boolean isExtractFilename)
            throws IOException {
        try {
            return upload(baseDir, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION, isExtractFilename);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 文件上传
     *
     * @param baseDir           相对应用的基目录
     * @param file              上传的文件
     * @param originalFilename  文件名称
     * @param contentType       文件类型
     * @param isExtractFilename 是否扩展文件名称
     * @return 返回上传成功的文件名
     * @throws IOException
     */
    public static final String upload(String baseDir,
                                      byte[] file,
                                      String originalFilename,
                                      String contentType,
                                      boolean isExtractFilename)
            throws IOException {
        try {
            return upload(baseDir, file, originalFilename, contentType,
                    MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION, isExtractFilename);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 文件上传
     *
     * @param baseDir          相对应用的基目录
     * @param file             上传的文件
     * @param allowedExtension 上传文件类型
     * @return 返回上传成功的文件名
     * @throws IOException
     */
    public static final String upload(String baseDir, MultipartFile file, String[] allowedExtension)
            throws IOException {
        try {
            return upload(baseDir, file, allowedExtension, true);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 文件上传
     *
     * @param baseDir          相对应用的基目录
     * @param file             上传的文件
     * @param originalFilename 文件名称
     * @param contentType      文件类型
     * @param allowedExtension 上传文件类型
     * @return 返回上传成功的文件名
     * @throws IOException
     */
    public static final String upload(String baseDir,
                                      byte[] file,
                                      String originalFilename,
                                      String contentType,
                                      String[] allowedExtension)
            throws IOException {
        try {
            return upload(baseDir, file, originalFilename, contentType, allowedExtension, true);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * 文件上传
     *
     * @param baseDir           相对应用的基目录
     * @param file              上传的文件
     * @param allowedExtension  上传文件类型
     * @param isExtractFilename 是否扩展文件名称
     * @return 返回上传成功的文件名
     * @throws FileSizeLimitExceededException       如果超出最大大小
     * @throws FileNameLengthLimitExceededException 文件名太长
     * @throws IOException                          比如读写文件出错时
     * @throws InvalidExtensionException            文件校验异常
     */
    public static final String upload(String baseDir, MultipartFile file, String[] allowedExtension, boolean isExtractFilename)
            throws FileSizeLimitExceededException, IOException, FileNameLengthLimitExceededException,
            InvalidExtensionException {
        int fileNameLength = Objects.requireNonNull(file.getOriginalFilename()).length();
        if (fileNameLength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH) {
            throw new FileNameLengthLimitExceededException(FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
        }

        assertAllowed(file, allowedExtension);

        String fileName;
        if (isExtractFilename) {
            fileName = extractFilename(file);
        } else {
            fileName = file.getOriginalFilename();
        }

        return upload(baseDir, fileName, file.getBytes());
    }

    /**
     * 文件上传
     *
     * @param baseDir           相对应用的基目录
     * @param file              上传的文件
     * @param originalFilename  文件名称
     * @param contentType       文件类型
     * @param allowedExtension  上传文件类型
     * @param isExtractFilename 是否扩展文件名称
     * @return 返回上传成功的文件名
     * @throws FileSizeLimitExceededException       如果超出最大大小
     * @throws FileNameLengthLimitExceededException 文件名太长
     * @throws IOException                          比如读写文件出错时
     * @throws InvalidExtensionException            文件校验异常
     */
    public static final String upload(String baseDir,
                                      byte[] file,
                                      String originalFilename,
                                      String contentType,
                                      String[] allowedExtension,
                                      boolean isExtractFilename)
            throws FileSizeLimitExceededException, IOException, FileNameLengthLimitExceededException,
            InvalidExtensionException {
        int fileNameLength = Objects.requireNonNull(originalFilename).length();
        if (fileNameLength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH) {
            throw new FileNameLengthLimitExceededException(FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
        }

        assertAllowed(file, originalFilename, contentType, allowedExtension);

        String fileName;
        if (isExtractFilename) {
            fileName = extractFilename(null, originalFilename, contentType);
        } else {
            fileName = originalFilename;
        }

        return upload(baseDir, fileName, file);
    }

    /**
     * 文件上传
     *
     * @param baseDir  相对应用的基目录
     * @param fileName 文件名称
     * @param file     上传的文件
     * @return 返回上传成功的文件名
     * @throws IOException 比如读写文件出错时
     */
    public static final String upload(String baseDir, String fileName, byte[] file)
            throws IOException {
        FileUtils.write(file, baseDir + "/" + fileName);

        return FileUtils.getPathFileName(baseDir, fileName);
    }

    /**
     * 编码文件名
     */
    public static final String extractFilename(MultipartFile file) {
        return extractFilename(IdUtils.simpleUUID(), file);
    }

    /**
     * 编码文件名
     */
    public static final String extractFilename(String identifier, MultipartFile file) {
        String extension = getExtension(file);
        return extract(identifier, extension);
    }

    /**
     * 编码文件名
     */
    public static final String extractFilename(String originalFileName) {
        return extractFilename(IdUtils.simpleUUID(), originalFileName);
    }

    /**
     * 编码文件名
     */
    public static final String extractFilename(String identifier, String originalFileName) {
        String extension = FileUtils.getExtension(originalFileName);
        return extract(identifier, extension);
    }

    /**
     * 编码文件名
     */
    public static final String extractFilename(String identifier, String originalFileName, String contentType) {
        if (StringUtils.isEmpty(identifier)) {
            identifier = IdUtils.simpleUUID();
        }
        String extension = getExtension(originalFileName, contentType);
        return extract(identifier, extension);
    }

    /**
     * 编码文件名
     */
    public static final String extract(String extension) {
        return extract(IdUtils.simpleUUID(), extension);
    }

    /**
     * 编码文件名
     */
    public static final String extract(String identifier, String extension) {
        return DateUtils.datePath() + "/" + identifier + "." + extension;
    }

    /**
     * 文件大小校验
     *
     * @param file 上传的文件
     * @return
     * @throws FileSizeLimitExceededException 如果超出最大大小
     * @throws InvalidExtensionException
     */
    public static final void assertAllowed(MultipartFile file, String[] allowedExtension)
            throws FileSizeLimitExceededException, InvalidExtensionException {
        long size = file.getSize();
        if (size > DEFAULT_MAX_SIZE) {
            throw new FileSizeLimitExceededException(DEFAULT_MAX_SIZE / 1024 / 1024);
        }

        String fileName = file.getOriginalFilename();
        String extension = getExtension(file);
        if (allowedExtension != null && !isAllowedExtension(extension, allowedExtension)) {
            if (allowedExtension == MimeTypeUtils.IMAGE_EXTENSION) {
                throw new InvalidExtensionException.InvalidImageExtensionException(allowedExtension, extension,
                        fileName);
            } else if (allowedExtension == MimeTypeUtils.DOCUMENT_EXTENSION) {
                throw new InvalidExtensionException.InvalidDocumentExtensionException(allowedExtension, extension,
                        fileName);
            } else if (allowedExtension == MimeTypeUtils.COMPRESS_EXTENSION) {
                throw new InvalidExtensionException.InvalidCompressExtensionException(allowedExtension, extension,
                        fileName);
            } else if (allowedExtension == MimeTypeUtils.FLASH_EXTENSION) {
                throw new InvalidExtensionException.InvalidFlashExtensionException(allowedExtension, extension,
                        fileName);
            } else if (allowedExtension == MimeTypeUtils.MEDIA_EXTENSION) {
                throw new InvalidExtensionException.InvalidMediaExtensionException(allowedExtension, extension,
                        fileName);
            } else if (allowedExtension == MimeTypeUtils.VIDEO_EXTENSION) {
                throw new InvalidExtensionException.InvalidVideoExtensionException(allowedExtension, extension,
                        fileName);
            } else {
                throw new InvalidExtensionException(allowedExtension, extension, fileName);
            }
        }
    }

    /**
     * 文件大小校验
     *
     * @param file             上传的文件
     * @param originalFilename 文件名称
     * @param contentType      文件类型
     * @param allowedExtension 允许上传的文件类型集合
     * @return
     * @throws FileSizeLimitExceededException 如果超出最大大小
     * @throws InvalidExtensionException
     */
    public static final void assertAllowed(byte[] file,
                                           String originalFilename,
                                           String contentType,
                                           String[] allowedExtension)
            throws FileSizeLimitExceededException, InvalidExtensionException {
        long size = file.length;
        if (size > DEFAULT_MAX_SIZE) {
            throw new FileSizeLimitExceededException(DEFAULT_MAX_SIZE / 1024 / 1024);
        }

        String extension = getExtension(originalFilename, contentType);
        if (allowedExtension != null && !isAllowedExtension(extension, allowedExtension)) {
            if (allowedExtension == MimeTypeUtils.IMAGE_EXTENSION) {
                throw new InvalidExtensionException.InvalidImageExtensionException(allowedExtension, extension,
                        originalFilename);
            } else if (allowedExtension == MimeTypeUtils.DOCUMENT_EXTENSION) {
                throw new InvalidExtensionException.InvalidDocumentExtensionException(allowedExtension, extension,
                        originalFilename);
            } else if (allowedExtension == MimeTypeUtils.COMPRESS_EXTENSION) {
                throw new InvalidExtensionException.InvalidCompressExtensionException(allowedExtension, extension,
                        originalFilename);
            } else if (allowedExtension == MimeTypeUtils.FLASH_EXTENSION) {
                throw new InvalidExtensionException.InvalidFlashExtensionException(allowedExtension, extension,
                        originalFilename);
            } else if (allowedExtension == MimeTypeUtils.MEDIA_EXTENSION) {
                throw new InvalidExtensionException.InvalidMediaExtensionException(allowedExtension, extension,
                        originalFilename);
            } else if (allowedExtension == MimeTypeUtils.VIDEO_EXTENSION) {
                throw new InvalidExtensionException.InvalidVideoExtensionException(allowedExtension, extension,
                        originalFilename);
            } else {
                throw new InvalidExtensionException(allowedExtension, extension, originalFilename);
            }
        }
    }

    /**
     * 判断MIME类型是否是允许的MIME类型
     *
     * @param extension
     * @param allowedExtension
     * @return
     */
    public static final boolean isAllowedExtension(String extension, String[] allowedExtension) {
        for (String str : allowedExtension) {
            if (str.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件名的后缀
     *
     * @param file 文件
     * @return 后缀名
     */
    public static final String getExtension(MultipartFile file) {
        return getExtension(file.getOriginalFilename(), file.getContentType());
    }

    /**
     * 获取文件名的后缀
     *
     * @param originalFilename 文件名称
     * @param contentType      文件类型
     * @return 后缀名
     */
    public static final String getExtension(String originalFilename, String contentType) {
        String extension = FileUtils.getExtension(originalFilename);
        if (StringUtils.isEmpty(extension)) {
            extension = MimeTypeUtils.getExtension(contentType);
        }
        return extension;
    }
}
