package com.aeotrade.server.chain.vo;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CertConnInitVo {
    //链标识，chainid
    private String chainId;
    //组织证书
    private MultipartFile caSignKeyFile;
    private MultipartFile caSignCertFile;
    //节点RPC地址
    private String nodeAddr;
    //管理员证书
    private MultipartFile adminSignKeyFile;
    private MultipartFile adminSignCertFile;
    private MultipartFile adminTlsKeyFile;
    private MultipartFile adminTlsCertFile;
    //RPC连接是否启用双向TLS认证
    private Boolean enableTls;
    //TLS hostname
    private String tlsHostName;
    //租户名字
    private String tenantName;
    //企业统一信用代码
    private String uscc;

}
