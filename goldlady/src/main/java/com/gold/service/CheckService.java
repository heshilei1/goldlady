package com.gold.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gold.common.BizException;
import com.gold.common.BizReturnCode;
import com.gold.common.Utility;
import com.gold.pst.CheckPst;
import com.gold.util.AESUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/2/4.
 */
@Service("checkService")
@Scope("prototype")
public class CheckService {

    @Autowired
    CheckPst checkPst;

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public JSONObject miniAppGetWxUserInfo(String code, JSONObject res)
            throws Exception {
        JSONObject result = new JSONObject();
        // 参数解析
        String encryptedData = res.getString("encryptedData");
        String iv = res.getString("iv");
        //初始化
        String openid = "";
        String session_key = "";
        JSONObject sessionobject = new JSONObject();
        try {
            String retunmsg = "success";
            //根据code获取session-key
            if (code == null) {
                retunmsg = "参数不完整。";
            } else {
                String appId = "wxc2762359e47a653f";
                String appSecret = "e5694377d4e64d8138ce1022101137a4";
                String caturl = "https://api.weixin.qq.com/sns/jscode2session";
                caturl =
                        caturl + "?appid=" + appId + "&secret=" + appSecret + "&js_code=" + code
                                + "&grant_type=authorization_code";
                JSONObject catreqjo = new JSONObject();
                sessionobject = Utility.HttpPostJSON(caturl, catreqjo.toJSONString());
                openid = sessionobject.getString("openid");
                session_key = sessionobject.getString("session_key");
            }
            //对用户数据进行解密
            JSONObject decodeData = decodeUserInfo(openid, iv, session_key, encryptedData);
            Boolean decodesuccess = (Boolean) decodeData.get("result");
            JSONObject userinfo = new JSONObject();
            if (decodesuccess) {
                userinfo = decodeData.getJSONObject("userinfo");
                retunmsg = "encryptedData解密成功";
            } else {
                retunmsg = "encryptedData解密失败";
            }
            //获取头像url
            String avatarUrl = userinfo.getString("avatarUrl");
            result.put("openId", openid);
            result.put("avatarUrl", avatarUrl);
        } catch (Exception ex) {
            throw (ex);
        }
        return result;
    }

