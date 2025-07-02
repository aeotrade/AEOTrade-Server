package com.aeotrade.provider.oauth.refreshStaffToken;

import com.aeotrade.provider.dto.UacAdminDto;
import com.aeotrade.provider.model.UacAdmin;
import com.aeotrade.provider.model.UacMember;
import com.aeotrade.provider.model.UacStaff;
import com.aeotrade.provider.model.UacUser;
import com.aeotrade.provider.service.UacAdminService;
import com.aeotrade.provider.service.UacMemberService;
import com.aeotrade.provider.service.UacStaffService;
import com.aeotrade.provider.util.mode.AeotradeAuthUser;
import com.aeotrade.utlis.JacksonUtil;
import lombok.SneakyThrows;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RefreshStaffUserDetailsService implements UserDetailsService {
    @Autowired
    private UacAdminService uacAdminService;
    @Autowired
    private UacStaffService uacStaffService;
    @Autowired
    private UacMemberService uacMemberService;
    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UacStaff uacStaff = uacStaffService.getById(username);
        if (uacStaff==null) {
            throw new UsernameNotFoundException("staff id error");
        }
        List<UacAdmin> uacAdminList = uacAdminService.lambdaQuery().eq(UacAdmin::getStaffId, username).list();
        if (uacAdminList==null||uacAdminList.size()==0) {
            throw new UsernameNotFoundException("UacAdmin staff id error");
        }
        UacAdmin user = uacAdminList.get(0);
        UacUser uacUserDto = new UacUser();
        uacUserDto.setUserId(user.getId());
        uacUserDto.setUsername(user.getUsername());
        uacUserDto.setPassword(user.getPassword());
        uacUserDto.setStatus(user.getStatus());
        uacUserDto.setMobile(user.getMobile());
        uacUserDto.setModifyTime(user.getUpdateTime());
        uacUserDto.setStaffId(user.getStaffId());
        uacUserDto.setCreateTime(user.getCreateTime());

        UacAdminDto uudto = new UacAdminDto();
        BeanUtils.copyProperties(uacUserDto, uudto);
        uudto.setAvatar(uacStaff.getWxLogo());
        uudto.setUserId(user.getUserId());
        uudto.setId(user.getId());
        AeotradeAuthUser aeotradeAuthUser = new AeotradeAuthUser(uacUserDto.getUsername(), uacUserDto.getPassword(), new ArrayList<GrantedAuthority>(), uudto);
        BeanUtils.copyProperties(uacUserDto, aeotradeAuthUser);

        uudto.setStaffName(uacStaff.getStaffName());
        uudto.setMobile(uacStaff.getTel());
        uudto.setWxOpenid(uacStaff.getWxOpenid());
        uudto.setWxUnionid(uacStaff.getWxUnionid());
        UacMember uacMember = null;
        if (uacStaff.getLastMemberId()!=null){
            uacMember = uacMemberService.getById(uacStaff.getLastMemberId());
            uudto.setMemberId(uacStaff.getLastMemberId());
        }
        if(uacMember==null){
            uacMember = uacMemberService.getById(uacStaff.getMemberId());
            uudto.setMemberId(uacStaff.getMemberId());
        }
        uudto.setUscCode(uacMember.getUscCode());
        uudto.setMemberName(uacMember.getMemberName());
        uudto.setMemberMobile(uacMember.getStasfTel());
        uudto.setLoginType("用户名密码");
//        uudto.setPassword(null);
        aeotradeAuthUser.setUsername(JacksonUtil.toJson(uudto));
        return aeotradeAuthUser;
    }
}
