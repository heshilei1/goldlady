package com.gold.service;

import com.gold.common.BizException;
import com.gold.common.BizReturnCode;
import com.gold.common.ShortMessagePlatForm;
import com.gold.common.Utility;
import com.gold.config.MsgConfig;
import com.gold.model.QuestionStatus;
import com.gold.pst.QuestionPst;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhengwei on 2017/11/25.
 */
@Service("questionService")
@Scope("prototype")
public class QuestionService {

    @Autowired
    private QuestionPst questionPst;
    @Autowired
    private UserService userService;
    @Autowired
    private SaleService saleService;
    @Autowired
    MsgConfig msgConfig;

    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionService.class);

    /**
     * 咨询问题提交
     * @param userid
     * @param content
     * @param phone
     * @param wechatid
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void insert(String userid, String content, String phone,String wechatid) throws Exception {
        questionPst.insert(Utility.generateId(),userid,content,phone,wechatid, QuestionStatus.NOT_ANSWER.getCode());
    }

    /**
     * 获取自己提交问题列表（包括回答）
     *
     * @param userid
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public List<Map<String, Object>> findAllByUserId(String userid) throws Exception {
        return questionPst.findAllByUserId(userid);
    }

    /**
     * 待回答的顾客咨询数
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Integer getNotAnswerCount() throws Exception {
        return questionPst.getNotAnswerCount(QuestionStatus.NOT_ANSWER.getCode());
    }

    /**
     * 获取所有顾客问题列表接口
     *
     * @param userid
     * @param phone
     * @param status
     * @param starttime
     * @param endtime
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getAllQuestion(String userid, String phone, Integer status, String starttime, String endtime) throws Exception {
        Map<String, Object> returnData = new HashMap<>();
        boolean isAllow = userService.checkIsAdmin(userid);
        if (isAllow) {
            returnData.put("allQuestion", questionPst.getAllQuestion(phone, status, starttime, endtime));
            return returnData;
        } else {
            throw new BizException(BizReturnCode.UserLevelLow, "用户权限不足");
        }
    }

    /**
     * 获取问题详情接口(包括问题的解答)
     *
     *
     * @param userid
     * @param questionid 问题编号
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getQuestionDetail(String userid, String questionid) throws Exception {
        boolean isAllow = userService.checkIsAdmin(userid);
        if (isAllow) {
            return questionPst.getQuestionDetail(questionid);
        } else {
            throw new BizException(BizReturnCode.UserLevelLow, "用户权限不足");
        }
    }

    /**
     * 提交问题解答
     *
     * @param questionid
     * @param userid
     * @param content
     * @param phone
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void answer(String questionid,String userid, String content,String phone) throws Exception {
        boolean isAllow = userService.checkIsAdmin(userid);
        if (isAllow) {
            Map<String, Object> answer = this.findByQuestionId(questionid);
            if (answer != null) {
                throw new BizException(BizReturnCode.QuestionsCannotBeAnsweredRepeatedly, "问题已回答,不能重复回答!");
            }
            //回答
            questionPst.insertAnswer(questionid,"1",userid,content);
            //问题状态改为已回答
            questionPst.updateStatusByQuestionId(questionid,QuestionStatus.ALREADY_ANSWER.getCode());
            if (msgConfig.getIfopen()){
                if(StringUtils.isNotEmpty(phone)){
                    //短信通知
                    try {
                        ShortMessagePlatForm.send(phone,msgConfig.getLabel() +ShortMessagePlatForm.AnswerRemind);
                    } catch (Exception e) {
                        LOGGER.info("问题状态改为已回答:短信通知失败");
                        e.printStackTrace();
                    }
                }
            }
        } else {
            throw new BizException(BizReturnCode.UserLevelLow, "用户权限不足");
        }

    }

    /**
     * 通过questionid查询
     *
     * @param questionid
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> findByQuestionId(String questionid) {
        return questionPst.findByQuestionId(questionid);
    }

    /**
     * 咨询问题提交
     * @param userid
     * @param content
     * @param phone
     * @param wechatid
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void consult(String userid, String content, String phone, String wechatid) throws Exception {
        Map<String, Object> isExist = userService.validUser(userid);
        if (isExist != null) {
            phone = phone == null ? "" : phone;
            wechatid = wechatid == null ? "" : wechatid;
            this.insert(userid, content, phone,wechatid);
        } else {
            throw new BizException(BizReturnCode.UserNotExistError, "用户不存在");
        }
    }

    /**
     * 获取自己提交问题列表
     * @param userid
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String,Object> myList(String userid) throws Exception {
        Map<String, Object> returnMap = new HashMap<>();
        Map<String, Object> isExist = userService.validUser(userid);
        if (isExist != null) {
            List<Map<String, Object>> returnList = this.findAllByUserId(userid);
            returnMap.put("returnList", returnList);
            return returnMap;
        } else {
            throw new BizException(BizReturnCode.UserNotExistError, "用户不存在");
        }
    }

    /**
     * 待回答的顾客咨询数
     * @param userid
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String,Object> getNotAnswerCount(String userid) throws Exception {
        Map<String, Object> returnData = new HashMap<>();
        boolean isAllow = userService.checkIsAdmin(userid);
        if (isAllow) {
            Integer count = this.getNotAnswerCount();
            returnData.put("sumtotal", saleService.getSumSaleTotal());
            returnData.put("usercount", userService.getSumCount());
            returnData.put("count", count);
            return returnData;
        } else {
            throw new BizException(BizReturnCode.UserLevelLow, "用户权限不足");
        }
    }
}
