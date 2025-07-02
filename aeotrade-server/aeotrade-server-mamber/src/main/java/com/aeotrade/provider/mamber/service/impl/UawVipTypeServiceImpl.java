package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.*;
import com.aeotrade.provider.mamber.mapper.UawVipTypeMapper;
import com.aeotrade.provider.mamber.service.UawVipTypeService;
import com.aeotrade.provider.mamber.vo.UawVipTypeDto;
import com.aeotrade.provider.mamber.vo.UawVipTypeVO;
import com.aeotrade.provider.mamber.vo.VipTypeVos;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.MPJWrappers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 会员分类表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Service
public class UawVipTypeServiceImpl extends ServiceImpl<UawVipTypeMapper, UawVipType> implements UawVipTypeService {
    @Autowired
    private UawWorkbenchServiceImpl uawWorkbenchMapper;
    @Autowired
    private UawVipTypeGroupServiceImpl uawVipTypeGroupMapper;
    @Autowired
    private UacStaffServiceImpl uacStaffService;
    @Autowired
    private UacAdminServiceImpl uacAdminService;

    public VipTypeVos findAll(int pageSize, int pageNo, int apply, long groupId) {
        Page<UawVipTypeDto> documents =null;
        if(groupId!=0){
            documents = this.selectJoinListPage(new Page<>(pageNo, pageSize), UawVipTypeDto.class,
                    MPJWrappers.<UawVipType>lambdaJoin().disableSubLogicDel().disableLogicDel()
                            .selectAll(UawVipType.class)
                            .select(UawVipTypeGroup::getGroupName)
                            .leftJoin(UawVipTypeGroup.class,UawVipTypeGroup::getId,UawVipType::getGroupId)
                            .eq(UawVipType::getStatus,0)
                            .eq(UawVipTypeGroup::getStatus,0)
                            .eq(UawVipTypeGroup::getApply,apply)
                            .eq(UawVipTypeGroup::getId,groupId)
            );
        }else{
            documents = this.selectJoinListPage(new Page<>(pageNo, pageSize), UawVipTypeDto.class,
                    MPJWrappers.<UawVipType>lambdaJoin().disableSubLogicDel().disableLogicDel()
                            .selectAll(UawVipType.class)
                            .select(UawVipTypeGroup::getGroupName)
                            .leftJoin(UawVipTypeGroup.class,UawVipTypeGroup::getId,UawVipType::getGroupId)
                            .eq(UawVipType::getStatus,0)
                            .eq(UawVipTypeGroup::getStatus,0)
                            .eq(UawVipTypeGroup::getApply,apply)

            );
        }
        for (UawVipTypeDto record : documents.getRecords()) {
            UacAdmin uacAdmin = uacAdminService.getById(record.getCreatedBy());
            record.setStaffName(uacAdmin.getNickName());
        }

        VipTypeVos vipTypeVos = new VipTypeVos();
        List<UawVipTypeVO> vipType = new ArrayList<>();
        vipTypeVos.setTotal(Math.toIntExact(documents.getTotal()));
        for (UawVipTypeDto document : documents.getRecords()) {
            UawVipTypeVO uawVipTypeVO = new UawVipTypeVO();
            BeanUtils.copyProperties(document, uawVipTypeVO);
            if (null != document.getWorkbench()) {
                UawWorkbench uawWorkbench = uawWorkbenchMapper.getById(document.getWorkbench());
                if (null != uawWorkbench) {
                    uawVipTypeVO.setUawWorkbench(uawWorkbench);
                }
            }
            vipType.add(uawVipTypeVO);
        }
        vipTypeVos.setVipType(vipType);
        return vipTypeVos;
    }

    public void deleteType(Long id) {
        UawVipType vipType = this.getById(id);
        vipType.setStatus(1);
        this.saveOrUpdate(vipType);
    }

    public UawWorkbench findListworkbench(Long id) {
        UawWorkbench uawWorkbench = uawWorkbenchMapper.getById(id);
        return uawWorkbench;
    }

    public List<UawVipType> findVipAll() {

        List<UawVipType> vipAll =this.selectJoinList(UawVipType.class,
                MPJWrappers.<UawVipType>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UawVipType.class)
                        .select(UawVipTypeGroup::getGroupName)
                        .leftJoin(UawVipTypeGroup.class,UawVipTypeGroup::getId,UawVipType::getGroupId)
                        .eq(UawVipType::getStatus,0)
                        .eq(UawVipTypeGroup::getStatus,0)
                        .eq(UawVipType::getVipTypeStatus,0)
                        .eq(UawVipTypeGroup::getApply,1)
        );
        for (UawVipType uawVipType : vipAll) {
            UacAdmin entity = uacAdminService.getById(uawVipType.getCreatedBy());
            if(null!=entity){
                uawVipType.setStaffName(entity.getNickName());
            }
        }
        return vipAll;
    }

    public List<UawVipType> findVipMam() {
        List<UawVipType> uawVipTypes = new ArrayList<>();
        List<UawVipTypeGroup> list = uawVipTypeGroupMapper.lambdaQuery()
                .eq(UawVipTypeGroup::getStatus,0)
                .eq(UawVipTypeGroup::getApply,1)
                .eq(UawVipTypeGroup::getIsDefaultVip,1).list();
        for (UawVipTypeGroup vipTypeGroup : list) {
            List<UawVipType> vipTypes = this.lambdaQuery().eq(UawVipType::getStatus, 0).eq(UawVipType::getGroupId, vipTypeGroup.getId()).list();
            uawVipTypes.addAll(vipTypes);
        }
        return uawVipTypes;
    }

    public UawVipType findBystaff(Long staffId) {
        UacStaff uacStaff = uacStaffService.getById(staffId);
        if (null == uacStaff.getLastWorkbenchId() || uacStaff.getLastWorkbenchId() == 1) {
            throw new AeotradeException("查不到用户使用的工作台");
        }
        List<UawVipType> list = this.lambdaQuery()
                .eq(UawVipType::getStatus, 0)
                .eq(UawVipType::getWorkbench, uacStaff.getLastWorkbenchId())
                .list();
        return list.get(0);
    }
}
