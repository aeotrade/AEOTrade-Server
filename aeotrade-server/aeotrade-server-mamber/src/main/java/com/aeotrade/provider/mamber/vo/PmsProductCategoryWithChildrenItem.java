package com.aeotrade.provider.mamber.vo;


import com.aeotrade.provider.mamber.entity.PmsProductCategory;

import java.util.List;

/**
 * Created by hmm on 2018/5/25.
 */
public class PmsProductCategoryWithChildrenItem extends PmsProductCategory {
    private List<PmsProductCategory> children;

    public List<PmsProductCategory> getChildren() {
        return children;
    }

    public void setChildren(List<PmsProductCategory> children) {
        this.children = children;
    }
}
