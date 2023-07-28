package xyz.vcluster.cassiopeia.web.controller.common;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.vcluster.cassiopeia.common.config.CassiopeiaConfig;
import xyz.vcluster.cassiopeia.common.constant.Constants;
import xyz.vcluster.cassiopeia.common.core.domain.AjaxResult;
import xyz.vcluster.cassiopeia.common.utils.MessageUtils;
import xyz.vcluster.cassiopeia.common.utils.StringUtils;
import xyz.vcluster.cassiopeia.common.utils.file.FileUploadUtils;
import xyz.vcluster.cassiopeia.common.utils.file.FileUtils;
import xyz.vcluster.cassiopeia.common.utils.file.MimeTypeUtils;
import xyz.vcluster.cassiopeia.common.utils.http.WebUtils;
import xyz.vcluster.cassiopeia.framework.config.ServerConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * 通用请求处理.
 *
 * @author cassiopeia
 */
@Api(tags = "通用接口")
@RestController
public class CommonController {

    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private CassiopeiaConfig cassiopeiaConfig;

    public enum ViewType {
        JSON,
        ATTACHMENT,
        INLINE,
        NONE,
    }

    /**
     * 通用下载请求.
     *
     * @param fileNames         文件名称
     * @param originalFileNames 文件原始名称
     * @param deletes           是否删除
     */
    @ApiOperation("通用下载")
    @GetMapping("common/download")
    public void downloadFile(@ApiParam("文件名") @RequestParam("fileName") String[] fileNames,
                             @ApiParam("文件原始名称") @RequestParam(value = "originalFileName", required = false) String[] originalFileNames,
                             @ApiParam("下载后删除文件") @RequestParam(value = "delete", required = false) Boolean[] deletes,
                             @ApiParam("下载后打开方式") @RequestParam(value = "viewType", required = false) ViewType viewType,
                             HttpServletResponse response,
                             HttpServletRequest request) {
        try {
            if (fileNames == null || fileNames.length == 0) {
                throw new Exception(MessageUtils.message("download.non.downloader.request"));
            }

            if (originalFileNames != null
                    && originalFileNames.length > 0
                    && originalFileNames.length != fileNames.length) {
                throw new Exception(MessageUtils.message("download.non.downloader.request1"));
            }

            if (deletes != null
                    && deletes.length > 0
                    && deletes.length != fileNames.length) {
                throw new Exception(MessageUtils.message("download.non.downloader.request2"));
            }

            List<String> realFileNames = new LinkedList<>();
            List<String> filePaths = new LinkedList<>();
            for (int i = 0; i < fileNames.length; i++) {
                String fileName = fileNames[i];

                String originalFileName = null;
                if (originalFileNames != null) {
                    originalFileName = originalFileNames[i];
                }

                if (!FileUtils.checkAllowDownload(fileName)) {
                    throw new Exception(MessageUtils.message("download.invalid.fileName", fileName));
                }

                String realFileName;
                if (StringUtils.isNotEmpty(originalFileName)) {
                    realFileName = originalFileName;
                } else {
                    realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
                }
                realFileNames.add(realFileName);
                String filePath = CassiopeiaConfig.getDownloadPath() + "/" + fileName;
                filePaths.add(filePath);
            }

            byte[] data = FileUtils.read(filePaths.toArray(new String[0]), realFileNames.toArray(new String[0]));
            if (fileNames.length > 1) {
                response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                String downloadFileName = System.currentTimeMillis() + ".zip";
                if (Objects.equals(ViewType.INLINE, viewType)) {
                    FileUtils.setInlineResponseHeader(response, downloadFileName);
                } else {
                    FileUtils.setAttachmentResponseHeader(response, downloadFileName);
                }
            } else {
                String extension = FileUtils.getExtension(realFileNames.get(0));
                response.setContentType(MimeTypeUtils.getContentType(extension) + ";charset=UTF-8");

                if (Objects.equals(ViewType.INLINE, viewType)) {
                    FileUtils.setInlineResponseHeader(response, realFileNames.get(0));
                } else if (Objects.equals(ViewType.NONE, viewType)) {
                } else {
                    FileUtils.setAttachmentResponseHeader(response, realFileNames.get(0));
                }
            }

            if (!Objects.equals(ViewType.NONE, viewType)) {
                response.getOutputStream().write(data);
            }

            for (int i = 0; i < fileNames.length; i++) {
                Boolean delete = false;
                if (deletes != null) {
                    delete = deletes[i];
                }
                if (delete) {
                    FileUtils.delete(filePaths.get(i));
                }
            }
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    @ApiOperation("通用上传")
    @PostMapping(value = "common/upload", params = {"t=base64"})
    public AjaxResult uploadFile(@RequestBody Map<String, String> fileModel) {
        try {
            // 上传文件路径
            String filePath = CassiopeiaConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, Base64.getDecoder().decode(fileModel.get("file")),
                    fileModel.get("originalFilename"), fileModel.get("contentType"));
            String url = serverConfig.getUrl() + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("originalFileName", fileModel.get("originalFilename"));
            ajax.put("fileName", fileName);
            ajax.put("url", url);
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 通用上传请求.
     */
    @ApiOperation("通用上传")
    @PostMapping("common/upload")
    public AjaxResult uploadFile(@ApiParam("文件") MultipartFile file) {
        try {
            // 上传文件路径
            String filePath = CassiopeiaConfig.getUploadPath();
            // 上传并返回新文件名称
            String fileName = FileUploadUtils.upload(filePath, file);
            String url = serverConfig.getUrl() + fileName;
            AjaxResult ajax = AjaxResult.success();
            ajax.put("originalFileName", file.getOriginalFilename());
            ajax.put("fileName", fileName);
            ajax.put("url", url);
            return ajax;
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 资源通用访问.
     */
    @ApiOperation("资源通用访问")
    @GetMapping("common/resource")
    public void resource(@ApiParam("资源名") String name,
                         @ApiParam("资源原始名") @RequestParam(required = false) String originalName,
                         @ApiParam("展示方式") @RequestParam(required = false) ViewType viewType,
                         HttpServletRequest request,
                         HttpServletResponse response) {
        try {
            if (!FileUtils.checkAllowDownload(name)) {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", name));
            }
            // 本地资源路径
            String localPath = CassiopeiaConfig.getProfile();
            // 数据库资源地址
            String downloadPath = localPath + StringUtils.substringAfter(name, Constants.RESOURCE_PREFIX);
            // 下载名称
            String downloadName;
            if (StringUtils.isNotEmpty(originalName)) {
                downloadName = originalName;
            } else {
                downloadName = StringUtils.substringAfterLast(downloadPath, "/");
            }

            String extension = FileUtils.getExtension(name);
            response.setContentType(MimeTypeUtils.getContentType(extension) + ";charset=UTF-8");

            switch (viewType) {
                case ATTACHMENT:
                    FileUtils.setAttachmentResponseHeader(response, downloadName);
                    break;
                case INLINE:
                    FileUtils.setInlineResponseHeader(response, downloadName);
                    break;
                default:
                    if (FileUploadUtils.isAllowedExtension(extension, MimeTypeUtils.IMAGE_EXTENSION)
                            || FileUploadUtils.isAllowedExtension(extension, MimeTypeUtils.DOCUMENT_EXTENSION)
                    ) {
                        FileUtils.setInlineResponseHeader(response, downloadName);
                    } else {
                        FileUtils.setAttachmentResponseHeader(response, downloadName);
                    }
            }

            byte[] data = FileUtils.read(downloadPath);
            response.getOutputStream().write(data);
        } catch (Exception e) {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 资源逆向名称查看.
     */
    @ApiOperation("资源逆向名称查看")
    @GetMapping("common/resource/{originalName:.*}")
    public void profile(@ApiParam("资源名") String name,
                        @ApiParam("资源原始名") @PathVariable String originalName,
                        HttpServletRequest request,
                        HttpServletResponse response) {
        resource(name, originalName, ViewType.INLINE, request, response);
    }

    /**
     * 资源伪静态访问.
     */
    @ApiOperation("资源伪静态访问")
    @GetMapping("profile/**")
    public void profile(HttpServletRequest request,
                        HttpServletResponse response) {
        resource(request.getRequestURI(), request.getParameter("originalName"), ViewType.INLINE, request, response);
    }

    @GetMapping("common/chunkUpload")
    public AjaxResult chunkInfo(String identifier,
                                String filename,
                                Integer chunkNumber,
                                Integer currentChunkSize,
                                Integer chunkSize,
                                Integer totalSize) {
        String validated = validateRequest(identifier, filename, chunkNumber, chunkSize, totalSize, null);
        if ("valid".equals(validated)) {
            AjaxResult ajax = AjaxResult.success();
            String savedFileName = FileUploadUtils.extractFilename(identifier, filename);
            if (FileUtils.exists(CassiopeiaConfig.getUploadPath() + "/" + savedFileName)) {
                String viewFileName = FileUtils.getPathFileName(CassiopeiaConfig.getUploadPath(), savedFileName);
                String url = serverConfig.getUrl() + viewFileName;

                ajax.put("originalFileName", filename);
                ajax.put("fileName", viewFileName);
                ajax.put("url", url);
                ajax.put("skipUpload", true);
            } else {
                ajax.put("skipUpload", false);

                String chunkPath = getChunkPath(identifier);
                if (FileUtils.exists(chunkPath)) {
                    String[] uploadedChunked = FileUtils.list(chunkPath);
                    ajax.put("uploaded", uploadedChunked);
                }

                int currentTestChunk = 1;
                int numberOfChunks = (int) Math.max(Math.floor(totalSize / (chunkSize * 1.0)), 1);
                boolean merged = testChunkExists(currentTestChunk, numberOfChunks, numberOfChunks, identifier);
                ajax.put("needMerge", merged && numberOfChunks > 1);
            }

            return ajax;
        } else {
            return AjaxResult.error(validated);
        }
    }

    public AjaxResult chunkUpload(String identifier,
                                  String filename,
                                  Integer chunkNumber,
                                  Integer currentChunkSize,
                                  Integer chunkSize,
                                  Integer totalSize,
                                  byte[] contents,
                                  String originalFilename) {
        try {
            String validated = validateRequest(identifier, filename, chunkNumber, chunkSize, totalSize, null);
            if ("valid".equals(validated)) {
                int numberOfChunks = (int) Math.max(Math.floor(totalSize / (chunkSize * 1.0)), 1);
                String fileName;
                AjaxResult ajax = AjaxResult.success();
                if (numberOfChunks == 1) {
                    String mergedFileName = FileUploadUtils.extractFilename(identifier, filename);
                    fileName = FileUploadUtils.upload(CassiopeiaConfig.getUploadPath(), mergedFileName, contents);
                    String url = serverConfig.getUrl() + fileName;

                    ajax.put("originalFileName", originalFilename);
                    ajax.put("fileName", fileName);
                    ajax.put("url", url);
                } else {
                    String chunkPath = getChunkPath(identifier);
                    fileName = FileUploadUtils.upload(chunkPath, String.valueOf(chunkNumber), contents);
                    ajax.put("identifier", identifier);
                    ajax.put("chunkFilename", fileName);
                }

                int currentTestChunk = 1;
                boolean merged = testChunkExists(currentTestChunk, chunkNumber, numberOfChunks, identifier);
                ajax.put("needMerge", merged && numberOfChunks > 1);

                return ajax;
            } else {
                return AjaxResult.error(validated);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return AjaxResult.error(MessageUtils.message("error.exception"));
        }
    }

    @PostMapping("common/chunkUpload")
    public AjaxResult chunkUpload(String identifier,
                                  String filename,
                                  Integer chunkNumber,
                                  Integer currentChunkSize,
                                  Integer chunkSize,
                                  Integer totalSize,
                                  MultipartFile file) {
        try {
            return chunkUpload(identifier, filename, chunkNumber, currentChunkSize, chunkSize, totalSize,
                    file.getBytes(), file.getOriginalFilename());
        } catch (Exception e) {
            log.error(e.getMessage());
            return AjaxResult.error(MessageUtils.message("error.exception"));
        }
    }

    @PostMapping("common/chunkMerge")
    public AjaxResult chunkMerge(String identifier, String filename) {
        String mergedFileName = FileUploadUtils.extractFilename(identifier, filename);
        // 文件合并
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            if (FileUtils.exists(getChunkPath(identifier))) {
                write(identifier, os);
                String viewFileName = FileUploadUtils.upload(CassiopeiaConfig.getUploadPath(),
                        mergedFileName,
                        os.toByteArray());
                String url = serverConfig.getUrl() + viewFileName;

                clean(identifier);

                AjaxResult ajax = AjaxResult.success();
                ajax.put("originalFileName", filename);
                ajax.put("fileName", viewFileName);
                ajax.put("url", url);
                return ajax;
            } else {
                return AjaxResult.error(MessageUtils.message("upload.merge.file.not.exists", filename));
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return AjaxResult.error(MessageUtils.message("error.exception"));
        } finally {
            try {
                os.close();
            } catch (Throwable t) {
                log.error(t.getMessage());
            }
        }
    }

    /**
     * 最大文件大小
     */
    @Value("${cassiopeia.maxFileSize:10737418240}")
    private Long maxFileSize;

    public String cleanIdentifier(String identifier) {
        return identifier.replaceAll("[^0-9A-Za-z_-]", "");
    }

    public String getChunkPath(String identifier) {
        identifier = cleanIdentifier(identifier);
        return CassiopeiaConfig.getTemporaryPath() + "/" + identifier;
    }

    public String getChunkFileName(int chunkNumber, String identifier) {
        identifier = cleanIdentifier(identifier);
        return CassiopeiaConfig.getTemporaryPath() + "/" + identifier + "/" + chunkNumber;
    }

    public String validateRequest(String identifier,
                                  String filename,
                                  int chunkNumber,
                                  int chunkSize,
                                  int totalSize,
                                  Integer fileSize) {
        identifier = cleanIdentifier(identifier);

        if (chunkNumber == 0 || chunkSize == 0 || totalSize == 0 || identifier.length() == 0
                || filename.length() == 0) {
            return MessageUtils.message("upload.non.uploader.request");
        }
        int numberOfChunks = (int) Math.max(Math.floor(totalSize / (chunkSize * 1.0)), 1);
        if (chunkNumber > numberOfChunks) {
            return MessageUtils.message("upload.invalid.uploader.request1", chunkNumber, numberOfChunks);
        }

        if (maxFileSize != null && totalSize > maxFileSize) {
            return MessageUtils.message("upload.invalid.uploader.request2");
        }

        if (fileSize != null) {
            if (chunkNumber < numberOfChunks && fileSize != chunkSize) {
                return MessageUtils.message("upload.invalid.uploader.request3", chunkNumber);
            }
            if (numberOfChunks > 1 && chunkNumber == numberOfChunks
                    && fileSize != ((totalSize % chunkSize) + chunkSize)) {
                return MessageUtils.message("upload.invalid.uploader.request4", chunkNumber);
            }
            if (numberOfChunks == 1 && fileSize != totalSize) {
                return MessageUtils.message("upload.invalid.uploader.request5", chunkNumber);
            }
        }

        return "valid";
    }

    private void pipeChunk(int number, String identifier, OutputStream writableStream)
            throws IOException {
        String chunkFileName = getChunkFileName(number, identifier);
        if (FileUtils.exists(chunkFileName)) {
            InputStream inputStream = new ByteArrayInputStream(FileUtils.read(chunkFileName));
            int maxlength = 1024;
            int len = 0;
            try {
                byte[] buff = new byte[maxlength];
                while ((len = inputStream.read(buff, 0, maxlength)) > 0) {
                    writableStream.write(buff, 0, len);
                }
            } finally {
                inputStream.close();
            }
            pipeChunk(number + 1, identifier, writableStream);
        }
    }

    public void write(String identifier, OutputStream writableStream) throws IOException {

        pipeChunk(1, identifier, writableStream);
    }

    /**
     * @param currentTestChunk 测试块
     * @param chunkNumber      当前上传块
     * @param numberOfChunks   总块数
     * @param identifier       文件
     * @return
     */
    private boolean testChunkExists(int currentTestChunk, int chunkNumber, int numberOfChunks, String identifier) {
        String currentFile = getChunkFileName(currentTestChunk, identifier);
        if (FileUtils.exists(currentFile)) {
            currentTestChunk++;
            if (currentTestChunk >= numberOfChunks) {
                log.debug("currentTestChunk({}) >= numberOfChunks({})", currentTestChunk, numberOfChunks);
                return currentTestChunk == numberOfChunks;
            } else {
                return testChunkExists(currentTestChunk, chunkNumber, numberOfChunks, identifier);
            }
        }
        return false;
    }

    public void clean(String identifier) {
        FileUtils.delete(getChunkPath(identifier));
    }

    /**
     * 网络图片资源本地化
     *
     * @param netImages 网络图片资源
     * @return
     */
    @ApiOperation("网络图片资源本地化")
    @PostMapping(value = "common/netImageCatch", params = "t=netImage")
    public Map<String, Object> netImageCatch(@RequestParam("files[]") List<String> netImages) {

        Map<String, Object> result = new HashMap<>();

        try {
            List<Object> list = new LinkedList<>();
            for (String u : netImages) {
                if (isIgnoreNetImageCatchUrl(u)) {
                    continue;
                }
                String fileName = WebUtils.cacheImgLocal(u);
                Map<String, String> fileMap = new LinkedHashMap<>();
                fileMap.put("source", u);
                if (fileName != null) {
                    fileMap.put("state", "SUCCESS");
                    fileMap.put("url", fileName);
                } else {
                    fileMap.put("state", "FAILED");
                }
                list.add(fileMap);
            }
            result.put("list", list);
            result.put("state", "SUCCESS");
        } catch (Exception e) {
            result.put("state", "FAILED");
            log.error(e.getMessage());
        }
        return result;
    }

    private Boolean isIgnoreNetImageCatchUrl(String url) {
        List<String> ignore = cassiopeiaConfig.getIgnoreNetImageCatchDomains();
        boolean result = false;
        if (StringUtils.isNotEmpty(url)) {
            for (String u : ignore) {
                if (url.contains(u)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
}
