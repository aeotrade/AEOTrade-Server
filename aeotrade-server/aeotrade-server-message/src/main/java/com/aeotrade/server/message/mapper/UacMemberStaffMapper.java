package com.aeotrade.server.message.mapper;

import com.aeotrade.server.message.model.UacMemberStaff;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.github.yulichang.base.MPJBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Auther: 吴浩
 * @Date: 2024/5/10 9:29
 */
@DS("aeotrade")
@Mapper
public interface UacMemberStaffMapper extends MPJBaseMapper<UacMemberStaff> {
}
