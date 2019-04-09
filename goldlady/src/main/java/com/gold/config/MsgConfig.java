package com.gold.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2017/12/31.
 */
@ConfigurationProperties("msg")
@Component
public class MsgConfig {
    private Boolean ifopen;
    private String label;

    public Boolean getIfopen() {
        return ifopen;
    }

    public void setIfopen(Boolean ifopen) {
        this.ifopen = ifopen;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
