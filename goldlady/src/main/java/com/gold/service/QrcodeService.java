package com.gold.service;

import com.alibaba.fastjson.JSONObject;
import com.gold.common.Utility;
import com.gold.common.WeChatConfig;
import com.gold.util.QRCodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhengwei on 2017/11/8.
 */
@Service("qrcodeService")
@Scope("prototype")
public class QrcodeService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private QRCodeUtil qrCodeUtil;

    @Autowired
    private WeixinUserService weixinUserService;

    @Autowired
    WeChatConfig weChatConfig;

    /**
     * 直接获取我的二维码
     *
     * @param response
     * @param openid
     * @throws Exception
     */
    public void getMyQrcode(HttpServletResponse response, String openid) throws Exception {
        JSONObject userInfo = weixinUserService.getWeixinUserInfoByOpenId(weChatConfig.getItem(), openid);
        //获取微信用户头像
        String avatarUrl = userInfo.get("headimgurl") + "";
        byte[] avatar = Utility.HttpGetByteArray(avatarUrl);
        //先用临时二维码测试，永久二维码数量有限
        byte[] qrcode = QRCodeUtil.createTempStrTicket(userInfo.getString("accesstoken"), "2592000", ";1;" + openid);
        logger.info("back:" + weChatConfig.getBack());
        qrCodeUtil.getMyQrcode(response, qrcode, avatar, weChatConfig.getBack(), weChatConfig.getBrand(), userInfo.get("nickname") + "");
    }

    /**
     * 获取我的二维码上传到微信素材库之后的 media_id
     */
    public String getMaterialMediaId(String openid) throws Exception {
        JSONObject userInfo = weixinUserService.getWeixinUserInfoByOpenId(weChatConfig.getItem(), openid);
        //获取微信用户头像
        String avatarUrl = userInfo.get("headimgurl") + "";
        byte[] avatar = Utility.HttpGetByteArray(avatarUrl);
        //先用临时二维码测试，永久二维码数量有限
        byte[] qrcode = QRCodeUtil.createTempStrTicket(userInfo.getString("accesstoken"), "2592000", ";1;" + openid);
        return qrCodeUtil.getMaterialMediaId(userInfo.getString("accesstoken"), qrcode, avatar, WeChatConfig.BACKGROUND_IMAGE, weChatConfig.getBrand(), userInfo.get("nickname") + "");
    }

}
