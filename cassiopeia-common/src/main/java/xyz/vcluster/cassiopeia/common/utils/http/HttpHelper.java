package xyz.vcluster.cassiopeia.common.utils.http;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import xyz.vcluster.cassiopeia.common.utils.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

/**
 * 通用http工具封装
 *
 * @author cassiopeia
 */
public class HttpHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHelper.class);

    public static String getBodyString(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try (InputStream inputStream = request.getInputStream()) {
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            LOGGER.warn("getBodyString出现问题！");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error(ExceptionUtils.getMessage(e));
                }
            }
        }
        return sb.toString();
    }

    public static String buildUrl(HttpServletRequest request, String path) {
        if (StringUtils.isNotEmpty(path)) {
            if (path.startsWith("http")) {
                return path;
            } else {
                UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
                String scheme = request.getHeader("x-forwarded-proto");
                if (StringUtils.isNotEmpty(scheme)) {
                    builder.scheme(scheme);
                } else {
                    builder.scheme(request.getScheme());
                }

                String host = request.getHeader("x-forwarded-host");
                if (StringUtils.isNotEmpty(host)) {
                    builder.host(host);
                } else {
                    host = request.getHeader("host");
                    if (StringUtils.isNotEmpty(host)) {
                        builder.host(host);
                    } else {
                        builder.host(request.getRemoteHost());
                    }
                }
                builder.path(path);

                return builder.build().toString();
            }
        }
        return null;
    }

    public static String encodeUrl(String url, List<RequestParameter> requestParameters) {
        if (requestParameters != null && requestParameters.size() > 0) {
            StringBuilder builder = new StringBuilder();
            requestParameters.sort(Comparator.comparing(RequestParameter::getName));
            if (StringUtils.isNotEmpty(url)) {
                builder.append(url);
                if (url.contains("?")) {
                    builder.append("&");
                } else {
                    builder.append("?");
                }
            }

            for (int i = 0; i < requestParameters.size(); i++) {
                if (i != 0) {
                    builder.append("&");
                }

                RequestParameter requestParameter = requestParameters.get(i);
                if (requestParameter != null) {
                    String name = requestParameter.getName();
                    if (StringUtils.isNotEmpty(name)) {
                        builder.append(UriUtils.encode(name, "UTF-8"));
                        builder.append("=");
                        String value = requestParameter.getValue() != null ? requestParameter.getValue() : "";
                        builder.append(UriUtils.encode(value, "UTF-8"));
                    }
                }
            }
            return builder.toString();
        } else {
            return url;
        }
    }

    public static void dump(HttpServletRequest request) {
        LOGGER.info("request:{}", request.getRequestURL());

        Enumeration<String> parameterNames = request.getParameterNames();
        if (parameterNames != null) {
            while (parameterNames.hasMoreElements()) {
                String parameterName = parameterNames.nextElement();
                LOGGER.info("parameter {}:{}", parameterName, request.getParameter(parameterName));
            }
        }

        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                LOGGER.info("header {}:{}", headerName, request.getHeader(headerName));
            }
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                LOGGER.info("cookie {}:{}", cookie.getName(), cookie.getValue());
            }
        }

        Enumeration<String> attributeNames = request.getAttributeNames();
        attributeNames = request.getSession().getAttributeNames();
        if (attributeNames != null) {
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                LOGGER.info("attribute {}:{}", attributeName, request.getAttribute(attributeName));
            }
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            attributeNames = request.getSession().getAttributeNames();
            if (attributeNames != null) {
                while (attributeNames.hasMoreElements()) {
                    String attributeName = attributeNames.nextElement();
                    LOGGER.info("session attribute {}:{}", attributeName, session.getAttribute(attributeName));
                }
            }
        }
    }

    public static class RequestParameter {
        private String name;

        private String value;

        public RequestParameter(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
