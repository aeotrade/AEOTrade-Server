package com.aeotrade.provider.oauth.service.impl;

import com.aeotrade.provider.dto.UacAdminDto;
import com.aeotrade.provider.model.UacAdmin;
import com.aeotrade.provider.model.UacMember;
import com.aeotrade.provider.model.UacStaff;
import com.aeotrade.provider.model.UacUserConnection;
import com.aeotrade.provider.oauth.service.WxUserDetailsService;
import com.aeotrade.provider.service.UacAdminService;
import com.aeotrade.provider.service.UacMemberService;
import com.aeotrade.provider.service.UacStaffService;
import com.aeotrade.provider.service.UacUserConnectionService;
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
 * @Date: 17:09 2020/11/25
 * @Description:
 */
@Service
public class WxUserDetailsServiceImpl implements WxUserDetailsService, UserDetailsService {
    @Autowired
    private UacAdminService uacAdminService;
    @Autowired
    private UacStaffService uacStaffService;
    @Autowired
    private UacMemberService uacMemberService;
    @Autowired
    private UacUserConnectionService uacUserConnectionService;

    @SneakyThrows
    @Override
    public UserDetails loadUserByOpenId(String openId) throws IOException {
        if (StringUtils.startsWithIgnoreCase(openId,"{")){
            UacAdminDto udto= JacksonUtil.parseJson(openId, UacAdminDto.class);
            openId=udto.getUsername();
        }
//        public Optional<UacUserConnection> findByStaffId(String openId) {
//            List<UacUserConnection> uacUserConnectionBy = uacUserConnectionMapper.findUacUserConnectionBy(Long.valueOf(openId));
//            if(!CommonUtil.isEmpty(uacUserConnectionBy)){
//                return Optional.ofNullable(uacUserConnectionBy.get(0));
//            }
//            return  Optional.ofNullable(null);
//        }
        List<UacUserConnection> list = uacUserConnectionService.lambdaQuery()
                .eq(UacUserConnection::getStaffId, openId).list();
        Optional<UacUserConnection>  uacUserConnection= Optional.ofNullable(list.size()>0?list.get(0):null);

        //Optional<UacUser> uacUserOptional = uacUserService.queryUacUser(openId);

        if (!uacUserConnection.isPresent()){
            throw new UsernameNotFoundException("未找到该账号");
        }
        UacStaff uacStaff=uacStaffService.getById(uacUserConnection.get().getStaffId());
        UacAdmin uacUserDto=new UacAdmin();
        uacUserDto.setId(1000L);
        uacUserDto.setUsername(uacStaff.getStaffName());
        uacUserDto.setPassword("password");
        //Optional<List<String>> stringsOptional = uacRoleService.findAuthority(uacUserDto.getUserId());

//        Optional<List<String>> stringsOptional = uacRoleService.findAuthority(uacUserDto.getId());
//        List<GrantedAuthority> authList = stringsOptional.get().stream().map(as -> new SimpleGrantedAuthority(as)).collect(Collectors.toList());


        UacAdminDto uudto = new UacAdminDto();
        BeanUtils.copyProperties(uacUserDto,uudto);
        AeotradeAuthUser aeotradeAuthUser=new AeotradeAuthUser(openId,uacUserDto.getPassword(),new ArrayList<GrantedAuthority>(),uudto);
        BeanUtils.copyProperties(uacUserDto,aeotradeAuthUser);
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
        uudto.setLoginType("微信");
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
        LambdaQueryWrapper<UacAdmin> uacAdminLambdaQueryWrapper=new LambdaQueryWrapper<>();
        uacAdminLambdaQueryWrapper.eq(UacAdmin::getUsername,s).eq(UacAdmin::getStatus,1).eq(UacAdmin::getIsTab,1);
        Optional<UacAdmin> uacUserOptional = Optional.ofNullable(uacAdminService.list(uacAdminLambdaQueryWrapper).get(0));

        if (!uacUserOptional.isPresent()){
            throw new UsernameNotFoundException("用户名错误");
        }
        UacAdmin uacUserDto=uacUserOptional.get();

        //Optional<List<String>> stringsOptional = uacRoleService.findAuthority(uacUserDto.getUserId());

//        Optional<List<String>> stringsOptional = uacRoleService.findAuthority(uacUserDto.getId());
//        List<GrantedAuthority> authList = stringsOptional.get().stream().map(as -> new SimpleGrantedAuthority(as)).collect(Collectors.toList());

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
