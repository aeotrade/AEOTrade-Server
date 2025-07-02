package com.aeotrade.server.chain.service.impl;

import com.aeotrade.chainmaker.model.ChainCertCaTenant;
import com.aeotrade.chainmaker.model.ChainCertUserMember;
import com.aeotrade.chainmaker.model.ChainSdkCert;
import com.aeotrade.chainmaker.repository.ChainCertCaTenantMapper;
import com.aeotrade.chainmaker.repository.ChainCertUserMemberMapper;
import com.aeotrade.chainmaker.repository.ChainSdkCertMapper;
import com.aeotrade.server.chain.config.Page;
import com.aeotrade.server.chain.service.ChainCertUserMemberService;
import com.aeotrade.server.chain.vo.ChainCertUserMemberVO;
import com.aeotrade.utlis.HttpRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 用户证书管理 服务实现类
 * </p>
 *
 * @author shougeji
 * @since 2022-05-19
 */
@Service
public class ChainCertUserMemberServiceImpl implements ChainCertUserMemberService {
    @Autowired
    private ChainCertUserMemberMapper chainCertUserMemberMapper;
    @Autowired
    private ChainSdkCertMapper chainSdkCertMapper;
    @Autowired
    private ChainCertCaTenantMapper chainCertCaTenantMapper;
    @Override
    public Page<ChainCertUserMemberVO> listStaff(Integer pageNo, Integer pageSize, String staffId) throws Exception {
        Page<ChainCertUserMemberVO> chainCertUserMemberVOPage=new Page<>();
       /* Page<Map<String, Object>> mapPage = chainCertUserMemberMapper.selectMapsPage(new Page<>(pageNo, pageSize),
                Wrappers.<ChainCertUserMember>lambdaQuery()
                        .eq(ChainCertUserMember::getUserId, staffId)
                        .eq(ChainCertUserMember::getIsDel, false));*/

//        org.springframework.data.domain.Page<Map<String, Object>> mapPage=
//                chainCertUserMemberMapper.findByUserIdAndIsDel(PageRequest.of(pageNo, pageSize),String.valueOf(staffId),false);
        ChainCertUserMember chainCertUserMember=new ChainCertUserMember();
        chainCertUserMember.setUserId(staffId);
        if(pageNo <=0){
            pageNo = 1;
        }
        pageNo = pageNo -1;
        if(pageSize<=0){
            pageSize = 10;
        }
        Pageable pageable = PageRequest.of(pageNo,pageSize);

        ExampleMatcher matcher = ExampleMatcher.matching() //构建对象
                .withMatcher("tenantId", ExampleMatcher.GenericPropertyMatchers.contains()) //姓名采用“开始匹配”的方式查询
                .withMatcher("isDel", ExampleMatcher.GenericPropertyMatchers.contains()) //姓名采用“开始匹配”的方式查询
                .withIgnorePaths("id","uscc");  //忽略属性：是否关注。因为是基本类型，需要忽略掉
        Example<ChainCertUserMember> example = Example.of(chainCertUserMember, matcher);
        org.springframework.data.domain.Page<ChainCertUserMember> all = chainCertUserMemberMapper.findAll(example, pageable);
        chainCertUserMemberVOPage.setTotal(all.getTotalElements());
        List<ChainCertUserMemberVO> chainCertUserMemberVOList=new ArrayList<>();
        for (ChainCertUserMember certUserMember : all.getContent()) {
            ChainCertUserMemberVO chainCertUserMemberVO = new ChainCertUserMemberVO();
            //查询员工证书
            ChainSdkCert signCert = chainSdkCertMapper.findById((Long) certUserMember.getSignCertId()).get();
            ChainSdkCert tlsCert = chainSdkCertMapper.findById((Long) certUserMember.getTlsCertId()).get();
            Map<String, String> map = new HashMap<>();
            map.put("signcert", signCert.getCert());
            map.put("signprivatekey", signCert.getPrivateKey());
            map.put("tlscert", tlsCert.getCert());
            map.put("tlsprivatekey", tlsCert.getPrivateKey());
            chainCertUserMemberVO.setDid(certUserMember.getDid());
            //员工用户链上身份id
            chainCertUserMemberVO.setOrgId(signCert.getCertName());
            //员工用户创建时间
            chainCertUserMemberVO.setCreatTime((Date) certUserMember.getCreateAt());
            //员工用户类型
            chainCertUserMemberVO.setUserType(String.valueOf(certUserMember.getUserType()));
            //员工用户证书
            chainCertUserMemberVO.setStringStringMap(map);
            //组织名称
            chainCertUserMemberVO.setOrganName(certUserMember.getTenantName());
            chainCertUserMemberVOList.add(chainCertUserMemberVO);
        }
        chainCertUserMemberVOPage.setRecords(chainCertUserMemberVOList);
        return chainCertUserMemberVOPage;
    }

    @Override
    public int deleteStaff(String memberId, String staffId) {
        ChainCertUserMember top1ByTenantIdAndUserIdAndIsDel = chainCertUserMemberMapper.findTop1ByTenantIdAndUserIdAndIsDel(memberId, staffId, false);
        if(null!=top1ByTenantIdAndUserIdAndIsDel){
            chainSdkCertMapper.delete(chainSdkCertMapper.findById(top1ByTenantIdAndUserIdAndIsDel.getTlsCertId()).get());
            chainSdkCertMapper.delete(chainSdkCertMapper.findById(top1ByTenantIdAndUserIdAndIsDel.getSignCertId()).get());
            chainCertUserMemberMapper.delete(top1ByTenantIdAndUserIdAndIsDel);
            return 1;
        }
        return 0;
    }

    @Override
    public Map<String, String> findUser(String memberId, String userId) {
        Map<String,String> map=new HashMap<>();
        ChainCertUserMember userIdAndIsDel = chainCertUserMemberMapper.findTop1ByTenantIdAndUserIdAndIsDel(memberId, userId, false);
        if(userIdAndIsDel!=null){
            Optional<ChainSdkCert> signCert = chainSdkCertMapper.findById(userIdAndIsDel.getSignCertId());
            Optional<ChainSdkCert> tlsCert = chainSdkCertMapper.findById(userIdAndIsDel.getTlsCertId());
            ChainCertCaTenant top1ByTenantIdAndIsDel = chainCertCaTenantMapper.findTop1ByTenantIdAndIsDel(memberId, false);
            map.put("orginName",top1ByTenantIdAndIsDel.getCaOrgId());
            map.put("certName",signCert.get().getCertName());
            map.put("signCertCert",signCert.get().getCert());
            map.put("signCertPrivateKey",signCert.get().getPrivateKey());
            map.put("tlsCertCert",tlsCert.get().getCert());
            map.put("tlsCertPrivateKey",tlsCert.get().getPrivateKey());
        }
        return map;
    }
}
