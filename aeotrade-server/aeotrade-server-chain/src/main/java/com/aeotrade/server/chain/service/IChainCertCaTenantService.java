package com.aeotrade.server.chain.service;

import com.aeotrade.chainmaker.model.ChainCertCaTenant;
import com.aeotrade.server.chain.vo.ChainCertCaTenantVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 企业租户证书管理 服务类
 * </p>
 *
 * @author shougeji
 * @since 2022-05-19
 */
public interface IChainCertCaTenantService {

    ChainCertCaTenantVo findById(String memberId, String staffId,Integer pageNo,Integer pageSize,Integer isAdmin);

    ChainCertCaTenant getById(String id);

    Boolean findByRobotId(String memberId);

    Map<String, Object> getByUscc(String uscc) throws Exception;

    /**
     * 查询所有组织名称
     * @return
     */
    List<ChainCertCaTenant> getAllOrgIds();

    void bathUpdateChainCertCaTenant(List<ChainCertCaTenant> chainCertCaTenantList);
    void bathUpdateChainCertCaTenantSuccess(List<String> chainCertCaTenantList);
}
