package com.aeotrade.server.chain.service;

import com.aeotrade.server.chain.config.Page;
import com.aeotrade.server.chain.vo.ChainCertUserMemberVO;

import java.util.Map;

/**
 * <p>
 * 用户证书管理 服务类
 * </p>
 *
 * @author shougeji
 * @since 2022-05-19
 */
public interface ChainCertUserMemberService {

    Page<ChainCertUserMemberVO> listStaff(Integer pageNo, Integer pageSize, String staffId) throws Exception;

    int deleteStaff(String memberId, String staffId);

    Map<String,String> findUser(String memberId, String userId);
}
