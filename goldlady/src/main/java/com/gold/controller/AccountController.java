package com.gold.controller;

import com.gold.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by Administrator on 2017/11/18.
 */
@Scope("prototype")
@Controller
@RequestMapping("/gold/account")
public class AccountController extends DsbBaseController {

    @Autowired
    AccountService accountService;

    /**
     * 获取账户余额信息
     *
     * @param userId
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/getaccountinfo", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> getAccountInfo(@RequestParam String userId) throws Exception {
        return accountService.getAccountInfo(userId);
    }

    /**
     * 获取自己的收支明细
     *
     * @param userId
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/getaccountstanding", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> getAccountStanding(@RequestParam String userId) throws Exception {
        return accountService.getAccountStanding(userId);
    }

}
