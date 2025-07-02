package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.base.constant.SgsConstant;
import com.aeotrade.base.constant.WorkBenchConstant;
import com.aeotrade.base.constant.WorkBenchService;
import com.aeotrade.exception.AeotradeException;
import com.aeotrade.provider.mamber.entity.*;
import com.aeotrade.provider.mamber.mapper.UawAptitudesMapper;
import com.aeotrade.provider.mamber.service.UawAptitudesService;
import com.aeotrade.provider.mamber.vo.UawTypeErp;
import com.aeotrade.suppot.PageList;
import com.aeotrade.utlis.CommonUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 入驻申请表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Service
public class UawAptitudesServiceImpl extends ServiceImpl<UawAptitudesMapper, UawAptitudes> implements UawAptitudesService {
    @Autowired
    private UawVipMessageServiceImpl uawVipMessageService;
    @Autowired
    private UawVipTypeServiceImpl uawVipTypeMapper;

    @Autowired
    private UacMemberShopServiceImpl uacMemberShopMapper;
    @Autowired
    private UacMemberServiceImpl uacMemberService;
    @Autowired
    private UawVipTypeGroupServiceImpl uawVipTypeGroupService;
    @Autowired
    private UawWorkbenchServiceImpl uawWorkbenchService;
    public Integer findStatus(Long memberId, Long id) {
        List<UawAptitudes> list = this.lambdaQuery().eq(UawAptitudes::getMemberId, memberId)
                .eq(UawAptitudes::getVipTypeId, id).eq(UawAptitudes::getStatus, 0).list();
        if (!CommonUtil.isEmpty(list)) {
            return list.get(0).getSgsStatus();
        } else {
            return 0;
        }
    }

    public PageList<UawAptitudes> findAll(Integer pageSize, Integer pageNo, String memberName, Long vipTypeId) {
        LambdaQueryWrapper<UawAptitudes> uawAptitudesLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(memberName)) {
            uawAptitudesLambdaQueryWrapper.like(UawAptitudes::getMemberName, memberName);
        }
        if (null != vipTypeId) {
            uawAptitudesLambdaQueryWrapper.eq(UawAptitudes::getVipTypeId, vipTypeId);
        }

