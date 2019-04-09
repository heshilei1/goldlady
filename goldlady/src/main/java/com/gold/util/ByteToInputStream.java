package com.gold.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;  
import java.io.IOException;  
import java.io.InputStream;
/**
 * Byte和 InputStream互相转换
 *
 * @author zhengwei
 * @email wei139806@163.com
 * @date 2017/11/7 16:14
 */
public class ByteToInputStream {  
  
    public static final InputStream byte2InputStream(byte[] buf) {
        return new ByteArrayInputStream(buf);  
    }  
  
    public static final byte[] inputStream2byte(InputStream inStream)
            throws IOException {  
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();  
        byte[] buff = new byte[100];  
        int rc = 0;  
        while ((rc = inStream.read(buff, 0, 100)) > 0) {  
            swapStream.write(buff, 0, rc);  
        }  
        byte[] in2b = swapStream.toByteArray();  
        return in2b;  
    }
} 