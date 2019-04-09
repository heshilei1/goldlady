package com.gold.pst;

import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengwei on 2017/11/17.
 */
@Repository("accountPst")
@Scope("prototype")
public class AccountPst {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void grantAward(String userid, BigDecimal percent) {
        String sql = "update account set balance = (balance + ?),updatetime=? where userid=?";
        jdbcTemplate.update(sql, percent, new Date(), userid);
    }

    public Map<String, Object> getAccountInfo(String userId) throws Exception {
        String sql = "SELECT accountid,userid,balance FROM account WHERE userid=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    public void update(BigDecimal money, String userid) {
        String sql = "update account set balance = ?,updatetime=? where userid=?";
        jdbcTemplate.update(sql, money, new Date(), userid);
    }

    public Map<String, Object> getBalanceByUserId(String userId) throws Exception {
        String sql = "SELECT ifnull(balance,0) as balance FROM account WHERE userid=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }
}
