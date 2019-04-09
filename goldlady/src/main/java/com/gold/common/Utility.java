package com.gold.common;

import com.alibaba.fastjson.JSONObject;
import com.gold.util.MatrixToImageWriter;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by user on 2017/11/3.
 */
public class Utility {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Utility.class);

    public static NodeList GetXmlNodeList(String xmltext, String xpathexp) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        StringReader sr = new StringReader(xmltext);
        InputSource is = new InputSource(sr);
        Document doc = dBuilder.parse(is);
        doc.getDocumentElement().normalize();

        XPath xPath = XPathFactory.newInstance().newXPath();

        NodeList nodeList = (NodeList) xPath.compile(xpathexp).evaluate(doc, XPathConstants.NODESET);
        return nodeList;
    }

    public static String GetXmlNodeValue(String xmltext, String xpathexp) throws Exception {
        NodeList nodeList = GetXmlNodeList(xmltext, xpathexp);
        return nodeList.item(0).getTextContent();
    }

    //生成二维码图片
    public static void CreateQRCode(String content, OutputStream stream) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            Map<EncodeHintType, String> hints = new HashMap<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix
                    bitMatrix =
                    multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 400, 400, hints);
            bitMatrix = deleteWhite(bitMatrix);//删除白边
            MatrixToImageWriter.writeToStream(bitMatrix, "jpg", stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //生成条形码图片
    public static void CreateBarCode(String content, OutputStream stream) {
        try {
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            Map<EncodeHintType, String> hints = new HashMap<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            BitMatrix
                    bitMatrix =
                    multiFormatWriter.encode(content, BarcodeFormat.CODE_128, 400, 1, hints);
            MatrixToImageWriter.writeToStream(bitMatrix, "jpg", stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //删除条形码白边
    private static BitMatrix deleteWhite(BitMatrix matrix) {
        int[] rec = matrix.getEnclosingRectangle();
        int resWidth = rec[2] + 1;
        int resHeight = rec[3] + 1;

        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
        resMatrix.clear();
        for (int i = 0; i < resWidth; i++) {
            for (int j = 0; j < resHeight; j++) {
                if (matrix.get(i + rec[0], j + rec[1])) {
                    resMatrix.set(i, j);
                }
            }
        }
        return resMatrix;
    }

    public static JSONObject HttpGetJSON(String url) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        try {
            HttpResponse res = client.execute(get);
            String responseContent = null; // 响应内容
            HttpEntity entity = res.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
            System.out.print("请求结果：" + responseContent + "\r\n");
            JSONObject json = JSONObject.parseObject(responseContent);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                if (json.getString("errcode") != null) {
                    // 错误时微信会返回错误码等信息，{"errcode":40013,"errmsg":"invalid appid"}
                    String errcode = json.getString("errcode");
                    String errmsg = json.getString("errmsg");
                    System.out.print("调用微信接口" + url + "\r\n发生错误：" + errcode + " " + errmsg + "\r\n");
                } else {
                    return json;
                }
            }
        } catch (Exception e) {
            throw (e);
        } finally {
            // 关闭连接 ,释放资源
            client.getConnectionManager().shutdown();
        }
        return null;
    }

    public static JSONObject HttpPostJSON(String url, String jsoncontent) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        try {
            StringEntity s = new StringEntity(jsoncontent, "UTF-8");
            s.setContentType("application/json");
            post.setEntity(s);
            HttpResponse res = client.execute(post);
            String responseContent = null; // 响应内容
            HttpEntity entity = res.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
            System.out.print("请求结果：" + responseContent + "\r\n");
            JSONObject json = JSONObject.parseObject(responseContent);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                if (json.getInteger("errcode") != null && json.getInteger("errcode") != 0) {
                    // 错误时微信会返回错误码等信息，{"errcode":40013,"errmsg":"invalid appid"}
                    String errcode = json.getString("errcode");
                    String errmsg = json.getString("errmsg");
                    String msg = "调用微信接口" + url + "\r\n发生错误：" + errcode + " " + errmsg + "\r\n";
                    System.out.print(msg);
                    throw (new Exception(msg));
                } else {
                    return json;
                }
            }
        } catch (Exception e) {
            throw (e);
        } finally {
            // 关闭连接 ,释放资源
            client.getConnectionManager().shutdown();
        }
        return null;
    }

    public static JSONObject HttpUpload(String url, String filepath) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        try {
            // 把文件转换成流对象FileBody
            FileBody bin = new FileBody(new File(filepath));
            MultipartEntity multientity = new MultipartEntity();
            multientity.addPart("media", bin);
            post.setEntity(multientity);
            HttpResponse res = client.execute(post);
            String responseContent = null; // 响应内容
            HttpEntity entity = res.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
            System.out.print("上传文件返回结果：" + responseContent + "\r\n");
            JSONObject json = JSONObject.parseObject(responseContent);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                if (json.getInteger("errcode") != null
                        && json.getInteger("errcode") != 0) {
                    // 错误时微信会返回错误码等信息，{"errcode":40013,"errmsg":"invalid appid"}
                    String errcode = json.getString("errcode");
                    String errmsg = json.getString("errmsg");
                    String msg = "调用微信接口" + url + "\r\n发生错误：" + errcode + " "
                            + errmsg + "\r\n";
                    System.out.print(msg);
                    throw (new Exception(msg));
                } else {
                    return json;
                }
            }
        } catch (Exception e) {
            throw (e);
        } finally {
            // 关闭连接 ,释放资源
            client.getConnectionManager().shutdown();
        }
        return null;
    }


    public static String generateId() {
        String id = String.valueOf(IdWork.INSTANCE.getId());
        logger.info("获取ID为：" + id);
        return id;
    }

    public static String getRandom() throws Exception {
        int i = 1;//i在此程序中只作为判断是否是将随机数添加在首位，防止首位出现0；
        Random r = new Random();
        int tag[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        String rnumber = "";
        int temp = 0;

        while (rnumber.length() < 4) {
            temp = r.nextInt(10);//取出0(含)~10(不含)之间任意数
            if (i == 1 && temp == 0) {
                continue;
            } else {
                i = 2;
                if (tag[temp] == 0) {
                    rnumber = rnumber + temp;
                    tag[temp] = 1;
                }
            }
        }
        String str = rnumber.toLowerCase();
        return str;
    }

    /**
     * get获取bytes（二维码用）
     *
     * @param url
     * @author zhengwei
     * @time 2017/11/7 21:50
     */
    public static byte[] HttpGetByteArray(String url) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        try {
            HttpResponse res = client.execute(get);
            HttpEntity entity = res.getEntity();
            byte[] bytes = EntityUtils.toByteArray(entity);
            return bytes;
        } catch (Exception e) {
            throw (e);
        } finally {
            // 关闭连接 ,释放资源
            client.getConnectionManager().shutdown();
        }
    }

    /**
     * 上传微信素材库
     *
     * @param url
     * @param data
     * @return
     * @throws Exception
     */
    public static JSONObject HttpUploadByteArray(String url, byte[] data) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        try {
            File file = new File(WeChatConfig.FILE_PATH);
            OutputStream output = new FileOutputStream(file);
            BufferedOutputStream bufferedOutput = new BufferedOutputStream(output);
            bufferedOutput.write(data);
            // 把文件转换成流对象FileBody
            FileBody bin = new FileBody(file);
            MultipartEntity multientity = new MultipartEntity();
            multientity.addPart("media", bin);
            post.setEntity(multientity);
            HttpResponse res = client.execute(post);
            String responseContent = null; // 响应内容
            HttpEntity entity = res.getEntity();
            responseContent = EntityUtils.toString(entity, "UTF-8");
            System.out.print("上传文件返回结果：" + responseContent + "\r\n");

            JSONObject json = JSONObject.parseObject(responseContent);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                if (json.getInteger("errcode") != null && json.getInteger("errcode") != 0) {
                    // 错误时微信会返回错误码等信息，{"errcode":40013,"errmsg":"invalid appid"}
                    String errcode = json.getString("errcode");
                    String errmsg = json.getString("errmsg");
                    String msg = "调用微信接口" + url + "\r\n发生错误：" + errcode + " " + errmsg + "\r\n";
                    System.out.print(msg);
                    throw (new Exception(msg));
                } else {
                    return json;
                }
            }
        } catch (Exception e) {

            throw (e);
        } finally {
            // 关闭连接 ,释放资源
            client.getConnectionManager().shutdown();
        }
        return null;
    }

    public static Map<String, Object> timestampToString(Map<String, Object> map) throws Exception {
        Map newMap = new HashMap();
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Timestamp) {
                    DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String timedata = sdf.format(value);
                    map.put(key, timedata);
                }
            }
        }
        newMap = map;
        return newMap;
    }

    public static List timestampToString(List<Map<String, Object>> list) throws Exception {
        List newlist = new LinkedList();
        if (list.size() > 0 && list != null) {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> oneMap = (Map<String, Object>) list.get(i);
                newlist.add(timestampToString(oneMap));
            }
        }
        return newlist;
    }

}
