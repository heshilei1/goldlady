package com.gold.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期处理
 *
 * @author wangyang
 * @email 1471191500@qq.com
 * @date 2017年8月22日 下午10:11:27
 */
public class DateUtils {
	/** 时间格式(yyyy-MM-dd) */
	public final static String DATE_PATTERN = "yyyy-MM-dd";
	/** 时间格式(yyyy-MM-dd HH:mm:ss) */
	public final static String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
	
	public static String formatDate(Date date) {
        return format(date, DATE_PATTERN);
    }

    public static String formatDateTime(Date date) {
        return format(date, DATE_TIME_PATTERN);
    }

    public static String format(Date date, String pattern) {
        if(date != null){
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            return df.format(date);
        }
        return null;
    }

    public static Date string2date(String date) throws ParseException {
        if(date != null){
            SimpleDateFormat sdf=new SimpleDateFormat(DATE_TIME_PATTERN);
            Date time =sdf.parse(date);
            return time;
        }
        return null;
    }

    public static String timestamp2string(Timestamp ts, String pattern){
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        String str = df.format(ts);
        return str;
    }

    public static void main(String[] args) {
        //1510244544000
        Timestamp ts = new Timestamp(new Long("1510244544000"));
        System.out.println(ts.toString());
        String s = timestamp2string(ts, "yyyy-MM-dd");
        System.out.println(s);
    }

}
