package com.gold.service;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jsms.api.SendSMSResult;
import cn.jsms.api.ValidSMSResult;
import cn.jsms.api.common.SMSClient;
import cn.jsms.api.common.model.SMSPayload;
import com.gold.common.BizException;
import com.gold.common.BizReturnCode;
import com.gold.common.ShortMessagePlatForm;
import com.gold.common.Utility;
import com.gold.config.JgMsgConfig;
import com.gold.config.MsgConfig;
import com.gold.pst.UserPst;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by hsl on 2017/11/7.
 */
@Service("safeCodeService")
@Scope("prototype")
public class SafeCodeService {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    UserPst userPst;
    @Autowired
    MsgConfig msgConfig;
    @Autowired
    JgMsgConfig jgMsgConfig;


    public void getPhoneCode(String phone, String code) throws Exception {
//        //检查手机号是否注册过
//        if (userPst.checkPhoneIfRepeat(phone)) {
//            throw new BizException(BizReturnCode.UserPhoneRepeatError, "该手机号已注册过用户！");
//        }
        //检查该手机号是否获取过验证码
        if (userPst.checkIfGetCodeByPhone(phone)) {
            //更新
            userPst.updatePhoneCode(phone, code);
        } else {
            //新增
            userPst.insertPhoneCode(phone, code);
        }

        String content = msgConfig.getLabel() + "您的验证码是：" + code + ",10分钟内有效";
        //发送注册验证码
        ShortMessagePlatForm.send(phone, content);
    }


    public void checkPhoneCode(String phone, String checkCode) throws Exception {
        //获取数据库中短信码
        Map<String, Object> map = userPst.getPhoneCodeByPhone(phone);
        if (null == map) {
            throw new BizException(BizReturnCode.PhoneCodeError, "手机验证码错误");
        }
        String saveCode = (String) map.get("code");
        Date time = (Timestamp) map.get("time");
        long diff = new Date().getTime() - time.getTime();
        if (diff > 10 * 60 * 1000) {
            throw new BizException(BizReturnCode.PhoneCodeError, "手机验证码错误");
        }
        if (!StringUtils.equals(checkCode, saveCode)) {
            throw new BizException(BizReturnCode.PhoneCodeError, "手机验证码错误");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void checkPhoneIfCanBindAndSend(String phone) throws Exception {
        //校验手机号是否可以进行绑定
        checkPhoneIfCanBind(phone);
        //获取四位随机数
        String code = Utility.getRandom();
        logger.info("手机号：" + phone + ";验证码：" + code);
        //发送手机短信
        getPhoneCode(phone, code);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> checkPhoneIfCanBind(String phone) throws Exception {
        Map<String, Object> usermap = userPst.getUserSuperInfoByPhone(phone);
        if (usermap != null) {
            String userId = (String) usermap.get("userid");
            Map<String, Object> weixinusermap = userPst.getWeixinUserInfoByUserId(userId);
            if (weixinusermap != null) {
                //该手机号绑定过用户，且该用户已有微信信息，则返回错误
                throw new BizException(BizReturnCode.UserPhoneRepeatError, "该手机号已被绑定");
            }
            //该手机号绑定过用户，但该用户无微信信息（该用户为PC端添加）
            return usermap;
        } else {
            //该手机未绑定过手机
            return null;
        }
    }

    public Map<String, Object> sendJgMsg(String phone, String jgMsgTempletId) throws Exception {
        SMSClient client = new SMSClient(jgMsgConfig.getSecret(), jgMsgConfig.getAppkey());
        SMSPayload payload = SMSPayload.newBuilder()
                .setMobileNumber(phone)
                .setTempId(Integer.parseInt(jgMsgConfig.getSmgcodeid()))
                .build();
        Map<String, Object> result = new HashMap<>();
        try {
            SendSMSResult res = client.sendSMSCode(payload);
            result.put("msgId", res.getMessageId());
            logger.info("调用极光短信返回：" + res.toString());
        } catch (APIConnectionException e) {
            logger.error("Connection error. Should retry later.", e);
            throw e;
        } catch (APIRequestException e) {
            logger.error("Error response from JPush server. Should review and fix it. ", e);
            logger.info("HTTP Status: " + e.getStatus());
            logger.info("Error Message: " + e.getMessage());
            throw e;
        }
        return result;
    }

    public Map<String, Object> getMsgCode(String phone) throws Exception {
        //TODO 校验手机号是否可以发送短信验证码（极光已有超频控制，暂不添加）
        //发送短信
        return sendJgMsg(phone, jgMsgConfig.getSmgcodeid());
    }

    public Map<String, Object> checkJgMsgCode(String msgId, String code) throws Exception {
        SMSClient client = new SMSClient(jgMsgConfig.getSecret(), jgMsgConfig.getAppkey());
        Map<String, Object> result = new HashMap<>();
        Boolean valid = false;
        try {
            ValidSMSResult res = client.sendValidSMSCode(msgId, code);
            valid = res.getIsValid();
            logger.info(res.toString());
            result.put("valid", valid);
        } catch (APIConnectionException e) {
            e.printStackTrace();
            logger.error("Connection error. Should retry later. ", e);
            throw e;
        } catch (APIRequestException e) {
            e.printStackTrace();
            logger.error("Error response from JPush server. Should review and fix it. ", e);
            logger.info("HTTP Status: " + e.getStatus());
            logger.info("Error Message: " + e.getMessage());
            if (e.getErrorCode() == 50010 ){
                result.put("valid", valid);
                result.put("message", "验证码无效");
            }else if( e.getErrorCode() == 50011){
                result.put("valid", valid);
                result.put("message", "验证码过期");
            }else if(e.getErrorCode() == 50015){
                result.put("valid", valid);
                result.put("message", "验证码为空");
            }else if(e.getErrorCode() == 50026){
                result.put("valid", valid);
                result.put("message", "msg_id 无效");
            }else if(e.getErrorCode() == 50012) {
                result.put("valid", valid);
                result.put("message", "验证码已验证通过");
            } else {
                throw e;
            }
        }catch (Exception e){
            throw new BizException(BizReturnCode.JgMsgCodeError,"验证失败");
        }
        return result;
    }
}
