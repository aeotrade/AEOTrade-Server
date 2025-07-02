package com.aeotrade.provider.util.mode;

import com.aeotrade.provider.dto.UacAdminDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
@Data
@EqualsAndHashCode(callSuper = true)
public class AeotradeAuthUser extends User {

    private Long userId;

    private String username;

    private String avatar;

    private Long deptId;

    private String description;

    private String email;

    private String mobile;

    private Integer ssex;

    private Long staffId;

    private Integer status;

    private String theme;

    public AeotradeAuthUser(String username, String password, Collection<? extends GrantedAuthority> authorities, UacAdminDto uacUserDto) {
        super(username, password, authorities);
        this.userId = uacUserDto.getId();
    }

}