    /**
     * 解密微信小程序用户信息
     */
    public JSONObject decodeUserInfo(String openid, String iv, String session_key,
                                     String encryptedData) throws Exception {
        JSONObject result = new JSONObject();
        Boolean issuccess = false;
        try {
            AESUtil aes = new AESUtil();
            byte[] data = Base64.decodeBase64(encryptedData);
            byte[] key = Base64.decodeBase64(session_key);
            byte[] iv_a = Base64.decodeBase64(iv);
            byte[] resultByte = aes.decrypt(data, key, iv_a);
            if (null != resultByte && resultByte.length > 0) {
                String userInfo = new String(resultByte, "UTF-8");
                JSONObject userjson = JSONObject.parseObject(userInfo);
                result.put("userinfo", userjson);
                issuccess = true;
            }
            result.put("result", issuccess);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> checkWxIfBind(String openId) throws Exception {
        return checkPst.checkWxIfBind(openId);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public String getUserTyp(String userId) throws Exception {
        String userType = checkPst.getUserType(userId);
        if (StringUtils.isEmpty(userType)) {
            throw new BizException(BizReturnCode.UserNotExistError, "该用户不存在");
        }
        return userType;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void wxBind(String phone, String pwd, String openId) throws Exception {
        //根据手机号与密码获取用户信息
        Map<String, Object> usermap = checkPst.getUserInfoByPhone(phone, pwd);
        if (null == usermap) {
            throw new BizException(BizReturnCode.PwdError, "密码错误");
        }
        String userId = (String) usermap.get("userid");
        String userName = (String) usermap.get("username");
        //绑定微信
        checkPst.wxBind(openId, userId, phone, userName);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void addCheck(JSONObject jsonObject) throws Exception {
        JSONObject checkjson = jsonObject.getJSONObject("checkdata");
        JSONArray checkdetailjson = jsonObject.getJSONArray("checkdetaildata");
        String checkId = Utility.generateId();
        if (checkjson != null) {
            //保存主表
            saveCheck(checkjson, checkId);
        }
        if (checkdetailjson.size() > 0) {
            //保存明细
            saveCheckDetail(checkdetailjson, checkId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void saveCheck(JSONObject json, String checkId) throws Exception {
        String userId = json.getString("userid");
        String checkTime = json.getString("checktime");
        String checkRegion = json.getString("checkregion");
        String personLiable = json.getString("personliable");
        String personliableid = json.getString("personliableid");
        checkPst.saveCheck(checkId, userId, checkTime, checkRegion, personLiable, personliableid);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void saveCheckDetail(JSONArray jsonArray, String checkId)
            throws Exception {
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject json = jsonArray.getJSONObject(i);
            String description = json.getString("description");
            String image = json.getString("image");
            checkPst.saveCheckDetail(checkId, String.valueOf(i), description, image);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getCheck(String userId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        List list = new LinkedList();
        List<Map<String, Object>> checkList = checkPst.getCheck(userId);
        for (int i = 0; i < checkList.size(); i++) {
            Map<String, Object> checkMap = checkList.get(i);
            String checkId = (String) checkMap.get("checkid");
            //获取明细
            List<Map<String, Object>> detaillist = checkPst.getCheckDetail(checkId);
            if (detaillist.size() > 0) {
                checkMap.put("checkdetail", detaillist);
            }
            list.add(checkMap);
        }
        result.put("check", checkList);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void getCheckDetailImage(String checkId, String serialno, HttpServletResponse response)
            throws Exception {
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Type", "image/jpeg");
        checkPst.getCheckDetailImage(checkId, serialno, response.getOutputStream());
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getCheckDetail(String checkId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        //获取明细
        List<Map<String, Object>> detaillist = checkPst.getCheckDetail(checkId);
        if (detaillist.size() > 0) {
            result.put("checkdetail", detaillist);
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getCheckRegion() throws Exception {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> regionList = checkPst.getCheckRegion();
        result.put("checkregionlist", regionList);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> uploadPic(HttpServletRequest request, MultipartFile file)
            throws Exception {
        Map<String, Object> result = new HashMap<>();
        if (file != null) {
            InputStream in = file.getInputStream();
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            byte[] buff = new byte[100];
            int rc = 0;
            while ((rc = in.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
            byte[] in2b = swapStream.toByteArray();
            String miniPicId = java.util.UUID.randomUUID().toString();
            checkPst.saveMiniPic(miniPicId, in2b);
            result.put("miniPicId", miniPicId);
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void getPic(String minipicid, HttpServletResponse response)
            throws Exception {
        response.setHeader("Content-Type", "image/jpeg");
        OutputStream out = response.getOutputStream();
        checkPst.getPic(minipicid, out);
        out.flush();
        out.close();
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getCheckByRegion(String personliableid) throws Exception {
        Map<String, Object> result = new HashMap<>();
        List list = new LinkedList();
        List<Map<String, Object>> checkList = checkPst.getCheckByRegion(personliableid);
        for (int i = 0; i < checkList.size(); i++) {
            Map<String, Object> checkMap = checkList.get(i);
            String checkId = (String) checkMap.get("checkid");
            //获取明细
            List<Map<String, Object>> detaillist = checkPst.getCheckDetail(checkId);
            if (detaillist.size() > 0) {
                checkMap.put("checkdetail", detaillist);
            }
            list.add(checkMap);
        }
        result.put("check", checkList);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void finishCheck(String checkId) throws Exception {
        //检查该点检记录是否存在
        Map<String, Object> checkmap = checkPst.getCheckByCheckId(checkId);
        if (checkmap == null) {
            throw new BizException(BizReturnCode.NoCheckError, "该点检记录不存在");
        }
        checkPst.finishCheck(checkId);
    }
}
