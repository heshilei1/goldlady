package com.gold.common;

/**
 * Created by hsl on 2017/11/2.
 */
public class BizException extends Exception {

    private String errorCode = BizReturnCode.DefaultError;  //返回码
    private Object returnData = null;

    public BizException() {

        super();

    }

    public BizException(String returnCode, String message) {
        super(message);
        setErrorCode(returnCode);
    }

    public BizException(String returnCode, String message, Object data) {
        super(message);
        setErrorCode(returnCode);
        setReturnData(data);
    }

    public BizException(String msg) {

        super(msg);

    }

    public BizException(String msg, Throwable cause) {

        super(msg, cause);

    }

    public BizException(Throwable cause) {

        super(cause);

    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Object getReturnData() {
        return returnData;
    }

    public void setReturnData(Object returnData) {
        this.returnData = returnData;
    }


}
