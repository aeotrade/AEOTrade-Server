package com.aeotrade.server.chain.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.chainmaker.pb.common.ContractOuterClass;
import org.springframework.web.multipart.MultipartFile;

@Data
@EqualsAndHashCode
@NoArgsConstructor
public class BizVoteContractParam {
    /**
     * 需要投票总数
     */
    private Integer voteTall;
    /**
     * 合约名
     */
    private String contractName;
    /**
     * 投票组织标识
     */
    private String orgId;
    /**
     * 合约ID
     */
    private Long contractId;
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
//    private Map<String, byte[]> params=new HashMap<>();
    /**
     * 合约字节数组
     */
    private MultipartFile byteCodes;
}
