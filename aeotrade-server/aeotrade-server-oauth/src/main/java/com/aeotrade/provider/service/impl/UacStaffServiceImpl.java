package com.aeotrade.provider.service.impl;

import com.aeotrade.base.constant.BizConstant;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.dto.WxTokenDto;
import com.aeotrade.provider.dto.AtciLogDto;
import com.aeotrade.provider.dto.AtclMemberDto;
import com.aeotrade.provider.dto.WxTencentDto;
import com.aeotrade.provider.dto.WxUacStaffDto;
import com.aeotrade.provider.mapper.AtciLogMapper;
import com.aeotrade.provider.mapper.UacAdminMapper;
import com.aeotrade.provider.mapper.UacMemberStaffMapper;
import com.aeotrade.provider.mapper.UacStaffMapper;
import com.aeotrade.provider.model.*;
import com.aeotrade.provider.service.UacMemberService;
import com.aeotrade.provider.service.UacStaffService;
import com.aeotrade.provider.util.ThreadPoolUtils;
import com.aeotrade.provider.vo.AtclMemberVO;
import com.aeotrade.provider.vo.BigScreen;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.base.MPJBaseServiceImpl;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 企业员工 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
@Service
public class UacStaffServiceImpl extends MPJBaseServiceImpl<UacStaffMapper, UacStaff> implements UacStaffService {
    @Autowired
    private UacStaffMapper uacStaffMapper;
    @Autowired
    private UacAdminMapper uacAdminMapper;
    @Autowired
    private UacMemberService uacMemberService;

    @Autowired
    private UacMemberStaffMapper uacMemberStaffMapper;
    @Autowired
    private AtciLogMapper atciLogMapper;

    public void updatestaff(WxTencentDto wxTencentDto, WxUacStaffDto wxUacStaffDto, WxTokenDto wxTokenDto)throws RuntimeException {

        UacStaff staff = new UacStaff();
        BeanUtils.copyProperties(wxUacStaffDto ,staff);
        staff.setId(wxUacStaffDto.getId());
        staff.setSgsStatus(BizConstant.StaffBindStateEum.BOUND.getValue());
        staff.setRevision(0);
        uacStaffMapper.updateById(staff);
    }

    public AtclMemberVO findMemberBystaffId(AtclMemberDto atclMemberDto)throws RuntimeException {
        UacStaff uacStaff = uacStaffMapper.selectById(atclMemberDto.getStaffId());
        if(uacStaff==null)throw new AeotradeException("账号未注册");
        /**1.根据员工Id查询所有企业*/
        LambdaQueryWrapper<UacMemberStaff> uacMemberStaffLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacMemberStaffLambdaQueryWrapper.eq(UacMemberStaff::getStaffId,atclMemberDto.getStaffId());
        List<UacMemberStaff> listBuStaffId = uacMemberStaffMapper.selectList(uacMemberStaffLambdaQueryWrapper);
        AtclMemberVO vo = new AtclMemberVO();
        for ( UacMemberStaff uac:listBuStaffId) {
            UacMember uacMember = uacMemberService.getById(uac.getMemberId());
            if(null!=uacMember && StringUtils.isNotEmpty(uacMember.getUscCode())){
                if(atclMemberDto.getSubscriberUscc().equals(uacMember.getUscCode())&& uacMember.getMemberStatus()==
                        BizConstant.MemberStateEum.AUTHENTICATED.getValue()){
                    /**2.将企业信息修改到member表中*/
                    ThreadPoolUtils.execute(new Runnable() {
                        @Override
                        public void run() {
                            updateMember(uacMember,atclMemberDto );
                        }
                    });
                    vo.setMemberId(uacMember.getId());
                    return vo;
                }
            }
        }
        /**3.如果没匹配到便为新用户,新插入一条企业数据*/
        Long  memberId =insertMember(atclMemberDto);
        vo.setMemberId(memberId);
        return vo;

    }

