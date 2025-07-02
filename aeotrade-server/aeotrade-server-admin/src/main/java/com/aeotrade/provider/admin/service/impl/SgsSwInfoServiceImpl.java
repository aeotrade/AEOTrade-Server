package com.aeotrade.provider.admin.service.impl;


import com.aeotrade.provider.admin.entiy.UacMember;
import com.aeotrade.base.constant.SgsConstant;
import com.aeotrade.provider.admin.entiy.SgsApply;
import com.aeotrade.provider.admin.entiy.SgsSwInfo;
import com.aeotrade.provider.admin.mapper.SgsSwInfoMapper;
import com.aeotrade.provider.admin.service.SgsSwInfoService;
import com.aeotrade.provider.admin.uacVo.OnlyACParm;
import com.aeotrade.provider.admin.uacVo.OnlyDto;
import com.aeotrade.service.MqSend;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * <p>
 * 单一窗口认证表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Service
public class SgsSwInfoServiceImpl extends ServiceImpl<SgsSwInfoMapper, SgsSwInfo> implements SgsSwInfoService {
    @Autowired
    private SgsApplyServiceImpl sgsInfoMapper;
    @Autowired
    private UacMemberServiceImpl uacMemberMapper;
    @Autowired
    private MqSend mqSend;
    @Value("${hmtx.messageDetails:}")
    private String messageDetails;

    public OnlyACParm onlyAc(OnlyDto onlyDto) throws Exception {
        try {
            UacMember uacMember = uacMemberMapper.lambdaQuery()
                    .eq(UacMember::getUscCode,onlyDto.getUscc())
                    .eq(UacMember::getStatus,0)
                    .eq(UacMember::getKindId,1L).one();
            if (null == uacMember) {
                return new OnlyACParm(0, "认证企业不存在");
            }
//            onlyDto.setMemberId(uacMember.getId());
            int i = updateMemberByUscc(uacMember,onlyDto);
            if (i == 1) {
                HashMap<String, String> map = new HashMap<>();
                map.put("receive_type", "2");
                map.put("receive_id", String.valueOf(uacMember.getId()));
                map.put("receive_name", uacMember.getMemberName());
                map.put("details_type", "2");
                map.put("details_button","立即前往");
                map.put("message_details",messageDetails);
                map.put("message_source","基础平台");
                map.put("template_number","GG2024000002");
                mqSend.sendMessage(JSONObject.toJSONString(map),"GG2024000002");
                return new OnlyACParm(i, "认证成功");
            } else {
                return new OnlyACParm(i, "认证失败,信用代码不正确");
            }
        } catch (Exception e) {
            return new OnlyACParm(0, "认证失败");
        }
    }

    private int updateMemberByUscc( UacMember uacMember,OnlyDto onlyDto) {
        UacMember member = uacMemberMapper.getById(onlyDto.getMemberId());
        if (member.getUscCode().equals(onlyDto.getUscc())) {
            SgsApply sgsInfo = new SgsApply();
            sgsInfo.setMemberId(onlyDto.getMemberId());
            sgsInfo.setUserType(SgsConstant.SgsParent.MEMBER.getValue());
            sgsInfo.setSgsType(SgsConstant.SgsType.MEMBER_ONLY.getValue());
            sgsInfo.setSgsStatus(SgsConstant.SgsStatus.TONGGUO.getValue());
            sgsInfo.setSgsTypeName("企业单一窗口认证");
            sgsInfo.setMemberName(onlyDto.getMemberName());
            sgsInfo.setUscc(onlyDto.getUscc());
            sgsInfo.setCreatedTime(LocalDateTime.now());
            sgsInfo.setUpdatedTime(LocalDateTime.now());
            sgsInfo.setStatus(0);
            sgsInfoMapper.save(sgsInfo);
            SgsSwInfo sgsMemberOnly = new SgsSwInfo();
            sgsMemberOnly.setMemberId(onlyDto.getMemberId());
            sgsMemberOnly.setSgsStatus(SgsConstant.SgsStatus.TONGGUO.getValue());
            sgsMemberOnly.setMemberName(onlyDto.getMemberName());
            sgsMemberOnly.setUscc(onlyDto.getUscc());
            sgsMemberOnly.setSgsApplyId(sgsInfo.getId());
            sgsMemberOnly.setRevision(1);
            uacMember.setSgsStatus(1);
            uacMember.setRevision(1);
            if (StringUtils.isNotEmpty(onlyDto.getMemberName())) {
                sgsMemberOnly.setMemberName(onlyDto.getMemberName());
                uacMember.setMemberName(onlyDto.getMemberName());
            }
            if (StringUtils.isNotEmpty(onlyDto.getUscc())) {
                sgsMemberOnly.setUscc(onlyDto.getUscc());
                uacMember.setUscCode(onlyDto.getUscc());
            }
            sgsMemberOnly.setStatus(0);
            this.save(sgsMemberOnly);
            uacMemberMapper.updateById(uacMember);
            return 1;
        } else {
            SgsApply sgsInfo = new SgsApply();
            sgsInfo.setMemberId(onlyDto.getMemberId());
            sgsInfo.setUserType(SgsConstant.SgsParent.MEMBER.getValue());
            sgsInfo.setSgsType(SgsConstant.SgsType.MEMBER_ONLY.getValue());
            sgsInfo.setSgsStatus(SgsConstant.SgsStatus.SHIBAI.getValue());
            sgsInfo.setSgsTypeName("企业单一窗口认证");
            sgsInfo.setMemberName(onlyDto.getMemberName());
            sgsInfo.setUscc(onlyDto.getUscc());
            sgsInfo.setStatus(0);
            sgsInfo.setCreatedTime(LocalDateTime.now());
            sgsInfo.setUpdatedTime(LocalDateTime.now());
            sgsInfoMapper.save(sgsInfo);
            SgsSwInfo sgsMemberOnly = new SgsSwInfo();
            sgsMemberOnly.setMemberId(onlyDto.getMemberId());
            sgsMemberOnly.setSgsStatus(SgsConstant.SgsStatus.SHIBAI.getValue());
            sgsMemberOnly.setMemberName(onlyDto.getMemberName());
            sgsMemberOnly.setUscc(onlyDto.getUscc());
            sgsMemberOnly.setSgsApplyId(sgsInfo.getId());
            sgsMemberOnly.setStatus(0);
            this.save(sgsMemberOnly);
            return 0;
        }
    }
}
