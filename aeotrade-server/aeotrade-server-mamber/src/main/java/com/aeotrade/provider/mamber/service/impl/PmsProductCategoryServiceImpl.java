package com.aeotrade.provider.mamber.service.impl;

import com.aeotrade.provider.mamber.entity.PmsProductCategory;
import com.aeotrade.provider.mamber.entity.PmsProductCategoryAttributeRelation;
import com.aeotrade.provider.mamber.mapper.PmsProductCategoryMapper;
import com.aeotrade.provider.mamber.service.PmsProductCategoryAttributeRelationService;
import com.aeotrade.provider.mamber.service.PmsProductCategoryService;
import com.aeotrade.provider.mamber.vo.PmsProductCategoryParam;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-14 14:49
 */
@Service
public class PmsProductCategoryServiceImpl extends ServiceImpl<PmsProductCategoryMapper, PmsProductCategory> implements PmsProductCategoryService {
    @Autowired
    private PmsProductCategoryAttributeRelationService pmsProductCategoryAttributeRelationService;


    @Override
    public int create(PmsProductCategoryParam productCategoryParam) {
        PmsProductCategory productCategory = new PmsProductCategory();
        productCategory.setProductCount(0);
        BeanUtils.copyProperties(productCategoryParam, productCategory);
        //没有父分类时为一级分类
        setCategoryLevel(productCategory);
        boolean count = this.save(productCategory);
        //创建筛选属性关联
        List<Long> productAttributeIdList = productCategoryParam.getProductAttributeIdList();
        if(!CollectionUtils.isEmpty(productAttributeIdList)){
            insertRelationList(productCategory.getId(), productAttributeIdList);
        }
        return 1;
    }

    @Override
    public int updateNavStatus(List<Long> ids, Integer navStatus) {
        List<PmsProductCategory> list = this.lambdaQuery().in(PmsProductCategory::getId, ids).list();
        for (PmsProductCategory pmsProductCategory : list) {
            pmsProductCategory.setNavStatus(navStatus);
            this.updateById(pmsProductCategory);
        }
        return 1;
    }

    @Override
    public int updateShowStatus(List<Long> ids, Integer showStatus) {
        List<PmsProductCategory> list = this.lambdaQuery().in(PmsProductCategory::getId, ids).list();
        for (PmsProductCategory pmsProductCategory : list) {
            pmsProductCategory.setShowStatus(showStatus);
            this.updateById(pmsProductCategory);
        }
        return 1;
    }


    private void insertRelationList(Long productCategoryId, List<Long> productAttributeIdList) {
        List<PmsProductCategoryAttributeRelation> relationList = new ArrayList<>();
        for (Long productAttrId : productAttributeIdList) {
            PmsProductCategoryAttributeRelation relation = new PmsProductCategoryAttributeRelation();
            relation.setProductAttributeId(productAttrId);
            relation.setProductCategoryId(productCategoryId);
            relationList.add(relation);
        }
        pmsProductCategoryAttributeRelationService.saveBatch(relationList);
    }

    private void setCategoryLevel(PmsProductCategory productCategory) {
        //没有父分类时为一级分类
        if (productCategory.getParentId() == 0) {
            productCategory.setLevel(0);
        } else {
            //有父分类时选择根据父分类level设置
            PmsProductCategory parentCategory = this.getById(productCategory.getParentId());
            if (parentCategory != null) {
                productCategory.setLevel(parentCategory.getLevel() + 1);
            } else {
                productCategory.setLevel(0);
            }
        }
    }
}
