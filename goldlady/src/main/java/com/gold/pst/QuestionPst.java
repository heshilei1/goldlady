package com.gold.pst;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by zhengwei on 2017/11/25.
 */
@Repository("questionPst")
@Scope("prototype")
public class QuestionPst {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insert(String questionid, String userid, String content, String phone,String wechatid, int status) {
        String sql = "INSERT INTO question(questionid,userid,content,phone,wechatid,status) VALUES(?,?,?,?,?,?)";
        jdbcTemplate.update(sql,questionid,userid,content,phone,wechatid,status);
    }

    public List<Map<String, Object>> findAllByUserId(String userid) {
        String sql = "SELECT qt.userid,wxa.nickname as questionname,qt.wechatid,qt.content as questioncontent,qt.phone,qt.`status`,DATE_FORMAT(qt.createtime,'%Y-%m-%d %H:%i:%s') questiontime,an.content as answercontent,DATE_FORMAT(an.createtime,'%Y-%m-%d %H:%i:%s') answertime,wxb.nickname as answername\n" +
                "FROM question qt \n" +
                "LEFT JOIN weixinuser wxa on qt.userid=wxa.userid\n" +
                "LEFT JOIN answer an on qt.questionid=an.questionid \n" +
                "LEFT JOIN weixinuser wxb on an.userid=wxb.userid\n" +
                "WHERE qt.userid = ? ORDER BY qt.createtime DESC";
        return jdbcTemplate.queryForList(sql, userid);
    }

    public Integer getNotAnswerCount(int status) {
        String sql = "SELECT count(questionid) FROM question WHERE status="+status;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public List<Map<String, Object>> getAllQuestion(String phone, Integer status, String starttime, String endtime) {
        String sql = "SELECT qt.questionid,qt.userid,qt.wechatid,wxa.nickname as questionname,qt.content as questioncontent,qt.phone,qt.`status`,DATE_FORMAT(qt.createtime,'%Y-%m-%d %H:%i:%s') questiontime " +
                "FROM question qt " +
                "LEFT JOIN weixinuser wxa on qt.userid=wxa.userid " +
                "WHERE 1=1";
        if(StringUtils.isNotEmpty(phone)){
            sql += " AND qt.phone LIKE " + "'%" + phone + "%'";
        }
        if(status != null){
            sql += " AND qt.status = " + status;
        }
        if(StringUtils.isNotEmpty(starttime) && StringUtils.isNotEmpty(endtime)){
            sql += " AND (qt.createtime BETWEEN '" + starttime + "' AND '" + endtime + "')";
        }
        sql += " ORDER BY qt.status,qt.createtime";
        return jdbcTemplate.queryForList(sql);
    }

    public Map<String, Object> getQuestionDetail(String questionid) {
        String sql = "SELECT qt.userid,qt.wechatid,wxa.nickname as questionname,qt.content as questioncontent,qt.phone,qt.`status`,DATE_FORMAT(qt.createtime,'%Y-%m-%d %H:%i:%s') questiontime,an.content as answercontent,DATE_FORMAT(an.createtime,'%Y-%m-%d %H:%i:%s') answertime,wxb.nickname as answername " +
                "FROM question qt " +
                "LEFT JOIN weixinuser wxa on qt.userid=wxa.userid " +
                "LEFT JOIN answer an on qt.questionid=an.questionid " +
                "LEFT JOIN weixinuser wxb on an.userid=wxb.userid " +
                "WHERE qt.questionid = ?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql,questionid);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }

    public void insertAnswer(String questionid, String serialno, String userid, String content) {
        String sql = "INSERT INTO answer(questionid,serialno,userid,content) VALUES(?,?,?,?)";
        jdbcTemplate.update(sql, questionid, serialno, userid, content);
    }

    public void updateStatusByQuestionId(String questionid, int status) {
        String sql = "UPDATE question SET status = ? WHERE questionid = ?";
        jdbcTemplate.update(sql, status, questionid);
    }

    public Map<String, Object> findByQuestionId(String questionid) {
        String sql = "SELECT questionid,userid,content FROM answer WHERE questionid = ?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql,questionid);
        if (list.size() <= 0) {
            return null;
        }
        return list.get(0);
    }
}
