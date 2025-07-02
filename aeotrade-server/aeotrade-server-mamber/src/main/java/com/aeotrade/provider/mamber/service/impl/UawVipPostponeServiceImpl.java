package com.aeotrade.provider.mamber.service.impl;

import com.aeotrade.provider.mamber.entity.UawVipPostpone;
import com.aeotrade.provider.mamber.mapper.UawVipPostponeMapper;
import com.aeotrade.provider.mamber.service.UawVipPostponeService;
import com.aeotrade.suppot.PageList;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Service
public class UawVipPostponeServiceImpl extends ServiceImpl<UawVipPostponeMapper, UawVipPostpone> implements UawVipPostponeService {
    public PageList<UawVipPostpone> findPostpone(Integer pageSize, Integer pageNo,
                                                 String memberName, Long vipTypeId) {
        LambdaQueryWrapper<UawVipPostpone> uawVipPostponeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(memberName)) {
            uawVipPostponeLambdaQueryWrapper.like(UawVipPostpone::getMemberName, memberName);
        }
        if (null != vipTypeId && vipTypeId != 0) {
            uawVipPostponeLambdaQueryWrapper.like(UawVipPostpone::getVipTypeId, vipTypeId);
        }
        uawVipPostponeLambdaQueryWrapper.orderByDesc(UawVipPostpone::getOperatorTime);
        Page<UawVipPostpone> page = this.page(new Page<>(pageNo, pageSize), uawVipPostponeLambdaQueryWrapper);
        PageList<UawVipPostpone>  uawVipPostpones=new PageList<>();
        uawVipPostpones.setTotalSize(page.getTotal());
        uawVipPostpones.setRecords(page.getRecords());
        return uawVipPostpones;
    }
}
