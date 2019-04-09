package com.gold.common;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by Administrator on 2017/11/7.
 */
public class ShortMessagePlatForm {
    private final static Logger logger = LoggerFactory.getLogger(ShortMessagePlatForm.class);

    //奖励上级短信模板
////    public static String rewardSuperiorMsg = "【金夫人】您的好友在金夫人消费%s元，您获得%s元美丽基金，请在公众号推广中心进行提现操作。";
//    public static String rewardSuperiorMsg = "【金夫人】您获得%s元美丽基金，请在公众号推广中心进行提现操作。";
//    public static String withdrawalsApplyMsg = "【金夫人】您有新的提现申请，请登录网页进行处理。";
//    public static String AnswerRemind = "【金夫人】您咨询的问题有了新的回答，请在公众号个人中心进行查看。";
//    public static String RemindSuperAdmin = "【金夫人】%s在店消费了%s，共消费%s元。";

    public static String rewardSuperiorMsg = "您获得%s元美丽基金，请在公众号推广中心进行查看。";
    public static String withdrawalsApplyMsg = "您有新的提现申请，请登录网页进行处理。";
    public static String AnswerRemind = "您咨询的问题有了新的回答，请在公众号个人中心进行查看。";
    public static String RemindSuperAdmin = "%s在店消费了%s，共消费%s元。";

    public static void send(String mobile, String content) throws Exception {

        try {
            String url = "http://101.200.29.88:8082/SendMT/SendMessage";
            String UserName = "jinfuren";
            String UserPass = "nubt1v";
            String parameter = "CorpID=" + UserName + "&Pwd=" + UserPass + "&Content=" + content + "&Mobile=" + mobile;
            logger.info(url + "?" + parameter);
            String returndata = httpPostSend(url, parameter, "UTF-8");
            logger.info(returndata);
            returndata = returndata.substring(0, 2);
            if (StringUtils.equals(returndata, "02")) {
                throw new BizException(BizReturnCode.SendShortMessageError, "调用第三方短信平台错误，IP限制");
            } else if (StringUtils.equals(returndata, "04")) {
                throw new BizException(BizReturnCode.SendShortMessageError, "调用第三方短信平台错误，用户名错误");
            } else if (StringUtils.equals(returndata, "05")) {
                throw new BizException(BizReturnCode.SendShortMessageError, "调用第三方短信平台错误，密码错误");
            } else if (StringUtils.equals(returndata, "07")) {
                throw new BizException(BizReturnCode.SendShortMessageError, "调用第三方短信平台错误，发送时间有误");
            } else if (StringUtils.equals(returndata, "08")) {
                throw new BizException(BizReturnCode.SendShortMessageError, "调用第三方短信平台错误，内容有误");
            } else if (StringUtils.equals(returndata, "09")) {
                throw new BizException(BizReturnCode.SendShortMessageError, "调用第三方短信平台错误，手机号码有误");
            } else if (StringUtils.equals(returndata, "11")) {
                throw new BizException(BizReturnCode.SendShortMessageError, "调用第三方短信平台错误，余额不足");
            } else if (StringUtils.equals(returndata, "-1")) {
                throw new BizException(BizReturnCode.SendShortMessageError, "服务器内部异常");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static String httpPostSend(String sendUrl, String parameter, String encoded) throws
            SocketTimeoutException, Exception {
        String urlPath = sendUrl;
        StringBuffer sbf = new StringBuffer();
        BufferedWriter writer = null;
        BufferedReader reader = null;
        HttpURLConnection uc = null;
        try {
            URL url = new URL(urlPath);
            uc = (HttpURLConnection) url.openConnection();
            uc.setConnectTimeout(30000);
            uc.setReadTimeout(30000);
            uc.setRequestMethod("POST");
            uc.setDoOutput(true);
            uc.setDoInput(true);

            writer = new BufferedWriter(new OutputStreamWriter(uc.getOutputStream(), encoded)); // 向服务器传送数据
            writer.write(parameter);
            writer.flush();
            writer.close();
            reader = new BufferedReader(new InputStreamReader(uc.getInputStream(), encoded));// 读取服务器响应信息
            String line;

            while ((line = reader.readLine()) != null) {
                sbf.append(line);
            }
            reader.close();
            uc.disconnect();
        } catch (SocketTimeoutException e) {
            throw new SocketTimeoutException();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("无法连接服务器");
        } finally {
            closeIO(writer, reader);
        }
        return sbf.toString().trim();
    }

    private static void closeIO(BufferedWriter writer, BufferedReader reader) {
        if (writer != null) {
            try {
                writer.close();
                writer = null;
            } catch (Exception e) {

            }
        }
        if (reader != null) {
            try {
                reader.close();
                reader = null;
            } catch (Exception e) {

            }
        }

    }


}
