package com.gold.service;

import com.alibaba.fastjson.JSONObject;
import com.gold.common.BizException;
import com.gold.common.BizReturnCode;
import com.gold.common.WeChatConfig;
import com.gold.pst.WeixinConfigPst;
import com.gold.util.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hsl on 2017/11/6.
 */
@Service("weixinConfigService")
@Scope("prototype")
public class WeixinConfigService {
    Logger logger = Logger.getLogger(WeixinConfigService.class);


    @Autowired
    WeixinConfigPst weixinConfigPst;
    @Autowired
    WeChatConfig weChatConfig;

    /**
     * 获取微信公众号配置
     *
     * @param item
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getWeixinConfig(String item) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map = weixinConfigPst.getWeixinConfigByItem(item);
        if (null == map) {
            throw new BizException(BizReturnCode.NoWeixinConfigError, "查不到" + item + "公众号的配置信息");
        }
        return map;
    }

    /**
     * 刷新token
     *
     * @param
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void updateAccessToken(String item, String token, Date tokenTime) throws Exception {
        weixinConfigPst.updateAccessToken(item, token, tokenTime);
    }

    /**
     * 检测token有效性并获取
     *
     * @return
     * @author zhengwei
     * @time 2017/11/7 21:55
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public String checkAndGetToken() throws Exception {
        logger.info("item:" + weChatConfig.getItem());
        Map<String, Object> config = getWeixinConfig(weChatConfig.getItem());
        String time = config.get("accesstokentime") + "";
        logger.info("accesstokentime:" + time);
        logger.info("appid:" + config.get("appid"));
        logger.info("appsecret:" + config.get("appsecret"));
        boolean bool = false;
        // 没有存
        if (StringUtils.isEmpty(time) || "null".equals(time)) {
            bool = true;
        }
        // 有数据，但是过期了
        if (time != null && new Date().getTime() > DateUtils.string2date(time).getTime() + 6200 * 1000) {
            bool = true;
        }
        // 重新获取并存储
        if (bool) {
            JSONObject accessToken = WeChatConfig.getAccessToken(config.get("appid") + "", config.get("appsecret") + "");
            logger.info("去微信获取token返回:" + accessToken.toJSONString());
            updateAccessToken(weChatConfig.getItem(), accessToken.get("access_token") + "", new Date());
            return accessToken.get("access_token") + "";
        }
        // 有数据，有效期之内，直接用
        if (time != null && new Date().getTime() < DateUtils.string2date(time).getTime() + 6200 * 1000) {
            return config.get("accesstoken") + "";
        }
        return null;
    }


}
