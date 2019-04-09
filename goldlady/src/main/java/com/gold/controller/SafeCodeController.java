package com.gold.controller;

import com.gold.common.Utility;
import com.gold.config.JgMsgConfig;
import com.gold.service.SafeCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by hsl on 2017/11/7.
 */
@Scope("prototype")
@Controller
@RequestMapping("/gold/safecode")
public class SafeCodeController extends DsbBaseController {

    @Autowired
    SafeCodeService safeCodeService;
    @Autowired
    JgMsgConfig jgMsgConfig;

    /**
     * 获取手机短信验证码(微信端绑定手机时调用)
     *
     * @param phone
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/getphonecode", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> getPhoneCode(@RequestParam String phone) throws Exception {
        safeCodeService.checkPhoneIfCanBindAndSend(phone);
        return getNoDataSuccessMap();
    }

    /**
     * 校验手机短信验证码
     *
     * @param phone
     * @param phoneCode
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/checkphonecode", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> checkPhoneCode(@RequestParam String phone, @RequestParam String phoneCode) throws Exception {
        safeCodeService.checkPhoneCode(phone, phoneCode);
        return getNoDataSuccessMap();
    }


    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/getmsgcode", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> getMsgCode(@RequestParam String phone) throws Exception {
        return safeCodeService.getMsgCode(phone);
    }

    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/checkmsgcode", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> checkMsgCode(@RequestParam String msgId, @RequestParam String code) throws Exception {
        return safeCodeService.checkJgMsgCode(msgId, code);
    }


}
