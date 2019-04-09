package com.gold.pst;

import com.gold.common.Utility;
import com.gold.model.UserType;
import org.apache.catalina.User;
import org.apache.commons.lang.StringUtils;
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
@Repository("userPst")
@Scope("prototype")
public class UserPst {
    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * 获取此userid的一级客户
     *
     * @param userId
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> getGuest(String userId) throws Exception {
        String sql = "SELECT\n" +
                "user.userid,\n" +
                "user.superiorid,\n" +
                "DATE_FORMAT(user.createtime,'%Y-%m-%d %H:%i:%s') createtime,\n" +
                "a.openid,\n" +
                "ifnull(a.nickname,user.username) as nickname,\n" +
                "a.headimgurl,\n" +
                "b.nickname as superiorname\n" +
                "FROM\n" +
                "user\n" +
                "LEFT JOIN weixinuser a ON `user`.userid = a.userid\n" +
                "LEFT JOIN weixinuser b ON `user`.superiorid = b.userid\n" +
                "\n" +
                "WHERE user.superiorid = ? ORDER BY createtime";
        return jdbcTemplate.queryForList(sql, userId);
    }

    public Map<String, Object> validUser(String userId) throws Exception {
        String sql = "SELECT userid FROM user WHERE userid=?";
        List<Map<String, Object>> userList = jdbcTemplate.queryForList(sql, userId);
        if (userList.size() <= 0) {
            return null;
        }
        return userList.get(0);
    }

    public Map<String, Object> saveSaleValidUser(String userId) throws Exception {
        String sql = "SELECT userid FROM user WHERE userid=?";
        List<Map<String, Object>> userList = jdbcTemplate.queryForList(sql, userId);
        if (userList.size() <= 0) {
            return null;
        }
        return userList.get(0);
    }


    public void insertUser(String userId) throws Exception {
        String sql = "INSERT INTO user(userid) VALUES(?)";
        jdbcTemplate.update(sql, userId);
    }

    public void insertUser(String userId, String superiorId) throws Exception {
        String sql = "INSERT INTO user(userid,superiorid) VALUES(?,?)";
        jdbcTemplate.update(sql, userId, superiorId);
    }

    public void insertUser(String userId, String superiorId, String phone) throws Exception {
        String sql = "INSERT INTO user(userid,superiorid,phone) VALUES(?,?,?)";
        jdbcTemplate.update(sql, userId, superiorId, phone);
    }

    public void insertUser(String userId, String userName, String phone, String superiorId, String userType, String remark) throws Exception {
        String sql = "INSERT INTO user(userid,username,phone,superiorid,usertype,remark,isactive) VALUES(?,?,?,?,?,?,?)";
        String isactive = "0";
        jdbcTemplate.update(sql, userId, userName, phone, superiorId, userType, remark, isactive);
    }

    public void updateWeixinUser(String userId, String openId) throws Exception {
        String sql = "UPDATE weixinuser SET userid=? WHERE openid=?";
        jdbcTemplate.update(sql, userId, openId);
    }

    public void updateWeixinSuperUser(String superUserId, String openId) throws Exception {
        String sql = "UPDATE weixinuser SET superiorid=? WHERE openid=?";
        jdbcTemplate.update(sql, superUserId, openId);
    }

    public void updateWeixinSuperUserByUserId(String superUserId, String userId) throws Exception {
        String sql = "UPDATE weixinuser SET superiorid=? WHERE userid=?";
        jdbcTemplate.update(sql, superUserId, userId);
    }


    public void insertUser(String userId, String userName, String phone, String password, String userType) throws Exception {
        String sql = "INSERT INTO user(userid,username,phone,password,usertype) VALUES(?,?,?,?,?)";
        jdbcTemplate.update(sql, userId, userName, phone, password, userType);
    }

    public Map<String, Object> getUserInfo(String userId) throws Exception {
        String sql = "SELECT userid,username,superiorid,phone,usertype,isactive,createtime,updatetime FROM user WHERE userid=?";
        List<Map<String, Object>> userList = jdbcTemplate.queryForList(sql, userId);
        if (userList.size() <= 0) {
            return null;
        }
        return userList.get(0);
    }

    public Map<String, Object> getUserAndWxInfo(String userId) throws Exception {
        String sql = "SELECT a.userid,a.username,a.superiorid,a.phone,a.usertype,a.isactive,a.createtime,a.updatetime,b.nickname as nickname FROM user a LEFT JOIN weixinuser b ON a.userid=b.userid WHERE  a.userid=?";
        List<Map<String, Object>> userList = jdbcTemplate.queryForList(sql, userId);
        if (userList.size() <= 0) {
            return null;
        }
        return userList.get(0);
    }

