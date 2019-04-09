package com.gold.util;


import com.alibaba.fastjson.JSONObject;
import com.gold.common.Utility;
import com.gold.common.WeChatConfig;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by zhengwei on 2017/11/6.
 */
@Repository("qRCodeUtil")
public class QRCodeUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(QRCodeUtil.class);

    /**
     * 获取我的二维码上传到微信素材库之后的 media_id
     *
     * @author zhengwei
     * @email wei139806@163.com
     * @date 2017/11/10 15:49
     */
    public String getMaterialMediaId(String accesstoken, byte[] qrcode, byte[] avator, String background, String brand, String userName){
        byte[] data = QRCodeGenerate(qrcode, avator, background, brand, userName);
        //上传到微信临时素材
        JSONObject jsonObject = null;
        try {
            jsonObject = Utility.HttpUploadByteArray(String.format(WeChatConfig.UPLOAD_TEMP_MATERIAL_URL, accesstoken, WeChatConfig.IMAGE), data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject.getString("media_id");
    }

    /**
     * 直接获取我的二维码
     *
     * @author zhengwei
     * @email wei139806@163.com
     * @date 2017/11/10 15:49
     */
    public void getMyQrcode(HttpServletResponse response, byte[] qrcode, byte[] avator, String background, String brand, String userName){
        byte[] data = QRCodeGenerate(qrcode, avator, background, brand, userName);
        try {
            OutputStream out = response.getOutputStream();
            out.write(data);
            out.flush();
            out.close();
            response.flushBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 文字、二维码和背景图的合并
     *
     * @param qrcode     byte数组形式的二维码
     * @param avator     byte数组形式的微信用户头像
     * @param background 背景图
     * @param brand      品牌
     * @param userName   用户名
     */
    public byte[] QRCodeGenerate(byte[] qrcode, byte[] avator, String background, String brand, String userName) {
        try {
            LOGGER.info("userName:" + userName);
            LOGGER.info("brand:" + brand);
            ByteArrayInputStream bin = new ByteArrayInputStream(qrcode);    //微信二维码作为输入流；
            ByteArrayInputStream headImg = new ByteArrayInputStream(avator);    //微信头像作为输入流；
            BufferedImage image = ImageIO.read(bin);
            BufferedImage bufferedImage = ImageIO.read(headImg);
            //头像裁剪圆形
            BufferedImage headImage = convertCircular(bufferedImage);

            InputStream stream = this.getClass().getClassLoader().getResourceAsStream(background);
            BufferedImage bg = ImageIO.read(stream);

            Graphics2D g = bg.createGraphics();
            g.drawImage(image, 66, 396, image.getWidth() * 250 / 433, image.getHeight() * 250 / 433, null);
            g.drawImage(headImage, 14, 45, 107, 107, null);
            processText(g, userName, 30, 195, 90, Color.red, new Color(255, 255, 255), "Microsoft YaHei UI");
            processText(g, brand, 25, 185, 133, Color.red, Color.WHITE, "Microsoft YaHei UI");
            g.dispose();
            bg.flush();
            image.flush();
            headImage.flush();
            //输出到响应流中
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            ImageOutputStream imOut = ImageIO.createImageOutputStream(bs);
            ImageIO.write(bg, "jpg", imOut);
            InputStream in = new ByteArrayInputStream(bs.toByteArray());
            byte[] data = ByteToInputStream.inputStream2byte(in);
            return data;
            /*//上传到微信临时素材
            JSONObject jsonObject = Utility.HttpUploadByteArray(String.format(WeChatConfig.UPLOAD_TEMP_MATERIAL_URL, accesstoken, WeChatConfig.IMAGE), data);
            long startTime2 = System.currentTimeMillis();
            return jsonObject.getString("media_id");*/
            //直接输出到页面
            /*OutputStream out = response.getOutputStream();
            out.write(data);
            out.flush();
            out.close();
            response.flushBuffer();*/
        } catch (Exception e) {
            LOGGER.info("分享图片生成失败");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 处理文字
     *
     * @param g               画笔对象
     * @param text            文本
     * @param fontSize        字体大小
     * @param x               x坐标
     * @param y               y坐标
     * @param color           字体颜色
     * @param backgroundColor 背景颜色
     * @param font            字体
     */
    public static void processText(Graphics2D g, String text, int fontSize, int x, int y, Color color, Color backgroundColor, String font) {
        g.setColor(color);
        g.setBackground(backgroundColor);

        AttributedString ats = new AttributedString(text);
        Font fo = new Font(font, Font.BOLD, fontSize);
        g.setFont(fo);
        /* 消除java.awt.Font字体的锯齿 */
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        /* 消除java.awt.Font字体的锯齿 */
        ats.addAttribute(TextAttribute.FONT, fo, 0, text.length());
        AttributedCharacterIterator iter = ats.getIterator();
        g.drawString(iter, x, y);
    }

    /**
     * 创建临时带参数二维码(数字)
     *
     * @param accessToken   accessToken
     * @param expireSeconds 该二维码有效时间，以秒为单位。 最大不超过2592000（即30天），此字段如果不填，则默认有效期为30秒。
     * @param sceneId       场景Id
     * @return
     */
    public static byte[] createTempTicket(String accessToken, String expireSeconds, int sceneId) throws Exception {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("access_token", accessToken);
        Map<String, Integer> intMap = new HashMap<>();
        intMap.put("scene_id", sceneId);
        Map<String, Map<String, Integer>> mapMap = new HashMap<>();
        mapMap.put("scene", intMap);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("expire_seconds", expireSeconds);
        paramsMap.put("action_name", WeChatConfig.QR_SCENE);
        paramsMap.put("action_info", mapMap);
        Gson gson = new Gson();
        String data = gson.toJson(paramsMap);
        //请求ticket
        JSONObject jsonObject = Utility.HttpPostJSON(String.format(WeChatConfig.GET_TEMP_TICKET_URL, accessToken), data);
        //换取二维码
        byte[] bytes = Utility.HttpGetByteArray(String.format(WeChatConfig.TICKET_FOR_QRCODE,
                URLEncoder.encode(jsonObject.get("ticket").toString(), "UTF-8")));
        return bytes;
    }

    /**
     * 创建临时带参数二维码(字符串)
     *
     * @param accessToken
     * @param expireSeconds 该二维码有效时间，以秒为单位。 最大不超过2592000（即30天），此字段如果不填，则默认有效期为30秒。
     * @param sceneStr      场景str
     * @return
     */
    public static byte[] createTempStrTicket(String accessToken, String expireSeconds, String sceneStr) throws Exception {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("access_token", accessToken);
        Map<String, String> strMap = new HashMap<>();
        strMap.put("scene_str", sceneStr);
        Map<String, Map<String, String>> mapMap = new HashMap<>();
        mapMap.put("scene", strMap);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("expire_seconds", expireSeconds);
        paramsMap.put("action_name", WeChatConfig.QR_STR_SCENE);
        paramsMap.put("action_info", mapMap);
        Gson gson = new Gson();
        String data = gson.toJson(paramsMap);
        //请求ticket
        JSONObject jsonObject = Utility.HttpPostJSON(String.format(WeChatConfig.GET_TEMP_TICKET_URL, accessToken), data);
        //换取二维码
        byte[] bytes = Utility.HttpGetByteArray(String.format(WeChatConfig.TICKET_FOR_QRCODE,
                URLEncoder.encode(jsonObject.get("ticket").toString(), "UTF-8")));
        return bytes;
    }

    /**
     * 创建永久二维码(数字)
     *
     * @param accessToken
     * @param sceneId     场景Id
     * @return
     */
    public static byte[] createPersistTicket(String accessToken, int sceneId) throws Exception {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("access_token", accessToken);
        Map<String, Integer> intMap = new HashMap<>();
        intMap.put("scene_id", sceneId);
        Map<String, Map<String, Integer>> mapMap = new HashMap<>();
        mapMap.put("scene", intMap);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("action_name", WeChatConfig.QR_LIMIT_SCENE);
        paramsMap.put("action_info", mapMap);
        Gson gson = new Gson();
        String data = gson.toJson(paramsMap);
        //请求ticket
        JSONObject jsonObject = Utility.HttpPostJSON(String.format(WeChatConfig.GET_PERSIST_TICKET_URL, accessToken), data);
        //换取二维码
        byte[] bytes = Utility.HttpGetByteArray(String.format(WeChatConfig.TICKET_FOR_QRCODE,
                URLEncoder.encode(jsonObject.get("ticket").toString(), "UTF-8")));
        return bytes;

    }

    /**
     * 创建永久二维码(字符串)
     *
     * @param accessToken
     * @param sceneStr    场景str
     * @return
     */
    public static byte[] createPersistStrTicket(String accessToken, String sceneStr) throws Exception {
        TreeMap<String, String> params = new TreeMap<>();
        params.put("access_token", accessToken);
        // output data
        Map<String, String> intMap = new HashMap<>();
        intMap.put("scene_str", sceneStr);
        Map<String, Map<String, String>> mapMap = new HashMap<>();
        mapMap.put("scene", intMap);
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("action_name", WeChatConfig.QR_LIMIT_STR_SCENE);
        paramsMap.put("action_info", mapMap);
        Gson gson = new Gson();
        String data = gson.toJson(paramsMap);
        //请求ticket
        JSONObject jsonObject = Utility.HttpPostJSON(String.format(WeChatConfig.GET_PERSIST_TICKET_URL, accessToken), data);
        //换取二维码
        byte[] bytes = Utility.HttpGetByteArray(String.format(WeChatConfig.TICKET_FOR_QRCODE,
                URLEncoder.encode(jsonObject.get("ticket").toString(), "UTF-8")));
        return bytes;
    }

    /**
     * 传入的图像必须是正方形的 才会 圆形  如果是长方形的比例则会变成椭圆的
     *
     * @param bi1 用户头像
     * @return
     * @throws IOException
     */
    public static BufferedImage convertCircular(BufferedImage bi1) throws IOException {
        //BufferedImage bi1 = ImageIO.read(new File(url));
        //这种是黑色底的
        //BufferedImage bi2 = new BufferedImage(bi1.getWidth(),bi1.getHeight(),BufferedImage.TYPE_INT_RGB);

        //透明底的图片
        BufferedImage bi2 = new BufferedImage(bi1.getWidth(), bi1.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, bi1.getWidth(), bi1.getHeight());
        Graphics2D g2 = bi2.createGraphics();
        g2.setClip(shape);
        // 使用 setRenderingHint 设置抗锯齿
        g2.drawImage(bi1, 0, 0, null);
        //设置颜色
        g2.setBackground(Color.green);
        g2.dispose();
        return bi2;
    }
}