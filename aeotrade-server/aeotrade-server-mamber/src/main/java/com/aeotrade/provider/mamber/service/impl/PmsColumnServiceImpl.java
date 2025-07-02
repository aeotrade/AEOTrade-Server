package com.aeotrade.provider.mamber.service.impl;

import com.aeotrade.provider.mamber.entity.PmsColumn;
import com.aeotrade.provider.mamber.mapper.PmsColumnMapper;
import com.aeotrade.provider.mamber.service.PmsColumnService;
import com.aeotrade.provider.mamber.vo.Column;
import com.aeotrade.provider.mamber.vo.ColumnVO;
import com.aeotrade.utlis.CommonUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-14 14:38
 */
@Service
public class PmsColumnServiceImpl extends ServiceImpl<PmsColumnMapper, PmsColumn> implements PmsColumnService {
    @Override
    public List<ColumnVO> findAll() {
        List<ColumnVO> vo = new ArrayList<>();
        ColumnVO coVO=null;
        /**获取所有parentId为0的父分类*/
        List<PmsColumn> list = this.lambdaQuery().eq(PmsColumn::getParentId,0L).list();
        if(!CommonUtil.isEmpty(list)){

            for ( PmsColumn colu:list) {
                coVO=  new ColumnVO();
                List<PmsColumn>  pmsColumn = this.lambdaQuery().eq(PmsColumn::getParentId,colu.getId()).list();
                if(pmsColumn!=null){
                    List<Column> collect = pmsColumn.stream().map(i -> new Column(i.getId(), i.getName(),
                            i.getColumnStatus())).collect(Collectors.toList());

                    coVO.setChildren(collect);
                    coVO.setId(colu.getId());
                    coVO.setName(colu.getName());
                    coVO.setColumnStatus(colu.getColumnStatus());
                    vo.add(coVO );
                }
            }
            return vo;
        }
        return null;
    }
}
