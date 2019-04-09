package com.gold.common;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by zhengwei on 2017/11/7.
 */
@ConfigurationProperties("weixin")
@Component
public class WeChatConfig {

    @Value("${web.upload-path}")
    private static String path;


    public static String item;
    public static String brand;
    public static String back;
    public static String getItem() {
        return item;
    }

    public static void setItem(String item) {
        WeChatConfig.item = item;
    }

    public static String getBrand() {
        return brand;
    }

    public static void setBrand(String brand) {
        WeChatConfig.brand = brand;
    }
    public static String getBack() {
        return back;
    }

    public static void setBack(String back) {
        WeChatConfig.back = back;
    }
    //我的二维码背景
    public static String BACKGROUND_IMAGE = "static/images/back2.png";

    //上传素材构建的文件
    public static String FILE_PATH = System.getProperty("user.dir") + File.separator +"file.jpg";

    /* 二维码类型 */

    //临时的整型参数值
    public static String QR_SCENE = "QR_SCENE";
    //临时的字符串参数值
    public static String QR_STR_SCENE = "QR_STR_SCENE";
    //永久的整型参数值
    public static String QR_LIMIT_SCENE = "QR_LIMIT_SCENE";
    //永久的字符串参数值
    public static String QR_LIMIT_STR_SCENE = "QR_LIMIT_STR_SCENE";

    //获取access_token
    public static String GET_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
    //临时二维码请求
    public static String GET_TEMP_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s";
    //永久二维码请求
    public static String GET_PERSIST_TICKET_URL = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s";
    //ticket换取二维码
    public static String TICKET_FOR_QRCODE = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=%s";
    //获取用户信息
    public static String GET_USERINFO_URL = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN";

    public static String UPLOAD_TEMP_MATERIAL_URL = "https://api.weixin.qq.com/cgi-bin/media/upload?access_token=%s&type=%s";

    /**
     * 注意点：
     * 1、临时素材media_id是可复用的。
     * 2、媒体文件在微信后台保存时间为3天，即3天后media_id失效。
     * 3、上传临时素材的格式、大小限制与公众平台官网一致。
     */
    //图片（image）: 2M，支持PNG\JPEG\JPG\GIF格式
    public static String IMAGE = "image";
    //语音（voice）：2M，播放长度不超过60s，支持AMR\MP3格式
    public static String VOICE = "voice";
    //视频（video）：10MB，支持MP4格式
    public static String VIDEO = "video";
    //缩略图（thumb）：64KB，支持JPG格式
    public static String THUMB = "thumb";

    /**
     * 去微信获取access_token
     *
     * @param appid
     * @param secret
     * @return
     * @throws Exception
     */
    public static JSONObject getAccessToken(String appid, String secret) throws Exception {
        String url = String.format(GET_ACCESS_TOKEN_URL, appid, secret);
        JSONObject jsonObject = Utility.HttpGetJSON(url);
        return jsonObject;
    }
}
