package com.gold.pst;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.util.FileCopyUtils;

import javax.sql.RowSet;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/4.
 */
@Repository("checkPst")
@Scope("prototype")
public class CheckPst {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public Map<String, Object> checkWxIfBind(String openId)
            throws SQLException {
        String sql = "SELECT a.openid,a.userid,b.username,a.phone,a.name,a.createtime,a.updatetime FROM employeewxuser a,user b WHERE a.userid=b.userid AND a.openid=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, openId);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public String getUserType(String userId) throws SQLException {
        String sql = "SELECT usertype FROM user WHERE userid=?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, userId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (IncorrectResultSizeDataAccessException ex) {
            return null;
        }
    }

    public Map<String, Object> getUserInfoByPhone(String phone, String password)
            throws Exception {
        String sql = "SELECT userid,username,superiorid,isactive FROM user WHERE phone=? and password=?";
        try {
            return jdbcTemplate.queryForMap(sql, phone, password);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (IncorrectResultSizeDataAccessException ex) {
            return null;
        }
    }

    public void wxBind(String openId, String userId, String phone, String userName) throws Exception {
        String sql = "INSERT INTO employeewxuser(openid,userid,phone,name) VALUES(?,?,?,?)";
        jdbcTemplate.update(sql, openId, userId, phone, userName);
    }

    public void saveCheck(String checkId, String userId, String checkTime,
                          String checkRegion, String personLiable, String personliableid)
            throws Exception {
        String
                sql =
                "INSERT INTO `checkmaster`(checkid,userid,checktime,checkregion,personliable,personliableid) VALUES(?,?,?,?,?,?)";
        jdbcTemplate.update(sql, checkId, userId, checkTime, checkRegion, personLiable, personliableid);
    }

    public void saveCheckDetail(String checkId, String serialno,
                                String description, String image) throws Exception {
        String sql = "INSERT INTO checkdetail(checkid,serialno,description,image) VALUES(?,?,?,?)";
        jdbcTemplate.update(sql, checkId, serialno, description, image);
    }

    public List<Map<String, Object>> getCheck(String userId) throws Exception {
        String
                sql =
                "SELECT checkid,userid,checktime,checkregion,personliable,status,createtime,updatetime FROM `checkmaster` WHERE userid=? ORDER BY createtime DESC";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId);
        return list;
    }

    public List<Map<String, Object>> getCheckDetail(String checkId)
            throws Exception {
        String
                sql =
                "SELECT checkid,serialno,description,image,createtime,updatetime FROM checkdetail WHERE checkid=? ORDER BY serialno ASC";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, checkId);
        return list;
    }

    public void getCheckDetailImage(String checkId, String serialno, final OutputStream os)
            throws Exception {
        String sql = "SELECT image FROM checkdetail WHERE checkid=? AND serialno=?";
        final LobHandler lobHandler = new DefaultLobHandler();  // reusable object
        jdbcTemplate.query(
                sql, new Object[]{checkId, serialno},
                new AbstractLobStreamingResultSetExtractor() {
                    public void streamData(ResultSet rs) throws SQLException, IOException {
                        FileCopyUtils.copy(lobHandler.getBlobAsBinaryStream(rs, 1), os);
                    }
                }
        );
    }

    public List<Map<String, Object>> getCheckRegion()
            throws Exception {
        String
                sql =
                "SELECT userid,username,shopid,shopname,region,createtime,updatetime FROM checkregion";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        return list;
    }

    public void saveMiniPic(String miniPicId, byte[] pic) throws Exception {
        String sql = "INSERT INTO minipic(minipicid,pic) VALUES(?,?)";
        jdbcTemplate.update(sql, miniPicId, pic);
    }

    public void getPic(String minipicid, final OutputStream os)
            throws Exception {
        String sql = "SELECT pic FROM minipic WHERE minipicid=?";
        final LobHandler lobHandler = new DefaultLobHandler();  // reusable object
        jdbcTemplate.query(
                sql, new Object[]{minipicid},
                new AbstractLobStreamingResultSetExtractor() {
                    public void streamData(ResultSet rs) throws SQLException, IOException {
                        FileCopyUtils.copy(lobHandler.getBlobAsBinaryStream(rs, 1), os);
                    }
                }
        );
    }

    public List<Map<String, Object>> getCheckByRegion(String personliableid)
            throws Exception {
        String
                sql =
                "SELECT checkid,userid,checktime,checkregion,personliable,status,createtime,updatetime FROM `checkmaster` WHERE personliableid=? ORDER BY createtime DESC";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, personliableid);
        return list;
    }

    public Map<String, Object> getCheckByCheckId(String checkId)
            throws Exception {
        String sql = "SELECT checkid,status FROM `checkmaster` WHERE checkid=?";
        try {
            return jdbcTemplate.queryForMap(sql, checkId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (IncorrectResultSizeDataAccessException ex) {
            return null;
        }
    }

    public void finishCheck(String checkId)
            throws SQLException {
        String sql = "UPDATE `checkmaster` SET status='1' WHERE checkid=?";
        jdbcTemplate.update(sql, checkId);
    }
}