    public Map<String, Object> getWeixinUserInfoByOpenId(String openId) throws Exception {
        String sql = "SELECT openid,userid,superiorid,nickname,sex,city,country,province,`language`,headimgurl FROM weixinuser WHERE openid=?";
        List<Map<String, Object>> userList = jdbcTemplate.queryForList(sql, openId);
        if (userList.size() <= 0) {
            return null;
        }
        return userList.get(0);
    }

    public void insertWeixinUser(String userId, String superiorid, String openId, String nickName, String sex, String city, String country, String province, String language, String headimgurl) throws Exception {
        String sql = "INSERT INTO weixinuser(openid,userid,superiorid,nickname,sex,city,country,province,`language`,headimgurl) VALUES(?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.update(sql, openId, userId, superiorid, nickName, sex, city, country, province, language, headimgurl);
    }

    public void insertUserAccount(String accountId, String userId) throws Exception {
        String sql = "INSERT INTO account(accountid,userid) VALUES(?,?)";
        jdbcTemplate.update(sql, accountId, userId);
    }

    public Boolean checkPhoneIfRepeat(String phone) throws Exception {
        Boolean result = true;
        String sql = "SELECT userid FROM user WHERE phone=? AND usertype=?";
        List<Map<String, Object>> userlist = jdbcTemplate.queryForList(sql, phone, UserType.CUSTOMER);
        if (userlist.size() <= 0) {
            result = false;
        }
        return result;
    }

    public Boolean checkAdminPhoneIfRepeat(String phone) throws Exception {
        Boolean result = true;
        String sql = "SELECT userid FROM user WHERE phone=? AND usertype<>?";
        List<Map<String, Object>> userlist = jdbcTemplate.queryForList(sql, phone, UserType.CUSTOMER);
        if (userlist.size() <= 0) {
            result = false;
        }
        return result;
    }

    public Boolean checkCustomerPhoneIfRepeat(String phone) throws Exception {
        Boolean result = true;
        String sql = "SELECT userid FROM user WHERE phone=? AND usertype=?";
        List<Map<String, Object>> userlist = jdbcTemplate.queryForList(sql, phone, UserType.CUSTOMER);
        if (userlist.size() <= 0) {
            result = false;
        }
        return result;
    }

    public Boolean checkIfGetCodeByPhone(String phone) throws Exception {
        String sql = "SELECT code FROM phonegetcode WHERE phone=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, phone);
        Boolean result = false;
        if (list.size() > 0) {
            result = true;
        }
        return result;
    }

    public Boolean checkIfUpdatePhone(String phone, String userId) throws Exception {
        Boolean result = true;
        String sql = "SELECT userid FROM user WHERE phone=? AND userid<>?";
        List<Map<String, Object>> userlist = jdbcTemplate.queryForList(sql, phone, userId);
        if (userlist.size() <= 0) {
            result = false;
        }
        return result;
    }

    public void insertPhoneCode(String phone, String code) throws Exception {
        Date now = new Date();
        String sql = "INSERT INTO phonegetcode(phone,code,time) VALUES(?,?,?)";
        jdbcTemplate.update(sql, phone, code, now);
    }

    public void updatePhoneCode(String phone, String code) throws Exception {
        Date now = new Date();
        String sql = "UPDATE phonegetcode SET code=?,time=? WHERE phone=?";
        jdbcTemplate.update(sql, code, now, phone);
    }

    public Map<String, Object> getPhoneCodeByPhone(String phone) throws Exception {
        String sql = "SELECT code,time FROM phonegetcode WHERE phone=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, phone);
        Map<String, Object> map = null;
        if (list.size() > 0) {
            map = list.get(0);
        }
        return map;
    }

    public Map<String, Object> checkUserInfo(String phone, String password) throws Exception {
        String sql = "SELECT userid,superiorid,phone,usertype,isactive,DATE_FORMAT(createtime,'%Y-%m-%d %H:%i:%s') createtime,DATE_FORMAT(updatetime,'%Y-%m-%d %H:%i:%s') updatetime FROM user WHERE phone=? AND password=? AND isactive = 1";
        List<Map<String, Object>> userList = jdbcTemplate.queryForList(sql, phone, password);
        if (userList.size() <= 0) {
            return null;
        }
        return userList.get(0);
    }

    public List<Map<String, Object>> getAllUser() {
        String sql = "SELECT\n" +
                "user.userid,user.phone,\n" +
                "DATE_FORMAT(user.createtime,'%Y-%m-%d %H:%i:%s') createtime,\n" +
                "a.openid,\n" +
                "a.nickname\n" +
                "FROM\n" +
                "user\n" +
                "INNER JOIN weixinuser a ON `user`.userid = a.userid AND user.isactive=1 AND usertype = 0\n" +
                "\n" +
                "ORDER BY createtime";
        return jdbcTemplate.queryForList(sql);

    }

