package com.aeotrade.provider.mamber.service.impl;


import com.aeotrade.provider.mamber.entity.UawRightsRightsType;
import com.aeotrade.provider.mamber.entity.UawVipRightsType;
import com.aeotrade.provider.mamber.mapper.UawRightsRightsTypeMapper;
import com.aeotrade.provider.mamber.service.UawRightsRightsTypeService;
import com.aeotrade.provider.mamber.vo.RightsVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 权益项和权益类型关联表 服务实现类
 * </p>
 *
 * @author aeo
 * @since 2023-11-07
 */
@Service
public class UawRightsRightsTypeServiceImpl extends ServiceImpl<UawRightsRightsTypeMapper, UawRightsRightsType> implements UawRightsRightsTypeService {

    @Autowired
    private UawVipRightsTypeServiceImpl uawVipRightsTypeMapper;


    @Transactional(propagation = Propagation.REQUIRED)
    public Boolean insertVip(Long id, List<RightsVo> rightsVoList) {
        try {
            //判断权益集合是否为空
            if (rightsVoList.isEmpty()) {
                return false;
            }
            //判断会员等级id是否为空
            if (id == null) {
                return false;
            }
            //遍历权益集合
            for (RightsVo vo : rightsVoList) {
                //创建权益会员关联对象
                UawVipRightsType uawVipRightstype = new UawVipRightsType();
                //将会员等级id添加权益会员关联对象
                uawVipRightstype.setVipClassId(id);
                //将权益类型id添加到权益会员关联对象
                uawVipRightstype.setRightsTypeId(vo.getId());
                //执行权益会员关联对象添加操作
                uawVipRightsTypeMapper.save(uawVipRightstype);
                //取出权益项数组
                Long[] ids = vo.getIds();
                //循环遍历数组
                for (Long aLong : ids) {
                    //创建权益项权益类型关联表对象
                    UawRightsRightsType uawRigType = new UawRightsRightsType();
                    //添加会员等级id
                    uawRigType.setVipClassId(id);
                    //添加权益类型id
                    uawRigType.setRightsTypeId(vo.getId());
                    //添加权益项id
                    uawRigType.setRightsId(aLong);
                    //执行添加操作
                    this.save(uawRigType);
                }
            }
            return true;
        } catch (Exception e) {
            log.warn(e.getMessage());
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Boolean updateVip(Long id, List<RightsVo> rightsVoList) {
        try {
            //遍历权益集合
            for (RightsVo vo : rightsVoList) {
                //根据会员等级id查询权益类型对象集合
                List<UawVipRightsType> list = uawVipRightsTypeMapper.lambdaQuery().eq(UawVipRightsType::getStatus,0)
                .eq(UawVipRightsType::getVipClassId,id).list();
                //循环遍历集合
                for (UawVipRightsType vipRigtype : list) {
                    //将之前权益类型根据权益类型id删除
                    uawVipRightsTypeMapper.removeById(vipRigtype.getId());
                }
                //循环遍历数组
                for (UawVipRightsType uawVipRightsType : list) {

                    List<UawRightsRightsType> types =this.lambdaQuery().eq(UawRightsRightsType::getStatus,0)
                            .eq(UawRightsRightsType::getVipClassId,id)
                            .eq(UawRightsRightsType::getRightsTypeId,uawVipRightsType.getRightsTypeId()).list();
                    //遍历集合
                    for (UawRightsRightsType type : types) {
                        //执行删除操作
                        this.removeById(type.getId());
                    }
                }
            }
            //执行成功返回true
            return true;
        } catch (Exception e) {
            log.warn(e.getMessage());
            return false;
        }
    }
}
