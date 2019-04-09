package com.gold.pst;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("standingPst")
@Scope("prototype")
public class StandingPst {
    @Autowired
    JdbcTemplate jdbcTemplate;

    //    查询美丽基金台帐记录
    public List<Map<String, Object>> findStandingRecord(String phone,String date,String start,String end) throws Exception {
        String sql = "SELECT \n" +
                "    a.standingid,\n" +
                "    a.userid,\n" +
                "    b.username,\n" +
                "    b.phone,\n" +
                "    c.nickname,\n" +
                "    a.type,\n" +
                "    a.total,\n" +
                "    DATE_FORMAT(a.createtime,'%Y-%m-%d %H:%i:%s') as createtime \n" +
                "FROM\n" +
                "    accountstanding AS a\n" +
                "        LEFT JOIN\n" +
                "    user AS b ON a.userid = b.userid\n" +
                "        LEFT JOIN\n" +
                "    weixinuser AS c ON a.userid = c.userid WHERE 1=1";
        try {
            if (StringUtils.isNotEmpty(phone)){
                sql+=" AND b.phone LIKE '%"+phone+"'";
            }
            if (StringUtils.isNotEmpty(date)){
                sql += " AND a.createtime like '%" + date + "%'";
            }
            if (StringUtils.isNotEmpty(start) && StringUtils.isNotEmpty(end)) {
                sql += " AND a.createtime BETWEEN '" + start + "' AND DATE_ADD('" + end + "',INTERVAL 1 DAY)";
            }
            sql += " ORDER BY createtime DESC";
            return jdbcTemplate.queryForList(sql);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (IncorrectResultSizeDataAccessException ex) {
            return null;
        }
    }
}
