package com.aeotrade.provider.mamber.service;

import com.aeotrade.provider.mamber.entity.PmsColumn;
import com.aeotrade.provider.mamber.vo.ColumnVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-14 14:38
 */
public interface PmsColumnService extends IService<PmsColumn> {
    List<ColumnVO> findAll();
}
