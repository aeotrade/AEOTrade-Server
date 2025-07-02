package com.aeotrade.server.chain.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.chainmaker.pb.common.ContractOuterClass;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Data
@EqualsAndHashCode
@NoArgsConstructor
public class BizContractParam {

    private String userId;
    private String chainId;

    /**
     * 合约名
     */
    private String contractName;

    /**
     * 版本号
     */
    private String version;
    /**
     * 合约运行环境
     */
    private ContractOuterClass.RuntimeType runtimeType;
    /**
     * 合约初始化参数 后面将MAP换成实体接收参数
     */
//    private Map<String, byte[]> params;
    /**
     * 合约字节数组
     */
    private MultipartFile byteCodes;
}
