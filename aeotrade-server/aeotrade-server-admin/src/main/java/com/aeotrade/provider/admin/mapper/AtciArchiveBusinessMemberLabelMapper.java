package com.aeotrade.provider.admin.mapper;


import com.aeotrade.provider.admin.entiy.AtciArchiveBusinessMemberLabel;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 企业运营业务类型标签列表 Mapper 接口
 * </p>
 *
 * @author aeo
 * @since 2023-10-30
 */
@Mapper
@DS("atci")
public interface AtciArchiveBusinessMemberLabelMapper extends BaseMapper<AtciArchiveBusinessMemberLabel> {

}
