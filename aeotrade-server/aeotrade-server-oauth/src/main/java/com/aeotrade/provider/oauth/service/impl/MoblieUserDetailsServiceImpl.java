package com.aeotrade.provider.oauth.service.impl;

import com.aeotrade.provider.dto.UacAdminDto;
import com.aeotrade.provider.model.UacAdmin;
import com.aeotrade.provider.model.UacMember;
import com.aeotrade.provider.model.UacStaff;
import com.aeotrade.provider.oauth.service.MoblieUserDetailsService;
import com.aeotrade.provider.service.UacAdminService;
import com.aeotrade.provider.service.UacMemberService;
import com.aeotrade.provider.service.UacStaffService;
import com.aeotrade.provider.service.UacUserService;
import com.aeotrade.provider.util.mode.AeotradeAuthUser;
import com.aeotrade.utlis.JacksonUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
 * @Author: yewei
 * @Date: 19:05 2020/11/23
 * @Description:
 */
@Service
public class MoblieUserDetailsServiceImpl implements MoblieUserDetailsService,UserDetailsService {

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
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        if (StringUtils.startsWithIgnoreCase(s,"{")){
            UacAdminDto udto= JacksonUtil.parseJson(s, UacAdminDto.class);
            s=udto.getUsername();
        }

        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getUsername,s).eq(UacAdmin::getStatus,1).eq(UacAdmin::getIsTab,1);
        Optional<UacAdmin> uacUserOptional = Optional.ofNullable(uacAdminService.list(uacAdminLambdaQueryWrapper).get(0));

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
    @SneakyThrows
    @Override
    public UserDetails loadUserByMoblie(String mobile) throws UsernameNotFoundException, IOException {
        if (StringUtils.startsWithIgnoreCase(mobile,"{")){
            UacAdminDto udto= JacksonUtil.parseJson(mobile, UacAdminDto.class);
            mobile=udto.getUsername();
        }
        List<UacAdmin> list = uacAdminService.lambdaQuery().eq(UacAdmin::getMobile, mobile).eq(UacAdmin::getStatus, 1)
                .eq(UacAdmin::getIsTab, 1).orderByDesc(UacAdmin::getCreateTime).list();
        Optional<UacAdmin> uacUserOptional = Optional.ofNullable(list.size()>0?list.get(0):null);
        if (!uacUserOptional.isPresent()){
            throw new UsernameNotFoundException("用户名错误");
        }
        UacAdmin uacUserDto=uacUserOptional.get();
        uacUserDto.setId(uacUserOptional.get().getId());
        uacUserDto.setUsername("手机号");
        uacUserDto.setPassword("password");

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
        if (uacMember.getKindId() == 1) {
            uudto.setUscCode(uacMember.getUscCode());
            uudto.setMemberName(uacMember.getMemberName());
            uudto.setMemberMobile(uacMember.getStasfTel());
        }
        uudto.setLoginType("手机号");
        uudto.setStaffId(uacStaff.getId());
        aeotradeAuthUser.setUsername(JacksonUtil.toJson(uudto));
        return aeotradeAuthUser;

    }

}
