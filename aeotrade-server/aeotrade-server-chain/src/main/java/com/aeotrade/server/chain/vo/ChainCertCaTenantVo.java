package com.aeotrade.server.chain.vo;

import com.aeotrade.chainmaker.model.ChainCertCaTenant;
import lombok.Data;

import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2022-05-26 13:28
 */
@Data
public class ChainCertCaTenantVo {
    private ChainCertCaTenant chainCertCaTenant;
    private String chainSdkCertId;
    private String cert;
    private String privateKey;
    private Boolean isChainAuth;
    private List<ChainCertUserMemberVO> chainCertUserMemberVOS;
    private Long total;
}
