package com.gold.controller;

import com.gold.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by zhengwei on 2017/11/17.
 */
@RequestMapping("gold/sale")
@RestController
public class SaleController extends DsbBaseController {

    @Autowired
    private SaleService saleService;

    /**
     * 录入消费信息（PC）
     *
     * @param adminid  管理员ID
     * @param userid   用户ID
     * @param nickname 用户昵称
     * @param total    消费金额
     * @param pluid    消费商品ID
     * @param remark   备注
     */
    @RequestMapping("savesaleinfo")
    public Map<String, Object> saveSaleInfo(String adminid, String userid, String nickname, BigDecimal total, String pluid, String remark) throws Exception {
        saleService.saveSaleInfo(adminid,userid,nickname,total,pluid,remark);
        return getNoDataSuccessMap();
    }

    /**
     * 管理员获取消费明细（PC）
     *
     * @param userid 管理员ID
     * @param phone 手机号
     * @param date 日期
     * @return
     */
    @RequestMapping("getall")
    public Map<String, Object> getexpenseDetail(@RequestParam String userid,@RequestParam(required = false) String phone,
                                                @RequestParam(required = false) String date) throws Exception {
        return saleService.getexpenseDetail(userid,phone,date);
    }
}
