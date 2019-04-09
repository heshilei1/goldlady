package com.gold.common;

import com.alibaba.fastjson.JSONObject;
import com.gold.controller.DsbBaseController;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by hsl on 2017/11/2.
 */
@Aspect
@Component
public class ControllerAspect {


    Logger logger = Logger.getLogger(ControllerAspect.class);


    @Pointcut(value = "execution(* com.gold.controller..*(..)) && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void controllerMethodPointcut(){}
    /**
     * 对MnsController方法的包围处理
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("controllerMethodPointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        DsbBaseController controller = (DsbBaseController)pjp.getThis();
        //http请求
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        controller.setHttpRequest(request);
        JSONObject requestParams = JSONObject.parseObject(request.getParameter("requestParams"));
        JSONObject requestHead = JSONObject.parseObject(request.getParameter("requestHead"));
        logger.info("请求地址："+request.getRequestURL());
        //报文头处理
        if(request.getParameter("requestHead")!=null) {
            controller.setRequestHead(requestHead);
            logger.info("请求报文头："+requestHead.toString());
        }
        //请求参数处理
        if(request.getParameter("requestParams")!=null) {
            controller.setRequestParams(requestParams);
            logger.info("请求参数：" + requestParams.toString());
        }

        try {
            //原处理过程！
            Object returnObj = pjp.proceed();
            //业务正常报文
            Map<String,Object> returnMap = controller.getBizSuccessReturnMap(returnObj);
            logger.info("返回数据："+returnMap.toString());
            return returnMap;
        }
        catch (BizException ex)
        {
            //业务异常报文
            Map<String,Object> returnMap = controller.getBizErrorReturnMap(ex);
            logger.info("返回业务异常："+returnMap.toString()+"\r\n");
            return returnMap;
        }
        catch (Exception ex)
        {
            //其他异常抛出
            throw(ex);
        }
    }
    private Object getBean(String name)
    {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        WebApplicationContext ac= RequestContextUtils.getWebApplicationContext(request, request.getSession().getServletContext());
        return ac.getBean(name);
    }
}
