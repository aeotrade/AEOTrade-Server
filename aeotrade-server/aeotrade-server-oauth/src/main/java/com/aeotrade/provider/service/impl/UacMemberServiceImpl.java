package com.aeotrade.provider.service.impl;

import com.aeotrade.provider.mapper.UacMemberMapper;
import com.aeotrade.provider.mapper.UacMemberStaffMapper;
import com.aeotrade.provider.mapper.UacStaffMapper;
import com.aeotrade.provider.model.UacMember;
import com.aeotrade.provider.model.UacMemberStaff;
import com.aeotrade.provider.service.UacMemberService;
import com.aeotrade.service.MqSend;
import com.aeotrade.utlis.HttpRequestUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>
 * 企业表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Service
public class UacMemberServiceImpl extends MPJBaseServiceImpl<UacMemberMapper, UacMember> implements UacMemberService {
    @Autowired
    private MqSend mqSend;
    @Value("${hmtx.lianjieqi:}")
    private String lianjieqi;
    @Autowired
    private UacMemberStaffMapper uacMemberStaffMapper;
    public void sheng(String memberId) throws Exception {
        List<UacMember> all=new ArrayList<>();
        SimpleDateFormat SDFormat = new SimpleDateFormat("MMddHHmmssSSS");
        if(StringUtils.isNotEmpty(memberId)){
            UacMember uacMember = baseMapper.selectById(Long.valueOf(memberId));
            all.add(uacMember);
        }else{
            LambdaQueryWrapper<UacMember> uacMemberLambdaQueryWrapper=new LambdaQueryWrapper<>();
            uacMemberLambdaQueryWrapper.ne(UacMember::getStatus,1).notIn(UacMember::getKindId,88,99);
            all = baseMapper.selectList(uacMemberLambdaQueryWrapper);
        }
        for (UacMember uacMember : all) {
            HashMap<String, String> chain = new HashMap<>();
            chain.put("tenantId", String.valueOf(uacMember.getId()));
            chain.put("tenantName", uacMember.getMemberName());
            chain.put("uscc", uacMember.getUscCode());
            chain.put("creatTime", SDFormat.format(uacMember.getCreatedTime()));
            chain.put("userType", "员工");
            chain.put("userId", String.valueOf(uacMember.getStaffId()));
            chain.put("roleCodeRulesEnum", "01");
            chain.put("chainId", "aeotradechain");
            chain.put("userTypeEnum", "管理员");
            mqSend.sendChain(JSONObject.toJSONString(chain), "chain");
            LambdaQueryWrapper<UacMemberStaff> uacMemberStaffLambdaQueryWrapper=new LambdaQueryWrapper<>();
            uacMemberStaffLambdaQueryWrapper.eq(UacMemberStaff::getMemberId,uacMember.getId());
            List<UacMemberStaff> listByMemberId = uacMemberStaffMapper.selectList(uacMemberStaffLambdaQueryWrapper);
            if (null != listByMemberId || listByMemberId.size() != 0) {
                for (UacMemberStaff uacMemberStaff : listByMemberId) {
                    if (uacMember.getStaffId() != uacMemberStaff.getStaffId()) {
                        HashMap<String, String> chainStaff = new HashMap<>();
                        chainStaff.put("tenantId", String.valueOf(uacMember.getId()));
                        chainStaff.put("tenantName", uacMember.getMemberName());
                        chainStaff.put("uscc", uacMember.getUscCode());
                        chainStaff.put("creatTime", SDFormat.format(uacMember.getCreatedTime()));
                        chainStaff.put("userType", "员工");
                        chainStaff.put("userId", String.valueOf(uacMemberStaff.getStaffId()));
                        chainStaff.put("roleCodeRulesEnum", "01");
                        chainStaff.put("chainId", "aeotradechain");
                        chainStaff.put("userTypeEnum", "员工");
                        mqSend.sendChain(JSONObject.toJSONString(chainStaff), "chain");
                    }
                }
            }
        }
    }

    public void shenglianjieqi() throws Exception {
        SimpleDateFormat SDFormat = new SimpleDateFormat("MMddHHmmssSSS");
        LambdaQueryWrapper<UacMember> uacMemberLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacMemberLambdaQueryWrapper.ne(UacMember::getStatus,1).notIn(UacMember::getKindId,88,99);
        List<UacMember> all = baseMapper.selectList(uacMemberLambdaQueryWrapper);
        for (UacMember uacMember : all) {
            Map<String, Object> map = new HashMap<>();
            map.put("memberName", uacMember.getMemberName());
            map.put("uscCode", uacMember.getUscCode());
            map.put("staffName", uacMember.getStaffName());
            map.put("memberId",uacMember.getId());
            map.put("userId",uacMember.getStaffId());
            if (!org.springframework.util.StringUtils.isEmpty(lianjieqi)) {
                HttpRequestUtils.httpPost(lianjieqi, map);
            }
        }
    }

    public String saveRobotCcie(String uscc) {
        LambdaQueryWrapper<UacMember> uacMemberLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacMemberLambdaQueryWrapper.eq(UacMember::getUscCode,uscc).ne(UacMember::getStatus,1).notIn(UacMember::getKindId,88,99);
        List<UacMember> byUscCode = baseMapper.selectList(uacMemberLambdaQueryWrapper);
        if(null!=byUscCode && byUscCode.size()!=0){
            HashMap<String, String> chain = new HashMap<>();
            chain.put("tenantId", String.valueOf(byUscCode.get(0).getId()));
            chain.put("tenantName", byUscCode.get(0).getMemberName());
            chain.put("uscc", byUscCode.get(0).getUscCode());
            SimpleDateFormat SDFormat = new SimpleDateFormat("MMddHHmmssSSS");
            chain.put("creatTime", SDFormat.format(new Date()));
            chain.put("userType", "机器人");
            chain.put("userId", "swai");
            chain.put("roleCodeRulesEnum", "01");
            chain.put("chainId", "aeotradechain");
            chain.put("userTypeEnum", "员工");
            mqSend.sendChain(JSONObject.toJSONString(chain), "chain");
            return null;
        }
        return uscc;
    }

}
