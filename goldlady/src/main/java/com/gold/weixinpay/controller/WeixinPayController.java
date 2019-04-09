package com.gold.weixinpay.controller;

import com.gold.controller.DsbBaseController;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by hsl on 2017/11/10.
 */
@Scope("prototype")
@Controller
@RequestMapping("/gold/weixin/pay")
public class WeixinPayController extends DsbBaseController{


    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/unifiedorder", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> unifiedOrder(@RequestParam String superiorId, @RequestParam String code) throws Exception {


        return getNoDataSuccessMap();
    }
}
