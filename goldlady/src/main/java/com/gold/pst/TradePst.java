package com.gold.pst;

import com.gold.model.WithdrawalsStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengwei on 2017/11/14.
 */
@Repository("tradePst")
@Scope("prototype")
public class TradePst {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public void insertWithdrawals(String userid, String username,String total, String withdrawalsId,String phone,String wechatno) {
        String sql = "INSERT INTO withdrawals(userid, username,total, withdrawalsId,phone,wechatno,status) VALUES(?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, userid, username,total, withdrawalsId,phone,wechatno, WithdrawalsStatus.ING);
    }

    public Map<String, Object> getUserWithdrawalsTotal(String userId) throws Exception {
        String sql = "SELECT ifnull(SUM(total),0) as total FROM withdrawals WHERE status=? and userid=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, WithdrawalsStatus.END, userId);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    public Integer findByStatus(String status) {
        String sql = "SELECT count(withdrawalsid) FROM withdrawals WHERE status="+status;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public List<Map<String, Object>> findAll() {
        String sql = "SELECT withdrawalsid,userid,total,status,username,phone,wechatno,DATE_FORMAT(createtime,'%Y-%m-%d %H:%i:%s') createtime FROM withdrawals";
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> findById(String withdrawalsid) {
        String sql = "SELECT withdrawalsid,userid,total,status,username,phone,wechatno,DATE_FORMAT(createtime,'%Y-%m-%d %H:%i:%s') createtime FROM withdrawals WHERE withdrawalsid=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql,withdrawalsid);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    public void update(String withdrawalsid, String end) {
        String sql = "UPDATE withdrawals SET status=?,updatetime=? WHERE withdrawalsid=?";
        jdbcTemplate.update(sql, end,new Date(), withdrawalsid);
    }

    public List<Map<String, Object>> findStatusByUserid(String userid) {
        String sql = "SELECT withdrawalsid,userid,total,status,username,phone,wechatno,DATE_FORMAT(createtime,'%Y-%m-%d %H:%i:%s') createtime FROM withdrawals WHERE userid=?";
        return jdbcTemplate.queryForList(sql,userid);
    }
}