    public List<Map<String, Object>> getAllSuperCustomer(String userId) {
        String sql = "SELECT\n" +
                "user.userid,user.phone,user.username,\n" +
                "DATE_FORMAT(user.createtime,'%Y-%m-%d %H:%i:%s') createtime,\n" +
                "a.openid,\n" +
                "a.nickname\n" +
                "FROM\n" +
                "user\n" +
                "LEFT JOIN weixinuser a ON `user`.userid = a.userid WHERE user.userid<>? AND usertype = 0\n" +
                "\n" +
                "ORDER BY createtime";
        return jdbcTemplate.queryForList(sql, userId);

    }

    /*public Map<String, Object> getUserAccountBalanceByUserId(String userid) {
        String sql = "SELECT accountid,userid,balance,DATE_FORMAT(createtime,'%Y-%m-%d') createtime,DATE_FORMAT(updatetime,'%Y-%m-%d') updatetime FROM account WHERE userid=?";
        List<Map<String, Object>> userList = jdbcTemplate.queryForList(sql, userid);
        if (userList.size() <= 0) {
            return null;
        }
        return userList.get(0);
    }*/

    public Map<String, Object> getSuperiorUsers(String userid) {
        String sql = "SELECT b.userid,b.phone,b.isactive FROM user b WHERE b.userid = (SELECT a.superiorid FROM user a WHERE a.userid = ?)";
        List<Map<String, Object>> userList = jdbcTemplate.queryForList(sql, userid);
        if (userList.size() <= 0) {
            return null;
        }
        return userList.get(0);
    }


