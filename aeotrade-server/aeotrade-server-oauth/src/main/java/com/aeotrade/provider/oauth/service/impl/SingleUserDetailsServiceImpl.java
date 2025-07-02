package com.aeotrade.provider.oauth.service.impl;

import com.aeotrade.provider.dto.UacAdminDto;
import com.aeotrade.provider.model.UacAdmin;
import com.aeotrade.provider.model.UacMember;
import com.aeotrade.provider.model.UacStaff;
import com.aeotrade.provider.oauth.service.SingleUserDetailsService;
import com.aeotrade.provider.service.UacAdminService;
import com.aeotrade.provider.service.UacMemberService;
import com.aeotrade.provider.service.UacStaffService;
import com.aeotrade.provider.service.UacUserService;
import com.aeotrade.provider.util.mode.AeotradeAuthUser;
import com.aeotrade.utlis.JacksonUtil;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @Auther: 吴浩
 * @Date: 2021-07-01 14:41
 */
@Service
public class SingleUserDetailsServiceImpl implements SingleUserDetailsService, UserDetailsService {
    @Autowired
    private UacUserService uacUserService;
    @Autowired
    private UacStaffService uacStaffService;
    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private UacAdminService uacAdminService;

    @SneakyThrows
    @Override
    public UserDetails loadUserByMoblie(String res) throws IOException {
        if (StringUtils.startsWithIgnoreCase(res,"{")){
            UacAdminDto udto= JacksonUtil.parseJson(res, UacAdminDto.class);
            res=udto.getUsername();
        }

        List<UacAdmin> list = uacAdminService.lambdaQuery().eq(UacAdmin::getSingleRes, res)
                .eq(UacAdmin::getStatus, 1).eq(UacAdmin::getIsTab, 1).list();
        Optional<UacAdmin> uacUserOptional = Optional.ofNullable(list.size()>0?list.get(0):null);
        if (!uacUserOptional.isPresent()){
            throw new UsernameNotFoundException("北京单一窗口用户唯一标识错误");
        }
        UacAdmin uacUserDto=uacUserOptional.get();
        uacUserDto.setId(uacUserOptional.get().getId());
        uacUserDto.setUsername(uacUserDto.getUsername());
        uacUserDto.setPassword(uacUserDto.getPassword());
        UacAdminDto uudto = new UacAdminDto();
        BeanUtils.copyProperties(uacUserDto,uudto);
        AeotradeAuthUser aeotradeAuthUser=new AeotradeAuthUser(uacUserDto.getUsername(),uacUserDto.getPassword(),new ArrayList<GrantedAuthority>(),uudto);
        BeanUtils.copyProperties(uacUserDto,aeotradeAuthUser);
        UacStaff uacStaff=uacStaffService.getById(uudto.getStaffId());
        uudto.setAvatar(uacStaff.getWxLogo());
        uudto.setMemberId(uacStaff.getMemberId());
        uudto.setStaffName(uacStaff.getStaffName());
        uudto.setMobile(uacStaff.getTel());
        uudto.setWxOpenid(uacStaff.getWxOpenid());
        uudto.setWxUnionid(uacStaff.getWxUnionid());
        UacMember uacMember = uacMemberService.getById(uacStaff.getMemberId());
        uudto.setUscCode(uacMember.getUscCode());
        uudto.setMemberName(uacMember.getMemberName());
        uudto.setMemberMobile(uacMember.getStasfTel());
        uudto.setLoginType("单一窗口本地标识登录");
        uudto.setStaffId(uacStaff.getId());
        aeotradeAuthUser.setUsername(JacksonUtil.toJson(uudto));
        return aeotradeAuthUser;
    }


    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        if (StringUtils.startsWithIgnoreCase(s,"{")){
            UacAdminDto udto= JacksonUtil.parseJson(s, UacAdminDto.class);
            s=udto.getUsername();
        }
        List<UacAdmin> list = uacAdminService.lambdaQuery().eq(UacAdmin::getUsername, s)
                .eq(UacAdmin::getStatus, 1).eq(UacAdmin::getIsTab, 1).list();
        Optional<UacAdmin> uacUserOptional = Optional.ofNullable(list.size()>0?list.get(0):null);

        if (!uacUserOptional.isPresent()){
            throw new UsernameNotFoundException("用户名错误");
        }
        UacAdmin uacUserDto=uacUserOptional.get();
        UacAdminDto uudto = new UacAdminDto();
        BeanUtils.copyProperties(uacUserDto,uudto);
        AeotradeAuthUser aeotradeAuthUser=new AeotradeAuthUser(uacUserDto.getUsername(),uacUserDto.getPassword(),new ArrayList<GrantedAuthority>(),uudto);
        BeanUtils.copyProperties(uacUserDto,aeotradeAuthUser);
        UacStaff uacStaff=uacStaffService.getById(uudto.getStaffId());
        uudto.setAvatar(uacStaff.getWxLogo());
        uudto.setMemberId(uacStaff.getMemberId());
        uudto.setStaffName(uacStaff.getStaffName());
        uudto.setMobile(uacStaff.getTel());
        uudto.setWxOpenid(uacStaff.getWxOpenid());
        uudto.setWxUnionid(uacStaff.getWxUnionid());
        UacMember uacMember = uacMemberService.getById(uacStaff.getMemberId());
        uudto.setUscCode(uacMember.getUscCode());
        uudto.setMemberName(uacMember.getMemberName());
        uudto.setMemberMobile(uacMember.getStasfTel());
        aeotradeAuthUser.setUsername(JacksonUtil.toJson(uudto));
        return aeotradeAuthUser;

    }
}
