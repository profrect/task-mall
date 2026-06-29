package com.mall.admin.configuration.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * packageName com.nemojoy.platform.configuration
 *
 * @author xugm
 * @date 2024/9/25
 * @description 不需要认证的 url配置
 */
@ConfigurationProperties(prefix = "nemojoy.web")
public class NoAuthUrlProperties {

    private List<String> noLoginUris;

    private List<String> noPermUris;

    public List<String> getNoLoginUris() {
        if(Objects.isNull(noLoginUris)) {
            return Collections.emptyList();
        }
        return noLoginUris;
    }

    public void setNoLoginUris(List<String> noLoginUris) {
        this.noLoginUris = noLoginUris;
    }

    public List<String> getNoPermUris() {
        if(Objects.isNull(noPermUris)) {
            return Collections.emptyList();
        }
        return noPermUris;
    }

    public void setNoPermUris(List<String> noPermUris) {
        this.noPermUris = noPermUris;
    }
}

