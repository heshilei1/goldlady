package com.gold.pst;

import com.gold.common.Utility;
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
@Repository("accountStandingPst")
@Scope("prototype")
public class AccountStandingPst {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void insert(String userid, BigDecimal percent,String type) {
        String sql = "INSERT INTO accountstanding(standingid,userid,total,type) VALUES(?,?,?,?)";
        jdbcTemplate.update(sql, Utility.generateId(), userid, percent,type);
    }

    public List<Map<String, Object>> getAccountStandingByUserId(String userId) throws Exception {
        String sql = "SELECT standingid,userid,type,total,createtime,updatetime FROM accountstanding WHERE userid=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId);
        if (list.size() <= 0) {
            return null;
        }
        return Utility.timestampToString(list);
    }
}
