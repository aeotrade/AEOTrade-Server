package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.provider.mamber.entity.AppCloud;
import com.aeotrade.provider.mamber.entity.UawRights;
import com.aeotrade.provider.mamber.entity.UawRightsType;
import com.aeotrade.provider.mamber.entity.UawVipRightsType;
import com.aeotrade.provider.mamber.mapper.UawRightsTypeMapper;
import com.aeotrade.provider.mamber.service.UawRightsTypeService;
import com.aeotrade.provider.mamber.vo.RightsTypeVo;
import com.aeotrade.suppot.PageList;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.toolkit.MPJWrappers;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 权益类型表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Service
public class UawRightsTypeServiceImpl extends ServiceImpl<UawRightsTypeMapper, UawRightsType> implements UawRightsTypeService {
    @Autowired
    private UawRightsServiceImpl uawRightsMapper;
    @Autowired
    private AppCloudServiceImpl appCloudService;

    public List<RightsTypeVo> findByClassid(Long id) {
        List<UawRightsType> bList = this.selectJoinList(UawRightsType.class,
                MPJWrappers.<UawRightsType>lambdaJoin().disableSubLogicDel().disableLogicDel()
                        .selectAll(UawRightsType.class)
                        .rightJoin(UawVipRightsType.class, UawVipRightsType::getRightsTypeId, UawRightsType::getId)
                        .eq(UawVipRightsType::getStatus, 0)
                        .eq(UawRightsType::getStatus, 0)
                        .eq(UawVipRightsType::getVipClassId, id)
        );
        List<RightsTypeVo> rightsTypeVos = new ArrayList<>();
        for (UawRightsType uawRightsType : bList) {
            RightsTypeVo typeVo = new RightsTypeVo();

            typeVo.setRightsTypeName(uawRightsType.getRightsTypeName());
            typeVo.setIco(uawRightsType.getIco());
            typeVo.setId(uawRightsType.getId());
            typeVo.setIds(uawRightsMapper.findByTypeid(uawRightsType.getId(), id));
            List<UawRights> list = uawRightsMapper.lambdaQuery()
                    .eq(UawRights::getStatus, 0).eq(UawRights::getRightsTypeId, uawRightsType.getId()).list();
            typeVo.setRightsListList(list);
            rightsTypeVos.add(typeVo);
        }
        return rightsTypeVos;
    }

    public PageList<UawRightsType> findByName(Integer pageSize, Integer pageNo, String name) {
        LambdaQueryWrapper<UawRightsType> uawRightsTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotEmpty(name)) {
            uawRightsTypeLambdaQueryWrapper.like(UawRightsType::getRightsTypeName, name);
        }
        uawRightsTypeLambdaQueryWrapper.eq(UawRightsType::getStatus, 0);
        Page<UawRightsType> page = this.page(new Page<>(pageNo, pageSize), uawRightsTypeLambdaQueryWrapper);
        PageList<UawRightsType> uawRightsTypes = new PageList<>();
        uawRightsTypes.setRecords(page.getRecords());
        uawRightsTypes.setTotalSize(page.getTotal());
        return uawRightsTypes;
    }

    //根据关联应用id查询应用名称
    public String findCloudById(String resourceName) {
        List<AppCloud> list = appCloudService.lambdaQuery()
                .eq(AppCloud::getId, resourceName)
                .eq(AppCloud::getPublishStatus, 1)
                .eq(AppCloud::getStatus, 0).list();
        return list.size()>0?list.get(0).getAppName():null;
    }
}
