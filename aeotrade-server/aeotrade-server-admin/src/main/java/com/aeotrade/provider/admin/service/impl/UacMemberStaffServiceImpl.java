package com.aeotrade.provider.admin.service.impl;



import com.aeotrade.provider.admin.entiy.UacMember;
import com.aeotrade.provider.admin.entiy.UacMemberStaff;
import com.aeotrade.provider.admin.entiy.UacStaff;
import com.aeotrade.provider.admin.mapper.UacMemberStaffMapper;
import com.aeotrade.provider.admin.service.UacMemberStaffService;
import com.aeotrade.provider.admin.uacVo.MemberInfo;
import com.aeotrade.suppot.PageList;
import com.aeotrade.utlis.CommonUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Service
public class UacMemberStaffServiceImpl extends ServiceImpl<UacMemberStaffMapper, UacMemberStaff> implements UacMemberStaffService {

    @Autowired
    private UacStaffServiceImpl uacStaffMapper;
    @Autowired
    private UacMemberServiceImpl uacMemberMapper;
    @Autowired
    private UacMemberStaffServiceImpl uacMemberStaffService;
    /**
     * 根据主键 [id] 获取一条记录(null)
     * @param id 主键 null
     * @return 返回主键对应的对象
     */
    public UacMemberStaff get(Long id) {
        return this.getById(id);
    }


    public PageList<UacStaff> findSubAdminList(Integer pageSize, Integer pageNo, Long memberId) {
        LambdaQueryWrapper<UacMemberStaff> uacMemberStaffLambdaQueryWrapper = new LambdaQueryWrapper<>();
        uacMemberStaffLambdaQueryWrapper.eq(UacMemberStaff::getMemberId, memberId);
        uacMemberStaffLambdaQueryWrapper.eq(UacMemberStaff::getIsAdmin, 1);
        Page<UacMemberStaff> list = this.page(new Page<>(pageNo, pageSize), uacMemberStaffLambdaQueryWrapper);

        if(!CommonUtil.isEmpty(list)){
            List<Long> collect = list.getRecords().stream().map(i -> i.getStaffId()).collect(Collectors.toList());
            PageList<UacStaff> volist = new PageList<>();
            Long[] longs = collect.toArray(new Long[collect.size()]);
            List<UacStaff> uacStaffs=uacStaffMapper.lambdaQuery().in(UacStaff::getId,longs).orderByDesc(UacStaff::getId).list();
            if(!CommonUtil.isEmpty(uacStaffs)){
                volist.setTotalSize(list.getTotal());
                volist.setRecords(uacStaffs);
                return volist;
            }
        }
        return null;
    }

    public void upadteAdmin(List<UacMemberStaff> uacMemberStaff) {
        uacMemberStaff.forEach(uac->{
            List<UacMemberStaff> uacMemberStaffs = this.lambdaQuery()
                    .eq(UacMemberStaff::getMemberId, uac.getMemberId())
                    .eq(UacMemberStaff::getStaffId, uac.getStaffId()).list();
            UacMemberStaff list=uacMemberStaffs.size()>0?uacMemberStaffs.get(0):null;
            if(!CommonUtil.isEmpty(list)){
                uac.setId(list.getId());
                this.updateById(uac);
            } });


    }

    public void usetAdmin(Long memberId, Long staffId) {
        UacMember uacMember = new UacMember();
        uacMember.setId(memberId);
        uacMember.setStaffId(staffId);
        uacMember.setRevision(1);
        uacMemberMapper.updateById(uacMember);
        List<UacMemberStaff> list = uacMemberStaffService.lambdaQuery().eq(UacMemberStaff::getMemberId, memberId).list();
        for (UacMemberStaff uacMemberStaff : list) {
            if(uacMemberStaff.getStaffId().equals(staffId)){
                uacMemberStaff.setIsAdmin(1);
            }else{
                uacMemberStaff.setIsAdmin(0);
            }
            uacMemberStaffService.updateById(uacMemberStaff);
        }

    }

    public UacStaff findMasterAdmin(Long memberId) {
        UacMember uacMember = uacMemberMapper.get(memberId);
        if(uacMember!=null){
            UacStaff uacStaff = uacStaffMapper.getById(uacMember.getStaffId());
            return uacStaff;
        }
        return null;
    }

    public MemberInfo findUserInfo(Long memberId) {
        UacMember uacMember = uacMemberMapper.get(memberId);
        if(uacMember!=null){
            Integer size= Math.toIntExact(this.lambdaQuery().eq(UacMemberStaff::getMemberId, memberId).count());
            Integer adminSize= Math.toIntExact(this.lambdaQuery().eq(UacMemberStaff::getMemberId, memberId).eq(UacMemberStaff::getIsAdmin, 1).count());
            MemberInfo memberInfo = new MemberInfo();
            if(uacMember.getStaffId()!=null){
                UacStaff uacStaff = uacStaffMapper.getById(uacMember.getStaffId());
                if(uacStaff!=null){
                    memberInfo.setMainInfo(uacStaff);
                }
            }
            memberInfo.setSgsStatus(uacMember.getSgsStatus());
            memberInfo.setAdminSize(adminSize);
            memberInfo.setStaffSize(size);
            return memberInfo;
        }
        return null;
    }
}