        uawAptitudesLambdaQueryWrapper.eq(UawAptitudes::getStatus, 0);
        uawAptitudesLambdaQueryWrapper.orderByDesc(UawAptitudes::getCreatedTime);
        Page<UawAptitudes> list = this.page(new Page<>(pageNo, pageSize), uawAptitudesLambdaQueryWrapper);
        PageList<UawAptitudes> uawAptitudes = new PageList<>();
        uawAptitudes.setTotalSize(list.getTotal());
        uawAptitudes.setRecords(list.getRecords());
        return uawAptitudes;
    }

    public void sgsListupdate(UawAptitudes uawAptitudes) throws Exception {

        if (Objects.equals(uawAptitudes.getSgsStatus(), SgsConstant.SgsStatus.TONGGUO.getValue())) {
            uawVipMessageService.insertMessage(uawAptitudes.getMemberId(), uawAptitudes.getVipTypeId());
        }
        UawVipType uawVipType = uawVipTypeMapper.getById(uawAptitudes.getVipTypeId());
        UawAptitudes aptitudes = this.getById(uawAptitudes.getId());
        UawTypeErp uawTypeErp = new UawTypeErp();
        String code = uawVipType.getCode();
        uawTypeErp.setCode(code);
        uawTypeErp.setMemberId(aptitudes.getMemberId());
        uawTypeErp.setStaffId(aptitudes.getCreatedById());
        System.out.println("调用参数为-------" + uawTypeErp);
        try {
            /**开通对应的店铺*/
            Integer integer = memberShop(aptitudes.getMemberId(), uawVipType.getCode());
            if (integer.equals(0)) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                throw new AeotradeException("店铺创建失败,请重试");
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        uawAptitudes.setRevision(1);
        uawAptitudes.setAuditor("系统");
        this.updateById(uawAptitudes);
        Thread.sleep(1500);
    }


    public Integer memberShop(Long memberId, String code) {
        UacMember uacMember = uacMemberService.getById(memberId);
        String shopCode = WorkBenchService.findShopCode(code);
        if (uacMember == null) throw new AeotradeException("未查询到企业信息");
        UacMemberShop shop = uacMemberShopMapper.findByMemberAndType(memberId, shopCode);
        if (null == shop) {
            if (shopCode.isEmpty()) {
                return 1;
            }
            Integer integer = toUacMemberShop(uacMember, shopCode);
            if (StringUtils.isNotEmpty(shopCode)) {
                return integer;
            }
            return 0;
        } else {
            return 1;
        }

    }

    public Integer toUacMemberShop(UacMember uacMember, String code) {
        UacMemberShop uacMemberShop = new UacMemberShop();
        uacMemberShop.setMemberId(uacMember.getId());
        uacMemberShop.setMemberName(uacMember.getMemberName());
        uacMemberShop.setShopName(uacMember.getMemberName());
        uacMemberShop.setLogoImg(uacMember.getLogoImg());
        uacMemberShop.setShopLogo(uacMember.getLogoImg());
        uacMemberShop.setShopBanner(uacMember.getLogoImg());
        uacMemberShop.setShopType(code);
        uacMemberShop.setRevision(0);
        uacMemberShop.setStatus(0);
        uacMemberShop.setCreatedTime(LocalDateTime.now());
        boolean save = uacMemberShopMapper.save(uacMemberShop);
        return save ? 1 : 0;
    }


    public UawAptitudes sgsListSave(UawAptitudes uawAptitudes) throws Exception {
        if (uawAptitudes.getVipTypeId() == 1) {
            List<UawVipType> list = uawVipTypeMapper.lambdaQuery()
                    .eq(UawVipType::getCode, WorkBenchConstant.type.CHUKOU.getValue())
                    .eq(UawVipType::getStatus, 0).list();
            UawVipType vipType = !list.isEmpty() ? list.get(0) : null;

            uawAptitudes.setVipTypeId(vipType.getId());
        }
        List<UawAptitudes> list = this.lambdaQuery().eq(UawAptitudes::getStatus, 0)
                .eq(UawAptitudes::getMemberId, uawAptitudes.getMemberId())
                .eq(UawAptitudes::getVipTypeId, uawAptitudes.getVipTypeId()).list();
        if (null != list && !list.isEmpty()) {
            UawAptitudes mapperByEntity = new UawAptitudes();
            for (int i = 0; i < list.size(); i++) {
                if (i == 0) {
                    BeanUtils.copyProperties(mapperByEntity, list.get(i));
                } else {
                    list.get(i).setStatus(1);
                    this.updateById(list.get(i));
                }
            }
            if (mapperByEntity.getSgsStatus() == 1) {
                this.sgsListupdate(mapperByEntity);
                return mapperByEntity;
            }
            if (mapperByEntity.getSgsStatus() == 2) {
                return mapperByEntity;
            }
        }
        UawVipType uawVipType = uawVipTypeMapper.getById(uawAptitudes.getVipTypeId());
        if (null != uawVipType && uawVipType.getIsAuditRequired() == 0) {
            uawAptitudes.setSgsStatus(SgsConstant.SgsStatus.TONGGUO.getValue());
        } else {
            uawAptitudes.setSgsStatus(SgsConstant.SgsStatus.WEIRENZ.getValue());
        }
        if (StringUtils.isEmpty(uawAptitudes.getVipGroupName())) {
            UawVipTypeGroup uawVipTypeGroup = null;
            if (uawVipType != null) {
                uawVipTypeGroup = uawVipTypeGroupService.getById(uawVipType.getGroupId());
            }
            if (uawVipTypeGroup != null) {
                uawAptitudes.setVipGroupName(uawVipTypeGroup.getGroupName());
                uawAptitudes.setVipTypeName(uawVipType.getTypeName());
            }
        }
        this.save(uawAptitudes);
        uawAptitudes.setSgsStatus(SgsConstant.SgsStatus.TONGGUO.getValue());
        this.sgsListupdate(uawAptitudes);
        if (uawVipType != null) {
            uawWorkbenchService.updateDefultWorkbench(uawAptitudes.getCreatedById(), uawAptitudes.getMemberId(), uawVipType.getWorkbench());
        }
        return uawAptitudes;
    }
}
