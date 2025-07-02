package com.aeotrade.provider.service;

import com.aeotrade.provider.dto.WxTokenDto;
import com.aeotrade.provider.dto.AtciLogDto;
import com.aeotrade.provider.dto.AtclMemberDto;
import com.aeotrade.provider.dto.WxTencentDto;
import com.aeotrade.provider.dto.WxUacStaffDto;
import com.aeotrade.provider.model.UacStaff;
import com.aeotrade.provider.vo.AtclMemberVO;
import com.github.yulichang.base.MPJBaseService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 企业员工 服务类
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
public interface UacStaffService extends MPJBaseService<UacStaff> {

    public void updatestaff(WxTencentDto wxTencentDto, WxUacStaffDto wxUacStaffDto, WxTokenDto wxTokenDto)throws RuntimeException;

    public int SetDefultMember(Long staffId,Long memberId);

}
