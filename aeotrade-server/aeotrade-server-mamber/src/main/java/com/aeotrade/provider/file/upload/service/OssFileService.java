package com.aeotrade.provider.file.upload.service;

import com.aeotrade.base.constant.OssConstatnt;
import com.aeotrade.provider.file.upload.dto.UploadFileResponse;
import com.aeotrade.provider.file.upload.exception.FileException;
import com.aeotrade.provider.file.upload.property.FileProperties;
import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Author: yewei
 * @Date: 2020/5/13 14:10
 */
@Service
@Slf4j
public class OssFileService {
    public static final String IMG_GATEWAY_SHOW_URL = "/img/oss/show/";
    private final Path fileStorageLocation;
    @Autowired
    private FileProperties fileProperties;
    @Value("${hmtx.uacmanager}")
    private String uacmanager;

    @Autowired
    public OssFileService(FileProperties fileProperties) {
        this.fileStorageLocation = Paths.get(Paths.get("..").toAbsolutePath().normalize().toString(),
                "upload").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public byte[] getFile(String bucketName, String key) throws IOException {
        byte[] filebytes = null;
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        conf.setSocketTimeout(200000);
        conf.setConnectionTimeout(200000);
        conf.setConnectionRequestTimeout(20000);
        conf.setIdleConnectionTime(200000);
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(OssConstatnt.endpoint_internal, OssConstatnt.accessKeyId, OssConstatnt.accessKeySecret,conf);

        // 获取Object，返回结果为OSSObject对象
        OSSObject object = ossClient.getObject(bucketName, key);

        // 获取Object的输入流,可读取此输入流获取其内容。
        InputStream objectContent = object.getObjectContent();

        if (objectContent != null) {
            // 数据读取完成后，获取的流必须关闭，否则会造成连接泄漏，导致请求无连接可用，程序无法正常工作。
            filebytes = IOUtils.toByteArray(objectContent);
            objectContent.close();
        }

        // 关闭OSSClient。
        ossClient.shutdown();

        return filebytes;

    }

    public UploadFileResponse uploadFileOss(MultipartFile file) throws IOException {

        OSS ossClient = new OSSClientBuilder().build(OssConstatnt.endpoint, OssConstatnt.accessKeyId, OssConstatnt.accessKeySecret);

        ObjectMetadata meta = new ObjectMetadata();
        /**设置文件类型*/
        meta.setContentType(file.getContentType());

        String s=sha256Filename(file);

        /**上传*/
        ossClient.putObject(OssConstatnt.bucketName,s,file.getInputStream(),meta);
        /**设置过期时间*/
        //Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
        /**获取url*/
        //URL urls = ossClient.generatePresignedUrl(OssConstatnt.bucketName, s, expiration);

        ossClient.shutdown();

        return ossFile(file,OssConstatnt.bucketName,s);

    }

    public List<UploadFileResponse> uploadFilesOss(MultipartFile[] file) throws IOException {
        List<UploadFileResponse> list = new ArrayList<>();
        for (int i = 0;i<file.length;i++){
            String contentType =file[i].getContentType();
            String originalFilename = file[i].getOriginalFilename();
            String substring = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            if(substring.equals("xlsx")){
                contentType ="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            }else if(substring.equals("xlsx")){
                contentType="application/vnd.ms-excel";
            }else if (substring.equals("doc")){
                contentType="application/msword";
            }else if (substring.equals("docx")){
                contentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }else if(substring.equals("jpg") ||substring.equals("jpge") || substring.equals("png")||substring.equals("svg")){
                contentType="image/jpg";
            }
            OSS ossClient = new OSSClientBuilder().build(OssConstatnt.endpoint_internal, OssConstatnt.accessKeyId, OssConstatnt.accessKeySecret);
            ObjectMetadata meta = new ObjectMetadata();
            /**设置文件类型*/
            meta.setContentType(contentType);

            String s=sha256Filename(file[i]);

            /**上传*/
            ossClient.putObject(OssConstatnt.bucketName,s,file[i].getInputStream(),meta);
            /**设置过期时间*/
            //Date expiration = new Date(System.currentTimeMillis() + 3600L * 1000 * 24 * 365 * 10);
            /**获取url*/
            //URL urls = ossClient.generatePresignedUrl(OssConstatnt.bucketName, s, expiration);
            ossClient.shutdown();

            UploadFileResponse upload = ossFile(file[i],OssConstatnt.bucketName,s);

            list.add(upload);
        }
        return list;
    }

    private UploadFileResponse ossFile(MultipartFile file,String bucketName,String ossfilename){

        String localurl="/oss/"+bucketName+"/"+ossfilename;
        String urls = "https://www.aeotrade.com/aeoapi/img" + localurl;

        return  new UploadFileResponse(file.getOriginalFilename(),bucketName,sizeFile(file.getSize()),urls,localurl,
                file.getContentType(),file.getSize(),new Timestamp(System.currentTimeMillis()));
    }


    private String sha256Filename(MultipartFile file){
        MessageDigest digest = null;
        InputStream in= null;
        try {
            in = file.getInputStream();
        } catch (IOException e) {
            log.warn(e.getMessage());
        }

        byte[] buffer = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            while ((len =in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0,len);
            }
            in.close();
        } catch (Exception e) {
            log.warn(e.getMessage());
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        if(!file.getOriginalFilename().contains(".")){
            return bigInt.toString(16)+file.getOriginalFilename();
        }else {
            return bigInt.toString(16)+file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        }

    }

    public void downloadFiles(List<UploadFileResponse>  files, HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<String> collect = files.stream().map(i -> i.getFileDownloadUri()).collect(Collectors.toList());
        String[] urls = collect.toArray(new String[collect.size()]);

        // 响应头的设置
        response.reset();
        response.setCharacterEncoding("utf-8");
        response.setContentType("multipart/form-data");

        // 设置压缩包的名字
        // 解决不同浏览器压缩包名字含有中文时乱码的问题
        String downloadName = System.currentTimeMillis()+".zip";
        String agent = request.getHeader("USER-AGENT");
        try {
            if (agent.contains("MSIE") || agent.contains("Trident")) {
                downloadName = URLEncoder.encode(downloadName, StandardCharsets.UTF_8);
            } else {
                downloadName = new String(downloadName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
        response.setHeader("Content-Disposition", "attachment;fileName=\"" + downloadName + "\"");

        // 设置压缩流：直接写入response，实现边压缩边下载
        ZipOutputStream zipos = null;
        try {
            zipos = new ZipOutputStream(new BufferedOutputStream(response.getOutputStream()));
            zipos.setMethod(ZipOutputStream.DEFLATED); // 设置压缩方法
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        // 循环将文件写入压缩流
        DataOutputStream os = null;
        for (UploadFileResponse fl :files ) {

            String fileName = fl.getFileName();
            File file = getFileByUrl(fl.getFileDownloadUri(), fileName);
            try {
                // 添加ZipEntry，并ZipEntry中写入文件流
                // 这里，加上i是防止要下载的文件有重名的导致下载失败
                zipos.putNextEntry(new ZipEntry(fileName));
                os = new DataOutputStream(zipos);
                InputStream is = new FileInputStream(file);
                byte[] b = new byte[100];
                int length = 0;
                while ((length = is.read(b)) != -1) {
                    os.write(b, 0, length);
                }
                is.close();
                zipos.closeEntry();
            } catch (IOException e) {
                log.warn(e.getMessage());
            }
        }

        // 关闭流
        try {
            os.flush();
            os.close();
            zipos.close();
        } catch (IOException e) {
            log.warn(e.getMessage());
        }

    }


    //url转file
    private File getFileByUrl(String fileUrl, String suffix) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        BufferedOutputStream stream = null;
        InputStream inputStream = null;
        File file = null;
        try {
            URL imageUrl = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            inputStream = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            file = File.createTempFile("file", suffix);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fileOutputStream);
            stream.write(outStream.toByteArray());
        } catch (Exception e) {
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (stream != null) {
                    stream.close();
                }
                outStream.close();
            } catch (Exception e) {
            }
        }
        return file;
    }


    /**
     * 文件的显示大小，如: 234 KB
     * @param length
     * @return
     */
    public String sizeFile(Long length){
        if (length<1024){
            return length+" bytes";
        }
        //小于1M
        if (length< 1048576L){
            return BigDecimal.valueOf(length).divide(BigDecimal.valueOf(1024))
                    .setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()+" KB";
        }
        //小于1G
        if (length< 1048576L *1024){
            return BigDecimal.valueOf(length).divide(BigDecimal.valueOf(1048576))
                    .setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()+" MB";
        }

        return BigDecimal.valueOf(length).divide(BigDecimal.valueOf(1048576*1024))
                .setScale(2, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()+" GB";
    }

    public UploadFileResponse uploadFile(MultipartFile file) {
        String fileName = this.storeFile(file,
                String.valueOf(LocalDateTime.now().atOffset(ZoneOffset.of("Z")).toInstant().toEpochMilli()));

        UriComponentsBuilder uriComponentsBuilder = null;
        if (StringUtils.isEmpty(fileProperties.getLocation())) {
            uriComponentsBuilder = ServletUriComponentsBuilder.fromPath("/");
        } else {
            uriComponentsBuilder = ServletUriComponentsBuilder.fromHttpUrl(fileProperties.getLocation());
        }
        String fileDownloadUri = uriComponentsBuilder
                .path(IMG_GATEWAY_SHOW_URL)
                .path(fileName)
                .toUriString();
        return new UploadFileResponse(file.getOriginalFilename(), "", sizeFile(file.getSize()), uacmanager + fileDownloadUri, fileDownloadUri,
                file.getContentType(), file.getSize(), new Timestamp(System.currentTimeMillis()));
    }


    /**
     * 加载文件
     *
     * @param fileName 文件名
     * @return 文件
     */
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileException("File not found " + fileName, ex);
        }
    }


    public String storeFile(MultipartFile file, String filename) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(filename)
                .concat(file.getOriginalFilename().substring(
                        file.getOriginalFilename().indexOf(".")));

        try {
            if (fileName.contains("..")) {
                throw new FileException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }


    public List<UploadFileResponse> uploadFiles(MultipartFile[] file) {
        return Arrays.stream(file)
                .map(this::uploadFile)
                .collect(Collectors.toList());
    }
}
