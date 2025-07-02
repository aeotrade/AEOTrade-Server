package com.aeotrade.server.chain.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.chainmaker.pb.common.ContractOuterClass;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BizContractRes {
    private String name;
    private String version;
    private ContractOuterClass.RuntimeType runtimeType;
}
