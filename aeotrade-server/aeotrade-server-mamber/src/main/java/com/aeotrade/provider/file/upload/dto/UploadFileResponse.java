package com.aeotrade.provider.file.upload.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;
//上传文件后，返回实体信息
public class UploadFileResponse {
    //文件名称
    private String fileName;
    //OSS目录名称
    private String bucketName;
    //显示文件大小，如：2KB
    private String showFileSize;
    //文件全路径URL
    private String fileDownloadUri;
    //文件项目路径地址，可拼接项目地址
    private String fileLocalUrl;
    //文件类型
    private String fileType;
    //文件实际大小
    private long size;
    //上传的时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private Timestamp time;

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public UploadFileResponse(String fileName, String fileDownloadUri, String fileType, long size) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileType = fileType;
        this.size = size;
    }

    public UploadFileResponse(String fileName, String fileDownloadUri,String fileLocalUrl, String fileType, long size,Timestamp time) {
        this.fileName = fileName;
        this.fileDownloadUri = fileDownloadUri;
        this.fileLocalUrl = fileLocalUrl;
        this.fileType = fileType;
        this.size = size;
        this.time = time;
    }

    public UploadFileResponse(String fileName, String bucketName, String showFileSize, String fileDownloadUri,
                              String fileLocalUrl, String fileType, long size, Timestamp time) {
        this.fileName = fileName;
        this.bucketName = bucketName;
        this.showFileSize = showFileSize;
        this.fileDownloadUri = fileDownloadUri;
        this.fileLocalUrl = fileLocalUrl;
        this.fileType = fileType;
        this.size = size;
        this.time = time;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileDownloadUri() {
        return fileDownloadUri;
    }

    public void setFileDownloadUri(String fileDownloadUri) {
        this.fileDownloadUri = fileDownloadUri;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFileLocalUrl() {
        return fileLocalUrl;
    }

    public void setFileLocalUrl(String fileLocalUrl) {
        this.fileLocalUrl = fileLocalUrl;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getShowFileSize() {
        return showFileSize;
    }

    public void setShowFileSize(String showFileSize) {
        this.showFileSize = showFileSize;
    }
}
