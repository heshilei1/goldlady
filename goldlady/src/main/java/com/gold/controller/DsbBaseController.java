package com.gold.controller;

import com.alibaba.fastjson.JSONObject;
import com.gold.common.BizException;
import com.gold.common.BizReturnCode;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/2.
 */
public class DsbBaseController extends BaseController {
    //请求报文头
    private JSONObject RequestHead;
    //http请求
    private HttpServletRequest HttpRequest;
    //请求参数
    private JSONObject RequestParams;


    /**
     * 获取业务成功的returnMap
     *
     * @param data
     * @return
     */
    public LinkedHashMap<String, Object> getBizSuccessReturnMap(Object data) {
        LinkedHashMap<String, Object> bizData = getBizSuccessReturnData(data);
        LinkedHashMap<String, Object> returnMap = getDefaultSuccessReturnMap();
        returnMap.put(returnData, bizData);
        JSONObject returnHead = createReturnHead(getHttpRequest());
        returnMap.put("returnHead", returnHead);
        return returnMap;
    }

    /**
     * 获取业务失败的retrunMap
     *
     * @param ex
     * @return
     */
    public LinkedHashMap<String, Object> getBizErrorReturnMap(BizException ex) {
        LinkedHashMap<String, Object> bizData = getBizErrorReturnData(ex);
        LinkedHashMap<String, Object> returnMap = getDefaultSuccessReturnMap();
        returnMap.put(returnData, bizData);
        JSONObject returnHead = createReturnHead(getHttpRequest());
        returnMap.put("returnHead", returnHead);
        return returnMap;
    }

    /**
     * 获取业务成功的bizReturnData
     *
     * @param data
     * @return
     */
    public LinkedHashMap<String, Object> getBizSuccessReturnData(Object data) {
        LinkedHashMap<String, Object> bizData = new LinkedHashMap<String, Object>();
        bizData.put("bizReturnCode", BizReturnCode.Success);
        bizData.put("bizReturnMessage", "成功");
        bizData.put("bizIsSucceed", true);
        bizData.put("bizReturnData", data);
        return bizData;
    }

    /**
     * 获取业务失败的bizReturnData
     *
     * @param ex
     * @return
     */
    public LinkedHashMap<String, Object> getBizErrorReturnData(BizException ex) {
        LinkedHashMap<String, Object> bizData = new LinkedHashMap<String, Object>();
        bizData.put("bizReturnCode", ex.getErrorCode());
        bizData.put("bizReturnMessage", ex.getMessage());
        bizData.put("bizIsSucceed", false);
        Object data = ex.getReturnData();
        if (data != null) {
            bizData.put("bizReturnData", data);
        } else {
            bizData.put("bizReturnData", new JSONObject());
        }
        return bizData;
    }

    /**
     * 根据请求，创建返回报文头
     *
     * @param request
     * @return
     */
    public static JSONObject createReturnHead(HttpServletRequest request) {
        JSONObject returnHead = new JSONObject();
        if (request.getParameter("requestHead") != null) {
            returnHead = JSONObject.parseObject(request.getParameter("requestHead"));
        }
        returnHead.put("tmStamp", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.sss").format(new Date()));
        return returnHead;
    }

    /**
     * 获取没有返回数据的成功消息
     *
     * @return
     */
    public Map<String, Object> getNoDataSuccessMap() {
        Map<String, Object> returnMap = new HashMap<>();
        //returnMap.put("success",true);
        return returnMap;
    }

    public JSONObject getRequestParams() {
        return RequestParams;
    }

    public void setRequestParams(JSONObject requestParams) {
        RequestParams = requestParams;
    }

    public HttpServletRequest getHttpRequest() {
        return HttpRequest;
    }

    public void setHttpRequest(HttpServletRequest httpRequest) {
        HttpRequest = httpRequest;
    }

    public JSONObject getRequestHead() {
        return RequestHead;
    }

    public void setRequestHead(JSONObject requestHead) {
        RequestHead = requestHead;
    }

}
