package com.aeotrade.provider.file.upload.controller;

import com.aeotrade.provider.file.upload.dto.UploadFileResponse;
import com.aeotrade.provider.file.upload.property.FileProperties;
import com.aeotrade.provider.file.upload.property.FileTypeEnum;
import com.aeotrade.provider.file.upload.service.OssFileService;
import com.aeotrade.suppot.BaseController;
import com.aeotrade.suppot.RespResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @Author: yewei
 * @Date: 2020/5/13 14:09
 */
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/oss")
public class OssFileController  extends BaseController {
    @Autowired
    private FileProperties fileProperties;
    @Autowired
    private OssFileService ossFileService;


    //单个上传文件
    @PostMapping(value = "/upload", consumes = "multipart/*", headers = "content-type=multipart/form-data")
    public RespResult uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (fileProperties.getType() != null && fileProperties.getType().equals(FileTypeEnum.LOCAL.name())) {
            return handleResult(ossFileService.uploadFile(file));
        }
        return handleResult(ossFileService.uploadFileOss(file));
    }


    //批量上传文件
    @PostMapping(value = "/uploads", consumes = "multipart/*", headers = "content-type=multipart/form-data")
    public RespResult uploadFiles(@RequestParam("file") MultipartFile[] file) throws IOException {
        if (fileProperties.getType() != null && fileProperties.getType().equals(FileTypeEnum.LOCAL.name())) {
            return handleResult(ossFileService.uploadFiles(file));
        }
        return handleResult(ossFileService.uploadFilesOss(file));

    }
    //批量下载文件
    @PostMapping("/downloads")
    public void downloadFiles( @RequestBody List<UploadFileResponse> file,HttpServletRequest request, HttpServletResponse response) throws IOException{
        try {
            ossFileService.downloadFiles(file,request,response);
        }catch (Exception e){
            log.warn(e.getMessage());
        }
    }
    //查看OSS的文件
    @GetMapping("/{decBucketName}/{key}")
    public ResponseEntity inlineFile(@PathVariable String decBucketName, @PathVariable String key) {
        try {
            byte[] bytes = ossFileService.getFile(decBucketName,key);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", key);
            headers=getHttpHeaders(headers,key,1);
            return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            log.warn(e.getMessage());
            return ResponseEntity.ok(handleFail(e));
        }

    }

    //下载OSS的单个文件
    @PostMapping("/{decBucketName}/{key}")
    public ResponseEntity downFile(@PathVariable String decBucketName,@PathVariable String key){
        try {
            byte[] bytes = ossFileService.getFile(decBucketName,key);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", key);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.warn(e.getMessage());
            return ResponseEntity.ok(handleFail(e));
        }

    }

    //下载文件
    @GetMapping("/downloadBill//{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = ossFileService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.warn(ex.getMessage());
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/show//{fileName:.+}")
    public ResponseEntity<Resource> showImage(
            @PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = ossFileService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.warn(ex.getMessage());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("X-Frame-Options", "")
                .body(resource);
    }

    private HttpHeaders getHttpHeaders(HttpHeaders headers,String fileName,int type /**1:online;2:download*/){
        if (type==1){
            headers.setContentDisposition(ContentDisposition.parse("inline"));
        }else {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return headers;
        }
        //获取文件的后缀名
        String suffix = fileName.substring(fileName.lastIndexOf(".")+1).toUpperCase();
        switch (suffix){
            case "PDF":{
                headers.setContentType(MediaType.APPLICATION_PDF);
                break;
            }
            case "GIF":{
                headers.setContentType(MediaType.IMAGE_GIF);
                break;
            }
            case "JPEG":
            case "JPG": {
                headers.setContentType(MediaType.IMAGE_JPEG);
                break;
            }
            case "PNG": {
                headers.setContentType(MediaType.IMAGE_PNG);
                break;
            }
            default:{
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
        }
        return headers;
    }
}
