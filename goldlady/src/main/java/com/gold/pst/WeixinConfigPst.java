package com.gold.pst;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by hsl on 2017/11/6.
 */
@Repository("weixinConfigPst")
@Scope("prototype")
public class WeixinConfigPst {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public Map<String, Object> getWeixinConfigByItem(String item) throws Exception {
        String sql = "SELECT item,appid,appsecret,aeskey,token,accesstoken,accesstokentime FROM weixinconfig WHERE item=?";
        List<Map<String, Object>> listmap = jdbcTemplate.queryForList(sql, item);
        if (listmap.size() <= 0) {
            return null;
        }
        return listmap.get(0);
    }

    public void updateAccessToken(String item, String token, Date tokenTime) throws Exception {
        String sql = "UPDATE weixinconfig SET accesstoken=?,accesstokentime=? WHERE item=?";
        int i = jdbcTemplate.update(sql, token, tokenTime, item);
        System.out.println(i);
    }
}
