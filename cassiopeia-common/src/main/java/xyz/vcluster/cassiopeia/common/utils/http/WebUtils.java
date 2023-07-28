package xyz.vcluster.cassiopeia.common.utils.http;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import xyz.vcluster.cassiopeia.common.config.CassiopeiaConfig;
import xyz.vcluster.cassiopeia.common.utils.StringUtils;
import xyz.vcluster.cassiopeia.common.utils.file.FileUploadUtils;
import xyz.vcluster.cassiopeia.common.utils.file.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.Map.Entry;

public class WebUtils {

    /**
     * GET请求
     *
     * @param url          URL
     * @param parameterMap 请求参数
     * @return 返回结果
     */
    public static void get(String url, File file, Map<String, Object> parameterMap) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            if (parameterMap != null) {
                for (Entry<String, Object> entry : parameterMap.entrySet()) {
                    String name = entry.getKey();
                    String value = Objects.toString(entry.getValue());
                    if (StringUtils.isNotEmpty(name)) {
                        nameValuePairs.add(new BasicNameValuePair(name, value));
                    }
                }
            }
            HttpGet httpGet = new HttpGet(url + (StringUtils.contains(url, "?") ? "&" : "?") + EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairs, "UTF-8")));
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            OutputStream output = new FileOutputStream(file);
            IOUtils.copy(httpEntity.getContent(), output);
            output.flush();
            output.close();
            EntityUtils.consume(httpEntity);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static byte[] cacheImg(String originalImageUrl) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>();

            HttpGet httpGet = new HttpGet(originalImageUrl + (StringUtils.contains(originalImageUrl, "?") ? "&" : "?") + EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairs, "UTF-8")));
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String contentType = httpEntity.getContentType().getValue();
            if (contentType.contains("image") || contentType.contains("video") || contentType.contains("audio")) {
                String suffix = getSuffix(contentType);
                if (suffix == null) {
                    System.out.println("******:" + contentType);
                    return null;
                }

                return FileUtils.toByteArray(httpEntity.getContent());
            }
        } catch (IOException | MimeTypeException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String cacheImgLocal(String originalImg, Integer i) {
        if (i < 3) {
            String url = cacheImgLocal(originalImg);
            if (url == null) {
                return cacheImgLocal(originalImg, ++i);
            } else {
                return url;
            }
        }
        return null;
    }

    public static String cacheImgLocal(String originalImg) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<>();

            HttpGet httpGet = new HttpGet(originalImg + (StringUtils.contains(originalImg, "?") ? "&" : "?") + EntityUtils.toString(new UrlEncodedFormEntity(nameValuePairs, "UTF-8")));
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String contentType = httpEntity.getContentType().getValue();
            if (contentType.contains("image") || contentType.contains("video") || contentType.contains("audio")) {
                String suffix = getSuffix(contentType);
                if (suffix == null) {
                    System.out.println("******:" + contentType);
                    return null;
                }
                String newFileBaseName = UUID.randomUUID().toString();
                String newFileName = newFileBaseName + suffix;
                byte[] bytes = FileUtils.toByteArray(httpEntity.getContent());

                return FileUploadUtils.upload(CassiopeiaConfig.getUploadPath(), bytes, newFileName, contentType);
            }
        } catch (IOException | MimeTypeException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getSuffix(String contentType) throws MimeTypeException {
        if (StringUtils.isNotBlank(contentType)) {
            String[] cTypes = contentType.split(";|\\ ");
            MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
            MimeType type = allTypes.forName(cTypes[0]);
            return type.getExtension();
        }
        return null;
    }
}
