package com.gold.controller;

import com.alibaba.fastjson.JSONObject;
import com.gold.common.Utility;
import com.gold.common.WeChatConfig;
import com.gold.config.HostConfig;
import com.gold.pst.WeixinUserPst;
import com.gold.service.QrcodeService;
import com.gold.service.UserService;
import com.gold.service.WeixinConfigService;
import com.gold.service.WeixinUserService;
import com.gold.util.*;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import qq.weixin.mp.aes.WXBizMsgCrypt;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Created by user on 2017/11/3.
 */
@Scope("prototype")
@Controller
@RequestMapping("/gold/weixin")
public class ListenerController extends DsbBaseController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    WeixinConfigService weixinConfigService;
    @Autowired
    QrcodeService qrcodeService;
    @Autowired
    UserService userService;
    @Autowired
    WeixinUserService weixinUserService;
    @Autowired
    WeixinUserPst weixinUserPst;
    @Autowired
    HostConfig hostConfig;
    @Autowired
    WeChatConfig weChatConfig;

    /**
     * 系统公众号的消息接收
     */
    @RequestMapping(value = "/msg", method = {RequestMethod.GET, RequestMethod.POST})
    public void msg(HttpServletRequest request, HttpServletResponse response) throws Exception {
        boolean isGet = request.getMethod().toLowerCase().equals("get");
        if (isGet) {
            logger.info("msg enter get");
            String retunmsg;
            // 微信加密签名
            String signature = request.getParameter("signature");
            // 时间戳
            String timestamp = request.getParameter("timestamp");
            // 随机数
            String nonce = request.getParameter("nonce");
            // 随机字符串
            String echostr = request.getParameter("echostr");

            if (signature == null || timestamp == null || nonce == null || echostr == null) {
                retunmsg = "参数不完整。";
            } else {
                // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
                if (SignUtil.checkSignature(signature, timestamp, nonce)) {
                    retunmsg = echostr;
                } else {
                    retunmsg = "验证失败。";
                }
            }
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            OutputStream stream = response.getOutputStream();
            stream.write(retunmsg.getBytes("UTF-8"));
            stream.close();
        } else {
            logger.info("msg enter post");
            try {
                // 接收消息并返回消息
                acceptPostMessage(request, response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 业务跳转接口
     *
     * @throws Exception
     */
    @RequestMapping(value = {"/redirectbizhtml"}, method = {org.springframework.web.bind.annotation.RequestMethod.GET})
    @ResponseBody
    public void redirectBizHtml()
            throws Exception {
        String code = request.getParameter("code");
        String funccode = request.getParameter("funccode");
        logger.info("code:" + code + ";funccode:" + funccode);
        JSONObject weixinuserinfo = weixinUserService.getWeixinUserInfoByCode(weChatConfig.getItem(), code);
        if (weixinuserinfo != null) {
            logger.info("微信用户信息：" + weixinuserinfo.toJSONString());
            String openId = weixinuserinfo.getString("openid");
            Map<String, Object> usermap = weixinUserPst.getUserIdByOpenId(openId);
            if (usermap == null) {
                redirectBindHtml(openId, funccode);
            } else {
                logger.info("用户信息：" + usermap.toString());
                String userId = String.valueOf(usermap.get("userid"));
                if (StringUtils.isEmpty(userId)) {
                    redirectBindHtml(openId, funccode);
                } else {
                    redirectBizHtml(openId, userId, funccode);
                }
            }
        }
    }

    private void redirectBizHtml(String openId, String userId, String funcode) throws Exception {
        String url = hostConfig.getUrl() + "/weixin/index.html?to=" + funcode + "&userId=" + userId + "&openId=" + openId;
        logger.info("跳转地址：" + url);
        response.sendRedirect(url);
    }

    private void redirectBindHtml(String openId, String funcode) throws Exception {
        String url = hostConfig.getUrl() + "/weixin/userLogin.html?to=" + funcode + "&openId=" + openId;
        logger.info("跳转绑定n地址：" + url);
        response.sendRedirect(url);
    }

    private void acceptPostMessage(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        // 处理接收消息
        ServletInputStream in = request.getInputStream();
        // 将POST流转换为XStream对象
        XStream xs = SerializeXmlUtil.createXstream();
        xs.processAnnotations(InputMessage.class);
        xs.processAnnotations(OutputMessage.class);
        // 将指定节点下的xml节点数据映射为对象
        xs.alias("xml", InputMessage.class);
        // 将流转换为字符串
        StringBuilder xmlMsg = new StringBuilder();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1; ) {
            xmlMsg.append(new String(b, 0, n, "UTF-8"));
        }

        // 将xml内容转换为InputMessage对象
        InputMessage inputMsg = (InputMessage) xs.fromXML(xmlMsg.toString());
        String servername = inputMsg.getToUserName();// 服务端
        String custermname = inputMsg.getFromUserName();// 客户端
        long createTime = inputMsg.getCreateTime();// 接收时间
        Long returnTime = Calendar.getInstance().getTimeInMillis() / 1000;// 返回时间

        // 取得消息类型
        String msgType = inputMsg.getMsgType();
        // 根据消息类型获取对应的消息内容
        if (msgType.equals(MsgType.Event.toString())) {
            // 事件消息
            String event = inputMsg.getEvent();
            String eventkey = inputMsg.getEventKey();
            String myopenid = inputMsg.getFromUserName();
            logger.info("开发者微信号：" + inputMsg.getToUserName());
            logger.info("发送方帐号：" + myopenid);
            logger.info("消息创建时间：" + inputMsg.getCreateTime() + new Date(createTime * 1000l));
            logger.info("事件编码：" + eventkey);
            String msg = "[空的消息]";
            StringBuffer str = new StringBuffer();
            if (event.trim().equals("subscribe")) {
                String superiorOpenId = "";
                String code = "";
                if (StringUtils.isNotEmpty(eventkey)) {
                    String[] strs = eventkey.split(";");
                    superiorOpenId = strs[2];
                    code = strs[1];
                }
                msg = "共享时代，您想成为" + weChatConfig.getBrand() + "的合伙人吗？赶紧点击下方邀请好友吧！";
                userService.reg(superiorOpenId, myopenid);
                str = createTxtMsg(custermname, servername, returnTime, "text", msg);
            } else if (event.trim().equals("CLICK")) {
                if (StringUtils.isNotEmpty(eventkey)) {
//                    String[] strs = eventkey.split("_");
//                    String param = strs[1];
                    if (StringUtils.equals(eventkey, "YQHY")) {
                        //调用获取二维码素材id
                        msg = qrcodeService.getMaterialMediaId(myopenid);
                        str = createMediaMsg(custermname, servername, returnTime, "image", "Image", "MediaId", msg);
                    }
                }
            } else if (event.trim().equals("VIEW")) {

            }
            response.setHeader("Content-type", "text/xml;charset=UTF-8");
            OutputStream stream = response.getOutputStream();
            stream.write(str.toString().getBytes("UTF-8"));
            stream.flush();
            stream.close();
        }
    }

    public StringBuffer createTxtMsg(String toUserName, String fromUserName, Long createTime, String msgType, String content) throws Exception {
        StringBuffer str = new StringBuffer();
        str.append("<xml>");
        str.append("<ToUserName><![CDATA[" + toUserName + "]]></ToUserName>");
        str.append("<FromUserName><![CDATA[" + fromUserName + "]]></FromUserName>");
        str.append("<CreateTime>" + createTime + "</CreateTime>");
        str.append("<MsgType><![CDATA[" + msgType + "]]></MsgType>");
        str.append("<Content><![CDATA[" + content + "]]></Content>");
        str.append("</xml>");
        logger.info(str.toString());
        return str;
    }

    public StringBuffer createMediaMsg(String toUserName, String fromUserName, Long createTime, String msgType, String MediaTag, String MediaType, String content) throws Exception {
        StringBuffer str = new StringBuffer();
        str.append("<xml>");
        str.append("<ToUserName><![CDATA[" + toUserName + "]]></ToUserName>");
        str.append("<FromUserName><![CDATA[" + fromUserName + "]]></FromUserName>");
        str.append("<CreateTime>" + createTime + "</CreateTime>");
        str.append("<MsgType><![CDATA[" + msgType + "]]></MsgType>");
        str.append("<" + MediaTag + "><" + MediaType + "><![CDATA[" + content + "]]></" + MediaType + "></" + MediaTag + ">");
        str.append("</xml>");
        logger.info(str.toString());
        return str;
    }

    //公众平台接收认证的接口
    @RequestMapping(value = "/auth", method = {RequestMethod.GET, RequestMethod.POST})
    public void auth(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.info("auth enter post");
        try {
            String retunmsg = "success";
            // 微信加密签名
            String signature = request.getParameter("signature");
            // 时间戳
            String timestamp = request.getParameter("timestamp");
            // 随机数
            String nonce = request.getParameter("nonce");
            // msgSignature
            String msgSignature = request.getParameter("msg_signature");

            // 处理接收消息
            ServletInputStream in = request.getInputStream();
            // 将流转换为字符串
            StringBuilder xmlMsg = new StringBuilder();
            byte[] b = new byte[4096];
            for (int n; (n = in.read(b)) != -1; ) {
                xmlMsg.append(new String(b, 0, n, "UTF-8"));
            }
            // 将xml内容转换为InputMessage对象
            String fromXML = xmlMsg.toString();
            logger.info("auth msg:\r\n" + fromXML);

            if (signature == null || timestamp == null || nonce == null) {
                retunmsg = "参数不完整。";
            } else {//校验access_token是否过期，过期则刷新
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    String item = weChatConfig.getItem();
                    logger.info("item:" + item);
                    //获取数据库保存公众号各配置项
                    Map<String, Object> weiXinConfigMap = weixinConfigService.getWeixinConfig(item);//由于只有这一个公众号，所以写死
                    String token = String.valueOf(weiXinConfigMap.get("token"));
                    String encodingAesKey = String.valueOf(weiXinConfigMap.get("aeskey"));
                    String appId = String.valueOf(weiXinConfigMap.get("appid"));
                    String appSecret = String.valueOf(weiXinConfigMap.get("appsecret"));
                    String accessToken = String.valueOf(weiXinConfigMap.get("accesstoken"));
                    String accessTokenTime = String.valueOf(weiXinConfigMap.get("accesstokentime"));
                    //获取推送component_verify_ticket
                    WXBizMsgCrypt pc = new WXBizMsgCrypt(token, encodingAesKey, appId);
                    String resultXml = pc.decryptMsg(msgSignature, timestamp, nonce, fromXML);
                    logger.info("解密后明文: " + resultXml);
                    String ticket = Utility.GetXmlNodeValue(resultXml, "/xml/ComponentVerifyTicket");
                    logger.info("ticket: " + ticket);
                    //获取第三方平台component_access_token，先检查是否快过期
                    if (StringUtils.isNotEmpty(accessToken)) {
                        Date component_access_token_date = sdf.parse(accessTokenTime);
                        //若过期
                        if (new Date().getTime() - component_access_token_date.getTime() > 90 * 60 * 1000) {
                            accessToken = refreshAccessToken(appId, appSecret, ticket);
                        } else {
                            logger.info("component_access_token未过期: " + accessToken);
                        }
                    } else {
                        accessToken = refreshAccessToken(appId, appSecret, ticket);
                    }
                } catch (Exception ex) {
                    //retunmsg=ex.getMessage();
                    ex.printStackTrace();
                }
            }
            response.setHeader("Content-type", "text/plain;charset=UTF-8");
            OutputStream stream = response.getOutputStream();
            stream.write(retunmsg.getBytes("UTF-8"));
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String refreshAccessToken(String appId, String appSecret, String ticket) throws Exception {
        String caturl = "https://api.weixin.qq.com/cgi-bin/component/api_component_token";
        JSONObject catreqjo = new JSONObject();
        catreqjo.put("component_appid", appId);
        catreqjo.put("component_appsecret", appSecret);
        catreqjo.put("component_verify_ticket", ticket);
        JSONObject catresjo = Utility.HttpPostJSON(caturl, catreqjo.toJSONString());
        String component_access_token = catresjo.getString("component_access_token");
        //保存token
        weixinConfigService.updateAccessToken("goldlady", component_access_token, new Date());
        logger.info("刷新后access_token:" + component_access_token);
        return component_access_token;
    }
}