    private Long insertMember( AtclMemberDto atclMemberDto) {
        UacMember uac = new UacMember();
        uac.setMemberName(atclMemberDto.getSubscriberName());
        uac.setUscCode(atclMemberDto.getSubscriberUscc());
        if(StringUtils.isNotEmpty(atclMemberDto.getLegalPerson())){
            uac.setLegalPerson(atclMemberDto.getLegalPerson());
            uac.setStaffName(atclMemberDto.getLegalPerson());
        }
        if(StringUtils.isNotEmpty(atclMemberDto.getLegalPersonEmail())) {
            uac.setLegalPersonEmail(atclMemberDto.getLegalPersonEmail());
            uac.setEmail(atclMemberDto.getLegalPersonEmail());
        }
        if(StringUtils.isNotEmpty(atclMemberDto.getLegalPersonMobile())) {
            uac.setLegalPersonMobile(atclMemberDto.getLegalPersonMobile());

        }
        if(StringUtils.isNotEmpty(atclMemberDto.getLegalPersonTel())) {
            uac.setLegalPersonTel(atclMemberDto.getLegalPersonTel());
            uac.setStasfTel(atclMemberDto.getLegalPersonTel());
        }
        if(StringUtils.isNotEmpty(atclMemberDto.getSubscriberCode())){
            uac.setSubscriberCode(atclMemberDto.getSubscriberCode());
        }
        uac.setMemberStatus(BizConstant.MemberStateEum.AUTHENTICATED.getValue());
        uac.setAtpwStatus(BizConstant.AtpwStatus.WEI_GOU_XUAN.getValue());//未勾选
        //uac.setPersonageStatus(0);
        uacMemberService.save(uac);
        /**插入关联表数据*/

        UacMemberStaff uacMemberStaff = new UacMemberStaff();
        uacMemberStaff.setStaffId(atclMemberDto.getStaffId());
        uacMemberStaff.setMemberId(uac.getId());
        uacMemberStaff.setIsAdmin(0);
        uacMemberStaff.setCreatedTime(new Timestamp(System.currentTimeMillis()).toLocalDateTime());
        uacMemberStaffMapper.insert(uacMemberStaff);
        return uac.getId();
    }

    private void updateMember(UacMember uacMember, AtclMemberDto atclMemberDto) {
        UacMember uac = new UacMember();
        uac.setId(uacMember.getId());
        if(StringUtils.isNotEmpty(atclMemberDto.getLegalPerson())){
            uac.setLegalPerson(atclMemberDto.getLegalPerson());
        }
        if(StringUtils.isNotEmpty(atclMemberDto.getLegalPersonEmail())) {
            uac.setLegalPersonEmail(atclMemberDto.getLegalPersonEmail());
        }
        if(StringUtils.isNotEmpty(atclMemberDto.getLegalPersonMobile())) {
            uac.setLegalPersonMobile(atclMemberDto.getLegalPersonMobile());
        }
        if(StringUtils.isNotEmpty(atclMemberDto.getLegalPersonTel())) {
            uac.setLegalPersonTel(atclMemberDto.getLegalPersonTel());
        }
        if(StringUtils.isNotEmpty(atclMemberDto.getSubscriberCode())){
            uac.setSubscriberCode(atclMemberDto.getSubscriberCode());
        }
        uac.setRevision(1);
        uacMemberService.updateById(uac);
    }

