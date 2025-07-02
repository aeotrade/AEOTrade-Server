package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.provider.mamber.entity.UawVipTypeGroup;
import com.aeotrade.provider.mamber.mapper.UawVipTypeGroupMapper;
import com.aeotrade.provider.mamber.service.UawVipTypeGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 会员类型分组表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Service
public class UawVipTypeGroupServiceImpl extends ServiceImpl<UawVipTypeGroupMapper, UawVipTypeGroup> implements UawVipTypeGroupService {

    public List<UawVipTypeGroup> findAll(int apply) {
        if(apply!=2){
            return this.lambdaQuery().eq(UawVipTypeGroup::getApply,apply)
                    .eq(UawVipTypeGroup::getStatus,0).list();
        }else{
            return this.lambdaQuery().eq(UawVipTypeGroup::getStatus,0).list();
        }
    }
}
