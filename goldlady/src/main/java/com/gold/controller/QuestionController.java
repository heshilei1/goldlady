package com.gold.controller;

import com.gold.service.QuestionService;
import com.gold.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by zhengwei on 2017/11/25.
 */
@Scope("prototype")
@RestController
@RequestMapping("/gold/question")
public class QuestionController extends DsbBaseController {

    @Autowired
    private QuestionService questionService;
    @Autowired
    SaleService saleService;

    /**
     * 咨询问题提交
     *
     * @param userid    用户ID
     * @param content   问题内容
     * @param phone     手机号 (非必需)
     * @param wechatid  微信号 (非必需)
     * @return
     */
    @RequestMapping("consult")
    public Map<String, Object> consult(@RequestParam String userid,@RequestParam String content,
                                       @RequestParam(required = false) String phone,
                                       @RequestParam(required = false) String wechatid) throws Exception {
        questionService.consult(userid,content,phone,wechatid);
        return getNoDataSuccessMap();
    }

    /**
     * 获取自己提交问题列表
     *
     * @param userid 用户ID
     * @return
     */
    @RequestMapping("mylist")
    public Map<String, Object> myList(String userid) throws Exception {
        return questionService.myList(userid);
    }

    /**
     * 待回答的顾客咨询数
     *
     * @param userid 管理员ID
     * @return
     * @throws Exception
     */
    @RequestMapping("notanswercount")
    public Map<String, Object> getNotAnswerCount(String userid) throws Exception {
        return questionService.getNotAnswerCount(userid);
    }

    /**
     * 获取所有顾客问题列表接口
     *
     * @param userid    管理员ID
     * @param phone     手机号(模糊)     非必须
     * @param status    状态           非必须
     * @param starttime 开始时间    非必须
     * @param endtime   结束时间      非必须
     * @return
     * @throws Exception
     */
    @RequestMapping("getall")
    public Map<String, Object> getAllQuestion(@RequestParam String userid,
                                              @RequestParam(required = false) String phone,
                                              @RequestParam(required = false) Integer status,
                                              @RequestParam(required = false) String starttime,
                                              @RequestParam(required = false) String endtime) throws Exception {
        return questionService.getAllQuestion(userid,phone, status, starttime, endtime);
    }

    /**
     * 获取问题详情接口(包括问题的解答)
     *
     * @param userid     管理员ID
     * @param questionid 问题编号
     * @return
     * @throws Exception
     */
    @RequestMapping("detail")
    public Map<String, Object> getQuestionDetail(String userid, String questionid) throws Exception {
        return questionService.getQuestionDetail(userid,questionid);
    }

    /**
     * 提交问题解答
     *
     * @param questionid 问题编号
     * @param userid     管理员ID
     * @param content    回答内容
     * @param phone      用户手机号
     * @return
     * @throws Exception
     */
    @RequestMapping("answer")
    public Map<String, Object> answer(@RequestParam String questionid,@RequestParam String userid,
                                      @RequestParam String content,@RequestParam(required = false) String phone) throws Exception {
        questionService.answer(questionid, userid, content, phone);
        return getNoDataSuccessMap();
    }
}
