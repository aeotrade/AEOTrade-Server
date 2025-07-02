package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.provider.mamber.entity.UawRights;
import com.aeotrade.provider.mamber.entity.UawRightsRightsType;
import com.aeotrade.provider.mamber.entity.UawRightsType;
import com.aeotrade.provider.mamber.entity.UawVipRightsType;
import com.aeotrade.provider.mamber.mapper.UawRightsMapper;
import com.aeotrade.provider.mamber.service.UawRightsService;
import com.aeotrade.provider.mamber.vo.RightsTypeVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.MPJWrappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 权益项表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Service
public class UawRightsServiceImpl extends ServiceImpl<UawRightsMapper, UawRights> implements UawRightsService {
    @Autowired
    private UawRightsTypeServiceImpl uawRightsTypeService;

    public List<RightsTypeVo> findByClassid(Long id) {
        //根据会员等级id查询权益项集合

        List<UawRightsType> bList =uawRightsTypeService.selectJoinList(UawRightsType.class,
                MPJWrappers.<UawRightsType>lambdaJoin().disableSubLogicDel().disableLogicDel()
                .selectAll(UawRightsType.class)
                .rightJoin(UawVipRightsType.class, UawVipRightsType::getRightsTypeId,UawRightsType::getId)
                .eq(UawVipRightsType::getStatus,0)
                .eq(UawRightsType::getStatus,0)
                .eq(UawVipRightsType::getVipClassId,id)
        );
        //创建一个ArrayList集合
        List<RightsTypeVo> rightsTypeVos = new ArrayList<>();
        for (UawRightsType uawRightsType : bList) {
            RightsTypeVo typeVo = new RightsTypeVo();
            typeVo.setRightsTypeName(uawRightsType.getRightsTypeName());
            typeVo.setIco(uawRightsType.getIco());
            typeVo.setId(uawRightsType.getId());
            typeVo.setIds(this.findByTypeid(uawRightsType.getId(), id));
            rightsTypeVos.add(typeVo);
        }
        return rightsTypeVos;
    }

    public List<UawRights> findByTypeid(Long id,Long classId) {
        return this.selectJoinList(UawRights.class,
                MPJWrappers.<UawRights>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UawRights.class)
                        .rightJoin(UawRightsRightsType.class, UawRightsRightsType::getRightsId,UawRights::getId)
                        .eq(UawRightsType::getStatus,0)
                        .eq(UawRightsRightsType::getStatus,0)
                        .eq(UawRightsRightsType::getRightsTypeId,id)
                        .eq(UawRightsRightsType::getVipClassId,classId)
        );
    }
}
