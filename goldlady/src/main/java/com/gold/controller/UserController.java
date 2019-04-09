package com.gold.controller;

import com.gold.common.ShortMessagePlatForm;
import com.gold.config.MsgConfig;
import com.gold.model.Customer;
import com.gold.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by user on 2017/11/6.
 */
@Scope("prototype")
@Controller
@RequestMapping("/gold/user")
public class UserController extends DsbBaseController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    @Autowired
    UserService userService;
    @Autowired
    MsgConfig msgConfig;

    /**
     * 获取客户列表
     * <p>
     * userid 当前客户id
     * type (非必需，不传默认0) 客户编号 : 一级客户:1 ,二级客户:2 ,全部客户:任意数字
     *
     * @throws Exception
     * @author zhengwei
     * @time 2017/11/10 22:32
     */
    @CrossOrigin
    @RequestMapping("getmycustomer")
    @ResponseBody
    public Map<String, Object> getMyCustomer(@RequestBody Customer customer) throws Exception {
        return userService.getMyCustomer(customer.getUserid(), customer.getType());
    }

    /**
     * 管理员登陆（PC）
     *
     * @param phone    手机号
     * @param password 密码
     * @return 用户信息
     */
    @CrossOrigin
    @RequestMapping("adminlogin")
    @ResponseBody
    public Map<String, Object> adminLogin(@RequestParam String phone, @RequestParam String password) throws Exception {
        return userService.adminLogin(phone, password);
    }

    /**
     * 管理员获取我的会员（PC）
     *
     * @param userid 管理员ID
     * @param phone  手机号（支持模糊查询）
     * @return _该系统所有会员信息
     */
    @CrossOrigin
    @RequestMapping("getallcustomer")
    @ResponseBody
    public Map<String, Object> getAllCustomer(@RequestParam String userid, @RequestParam(required = false) String phone) throws Exception {
        return userService.getDimUserInfoByPhone(userid, phone);
    }

    @CrossOrigin
    @RequestMapping("admingetallcustomer")
    @ResponseBody
    public Map<String, Object> adminGetAllCustomer(@RequestParam String userid) throws Exception {
        return userService.adminGetAllCustomer(userid);
    }


    @CrossOrigin
    @RequestMapping("getallsupercustomer")
    @ResponseBody
    public Map<String, Object> getAllSuperCustomer(@RequestParam String thisUserId, @RequestParam String userId, @RequestParam(required = false) String phone) throws Exception {
        return userService.getAllSuperCustomer(thisUserId, userId, phone);
    }

    /**
     * 校验该用户是否绑定手机号
     *
     * @param userId
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/checkphoneifbind", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> checkPhoneIfBind(@RequestParam String userId) throws Exception {
        userService.checkPhoneIfBind(userId);
        return getNoDataSuccessMap();
    }

    /**
     * 绑定手机号
     *
     * @param openId
     * @param phone
     * @param phonecode
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/bindphone", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> bindPhone(@RequestParam String openId, @RequestParam String phone, @RequestParam String phonecode) throws Exception {
        return userService.bindPhone(openId, phone, phonecode);
    }

    /**
     * 获取所有管理员用户列表
     *
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/getalladminuser", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> getAllAdminUser() throws Exception {
        return userService.getAllAdminUser();
    }

    /**
     * 增加管理用户
     *
     * @param userId
     * @param userName
     * @param phone
     * @param userType
     * @param password
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/addadminuser", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> addAdminUser(@RequestParam String userId, @RequestParam String userName, @RequestParam String phone, @RequestParam String userType, @RequestParam String password) throws Exception {
        userService.addAdminUser(userId, userName, phone, userType, password);
        return getNoDataSuccessMap();
    }

    /**
     * 修改管理员用户信息
     *
     * @param thisUserId
     * @param userId
     * @param userName
     * @param phone
     * @param userType
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/changeadminuserinfo", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> changeAdminUserInfo(@RequestParam String thisUserId, @RequestParam String userId, @RequestParam String userName, @RequestParam String phone, @RequestParam String userType) throws Exception {
        userService.changeAdminUserInfo(thisUserId, userId, userName, phone, userType);
        return getNoDataSuccessMap();
    }

    /**
     * 重置管理员用户密码
     *
     * @param thisUserId
     * @param userId
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/resetpassword", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> resetPassword(@RequestParam String thisUserId, @RequestParam String userId) throws Exception {
        userService.resetPassword(thisUserId, userId);
        return getNoDataSuccessMap();
    }

    /**
     * 禁用/启用管理员用户
     *
     * @param thisUserId
     * @param userId
     * @param isActive
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/changeadminuseractive", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> changeAdminUserActive(@RequestParam String thisUserId, @RequestParam String userId, @RequestParam String isActive) throws Exception {
        userService.changeAdminUserActive(thisUserId, userId, isActive);
        return getNoDataSuccessMap();
    }


    /**
     * 管理员新增会员
     *
     * @param userId
     * @param name
     * @param phone
     * @param superUser
     * @param remark
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/adduser", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> addUser(@RequestParam String userId, @RequestParam String name, @RequestParam String phone, @RequestParam String superUser, @RequestParam String remark) throws Exception {
        userService.addUser(userId, name, phone, superUser, remark);
        return getNoDataSuccessMap();
    }

    /**
     * 管理员禁用/启用会员
     *
     * @param thisUserId
     * @param userId
     * @param type
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/changeuseractive", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> changeUserActive(@RequestParam String thisUserId, @RequestParam String userId, @RequestParam String type) throws Exception {
        userService.changeUserActive(thisUserId, userId, type);
        return getNoDataSuccessMap();
    }


    /**
     * 管理员修改会员信息
     *
     * @param thisUserId
     * @param userId
     * @param name
     * @param phone
     * @param superUser
     * @param remark
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/changeuserinfo", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> changeUserInfo(@RequestParam String thisUserId, @RequestParam String userId, @RequestParam String name, @RequestParam String phone, @RequestParam String superUser, @RequestParam String remark) throws Exception {
        userService.changeUserInfo(thisUserId, userId, name, phone, superUser, remark);
        return getNoDataSuccessMap();
    }


    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/checkphonecodeandreguser", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> checkPhoneCodeAndRegUser(@RequestParam String openId, @RequestParam String phone, @RequestParam String phonecode) throws Exception {
        return userService.checkPhoneCodeAndRegUser(openId, phone, phonecode);
    }

}
