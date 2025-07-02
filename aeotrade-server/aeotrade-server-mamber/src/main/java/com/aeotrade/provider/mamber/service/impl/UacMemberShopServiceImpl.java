package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.provider.mamber.entity.UacMember;
import com.aeotrade.provider.mamber.entity.UacMemberShop;
import com.aeotrade.provider.mamber.mapper.UacMemberShopMapper;
import com.aeotrade.provider.mamber.service.UacMemberShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 企业店铺 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Service
public class UacMemberShopServiceImpl extends ServiceImpl<UacMemberShopMapper, UacMemberShop> implements UacMemberShopService {

    public UacMemberShop findByMemberAndType(Long memberId, String shopCode) {
        List<UacMemberShop> list = this.lambdaQuery().eq(UacMemberShop::getMemberId, memberId)
                .eq(UacMemberShop::getShopType, shopCode).eq(UacMemberShop::getStatus, 0).list();
        return !list.isEmpty() ?list.get(0):null;

    }
}
