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
 * Created by hsl on 2017/11/26.
 */
@Repository("productPst")
@Scope("prototype")
public class ProductPst {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void add(String pluId, String pluName, BigDecimal price, String firstRate, String secondRate, String remark) throws Exception {
        String sql = "INSERT INTO product(pluid,pluname,price,firstrate,secondrate,remark) VALUES(?,?,?,?,?,?)";
        jdbcTemplate.update(sql, pluId, pluName, price, firstRate, secondRate, remark);
    }

    public List<Map<String, Object>> getAll() throws Exception {
        String sql = "SELECT pluid,pluname,price,firstrate,secondrate,remark,createtime,updatetime FROM product";
        return Utility.timestampToString(jdbcTemplate.queryForList(sql));
    }

    public void change(String pluId, String pluName, BigDecimal price, String firstRate, String secondRate, String remark) throws Exception {
        String sql = "UPDATE product SET pluname=?,price=?,firstrate=?,secondrate=?,remark=? WHERE pluId=?";
        jdbcTemplate.update(sql, pluName, price, firstRate, secondRate, remark, pluId);
    }

    public Map<String, Object> findById(String pluid) {
        String sql = "SELECT pluid,pluname,price,firstrate,secondrate,remark FROM product WHERE pluid=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql,pluid);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }
}
