package com.gold.service;

import com.gold.common.BizException;
import com.gold.common.BizReturnCode;
import com.gold.pst.SalePst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengwei on 2017/11/17.
 */
@Service("saleService")
@Scope("prototype")
public class SaleService {

    @Autowired
    private SalePst salePst;
    @Autowired
    private UserService userService;
    @Autowired
    private ProductService productService;

    /**
     * 查出所有消费记录
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public List<Map<String, Object>> getAllExpenseDetail() {
        return salePst.findAll();
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public String getSumSaleTotal() throws Exception {
        String result = "0.00";
        Map<String, Object> map = salePst.getSumSaleTotal();
        if (null == map) {
            return result;
        }
        BigDecimal bigTotal = (BigDecimal) map.get("total");
        return bigTotal.setScale(2,BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 录入消费信息（PC）
     * @param adminid
     * @param userid
     * @param nickname
     * @param total
     * @param pluid
     * @param remark
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void saveSaleInfo(String adminid, String userid, String nickname, BigDecimal total, String pluid, String remark) throws Exception {
        boolean isAdmin = userService.checkIsAdmin(adminid);
        if (isAdmin) {
            Map<String, Object> product = productService.findById(pluid);
            Map<String, Object> user = userService.saveSaleValidUser(userid);
            if (user != null) {
                try {
                    //保存消费信息,向消费者相关上级用户分发奖励金额（只分发两级）同时记录账户台账,短信通知
                    userService.rewardSuperiorUsers(adminid,userid, nickname, total, product, remark);
                } catch (Exception e) {
                    throw new BizException(BizReturnCode.InputConsumeInfoFailedError, "录入消费信息失败,请联系管理员!");
                }
            } else {
                throw new BizException(BizReturnCode.UserNotExistError, "用户不存在错误");
            }
        } else {
            throw new BizException(BizReturnCode.UserLevelLow, "用户权限不足");
        }
    }

    /**
     * 管理员获取消费明细（PC）
     * @param userid
     * @param phone
     * @param date
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String,Object> getexpenseDetail(String userid, String phone, String date) throws Exception {
        Map<String,Object> returnData = new HashMap<>();
        boolean isAdmin = userService.checkIsAdmin(userid);
        if (isAdmin) {
            List<Map<String, Object>> expenseDetail = userService.getexpenseDetail(phone,date);
            returnData.put("expenseDetail",expenseDetail);
            return returnData;
        } else {
            throw new BizException(BizReturnCode.UserLevelLow,"用户权限不足");
        }
    }
}
