package com.gold.service;

import com.alibaba.fastjson.JSONObject;
import com.gold.common.BizException;
import com.gold.common.BizReturnCode;
import com.gold.common.ShortMessagePlatForm;
import com.gold.common.Utility;
import com.gold.config.MsgConfig;
import com.gold.model.StandingType;
import com.gold.model.UserType;
import com.gold.model.WithdrawalsStatus;
import com.gold.pst.*;
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

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by user on 2017/11/6.
 */
@Service("userService")
@Scope("prototype")
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    WeixinConfigService weixinConfigService;
    @Autowired
    SafeCodeService safeCodeService;
    @Autowired
    WeixinUserService weixinUserService;
    @Autowired
    UserPst userPst;
    @Autowired
    private AccountPst accountPst;
    @Autowired
    private AccountStandingPst accountStandingPst;
    @Autowired
    private TradePst tradePst;
    @Autowired
    private SalePst salePst;
    @Autowired
    MsgConfig msgConfig;

    /**
     * 获取全部客户
     *
     * @param userid
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public List<Map<String, Object>> getAllGuest(String userid) throws Exception {
        List<Map<String, Object>> together = new ArrayList<>();
        List<Map<String, Object>> firstGuests = userPst.getGuest(userid);
        together.addAll(firstGuests);
        for (Map<String, Object> m : firstGuests) {
            List<Map<String, Object>> secondGuests = userPst.getGuest(m.get("userid") + "");
            together.addAll(secondGuests);
        }
        return together;
    }

    /**
     * 获取二级客户
     *
     * @param userid
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public List<Map<String, Object>> getSecondLevelGuest(String userid) throws Exception {
        List<Map<String, Object>> together = new ArrayList<>();
        List<Map<String, Object>> firstGuests = userPst.getGuest(userid);
        for (Map<String, Object> m : firstGuests) {
            List<Map<String, Object>> secondGuests = userPst.getGuest(m.get("userid") + "");
            together.addAll(secondGuests);
        }
        return together;
    }

    /**
     * 获取一级客户
     *
     * @param userid
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public List<Map<String, Object>> getFirstLevelGuest(String userid) throws Exception {
        return userPst.getGuest(userid);
    }

    /**
     * 校验当前用户是否存在且可用
     *
     * @param userid
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> validUser(String userid) throws Exception {
        return userPst.validUser(userid);
    }

    /**
     * 校验当前用户是否存在且可用
     *
     * @param userid
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> saveSaleValidUser(String userid) throws Exception {
        return userPst.saveSaleValidUser(userid);
    }

    /**
     * 注册新用户(17-12-05修改为 只新增weixinuser记录，不新增user及account记录)
     *
     * @param superiorOpenId
     * @param openId
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void reg(String superiorOpenId, String openId) throws Exception {
        //校验扫码的微信用户是否存在
        Map<String, Object> userMap = userPst.getWeixinUserInfoByOpenId(openId);
        if (userMap != null) {//以前关注过
            String old_superuser = "";
            old_superuser = (String) userMap.get("superiorid");
            if (StringUtils.isEmpty(old_superuser)) {//以前关注时没有上级，取本次扫描的上级信息
                Map<String, Object> superUserInfo = new HashMap<>();
                if (StringUtils.isNotEmpty(superiorOpenId)) {
                    //校验该上级用户是否存在
                    superUserInfo = userPst.getWeixinUserInfoByOpenId(superiorOpenId);
                    if (null == superUserInfo) {
                        throw new BizException(BizReturnCode.SuperiorUserNotExistError, "上级用户不存在");
                    }
                    old_superuser = (String) superUserInfo.get("userid");
                }
            }
            //更新微信用户上级信息
            userPst.updateWeixinSuperUser(old_superuser, openId);
        } else {//以前未关注过
            Map<String, Object> superUserInfo = new HashMap<>();
            String superuserid = "";
            if (StringUtils.isNotEmpty(superiorOpenId)) {
                //校验该上级用户是否存在
                superUserInfo = userPst.getWeixinUserInfoByOpenId(superiorOpenId);
                if (null == superUserInfo) {
                    throw new BizException(BizReturnCode.SuperiorUserNotExistError, "上级用户不存在");
                }
                superuserid = (String) superUserInfo.get("userid");
            }
            //保存微信用户信息
            saveWeixinUser("", openId, superuserid);
        }


    }

    /**
     * 新增用户信息
     *
     * @param userId
     * @param superiorOpenId
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void insertUser(String userId, String superiorOpenId) throws Exception {
        if (StringUtils.isNotEmpty(superiorOpenId)) {
            LOGGER.info("上级用户openId：" + superiorOpenId);
            //校验该上级用户是否存在
            Map<String, Object> userInfo = userPst.getWeixinUserInfoByOpenId(superiorOpenId);
            if (null == userInfo) {
                throw new BizException(BizReturnCode.SuperiorUserNotExistError, "上级用户不存在");
            }
            userPst.insertUser(userId, (String) userInfo.get("userid"));
        } else {
            userPst.insertUser(userId);
        }
    }


    /**
     * 保存微信用户信息
     *
     * @param userId
     * @param openId
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void saveWeixinUser(String userId, String openId, String superUserId) throws Exception {
        JSONObject json = weixinUserService.getWeixinUserInfoByOpenId("goldlady", openId);
        userPst.insertWeixinUser(userId, superUserId, json.getString("openid"), json.getString("nickname"),
                json.getString("sex"), json.getString("city"), json.getString("country"),
                json.getString("province"), json.getString("language"), json.getString("headimgurl"));
    }

    /**
     * 创建用户账号ID
     *
     * @param userId
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void createUserAccount(String userId) throws Exception {
        //获取用户账户ID
        String accountId = Utility.generateId();
        userPst.insertUserAccount(accountId, userId);
    }

    /**
     * 根据类型 查询用户信息
     *
     * @param phone
     * @param password
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getUserInfo(String phone, String password) throws Exception {
        return userPst.checkUserInfo(phone, password);
    }

    /**
     * 检查是否为管理员
     *
     * @param userid
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public boolean checkIsAdmin(String userid) throws Exception {
        Map<String, Object> userInfo = userPst.getUserInfo(userid);
        if (userInfo != null) {
            if (!userInfo.get("usertype").equals(UserType.CUSTOMER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有会员信息
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public List<Map<String, Object>> getAllUser() {
        return userPst.getAllUser();
    }


    /**
     * 检查账户是否满足体现条件
     *
     * @param userid
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public int isUserAllowWithdrawals(String userid) throws Exception {
        Map<String, Object> userInfo = userPst.getUserInfo(userid);
        List<Map<String, Object>> statusList = tradePst.findStatusByUserid(userid);
        //检测到用户不存在
        if (userInfo == null) {
            return 1;
        }
        //您有未处理的提现申请，请等待处理完成后再进行提现
        if (statusList.size() != 0) {
            for (Map<String, Object> status : statusList) {
                if (WithdrawalsStatus.ING.equals(status.get("status"))) {
                    return 2;
                }
            }
        }
        return 0;
    }

    /**
     * 根据模糊手机号获取会员信息
     *
     * @param phone
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getDimUserInfoByPhone(String userid, String phone) throws Exception {
        Map<String, Object> returnData = new HashMap<>();
        boolean isAdmin = this.checkIsAdmin(userid);
        if (isAdmin) {
            List<Map<String, Object>> allUserInfo = new LinkedList<>();
            if (StringUtils.isNotEmpty(phone)) {
                List<Map<String, Object>> userList = userPst.getDimUserInfoByPhone2(phone);
                for (int i = 0; i < userList.size(); i++) {
                    Map<String, Object> usermap = userList.get(i);
                    String superuserid = (String) usermap.get("superuserid");
                    if (StringUtils.isEmpty(superuserid)) {
                        usermap.put("superusername", "");
                    } else {
                        Map<String, Object> wxusermap = userPst.getWeixinNameByUserId(superuserid);
                        String nickname = "";
                        if (wxusermap != null) {
                            nickname = (String) wxusermap.get("nickname");
                        }
                        usermap.put("superusername", nickname);
                    }
                    allUserInfo.add(usermap);
                }
                if (allUserInfo.size() == 0) {
                    throw new BizException(BizReturnCode.PhoneNotExistError, "手机号不存在错误");
                }
            } else {
                List<Map<String, Object>> userList = userPst.getDimUserInfo2();
                for (int i = 0; i < userList.size(); i++) {
                    Map<String, Object> usermap = userList.get(i);
                    String superuserid = (String) usermap.get("superuserid");
                    if (StringUtils.isEmpty(superuserid)) {
                        usermap.put("superusername", "");
                    } else {
                        Map<String, Object> wxusermap = userPst.getWeixinNameByUserId(superuserid);
                        String nickname = "";
                        if (wxusermap != null) {
                            nickname = (String) wxusermap.get("nickname");
                        }
                        usermap.put("superusername", nickname);
                    }
                    allUserInfo.add(usermap);
                }
            }
            returnData.put("allUserInfo", allUserInfo);
            return returnData;
        } else {
            throw new BizException(BizReturnCode.UserLevelLow, "用户权限不足");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> adminGetAllCustomer(String userid) throws Exception {
        Map<String, Object> returnData = new HashMap<>();
        List result = new LinkedList();
        boolean isAdmin = this.checkIsAdmin(userid);
        if (isAdmin) {
            List<Map<String, Object>> allUserInfo = userPst.adminGetAllUser();
            for (int i = 0; i < allUserInfo.size(); i++) {
                Map<String, Object> usermap = allUserInfo.get(i);
                String superuserid = (String) usermap.get("superuserid");
                if (StringUtils.isNotEmpty(superuserid)) {
                    Map<String, Object> superuserinfo = userPst.getUserAndWxInfo(superuserid);
                    String superusername = "";
                    if (superuserinfo != null) {
                        superusername = (String) superuserinfo.get("username");
                        if (StringUtils.isEmpty(superusername)) {
                            superusername = (String) superuserinfo.get("nickname");
                        }
                    }
                    usermap.put("superusername", superusername);
                } else {
                    usermap.put("superusername", "");
                }
                result.add(usermap);
            }
            returnData.put("allUserInfo", result);
            return returnData;
        } else {
            throw new BizException(BizReturnCode.UserLevelLow, "用户权限不足");
        }
    }

    /**
     * 向消费者相关上级用户分发奖励金额（只分发两级）同时记录账户台账,短信通知
     *
     * @param userid
     * @param total
     * @param product
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void rewardSuperiorUsers(String adminid,String userid, String nickname, BigDecimal total, Map<String, Object> product, String remark) throws Exception {

        LOGGER.info("录入消费信息:{},{},{},{}", userid, nickname, total.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), product.get("pluname"));
        salePst.insert(adminid,userid, total, (String) product.get("pluname"), remark);

        Map<String, Object> firstSuperiorUser = userPst.getSuperiorUsers(userid);
        Map<String, Object> secondSuperiorUser = new HashMap<>();
        secondSuperiorUser = null;
        if (firstSuperiorUser != null) {
            secondSuperiorUser = userPst.getSuperiorUsers(firstSuperiorUser.get("userid") + "");
        }
        Double firstrate = Double.parseDouble((String) product.get("firstrate")) / 100.0;
        Double secondrate = Double.parseDouble((String) product.get("secondrate")) / 100.0;

        //记录本人消费台账
//        accountStandingPst.insert(userid, total, StandingType.withdrawDeposit);
        BigDecimal first = total.multiply(new BigDecimal(firstrate));
        BigDecimal second = total.multiply(new BigDecimal(secondrate));
        //上级
        if (firstSuperiorUser != null && !firstSuperiorUser.get("isactive").equals("0")) {
            //分发奖励金额
            accountPst.grantAward(firstSuperiorUser.get("userid") + "", first);
            LOGGER.info("上级分发{}元奖励,比例{}", first.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), firstrate);
            //记录台账
            accountStandingPst.insert(firstSuperiorUser.get("userid") + "", first, StandingType.tradingOfShareOutBonus);
            LOGGER.info("上级分发奖励金额:记录台账");
            if (msgConfig.getIfopen()) {
                if (StringUtils.isNotEmpty((String) firstSuperiorUser.get("phone"))) {
                    //短信通知
                    try {
//                    ShortMessagePlatForm.send(firstSuperiorUser.get("phone") + "", String.format(ShortMessagePlatForm.rewardSuperiorMsg, total.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), first.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
                        ShortMessagePlatForm.send(firstSuperiorUser.get("phone") + "", String.format(msgConfig.getLabel() + ShortMessagePlatForm.rewardSuperiorMsg, first.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
                        LOGGER.info("上级分发奖励金额:短信通知");
                    } catch (Exception e) {
                        LOGGER.info("上级短信通知失败");
                        e.printStackTrace();
                    }
                }
            }
        }
        //上上级
        if (secondSuperiorUser != null && !secondSuperiorUser.get("isactive").equals("0")) {
            accountPst.grantAward(secondSuperiorUser.get("userid") + "", second);
            LOGGER.info("上上级分发{}元奖励,比例{}", second.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), secondrate);
            accountStandingPst.insert(secondSuperiorUser.get("userid") + "", second, StandingType.tradingOfShareOutBonus);
            LOGGER.info("上上级分发奖励金额:记录台账");
            if (msgConfig.getIfopen()) {
                if (StringUtils.isNotEmpty((String) secondSuperiorUser.get("phone"))) {
                    try {
//                    ShortMessagePlatForm.send(secondSuperiorUser.get("phone") + "", String.format(ShortMessagePlatForm.rewardSuperiorMsg, total.setScale(2, BigDecimal.ROUND_HALF_UP).toString(), second.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
                        ShortMessagePlatForm.send(secondSuperiorUser.get("phone") + "", String.format(msgConfig.getLabel() + ShortMessagePlatForm.rewardSuperiorMsg, second.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
                        LOGGER.info("上上级分发奖励金额:短信通知");
                    } catch (Exception e) {
                        LOGGER.info("上上级短信通知失败");
                        e.printStackTrace();
                    }
                }
            }
        }
        //超级管理员
        Map<String, Object> admin = userPst.getAdmin(UserType.SUPPER_ADMIN);
        if (admin != null && StringUtils.isNotEmpty(nickname)) {
            if (msgConfig.getIfopen()) {
                if (StringUtils.isNotEmpty((String) admin.get("phone"))) {
                    try {
                        LOGGER.info("分发奖励:短信通知超级管理员");
                        ShortMessagePlatForm.send((String) admin.get("phone"), String.format(msgConfig.getLabel() + ShortMessagePlatForm.RemindSuperAdmin, nickname, (String) product.get("pluname"), total.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
                    } catch (Exception e) {
                        LOGGER.info("超级管理员短信通知失败");
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void checkPhoneIfBind(String userId) throws Exception {
        Map<String, Object> userMap = userPst.getPhoneByUserId(userId);
        if (null == userMap) {
            throw new BizException(BizReturnCode.UserNotExistError, "该用户不存在");
        }
        String phone = (String) userMap.get("phone");
        if (StringUtils.isEmpty(phone)) {
            throw new BizException(BizReturnCode.NoPhoneError, "该用户未绑定手机号");
        }
    }

    /**
     * 根据手机号,日期筛选消费记录
     *
     * @param phone
     * @param date
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public List<Map<String, Object>> getexpenseDetail(String phone, String date) {
        String start = "";
        String end = "";
        if (StringUtils.isNotEmpty(date)) {
            if (date.length() > 13) {
                start = date.split(" ")[0];
                end = date.split(" ")[2];
                date = "";
            }
        }
        return userPst.getexpenseDetail(phone, date, start, end);
    }


    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> bindPhone(String openId, String phone, String phoneCode) throws Exception {
        Map<String, Object> result = new HashMap<>();
        //校验短信验证码
        safeCodeService.checkPhoneCode(phone, phoneCode);
        LOGGER.info("openId:" + openId);
        //获取用户ID
        Map<String, Object> weixinInfo = userPst.getWeixinUserInfoByOpenId(openId);
        String userId = (String) weixinInfo.get("userid");
        String superiorid = (String) weixinInfo.get("superiorid");
        if (StringUtils.isEmpty(superiorid)) {
            superiorid = "";
        }
        if (StringUtils.isEmpty(userId)) {
            //根据手机号查询用户表
            Map<String, Object> usermap = userPst.getUserSuperInfoByPhone(phone);
            if (usermap != null) {
                userId = (String) usermap.get("userid");
                superiorid = (String) usermap.get("superiorid");
                userPst.updateWeixinSuperUser(superiorid, openId);
            } else {
                userId = Utility.generateId();
                userPst.insertUser(userId, "", phone, superiorid, UserType.CUSTOMER, "");
                //创建用户账户
                createUserAccount(userId);
            }
        }
//        //绑定手机
//        userPst.bindPhone(userId, phone);
        userPst.updateWeixinUser(userId, openId);
        result.put("userId", userId);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getAllAdminUser() throws Exception {
        List list = userPst.getAllAdminUser();
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        return map;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void addAdminUser(String userId, String username, String phone, String usertype, String password) throws Exception {
        //校验操作人权限
        checkSupperAdminUser(userId);
        //校验手机号重复
        if (userPst.checkAdminPhoneIfRepeat(phone)) {
            throw new BizException(BizReturnCode.UserPhoneRepeatError, "该手机号已注册过管理员用户！");
        }
        if (!StringUtils.equals(usertype, UserType.ADMIN) && !StringUtils.equals(usertype, UserType.SENIOR_ADMIN)) {
            throw new BizException(BizReturnCode.NoPermission, "该用户无权限增加该类型用户");
        }
        String newUserId = Utility.generateId();
        userPst.insertUser(newUserId, username, phone, password, usertype);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void checkSupperAdminUser(String userId) throws Exception {
        Map<String, Object> map = userPst.getUserInfo(userId);
        if (null == map) {
            throw new BizException(BizReturnCode.UserNotExistError, "操作人不存在");
        }
        String userType = (String) map.get("usertype");
        if (!StringUtils.equals(userType, UserType.SENIOR_ADMIN) && !StringUtils.equals(userType, UserType.SUPPER_ADMIN)) {
            throw new BizException(BizReturnCode.NoPermission, "操作用户无权限");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void changeAdminUserInfo(String thisUserId, String userId, String userName, String phone, String userType) throws Exception {
        //校验操作人权限
        checkSupperAdminUser(thisUserId);
        //校验要修改的用户是否存在
        Map<String, Object> map = userPst.getUserInfo(userId);
        if (null == map) {
            throw new BizException(BizReturnCode.UserNotExistError, "要修改的用户不存在!");
        }
        String oldPhone = (String) map.get("phone");
        if (!StringUtils.equals(oldPhone, phone)) {
            //校验手机号重复
            if (userPst.checkAdminPhoneIfRepeat(phone)) {
                throw new BizException(BizReturnCode.UserPhoneRepeatError, "该手机号已注册过管理员用户！");
            }
        }

        if (!StringUtils.equals(userType, UserType.ADMIN) && !StringUtils.equals(userType, UserType.SENIOR_ADMIN)) {
            throw new BizException(BizReturnCode.NoPermission, "该用户无权限增加该类型用户");
        }
        userPst.updateAdminUserInfo(userId, userName, userType, phone);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void resetPassword(String thisUserId, String userId) throws Exception {
        //校验操作人权限
        checkSupperAdminUser(thisUserId);
        //校验要修改的用户是否存在
        Map<String, Object> map = userPst.getUserInfo(userId);
        if (null == map) {
            throw new BizException(BizReturnCode.UserNotExistError, "要修改的用户不存在!");
        }
        String password = "e10adc3949ba59abbe56e057f20f883e";//重置密码为123456
        userPst.resetAdminUserPassword(userId, password);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void changeAdminUserActive(String thisUserId, String userId, String isActive) throws Exception {
        //校验操作人权限
        checkSupperAdminUser(thisUserId);
        //校验要修改的用户是否存在
        Map<String, Object> map = userPst.getUserInfo(userId);
        if (null == map) {
            throw new BizException(BizReturnCode.UserNotExistError, "要修改的用户不存在!");
        }
        if (!StringUtils.equals((String) map.get("isactive"), isActive)) {
            userPst.changeAdminUserActive(userId, isActive);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public String getSumCount() throws Exception {
        String result = "0.00";
        Map<String, Object> map = userPst.getSumCount();
        if (null == map) {
            return result;
        }
        return map.get("usercount") + "";
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void addUser(String userId, String name, String phone, String superUser, String remark) throws Exception {
        //校验操作人权限
        checkSupperAdminUser(userId);
        //校验手机号是否注册
        if (userPst.checkCustomerPhoneIfRepeat(phone)) {
            throw new BizException(BizReturnCode.UserPhoneRepeatError, "该手机号已注册过会员");
        }
        //新增会员
        String newuserId = Utility.generateId();
        userPst.insertUser(newuserId, name, phone, superUser, UserType.CUSTOMER, remark);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void changeUserActive(String thisUserId, String userId, String type) throws Exception {
        //校验操作人权限
        checkSupperAdminUser(thisUserId);
        Map<String, Object> map = userPst.getUserInfo(userId);
        if (null == map) {
            throw new BizException(BizReturnCode.UserNotExistError, "该用户不存在!");
        }
        userPst.changeAdminUserActive(userId, type);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void changeUserInfo(String thisUserId, String userId, String name, String phone, String superUser, String remark) throws Exception {
        //校验操作人权限
        checkSupperAdminUser(thisUserId);
        Map<String, Object> map = userPst.getUserInfo(userId);
        if (null == map) {
            throw new BizException(BizReturnCode.UserNotExistError, "该用户不存在!");
        }
        //校验手机号是否注册
        if (userPst.checkIfUpdatePhone(phone, userId)) {
            throw new BizException(BizReturnCode.UserPhoneRepeatError, "该手机号已注册过会员");
        }
        userPst.updateUser(userId, name, phone, superUser, remark);
        userPst.updateWeixinSuperUserByUserId(superUser, userId);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getAllSuperCustomer(String thisUserId, String userid, String phone) throws Exception {
        Map<String, Object> returnData = new HashMap<>();
        checkSupperAdminUser(thisUserId);
        if (StringUtils.isEmpty(phone)) {
            List<Map<String, Object>> allUserInfo = userPst.getAllSuperCustomer(userid);
            returnData.put("allUserInfo", allUserInfo);
            return returnData;
        } else {
            //手机号不为空返回模糊查询会员信息
            List<Map<String, Object>> allUserInfo = userPst.getDimUserInfoByPhone(phone, userid);
            returnData.put("allUserInfo", allUserInfo);
            return returnData;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> checkPhoneCodeAndRegUser(String openId, String phone, String phoneCode) throws Exception {
        Map<String, Object> map = new HashMap<>();
        //校验手机号是否绑定
        Map<String, Object> userSuperMap = safeCodeService.checkPhoneIfCanBind(phone);
        //校验短信验证码
        safeCodeService.checkPhoneCode(phone, phoneCode);
        //获取微信信息
        Map<String, Object> weixinusermap = userPst.getWeixinUserInfoByOpenId(openId);
        if (null == weixinusermap) {
            throw new BizException(BizReturnCode.NoWeixinUserError, "该微信用户不存在");
        }
        String superiorid = (String) weixinusermap.get("superiorid");
        String userId = null;
        if (null == userSuperMap) {
            //注册用户级账户信息
            userId = Utility.generateId();
            userPst.insertUser(userId, superiorid, phone);
        } else {
            //若该手机号已在PC端添加过用户，则将当前微信信息与PC端添加用户进行关联
            userId = (String) userSuperMap.get("userid");
            String userSuperId = (String) userSuperMap.get("superiorid");
            if (StringUtils.isEmpty(userSuperId)) {
                //若PC端添加用户不存在上级，则更新该用户的上级用户信息
                userPst.updateUserSuperInfo(userId, superiorid);
            }
        }
        //创建用户账户
        createUserAccount(userId);
        //绑定weixinuser与user
        userPst.bindWeixinUserAndUser(openId, userId);
        map.put("userId", userId);
        return map;
    }

    /**
     * 管理员登录(PC)
     *
     * @param phone
     * @param password
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> adminLogin(String phone, String password) throws Exception {
        Map<String, Object> admin = this.getUserInfo(phone, password);
        if (admin != null) {
            if (!admin.get("usertype").equals(UserType.CUSTOMER)) {
                return admin;
            } else {
                throw new BizException(BizReturnCode.UserLevelLow, "权限不足!");
            }
        } else {
            throw new BizException(BizReturnCode.UserNotExistError, "账号或密码错误！");
        }
    }

    /**
     * 获取客户列表
     *
     * @param userid
     * @param type
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getMyCustomer(String userid, Integer type) throws Exception {
        Map<String, Object> returnData = new HashMap<>();
        //1.校验当前用户是否存在且可用
        Map<String, Object> user = this.validUser(userid);
        if (user == null) {
            throw new BizException(BizReturnCode.UserNotExistError, "此用户不存在!");
        }
        List<Map<String, Object>> userlistmap = new LinkedList<>();
        //2.查出对应条件的客户列表
        type = (type == null || (type != 1 && type != 2)) ? 0 : type;
        switch (type) {
            case 1:
                userlistmap = this.getFirstLevelGuest(userid);
                break;
            case 2:
                userlistmap = this.getSecondLevelGuest(userid);
                break;
            case 0:
                userlistmap = this.getAllGuest(userid);
                break;
        }
        List<Map<String, Object>> userlist = new LinkedList<>();
        for (int i = 0; i < userlistmap.size(); i++) {
            Map<String, Object> usermap = userlistmap.get(i);
            String user_id = (String) usermap.get("userid");
            //获取总消费金额
            Map<String, Object> saletotalmap = salePst.getSumSaleTotalByUserId(user_id);
            BigDecimal saletotal = new BigDecimal(0.00);
            if (saletotalmap != null) {
                saletotal = (BigDecimal) saletotalmap.get("total");
            }
            //获取美丽基金
            Map<String, Object> balancemap = accountPst.getBalanceByUserId(user_id);
            BigDecimal balance = new BigDecimal(0.00);
            if (balancemap != null) {
                balance = (BigDecimal) balancemap.get("balance");
            }
            usermap.put("saletotal", saletotal);
            usermap.put("beautifultotal", balance);
            userlist.add(usermap);
        }
        returnData.put("users", userlist);
        return returnData;
    }
}
