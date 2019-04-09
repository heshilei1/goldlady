package com.gold.pst;

import com.gold.common.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengwei on 2017/11/17.
 */
@Repository("salePst")
@Scope("prototype")
public class SalePst {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void insert(String adminid,String userid, BigDecimal total, String type, String remark) throws Exception {
        String sql = "INSERT INTO sale(billno,userid,total,type,remark,adminid) VALUES(?,?,?,?,?,?)";
        jdbcTemplate.update(sql, Utility.generateId(), userid, total, type, remark,adminid);
    }

    public List<Map<String, Object>> findAll() {
        String sql = "SELECT nickname,phone,total,type,DATE_FORMAT(createtime,'%Y-%m-%d %H:%i:%s') createtime,remark FROM saledetail ORDER BY createtime DESC";
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> getSumSaleTotal() throws Exception {
        String sql = "SELECT SUM(total) AS total FROM sale";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    public Map<String, Object> getSumSaleTotalByUserId(String userId) throws Exception {
        String sql = "SELECT ifnull(SUM(total),0) as total FROM sale WHERE userid=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }
}
