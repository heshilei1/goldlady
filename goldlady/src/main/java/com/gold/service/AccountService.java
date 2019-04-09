package com.gold.service;

import com.gold.common.BizException;
import com.gold.common.BizReturnCode;
import com.gold.pst.AccountPst;
import com.gold.pst.AccountStandingPst;
import com.gold.pst.TradePst;
import com.gold.pst.UserPst;
import org.apache.commons.lang.StringUtils;
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
 * Created by Administrator on 2017/11/18.
 */
@Service("accountService")
@Scope("prototype")
public class AccountService {

    @Autowired
    AccountPst accountPst;
    @Autowired
    AccountStandingPst accountStandingPst;
    @Autowired
    TradePst tradePst;
    @Autowired
    UserPst userPst;

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getAccountInfo(String userId) throws Exception {
        //获取账户现有余额
        Map<String, Object> accountMap = accountPst.getAccountInfo(userId);
        if (null == accountMap) {
            throw new BizException(BizReturnCode.UserNotExistError, "该用户不存在");
        }
        BigDecimal bigBalance = (BigDecimal) accountMap.get("balance");
        String balance = bigBalance.setScale(2, BigDecimal.ROUND_HALF_UP).toString();

        //获取已提现金额
        Map<String, Object> wTotalMap = tradePst.getUserWithdrawalsTotal(userId);
        BigDecimal bigTotal = (BigDecimal) wTotalMap.get("total");

        String total = "0.00";
        if (null != wTotalMap) {
            if (!StringUtils.equals(bigTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), "null") && StringUtils.isNotEmpty(bigTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString())) {
                total = bigTotal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            }
        }
        BigDecimal sumTotal = new BigDecimal(balance).add(new BigDecimal(total));
        Map<String, Object> result = new HashMap<>();
        result.put("sumtotal", sumTotal.toString());
        result.put("balance", balance);
        result.put("wtotal", total);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getAccountStanding(String userId) throws Exception {
        //校验用户是否存在
        checkUserIfExist(userId);
        List<Map<String, Object>> list = accountStandingPst.getAccountStandingByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void checkUserIfExist(String userId) throws Exception {
        Map<String, Object> userInfo = userPst.getUserInfo(userId);
        //检测到用户不存在
        if (userInfo == null) {
            throw new BizException(BizReturnCode.UserNotExistError, "该用户不存在");
        }
    }

}
