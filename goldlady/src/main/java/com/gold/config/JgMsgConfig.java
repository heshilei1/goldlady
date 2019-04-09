package com.gold.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2018/1/2.
 */
@ConfigurationProperties("msg.jg")
@Component
public class JgMsgConfig {
    private String appkey;
    private String secret;
    private String smgcodeid;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSmgcodeid() {
        return smgcodeid;
    }

    public void setSmgcodeid(String smgcodeid) {
        this.smgcodeid = smgcodeid;
    }
}
