package com.aeotrade.provider.service.async;

import com.aeotrade.base.constant.AeoConstant;
import com.aeotrade.base.constant.BizConstant;
import com.aeotrade.provider.dto.PmsTopicDto;
import com.aeotrade.provider.mapper.UacMemberMapper;
import com.aeotrade.provider.mapper.UacStaffMapper;
import com.aeotrade.provider.model.UacMember;
import com.aeotrade.provider.model.UacStaff;
import com.aeotrade.provider.service.feign.PmsFeign;
import com.aeotrade.provider.util.ThreadPoolUtils;
import com.aeotrade.suppot.RespResult;
import com.aeotrade.utlis.DateUtil;
import com.aeotrade.utlis.JacksonUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Generated;
import java.io.IOException;

@Generated("titan.lightbatis.web.generate.ServiceBeanSerializer")
@Component
public class StaffMemberAsync {

    @Autowired
    private PmsFeign pmsFeign;
    @Autowired
    private UacStaffMapper uacStaffMapper;
    @Autowired
    private UacMemberMapper uacMemberMapper;

    /**
     * 新注册用户完善基本信息
     */
    @Async(AeoConstant.ASYNC_POOL)
    @Transactional(rollbackFor = Exception.class)
    public void additionalStaffMember(Long xpid,Long memberId,Long staffId,String wxLogo,String wxOpenid,String unionid) throws IOException {
        if (xpid==null||memberId==null||staffId==null)return;


        ThreadPoolUtils.execute(new Runnable() {
            @Override
            public void run() {


            }
        });

        RespResult respResult=pmsFeign.findPmsTopicById(xpid);
        if (respResult!=null&&respResult.getCode()==respResult.SUCCESS_CODE&&respResult.getResult()!=null){
            PmsTopicDto pmsTopicDto= new PmsTopicDto();
            try {
                pmsTopicDto=JacksonUtil.parseJson(JacksonUtil.toJson(respResult.getResult()),PmsTopicDto.class);
            } catch (IOException e) {

                throw e;
            }
            //PmsTopicDto pmsTopicDto= (PmsTopicDto) respResult.getResult();
            //完善企业名称和手机号
            UacMember uacMember=uacMemberMapper.selectById(memberId);
            UacMember uacMemberTemp=new UacMember();
            uacMemberTemp.setId(uacMember.getId());
            uacMemberTemp.setUpdatedTime(DateUtil.getData().toLocalDateTime());
            uacMemberTemp.setEmail(pmsTopicDto.getSedMail());
            uacMemberTemp.setStasfTel(pmsTopicDto.getSedTel());
            uacMemberTemp.setStaffName(pmsTopicDto.getSedName());
            uacMemberTemp.setMemberName(pmsTopicDto.getSedMemberName());
            uacMemberTemp.setRevision(uacMember.getRevision()+1);
            uacMemberTemp.setStatus(0);
            if (StringUtils.isBlank(uacMemberTemp.getEmail())) {
                uacMemberTemp.setEmail(pmsTopicDto.getSedMail());
            }
            if (StringUtils.isBlank(uacMemberTemp.getLogoImg())) {
                uacMemberTemp.setLogoImg(wxLogo);
            }
            uacMemberMapper.updateById(uacMemberTemp);
            //完善员工的手机号
            UacStaff uacStaff=uacStaffMapper.selectById(staffId);
            UacStaff uacStaffTemp=new UacStaff();
            uacStaffTemp.setId(uacStaff.getId());
            uacStaffTemp.setStaffName(pmsTopicDto.getSedName());
            uacStaffTemp.setTel(pmsTopicDto.getSedTel());
            uacStaffTemp.setUpdatedTime(DateUtil.getData().toLocalDateTime());
            uacStaffTemp.setRevision(uacStaff.getRevision()+1);
            uacStaffTemp.setStatus(0);
            if(StringUtils.isBlank(uacStaffTemp.getWxLogo())) {
                uacStaffTemp.setWxLogo(wxLogo);
                uacStaffTemp.setWxOpenid(wxOpenid);
                uacStaffTemp.setWxUnionid(unionid);
            }
            uacStaffMapper.updateById(uacStaffTemp);
            //将询价补充到询盘数据里
            PmsTopicDto pmsTopicDtoTemp=new PmsTopicDto();
            pmsTopicDtoTemp.setId(xpid);
            pmsTopicDtoTemp.setSedMemberId(memberId);
            pmsTopicDtoTemp.setSedUserId(staffId);
            pmsTopicDtoTemp.setUpdateTime(DateUtil.getData());
            pmsFeign.updateTopic(pmsTopicDtoTemp);
        }
    }

    /**
     * 扫码授权子管理员
     * @param parentStaffId
     * @param memberId
     * @param staffId
     * @param wxLogo
     * @param wxOpenid
     * @param unionid
     */
    @Async(AeoConstant.ASYNC_POOL)
    @Transactional(rollbackFor = Exception.class)
    public void addSubAdministrator(
            Long parentStaffId,Long memberId,Long staffId,String wxLogo,String wxOpenid,String unionid){
        if (parentStaffId==null||memberId==null||staffId==null)return;

        UacStaff uacStaffTemp=new UacStaff();
        uacStaffTemp.setCreatedBy(staffId.toString());
        uacStaffTemp.setCreatedTime(DateUtil.getData().toLocalDateTime());
        uacStaffTemp.setRevision(1);
        uacStaffTemp.setStatus(0);
        if(StringUtils.isBlank(uacStaffTemp.getWxLogo())) {
            uacStaffTemp.setWxLogo(wxLogo);
            uacStaffTemp.setWxOpenid(wxOpenid);
            uacStaffTemp.setWxUnionid(unionid);
        }
        uacStaffTemp.setMemberId(memberId);
        uacStaffTemp.setSgsStatus(1);
        uacStaffTemp.setStaffType(BizConstant.StaffTypeEnum.ENTERPRISE.getValue());
        uacStaffMapper.updateById(uacStaffTemp);

    }
}