    public Map<String, Object> getPhoneByUserId(String userId) throws Exception {
        String sql = "SELECT phone FROM user WHERE userid=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    public List<Map<String, Object>> getDimUserInfoByPhone(String phone) {
        String sql = "SELECT u.userid,u.phone,u.isactive,u.username,DATE_FORMAT(u.createtime,'%Y-%m-%d %H:%i:%s') createtime,a.openid,a.nickname,b.userid as superuserid,b.nickname as superusername " +
                "FROM user u " +
                "LEFT JOIN weixinuser a ON u.userid = a.userid " +
                "LEFT JOIN weixinuser b ON u.superiorid=b.userid WHERE usertype = 0";
        sql += " AND u.phone LIKE '%" + phone + "%'";
        sql += " ORDER BY createtime";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getDimUserInfoByPhone2(String phone) {
        String sql = "SELECT u.userid,u.phone,u.isactive,u.username,u.superiorid as superuserid,DATE_FORMAT(u.createtime, '%Y-%m-%d %H:%i:%s') createtime,a.openid,a.nickname FROM user u LEFT JOIN weixinuser a ON u.userid = a.userid WHERE usertype = 0";
        sql += " AND u.phone LIKE '%" + phone + "%'";
        sql += " ORDER BY createtime";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getDimUserInfo() {
        String sql = "SELECT u.userid,u.phone,u.isactive,u.username,DATE_FORMAT(u.createtime,'%Y-%m-%d %H:%i:%s') createtime,a.openid,a.nickname,b.userid as superuserid,b.nickname as superusername " +
                "FROM user u " +
                "LEFT JOIN weixinuser a ON u.userid = a.userid " +
                "LEFT JOIN weixinuser b ON u.superiorid=b.userid WHERE usertype = 0";
        sql += " ORDER BY createtime";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getDimUserInfo2() {
        String sql = "SELECT u.userid,u.phone,u.isactive,u.username,u.superiorid as superuserid,DATE_FORMAT(u.createtime, '%Y-%m-%d %H:%i:%s') createtime,a.openid,a.nickname FROM user u LEFT JOIN weixinuser a ON u.userid = a.userid WHERE usertype = 0";
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> getWeixinNameByUserId(String userid) throws Exception {
        String sql = "SELECT nickname FROM weixinuser WHERE userid=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userid);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public List<Map<String, Object>> adminGetAllUser() {
        String sql = "SELECT user.userid,user.phone,user.superiorid as superuserid,user.isactive,user.username,user.remark,DATE_FORMAT(user.createtime,'%Y-%m-%d %H:%i:%s') createtime,weixinuser.openid,weixinuser.nickname,account.balance" +
                " FROM user LEFT JOIN weixinuser ON user.userid=weixinuser.userid LEFT JOIN account ON user.userid=account.userid" +
                " WHERE usertype = 0";
        sql += " ORDER BY createtime";
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getDimUserInfoByPhone(String phone, String userId) {
        String sql = "SELECT\n" +
                "user.userid,user.phone,\n" +
                "DATE_FORMAT(user.createtime,'%Y-%m-%d %H:%i:%s') createtime,\n" +
                "a.openid,\n" +
                "a.nickname\n" +
                "FROM\n" +
                "user\n" +
                "LEFT JOIN weixinuser a ON `user`.userid = a.userid WHERE user.isactive=1  AND user.userid<>? AND usertype = 0 AND user.phone LIKE '%" + phone + "%' ORDER BY createtime";
        return jdbcTemplate.queryForList(sql, userId);
    }

    public List<Map<String, Object>> getexpenseDetail(String phone, String date, String start, String end) {

        String sql = "SELECT username,nickname,phone,total,type,DATE_FORMAT(createtime,'%Y-%m-%d %H:%i:%s') createtime,remark FROM saledetail WHERE 1=1";
        if (StringUtils.isNotEmpty(phone)) {
            sql += " AND phone LIKE '%" + phone + "%'";
        }
        if (StringUtils.isNotEmpty(date)) {
            sql += " AND createtime like '%" + date + "%'";
        }
        if (StringUtils.isNotEmpty(start) && StringUtils.isNotEmpty(end)) {
            sql += " AND createtime BETWEEN '" + start + "' AND DATE_ADD('" + end + "',INTERVAL 1 DAY)";
        }
        sql += " ORDER BY createtime DESC";
        return jdbcTemplate.queryForList(sql);
    }

    public void bindPhone(String userId, String phone) throws Exception {
        String sql = "UPDATE user SET phone=? WHERE userid=?";
        jdbcTemplate.update(sql, phone, userId);
    }

    public Map<String, Object> findAdmin() {
        String sql = "SELECT userid,phone FROM user WHERE usertype=1 AND isactive='1'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    public List getAllAdminUser() throws Exception {
        String sql = "SELECT userid,username,phone,usertype,isactive,createtime FROM user WHERE usertype<>'0'";
        List list = jdbcTemplate.queryForList(sql);
        return Utility.timestampToString(list);
    }

    public void updateAdminUserInfo(String userId, String userName, String userType, String phone) throws Exception {
        String sql = "UPDATE user SET username=?,usertype=?,phone=? WHERE userid=?";
        jdbcTemplate.update(sql, userName, userType, phone, userId);
    }

    public void resetAdminUserPassword(String userId, String password) throws Exception {
        String sql = "UPDATE user SET password=? WHERE userid=?";
        jdbcTemplate.update(sql, password, userId);
    }

    public void changeAdminUserActive(String userId, String isActive) throws Exception {
        String sql = "UPDATE user SET isactive=? WHERE userid=?";
        jdbcTemplate.update(sql, isActive, userId);
    }

    public Map<String, Object> getAdmin(String adminId) {
        String sql = "SELECT userid,username,phone FROM user WHERE usertype=? AND isactive='1'";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, adminId);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    public Map<String, Object> getSumCount() throws Exception {
        String sql = "SELECT COUNT(userid) AS usercount FROM user WHERE usertype=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, UserType.CUSTOMER);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    public void updateUser(String userId, String name, String phone, String superId, String remark) throws Exception {
        String sql = "UPDATE user SET username=?,superiorid=?,phone=?,remark=? WHERE userid=?";
        jdbcTemplate.update(sql, name, superId, phone, remark, userId);
    }

    public Map<String, Object> getUserInfoByPhone(String phone) throws Exception {
        String sql = "SELECT userid FROM user WHERE phone=? AND usertype=?";
        List<Map<String, Object>> userlist = jdbcTemplate.queryForList(sql, phone, UserType.CUSTOMER);
        if (userlist.size() <= 0) {
            return null;
        }
        return userlist.get(0);
    }

    public Map<String, Object> getUserSuperInfoByPhone(String phone) throws Exception {
        String sql = "SELECT userid,superiorid FROM user WHERE phone=? AND usertype=?";
        List<Map<String, Object>> userlist = jdbcTemplate.queryForList(sql, phone, UserType.CUSTOMER);
        if (userlist.size() <= 0) {
            return null;
        }
        return userlist.get(0);
    }

    public Map<String, Object> getWeixinUserInfoByUserId(String userId) throws Exception {
        String sql = "SELECT openid,userid,nickname FROM weixinuser WHERE userid=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, userId);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    public void bindWeixinUserAndUser(String openId, String userId) throws Exception {
        String sql = "UPDATE weixinuser SET userid=? WHERE openid=?";
        jdbcTemplate.update(sql, userId, openId);
    }

    public void updateUserSuperInfo(String userId, String superiorid) throws Exception {
        String sql = "UPDATE user SET superiorid=? WHERE userid=?";
        jdbcTemplate.update(sql, superiorid, userId);
    }
}
