package com.gold.controller;

import com.gold.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by zhengwei on 2017/11/14.
 */
@RequestMapping("gold/withdrawals")
@RestController
public class TradeController extends DsbBaseController{

    @Autowired
    private TradeService tradeService;

    /**
     * 申请提现
     *
     * @param userid
     * @param username 真实姓名
     * @param phone
     * @param wechatno 微信号
     * @throws Exception
     */
    @CrossOrigin
    @RequestMapping("apply")
    public Map<String, Object> withdrawals(@RequestParam String userid, @RequestParam String username,
                                           @RequestParam String phone,@RequestParam String wechatno ) throws Exception {
        tradeService.withdrawals(userid,username,phone,wechatno);
        return getNoDataSuccessMap();
    }

    /**
     * 获取管理员待处理提现数量（PC）
     *
     * @param userid 管理员ID
     * @return
     */
    @RequestMapping("getnotdealcount")
    public Map<String, Object> getNotDealCount(String userid) throws Exception {
        return tradeService.getNotDealCount(userid);
    }

    /**
     * 获取提现记录
     *
     * @param userid
     * @return
     * @throws Exception
     */
    @RequestMapping("getall")
    public Map<String, Object> getWithDrawlsRecord(@RequestParam String userid) throws Exception {
        return tradeService.getWithDrawlsRecord(userid);
    }

    /**
     * 管理员处理体现
     *
     * @param userid
     * @param withdrawalsid
     * @return
     * @throws Exception
     */
    @RequestMapping("handle")
    public Map<String, Object> handleWithDrawls(@RequestParam String userid,@RequestParam String withdrawalsid) throws Exception {
        tradeService.handleWithDrawls(userid, withdrawalsid);
        return getNoDataSuccessMap();
    }
}
