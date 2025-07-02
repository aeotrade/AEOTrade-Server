package com.aeotrade.provider.oauth.service;

import com.aeotrade.provider.dto.UacAdminDto;
import com.aeotrade.provider.model.UacAdmin;
import com.aeotrade.provider.model.UacMember;
import com.aeotrade.provider.model.UacStaff;
import com.aeotrade.provider.model.UacUser;
import com.aeotrade.provider.oauth.config.ValidateCodeException;
import com.aeotrade.provider.service.UacAdminService;
import com.aeotrade.provider.service.UacMemberService;
import com.aeotrade.provider.service.UacStaffService;
import com.aeotrade.provider.service.UacUserService;
import com.aeotrade.provider.util.mode.AdminAuthUser;
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

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class AeotradeUserDetailsService implements UserDetailsService {

    @Autowired
    private UacUserService uacUserService;
    @Autowired
    private UacAdminService uacAdminService;
    @Autowired
    private UacStaffService uacStaffService;
    @Autowired
    private UacMemberService uacMemberService;

    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        if (s.contains("code-")) {
            String userName = s.replaceAll("code-", "");
            return loadAdminByUserName(userName);
        } else {
            if (StringUtils.startsWithIgnoreCase(s, "{")) {
                UacAdminDto udto = JacksonUtil.parseJson(s, UacAdminDto.class);
                s = udto.getUsername();
            }
            List<UacAdmin> list = uacAdminService.lambdaQuery().eq(UacAdmin::getUsername, s).eq(UacAdmin::getStatus, 1).eq(UacAdmin::getIsTab, 1).list();
            Optional<UacAdmin> uacUserOptional = Optional.ofNullable(list.size()>0?list.get(0):null);

            if (!uacUserOptional.isPresent()) {
                throw new UsernameNotFoundException("用户名错误");
            }
            if (uacUserOptional.get().getStatus() != 1) {
                throw new UsernameNotFoundException("账号已被禁用");
            }
            UacAdmin user = uacUserOptional.get();
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
            AeotradeAuthUser aeotradeAuthUser = new AeotradeAuthUser(uacUserDto.getUsername(), uacUserDto.getPassword(), new ArrayList<GrantedAuthority>(), uudto);
            BeanUtils.copyProperties(uacUserDto, aeotradeAuthUser);
            UacStaff uacStaff = uacStaffService.getById(uudto.getStaffId());
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
            uudto.setLoginType("用户名密码");
            aeotradeAuthUser.setUsername(JacksonUtil.toJson(uudto));
            return aeotradeAuthUser;
        }
    }

    private UserDetails loadAdminByUserName(String userName) throws IOException {
        if (StringUtils.startsWithIgnoreCase(userName, "{")) {
            UacAdminDto udto = JacksonUtil.parseJson(userName, UacAdminDto.class);
            userName = udto.getUsername();
        }
        List<UacAdmin> list = uacAdminService.lambdaQuery()
                .eq(UacAdmin::getUsername, userName)
                .eq(UacAdmin::getIsTab, 2).list();
        Optional<UacAdmin> uacUserOptional = Optional.ofNullable(list.size()>0?list.get(0):null);
        if (!uacUserOptional.isPresent()) {
            throw new ValidateCodeException("用户名错误");
        }

        if (uacUserOptional.get().getStatus() != 1) {
            throw new ValidateCodeException("账号已被禁用");
        }
        UacAdmin uacAdmin = uacUserOptional.get();

        AdminAuthUser aeotradeAuthUser = new AdminAuthUser(uacAdmin.getUsername(), uacAdmin.getPassword(),new ArrayList<GrantedAuthority>(), uacAdmin.getId());
        UacAdminDto uacAdminDto = new UacAdminDto();
        BeanUtils.copyProperties(uacAdmin, uacAdminDto);
        uacAdminDto.setStaffId(uacAdmin.getStaffId());
        uacAdminDto.setUserId(uacAdmin.getId().toString());
        UacStaff uacStaff = uacStaffService.getById(uacAdmin.getStaffId());
        if (uacStaff != null) {
            UacMember uacMember;
            if (uacStaff.getLastMemberId() != null) {
                uacMember = uacMemberService.getById(uacStaff.getLastMemberId());
                uacAdminDto.setMemberId(uacStaff.getLastMemberId());
            } else {
                uacMember = uacMemberService.getById(uacStaff.getMemberId());
                if (uacMember == null) {
                    throw new ValidateCodeException("账号问题，请联系系统管理员");
                }
            }
            uacAdminDto.setUscCode(uacMember.getUscCode());
            uacAdminDto.setMemberName(uacMember.getMemberName());
            uacAdminDto.setMemberMobile(uacMember.getStasfTel());
            uacAdminDto.setMemberId(uacMember.getId());
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        uacAdminDto.setLoginTime(uacAdmin.getLoginTime().format(formatter));
        uacAdminDto.setCreateTime(uacAdmin.getCreateTime().format(formatter));
        uacAdminDto.setLoginType("运营用户名密码");
        aeotradeAuthUser.setUsername(JacksonUtil.toJson(uacAdminDto));
        return aeotradeAuthUser;
    }
}
