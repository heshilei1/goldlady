package com.gold.model;

/**
 * Created by zhengwei on 2017/11/25.
 */
public enum QuestionStatus {
    NOT_ANSWER(0,"未回答"),
    ALREADY_ANSWER(1,"已回答"),
    ;
    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    QuestionStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
