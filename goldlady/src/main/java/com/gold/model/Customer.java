package com.gold.model;

import java.io.Serializable;

/**
 * Created by zhengwei on 2017/11/11.
 */
public class Customer implements Serializable {
    private String userid;
    private Integer type;

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
