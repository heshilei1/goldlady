package com.gold.service;

import com.gold.common.BizException;
import com.gold.common.BizReturnCode;
import com.gold.common.ShortMessagePlatForm;
import com.gold.common.Utility;
import com.gold.config.MsgConfig;
import com.gold.model.WithdrawalsStatus;
import com.gold.pst.AccountPst;
import com.gold.pst.TradePst;
import com.gold.pst.UserPst;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
 * Created by zhengwei on 2017/11/14.
 */
@Service("tradeService")
@Scope("prototype")
public class TradeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeService.class);

    @Autowired
    private TradePst tradePst;
    @Autowired
    AccountPst accountPst;

    @Autowired
    private UserPst userPst;
    @Autowired
    private UserService userService;
    @Autowired
    MsgConfig msgConfig;

    /**
     * 申请提现
     *
     * @param userid
     * @param username
     * @param withdrawalsId
     * @param phone
     * @param wechatno
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void applyForWithdrawal(String userid, String username, String withdrawalsId, String phone, String wechatno) throws Exception {
        Map<String, Object> accountMap = accountPst.getAccountInfo(userid);
        String balance = String.valueOf(accountMap.get("balance"));
        LOGGER.info("申请提现:用户[{}][{}] 金额[{}元]", username, userid, balance);
        tradePst.insertWithdrawals(userid, username, balance, withdrawalsId, phone, wechatno);
        //短信通知管理员
        if (msgConfig.getIfopen()) {
            Map<String, Object> admin = userPst.findAdmin();
            if (StringUtils.isNotEmpty((String) admin.get("phone"))) {
                LOGGER.info("申请提现:短信通知管理员");
                try {
                    ShortMessagePlatForm.send((String) admin.get("phone"), msgConfig.getLabel() + ShortMessagePlatForm.withdrawalsApplyMsg);
                } catch (Exception e) {
                    LOGGER.info("申请提现:短信通知管理员失败");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取管理员待处理提现数量（PC）
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Integer getNotDealCount() {
        return tradePst.findByStatus(WithdrawalsStatus.ING);
    }


    /**
     * 获取提现记录
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public List<Map<String, Object>> findAll() {
        return tradePst.findAll();
    }

    /**
     * 管理员处理体现
     *
     * @param userid
     * @param withdrawalsid
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void handleWithDrawls(String userid, String withdrawalsid) throws Exception {
        boolean isAdmin = userService.checkIsAdmin(userid);
        if (isAdmin) {
            LOGGER.info("处理提现,订单编号[{}]", withdrawalsid);
            try {
                Map<String, Object> withdrawals = tradePst.findById(withdrawalsid);
                if (!WithdrawalsStatus.ING.equals((String) withdrawals.get("status"))) {
                    LOGGER.info("提现失败!订单已经提现,不能重复提现!");
                    throw new BizException(BizReturnCode.OrderIsHandled, "该订单已提现,不能重复提现");
                }
                Map<String, Object> account = accountPst.getAccountInfo((String) withdrawals.get("userid"));
                BigDecimal total = (BigDecimal) withdrawals.get("total");
                BigDecimal balance = (BigDecimal) account.get("balance");
                if (balance.compareTo(total) == 1 || balance.compareTo(total) == 0) {
                    tradePst.update((String) withdrawals.get("withdrawalsid"), WithdrawalsStatus.END);
                    LOGGER.info("账户扣款,用户编号[{}],扣款金额[{}元],账户余额[{}元]", withdrawals.get("userid"), total, balance.subtract(total));
                    accountPst.update(balance.subtract(total), (String) withdrawals.get("userid"));
                } else {
                    throw new BizException(BizReturnCode.BalanceNotEnoughToWithdrawls, "余额不足以提现");
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new BizException(BizReturnCode.HandleWithdrawlsError, "处理提现失败");
            }
        } else {
            throw new BizException(BizReturnCode.UserLevelLow, "用户权限不足");
        }
    }

    /**
     * 获取提现记录
     *
     * @param userid
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getWithDrawlsRecord(String userid) throws Exception {
        Map<String, Object> returnData = new HashMap<>();
        boolean isAdmin = userService.checkIsAdmin(userid);
        if (isAdmin) {
            List<Map<String, Object>> record = this.findAll();
            returnData.put("withDrawlsRecord", record);
            return returnData;
        } else {
            throw new BizException(BizReturnCode.UserLevelLow, "用户权限不足");
        }
    }

    /**
     * 申请提现
     *
     * @param userid
     * @param username
     * @param phone
     * @param wechatno
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void withdrawals(String userid, String username, String phone, String wechatno) throws Exception {
        int isAllow = userService.isUserAllowWithdrawals(userid);
        switch (isAllow) {
            case 0:
                try {
                    //生成体现申请
                    this.applyForWithdrawal(userid, username, Utility.generateId(), phone, wechatno);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new BizException(BizReturnCode.WithdrawlsError, "提现失败");
                }
                break;
            case 1:
                throw new BizException(BizReturnCode.UserNotExistError, "用户不存在错误");
            case 2:
                throw new BizException(BizReturnCode.ThereArePendingApplicationsForWithdrawal, "您有未处理的提现申请，请等待处理完成后再进行提现");
            default:
                throw new BizException(BizReturnCode.WithdrawlsError, "提现失败");
        }
    }

    /**
     * 获取管理员待处理提现数量（PC）
     *
     * @param userid
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getNotDealCount(String userid) throws Exception {
        Map<String, Object> returnData = new HashMap<>();
        boolean isAllow = userService.checkIsAdmin(userid);
        if (isAllow) {
            Integer count = this.getNotDealCount();
            returnData.put("count", count);
            return returnData;
        } else {
            throw new BizException(BizReturnCode.UserLevelLow, "用户权限不足");
        }
    }
}
