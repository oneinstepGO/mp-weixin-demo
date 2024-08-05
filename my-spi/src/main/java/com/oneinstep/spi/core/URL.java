package com.oneinstep.spi.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * URL
 */
@Getter
@Setter
@ToString
public class URL implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 协议
     */
    private String protocol;
    /**
     * 主机
     */
    private String host;
    /**
     * 端口
     */
    private int port;
    /**
     * 路径
     */
    private String path;
    /**
     * 参数
     */
    private Map<String, String> urlParams;

    public URL(String protocol, String host, int port, String path, Map<String, String> urlParams) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.path = path;
        this.urlParams = Objects.requireNonNullElseGet(urlParams, HashMap::new);
    }

    public String getMethodParameter(String method, String extName, String defaultExtName) {
        String key = StringUtils.isNoneBlank(extName) ? extName : defaultExtName;
        return urlParams.get(method + "." + key);
    }

    public String getParameter(String extName, String defaultExtName) {
        String key = StringUtils.isNotBlank(extName) ? extName : defaultExtName;
        return urlParams.get(key);
    }

    public String getParameter(String extName) {
        return urlParams.get(extName);
    }

    public String getPathAddress() {
        StringBuilder address = new StringBuilder(protocol + "://" + host + ":" + port + "/" + path);
        if (urlParams != null && !urlParams.isEmpty()) {
            address.append("?");
            for (Map.Entry<String, String> entry : urlParams.entrySet()) {
                address.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            address = new StringBuilder(address.substring(0, address.length() - 1));
        }
        return address.toString();
    }

}
