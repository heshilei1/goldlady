package com.gold.util;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;

/**
 * Created by hsl on 2017/11/3.
 */
@XStreamAlias("ScanCodeInfo")
public class ScanCodeInfo implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    @XStreamAlias("ScanType")
    private String ScanType;
    @XStreamAlias("ScanResult")
    private String ScanResult;

    public String getScanType() {
        return ScanType;
    }

    public void setToUserName(String scanType) {
        ScanType = scanType;
    }

    public String getScanResult() {
        return ScanResult;
    }

    public void setScanResult(String scanResult) {
        ScanResult = scanResult;
    }
}
