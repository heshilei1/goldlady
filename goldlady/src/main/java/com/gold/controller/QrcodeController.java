package com.gold.controller;

import com.gold.common.BizException;
import com.gold.common.BizReturnCode;
import com.gold.service.QrcodeService;
import com.gold.service.WeixinUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by zhengwei on 2017/11/7.
 */
@RequestMapping("gold/qrcode")
@Scope("prototype")
@Controller
public class QrcodeController extends DsbBaseController {
    @Autowired
    private WeixinUserService weixinUserService;

    @Autowired
    private QrcodeService qrcodeService;

    /**
     * 获取合成后的二维码图片
     *
     * @param userid
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping(value = "/get", method = {RequestMethod.POST, RequestMethod.GET})
    public @ResponseBody Map<String, Object> getqcode(@RequestParam(value = "userid") String userid, HttpServletResponse response) throws Exception {
        //取openid
        Map<String, Object> user = weixinUserService.getOpenIdByUserid(userid);
        if(user == null){
            throw new BizException(BizReturnCode.UserNotExistError,"此用户不存在!");
        }
        qrcodeService.getMyQrcode(response, user.get("openid") + "");
        return getNoDataSuccessMap();
    }
}