    public void updateAtclLog(List<AtciLogDto>   atciLogDto) {
        for (AtciLogDto atci : atciLogDto ) {
            if(null==atci.getMemberId()){
                throw new AeotradeException("企业Id为空");
            }else if(null==atci.getBillCount()){
                throw new AeotradeException("单证数量为空");
            }else if(null==atci.getBillType()){
                throw new AeotradeException("单证类型为空");
            }else if(null==atci.getUpdateTime()){
                throw new AeotradeException("更新时间为空");
            }else if(StringUtils.isEmpty(atci.getBillDate())){
                throw new AeotradeException("日期为空");
            }else if(StringUtils.isEmpty(atci.getBillWay())){
                throw new AeotradeException("采集方式为空");
            }else if(StringUtils.isEmpty(atci.getOperator())){
                throw new AeotradeException("操作人为空");
            }else if(StringUtils.isEmpty(atci.getMemberUscc())){
                throw new AeotradeException("社会统一信用代码为空");
            }
            /**根据日期和单证类型,采集方式和企业ID判断*/
            LambdaQueryWrapper<AtciLog> atciLogLambdaQueryWrapper=new LambdaQueryWrapper<>();
            atciLogLambdaQueryWrapper.eq(AtciLog::getMemberId,atci.getMemberId()).eq(AtciLog::getBillType,atci.getBillType())
                    .eq(AtciLog::getBillDate,atci.getBillDate()).eq(AtciLog::getBillWay,atci.getBillWay());
            List<AtciLog> atciLogs = atciLogMapper.selectList(atciLogLambdaQueryWrapper);
            AtciLog log =atciLogs.size()>0?atciLogs.get(0):null;
            if(null==log) {
                AtciLog atciLog = new AtciLog();
                BeanUtils.copyProperties(atci, atciLog);
                atciLogMapper.insert(atciLog);
            }else if (log!=null){
                AtciLog atciLog = new AtciLog();
                BeanUtils.copyProperties(atci, atciLog);
                atciLog.setBillCount( log.getBillCount()+atci.getBillCount());
                atciLog.setId(log.getId());
                atciLog.setRevision(1);
                atciLogMapper.updateById(atciLog);
            }
        }
    }

    public int SetDefultMember(Long staffId,Long memberId) {
        UacStaff uacStaff = uacStaffMapper.selectById(staffId);
        if(uacStaff!=null){
            uacStaff.setMemberId(memberId);
            uacStaff.setRevision(0);
            uacStaff.setStatus(0);
            int i = uacStaffMapper.updateById(uacStaff);
            return i;
        }
        return 0;
    }

    public Map<String,Object> BigCountScreen() {
        List<UacMember> memberRemark = uacMemberService.lambdaQuery().eq(UacMember::getRemark,1)
                .ne(UacMember::getStatus,1).notIn(UacMember::getKindId,88,99).list();
        Map<String,Object> map=new HashMap<>();
        map.put("count",memberRemark.size());
        return map;
    }

    /**
     * 查询企业表数据
     */
    public Map<String,Object> BigtwoScreen() {
        List<UacMember> memberRemark = uacMemberService.lambdaQuery().eq(UacMember::getRemark,1)
                .ne(UacMember::getStatus,1).notIn(UacMember::getKindId,88,99).list();
        List<BigScreen> bigScreens=new ArrayList<>();
        Map<String,Object> map=new HashMap<>();
        map.put("count",memberRemark.size());
        for (UacMember uacMember : memberRemark) {
            BigScreen bigScreen=new BigScreen();
            String pinYinHeaderChar = getPinYinHeaderChar(uacMember.getMemberName());
            bigScreen.setName(pinYinHeaderChar+".aeotrade");
            String memberName = uacMember.getMemberName();
            bigScreen.setOrgan(pinYinHeaderChar+" ("+around(memberName)+")");
            bigScreen.setRole("普通节点");
            bigScreen.setTime(String.valueOf(uacMember.getCreatedTime()));
            bigScreens.add(bigScreen);
        }
        map.put("list",bigScreens);
        map.put("jiedian",4);
        return map;
    }

    /**
     * 得到汉字的首字母
     *
     * @param source
     * @return
     */
    public String getPinYinHeaderChar(String source) {
        if (StringUtils.isBlank(source)) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            char word = source.charAt(i);
            if (Character.toString(word).matches("[\\u4E00-\\u9FA5]")) {
                String[] pinYinArr = PinyinHelper.toHanyuPinyinStringArray(word);
                result.append(pinYinArr[0].charAt(0));
            } else {
                // 非汉字不进行转换，直接添加
                result.append(word);
            }
        }
        return result.toString();
    }
    /**
     * 企业名称脱敏
     */
    public String around(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        if(str.length()<2){
            return str;
        }else{
            String s= String.valueOf(str.charAt(str.length()-1));
            if(s.equals(")") || s.equals("）") || s.equals(" ")){
                s=String.valueOf(str.charAt(str.length()-2));
            }
            return str.charAt(0)+"******"+s;
        }
    }
}
