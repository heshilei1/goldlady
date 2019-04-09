package com.gold.controller;

import com.alibaba.fastjson.JSONObject;
import com.gold.service.CheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/4.
 */
@Scope("prototype")
@Controller
@RequestMapping("/gold/check")
public class CheckController extends DsbBaseController {

    @Autowired
    CheckService checkService;

    /**
     * 检查是否绑定过微信
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/checkifbind", method = {RequestMethod.POST})
    public
    @ResponseBody
    Map<String, Object> checkIfBind() throws Exception {
        JSONObject inParams = getRequestParams();
        String code = inParams.getString("code");
        JSONObject res = inParams.getJSONObject("res");
        JSONObject wxuser = checkService.miniAppGetWxUserInfo(code, res);
        String openId = wxuser.getString("openId");
        String avatarUrl = wxuser.getString("avatarUrl");
        Map<String, Object> wxusermap = checkService.checkWxIfBind(openId);
        Map<String, Object> result = new HashMap<>();
        Boolean ifbind = true;
        if (wxusermap == null) {
            ifbind = false;
        } else {
            wxusermap.put("avatarUrl", avatarUrl);
            wxusermap.put("usertype", checkService.getUserTyp((String) wxusermap.get("userid")));
        }
        result.put("ifBind", ifbind);
        result.put("openid", openId);
        result.put("userInfo", wxusermap);
        return result;
    }

    /**
     * 绑定微信
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/wxbind", method = {RequestMethod.POST})
    public
    @ResponseBody
    Map<String, Object> wxBind() throws Exception {
        JSONObject inParams = getRequestParams();
        String phone = inParams.getString("phone");
        String pwd = inParams.getString("pwd");
        String openId = inParams.getString("openid");
        checkService.wxBind(phone, pwd, openId);
        return getNoDataSuccessMap();
    }

    /**
     * 新增点检记录
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/addcheck", method = {RequestMethod.POST})
    public
    @ResponseBody
    Map<String, Object> addCheck() throws Exception {
        JSONObject inParams = getRequestParams();
        checkService.addCheck(inParams);
        return getNoDataSuccessMap();
    }

    /**
     * 获取该用户提交的点检记录
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getcheck", method = {RequestMethod.POST})
    public
    @ResponseBody
    Map<String, Object> getCheck() throws Exception {
        JSONObject inParams = getRequestParams();
        String userId = inParams.getString("userid");
        return checkService.getCheck(userId);
    }

    /**
     * 获取点检详情图片
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getcheckdetailimage", method = {RequestMethod.POST})
    public
    @ResponseBody
    Map<String, Object> getCheckDetailImage(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        JSONObject inParams = getRequestParams();
        String checkId = inParams.getString("checkid");
        String serialno = inParams.getString("serialno");
        checkService.getCheckDetailImage(checkId, serialno, response);
        return null;
    }

    /**
     * 获取该用户提交的点检记录
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getcheckdetail", method = {RequestMethod.POST})
    public
    @ResponseBody
    Map<String, Object> getCheckDetail() throws Exception {
        JSONObject inParams = getRequestParams();
        String checkId = inParams.getString("checkid");
        return checkService.getCheckDetail(checkId);
    }

    /**
     * 获取商户所有区域及负责人
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getcheckregion", method = {RequestMethod.POST})
    public
    @ResponseBody
    Map<String, Object> getCheckRegion() throws Exception {
        return checkService.getCheckRegion();
    }

    /**
     * 上传图片
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/uploadpic", method = {RequestMethod.POST})
    public
    @ResponseBody
    Map<String, Object> uploadPic(HttpServletRequest request, @RequestParam(value = "img", required = false) MultipartFile file) throws Exception {
        return checkService.uploadPic(request, file);
    }

    /**
     * 获取点检详情图片
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getpic", method = {RequestMethod.GET})
    public
    @ResponseBody
    void getPic(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String minipicid = request.getParameter("minipicid");
        checkService.getPic(minipicid, response);
    }

    /**
     * 获取该用户负责的点检记录
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getcheckbyregion", method = {RequestMethod.POST})
    public
    @ResponseBody
    Map<String, Object> getCheckByRegion() throws Exception {
        JSONObject inParams = getRequestParams();
        String personliableid = inParams.getString("personliableid");
        return checkService.getCheckByRegion(personliableid);
    }

    /**
     * 店员获取他负责的点检记录
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/getusertypebyopenid", method = {RequestMethod.POST})
    public
    @ResponseBody
    Map<String, Object> getUserTypeByOpenId() throws Exception {
        JSONObject inParams = getRequestParams();
        String personliableid = inParams.getString("personliableid");
        return checkService.getCheckByRegion(personliableid);
    }

    /**
     * 店员完成点检
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/finishcheck", method = {RequestMethod.POST})
    public
    @ResponseBody
    Map<String, Object> finishCheck() throws Exception {
        JSONObject inParams = getRequestParams();
        String checkid = inParams.getString("checkid");
        checkService.finishCheck(checkid);
        return getNoDataSuccessMap();
    }
}
