package com.gold.pst;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by zhengwei on 2017/11/7.
 */
@Repository("weixinUserPst")
@Scope("prototype")
public class WeixinUserPst {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> getOpenIdByUserId(String userid)
            throws Exception {
        String sql = "SELECT openid FROM weixinuser WHERE userid=?";
        List<Map<String, Object>> list = this.jdbcTemplate.queryForList(sql, new Object[]{userid});
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    public Map<String, Object> getUserIdByOpenId(String openId)
            throws Exception {
        String sql = "SELECT userid FROM weixinuser WHERE openid=?";
        List<Map<String, Object>> list = this.jdbcTemplate.queryForList(sql, new Object[]{openId});
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

}
