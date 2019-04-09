package com.gold.common;

/**
 * Created by hsl on 2017/11/2.
 */
public class BizReturnCode {
    public static String Success = "MNS-00000";//成功
    public static String DefaultError = "MNS-00100";//默认错误
    public static String NoWeixinConfigError = "MNS-00001";//无微信配置错误
    public static String SuperiorUserNotExistError = "MNS-00002";//上级用户不存在错误
    public static String UserPhoneRepeatError = "MNS-00003";//手机号重复
    public static String SendShortMessageError = "MNS-00004";//发送短信失败
    public static String PhoneCodeError = "MNS-00005";//手机验证码错误
    public static String UserNotExistError = "MNS-00006";//用户不存在错误
    public static String UserLevelLow = "MNS-00007";//用户权限不足
    public static String ThereArePendingApplicationsForWithdrawal = "MNS-00008";//您有未处理的提现申请，请等待处理完成后再进行提现
    public static String PhoneNotExistError = "MNS-00009";//手机号不存在错误
    public static String PhoneNotHaveExpenseDetailError = "MNS-00010";//手机号不存在消费记录
    public static String InputConsumeInfoFailedError = "MNS-00011";//录入消费信息失败
    public static String NoPhoneError = "MNS-00012";//未绑定手机号错误
    public static String WithdrawlsError = "MNS-00013";//提现失败
    public static String HandleWithdrawlsError = "MNS-00014";//处理提现失败
    public static String BalanceNotEnoughToWithdrawls = "MNS-00015";//余额不足以提现
    public static String OrderIsHandled = "MNS-00016";//该订单已提现,不能重复提现
    public static String NoPermission = "MNS-00017";//无权限
    public static String SameActiveType = "MNS-00018";//是否禁用类型相同
    public static String QuestionsCannotBeAnsweredRepeatedly = "MNS-00019";//问题已回答,不能重复回答
    public static String PhoneIsBindError = "MNS-00020";//手机号已被绑定
    public static String NoWeixinUserError = "MNS-00021";//该微信user不存在
    public static String JgMsgCodeError = "MNS-00022";//极光短信验证码验证错误
    public static String PwdError = "MNS-00023";//密码错误
    public static String NoCheckError = "MNS-00025";//点检记录不存在
}
