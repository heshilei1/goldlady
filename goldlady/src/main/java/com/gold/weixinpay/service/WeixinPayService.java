package com.gold.weixinpay.service;

import com.github.wxpay.sdk.WXPay;
import com.gold.weixinpay.common.MyConfig;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 2017/11/10.
 */
@Service("weixinPayService")
@Scope("prototype")
public class WeixinPayService {

    public Map<String, Object> unifiedOrder() throws Exception {
        //保存本地订单
        //向微信发起统一下单请求
        return null;
    }

    /**
     * 微信统一下单
     *
     * @return
     * @throws Exception
     */
    public Map<String, String> weiXinUnifiedOrder() throws Exception {
        MyConfig config = new MyConfig();
        WXPay wxpay = new WXPay(config);

        Map<String, String> data = new HashMap<String, String>();
        data.put("body", "腾讯充值中心-QQ会员充值");
        data.put("out_trade_no", "2016090910595900000012");
        data.put("device_info", "");
        data.put("fee_type", "CNY");
        data.put("total_fee", "1");
        data.put("spbill_create_ip", "123.12.12.123");
        data.put("notify_url", "http://www.example.com/wxpay/notify");
        data.put("trade_type", "NATIVE");  // 此处指定为扫码支付
        data.put("product_id", "12");

        try {
            Map<String, String> resp = wxpay.unifiedOrder(data);
            return resp;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
