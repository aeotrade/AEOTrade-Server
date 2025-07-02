package com.aeotrade.provider.service;

import com.aeotrade.provider.dto.UserDto;
import com.aeotrade.provider.model.UacMemberStaff;
import com.aeotrade.provider.model.UacStaff;
import com.aeotrade.provider.model.UacUserConnection;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author aeo
 * @since 2023-11-03
 */
public interface UacMemberStaffService extends IService<UacMemberStaff> {
    public void initUacStaffMember(Long staffId,Long pMemberId);
    public void deleteStaff(Long id);
    public void buildStaffAndMember(Long staffId,Long pMemberId,String staffName,String roleId,String deptId);
    public int upadteAdmin(UserDto userDto) throws Exception;
    public UacStaff initUacStaffAndUacMember(UacUserConnection uacUserConnection, Long pStaffId, Long pMemberId,String phone);
    public void update(Long staffId, Long memberId,String memberName,String uscc);
}
