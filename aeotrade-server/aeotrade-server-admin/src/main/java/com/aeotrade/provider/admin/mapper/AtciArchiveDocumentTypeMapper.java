package com.aeotrade.provider.admin.mapper;

import com.aeotrade.provider.admin.entiy.AtciArchiveDocumentType;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: 吴浩
 * @Date: 2023-11-10 14:39
 */
@Mapper
@DS("atci")
public interface AtciArchiveDocumentTypeMapper extends BaseMapper<AtciArchiveDocumentType> {
}
