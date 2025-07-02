package com.aeotrade.provider.mamber.utils;

/**
 * @Author: yewei
 * @Date: 2020/4/1 14:50
 */

import com.aeotrade.base.constant.OssConstatnt;
import com.aliyun.oss.OSSClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Slf4j
public class ImageUtil {


    private static String bucketName ;
    private static String endpoint ;
    private static  String accessKeySecret ;
    private static String  accessKeyId ;

    /**
     * 单个图片上传
     * @param inputStream
     * @return
     */
    public static String  uploadFile(InputStream inputStream){
        if(bucketName == null){
            return null;
        }
        /**oss上传*/
        OSSClient ossClient = new OSSClient(endpoint, OssConstatnt.accessKeyId, OssConstatnt.accessKeySecret);
        String s = UUID.randomUUID() +".png";
        ossClient.putObject(bucketName,s,inputStream);
        // 设置URL过期时间为10年  3600l* 1000*24*365*10
        Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
        //获取url
        URL urls = ossClient.generatePresignedUrl(bucketName, s, expiration);
        ossClient.shutdown();
        return urls.toString().split("\\?")[0];

    }
    /**
     * 多个图片上传
     * @param file
     * @return
     * @throws IOException
     */
    public static List<String>  uploadFiles(MultipartFile[] file) throws IOException {
        List<String > list = new ArrayList<>();
        for(int i =0;i<file.length;i++) {
            /**oss上传*/
            OSSClient ossClient = new OSSClient(endpoint, OssConstatnt.accessKeyId, OssConstatnt.accessKeySecret);
            String s = UUID.randomUUID() + ".png";
            ossClient.putObject(bucketName, s, file[i].getInputStream());
            // 设置URL过期时间为10年  3600l* 1000*24*365*10
            Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
            //获取url
            URL urls = ossClient.generatePresignedUrl(bucketName, s, expiration);
            ossClient.shutdown();
            list.add(urls.toString().split("\\?")[0]);
        }
        return list;
    }
    /**
     * 将html中的图片下载到服务器，并且使用服务器上图片的地址替换图片的网络路径
     * @param html 要处理的html
     * @param request
     * @param uploadFolder 服务器上保存图片的目录
     * @return
     */
    public static String transHtml(String html,HttpServletRequest request,String uploadFolder){
        List<String> imgList = getImgStrList(html,request);

        for (String imgStr : imgList) {
            try {
                String newUrl = reSaveImage(imgStr,uploadFolder);
                html = html.replace(imgStr, newUrl);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
        return html;
    }

    public static String UrlDownload(String httpUrl){
        HttpURLConnection connection = null;
        String wxUrl="";
        try {
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            /**oss上传*/
            OSSClient ossClient = new OSSClient(endpoint, OssConstatnt.accessKeyId, OssConstatnt.accessKeySecret);
            String s = UUID.randomUUID() +".png";
            ossClient.putObject(bucketName,s,connection.getInputStream());
            // 设置URL过期时间为10年  3600l* 1000*24*365*10
            Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
            //获取url
            URL urls = ossClient.generatePresignedUrl(bucketName, s, expiration);
            wxUrl = urls.toString();
            ossClient.shutdown();
        }catch (Exception e){

            log.warn(e.getMessage());
        } finally {
            //  in.close();
            // out.close();
            connection.disconnect();
        }

        return wxUrl;

    }

    /**
     * 将指定的网络图片保存到本地指定目录
     * @param httpUrl 图片原来的网络路径
     * @param uploadFolder 服务器上保存图片的目录
     * @return httpUrl newPath
     */
    private static String reSaveImage(String httpUrl,String uploadFolder){
        HttpURLConnection connection = null;
        Map<String, Object> urlMap = new HashMap<>();

        byte[] buf = new byte[1024];
        int len = 0;
        String wxUrl="";
        try {

            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            /**oss上传*/
            OSSClient ossClient = new OSSClient(endpoint, OssConstatnt.accessKeyId, OssConstatnt.accessKeySecret);
            String s = UUID.randomUUID() +".png";
            ossClient.putObject(bucketName,s,connection.getInputStream());
            // 设置URL过期时间为10年  3600l* 1000*24*365*10
            Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
            //获取url
            URL urls = ossClient.generatePresignedUrl(bucketName, s, expiration);
            wxUrl = urls.toString().split("\\?")[0];
            ossClient.shutdown();
        } catch (Exception e) {
            log.warn(e.getMessage());
        } finally {
            //  in.close();
            // out.close();
            connection.disconnect();
        }

        return wxUrl;
    }

    /**
     * 获取保存在服务器上的图片的实际存储地址以及访问地址
     * @param httpUrl 图片原来的网络路径
     * @param request
     * @param
     * @return
     */
    private static Map<String, Object> getNewPath(String httpUrl, HttpServletRequest request,String uploadFolder) {
        Map<String, Object> relMap = new HashMap<>();
        String fileName = getFileName();
        String filefix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();// 文件后缀
        fileName = UUID.randomUUID() + "." + filefix;
        String prefix = getUrlPrefix(request) + "/";
        //String prefix = "http://192.168.1.6/";

        relMap.put("newUrl", prefix + fileName);
        relMap.put("newPath", uploadFolder + fileName);
        return relMap;
    }

    /**
     * 设置图片的名称（时间+用户编号）
     * @return
     */
    private static String getFileName() {
        return "reload"+File.pathSeparator+System.currentTimeMillis()+".jpg";
    }

    /**
     * 提取HTML字符串中的img
     * @param htmlStr 要处理的html字符串
     * @return
     */
    private static List<String> getImgStrList(String htmlStr,HttpServletRequest request) {
        List<String> list = new ArrayList<>();
        String img = "";
        Pattern p_image;
        Matcher m_image;
        String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
        p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
        m_image = p_image.matcher(htmlStr);
        while (m_image.find()) {
            img = m_image.group();
            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
            while (m.find()) {
                String path = m.group(1);
                if(!path.startsWith(getUrlPrefix(request))){
                    list.add(handleSrc(path));
                }
            }
        }
        return list;
    }

    /**
     * 去除src路径中的前后引号
     * @param src 图片的src路径
     * @return
     */
    private static String handleSrc(String src) {
        if (src != null) {
            if (src.startsWith("'")|| src.startsWith("\"")) {
                return src.substring(1);
            }
            if (src.endsWith("'")|| src.endsWith("\"")) {
                return src;
            }
        }
        return src;
    }

    /**
     * 获取网站的URL
     * @param request
     * @return 例如：http://192.168.11.3:8089
     */
    public static String getUrlPrefix(HttpServletRequest request) {
        StringBuffer str = new StringBuffer();
        str.append(request.getScheme());
        str.append("://");
        str.append(request.getServerName());
        if (80 != request.getServerPort()) {
            str.append(":" + request.getServerPort());
        }
        str.append(request.getContextPath());
        return str.toString();
    }
}
