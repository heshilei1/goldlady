package com.gold.controller;

import com.alibaba.fastjson.JSONObject;
import com.gold.model.SystemFunRes;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by hsl on 2017/11/2.
 */
public class BaseController {
    Logger logger = Logger.getLogger(BaseController.class);
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    public static String webAppPath ="";

    public HttpSession getSession(){
        return request.getSession();
    }

    @ModelAttribute
    public void setRequestResponse(HttpServletRequest _request,HttpServletResponse _response){
        _response.setHeader("Access-Control-Allow-Origin","*");
        this.request=_request;
        this.response=_response;
        this.response.setCharacterEncoding("UTF-8");

    }

    public final String  returnCode="returnCode";
    public final String  returnMessage="returnMessage";
    public final String  isSucceed="isSucceed";
    public final String  returnData="returnData";

    public LinkedHashMap<String,Object> getDefaultSuccessReturnMap(){
        LinkedHashMap<String,Object> resultDataMap=new LinkedHashMap<String,Object>();
        resultDataMap.put(returnCode, SystemFunRes.SUCCESS_CODE);
        resultDataMap.put(returnMessage, SystemFunRes.SUCCESS_MESSAGE);
        resultDataMap.put(isSucceed,true);
        resultDataMap.put(returnData, getReturnData());
        return resultDataMap;
    }

    public LinkedHashMap<String,Object> getDefaultSuccessReturnMap(Map<String,Object> data){
        LinkedHashMap<String,Object> resultDataMap=new LinkedHashMap<String,Object>();
        resultDataMap.put(returnCode, SystemFunRes.SUCCESS_CODE);
        resultDataMap.put(returnMessage, SystemFunRes.SUCCESS_MESSAGE);
        resultDataMap.put(isSucceed,true);
        resultDataMap.put(returnData, data);
        return resultDataMap;
    }

    public LinkedHashMap<String,Object> getParamNeedReturnMap(String...paramNames){
        LinkedHashMap<String,Object> resultDataMap=new LinkedHashMap<String,Object>();
        resultDataMap.put(isSucceed, false);
        resultDataMap.put(returnCode, SystemFunRes.NEED_CODE);
        String returnMe = SystemFunRes.NEED_MESSAGE;
        for(int i=0;i<paramNames.length;i++){
            returnMe += paramNames[i]+";";
        }
        resultDataMap.put(returnMessage, returnMe);
        return resultDataMap;
    }

    public LinkedHashMap<String,Object> getParamNeedReturnMap(JSONObject json, String...paramNames){
        LinkedHashMap<String,Object> resultDataMap=new LinkedHashMap<String,Object>();
        resultDataMap.put(isSucceed, false);
        resultDataMap.put(returnCode, SystemFunRes.NEED_CODE);
        String returnNeedMessage="";
        for(int i=0;i<paramNames.length;i++){
            if(!json.containsKey(paramNames[i]))
                returnNeedMessage+=getReturnMessage(SystemFunRes.NEED_MESSAGE,paramNames[i])+"\r\n";
        }
        resultDataMap.put(returnMessage, returnNeedMessage);
        return resultDataMap;
    }
    //错误信息map
    public LinkedHashMap<String,Object> getErrorInfoReturnMap(Map<String,Object> data,String... paramNames){
        LinkedHashMap<String,Object> resultDataMap=new LinkedHashMap<String,Object>();
        resultDataMap.put(isSucceed, false);
        resultDataMap.put(returnCode, SystemFunRes.NEED_CODE);
        String reMessage=SystemFunRes.ERROR_CODE;
        for(int i=0;i<paramNames.length;i++){
            reMessage += paramNames[i];
        }
        resultDataMap.put(returnMessage, reMessage);
        return resultDataMap;
    }

    //获取returndata的数据
    private String getReturnData(){
        String data = "";
        return data;
    }
    public String getReturnMessage(String format,Object...shoulds){
        return  MessageFormat.format(format,shoulds);
    }

}
