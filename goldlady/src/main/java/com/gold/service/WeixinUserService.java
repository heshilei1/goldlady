package com.gold.service;

import com.alibaba.fastjson.JSONObject;
import com.gold.common.Utility;
import com.gold.common.WeChatConfig;
import com.gold.controller.ListenerController;
import com.gold.pst.WeixinUserPst;
import com.gold.util.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by zhengwei on 2017/11/7.
 */
@Service("weixinUserService")
@Scope("prototype")
public class WeixinUserService {
    Logger logger = Logger.getLogger(WeixinUserService.class);

    @Autowired
    WeixinConfigService weixinConfigService;

    @Autowired
    private WeixinUserPst weixinUserPst;

    /**
     * 通过userid获取openid
     *
     * @author zhengwei
     * @email wei139806@163.com
     * @date 2017/11/10 15:27
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getOpenIdByUserid(String userid) throws Exception {
        return weixinUserPst.getOpenIdByUserId(userid);
    }
    /**
     * 获取微信用户信息
     *
     * @param item
     * @param code
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public JSONObject getWeixinUserInfoByCode(String item, String code) throws Exception {
        Map<String, Object> configMap = weixinConfigService.getWeixinConfig(item);
        String appid = (String) configMap.get("appid");
        String secret = (String) configMap.get("appsecret");
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appid + "&secret=" + secret + "&code=" + code + "&grant_type=authorization_code";
        try {
            JSONObject weixinUserJson = Utility.HttpGetJSON(url);
            this.logger.info("获取微信用户信息完成");
            return weixinUserJson;
        } catch (Exception e) {
            logger.error("获取微信用户信息失败");
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public JSONObject getWeixinUserInfoByOpenId(String item, String openId) throws Exception {
        /*SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Map<String, Object> configMap = weixinConfigService.getWeixinConfig(item);
        String appid = (String) configMap.get("appid");
        String secret = (String) configMap.get("appsecret");
        String accessToken = (String) configMap.get("accesstoken");
        String accessTokenTime =  (String)configMap.get("accesstokentime");
        Date component_access_token_date = sdf.parse(accessTokenTime);
        if (StringUtils.isEmpty(accessToken) || new Date().getTime() - component_access_token_date.getTime() > 90 * 60 * 1000) {
            accessToken = refreshAccessToken(appid, secret);
        }*/
        String accessToken = weixinConfigService.checkAndGetToken();
        String url = String.format(WeChatConfig.GET_USERINFO_URL, accessToken, openId);
        JSONObject weixinUserJson = Utility.HttpGetJSON(url);
        weixinUserJson.put("accesstoken",accessToken);
        return weixinUserJson;
    }

    private String refreshAccessToken(String appId, String appSecret) throws Exception {
        //String caturl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret + "";
        JSONObject tokenJson = Utility.HttpGetJSON(String.format(WeChatConfig.GET_ACCESS_TOKEN_URL, appId, appSecret));
        String access_token = tokenJson.getString("access_token");
        if (StringUtils.isEmpty(access_token)) {
            logger.info("access_token刷新失败");
        }
        //保存token
        weixinConfigService.updateAccessToken("goldlady", access_token, new Date());
        return access_token;
    }
}
