package com.aeotrade.provider.util.mode;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminAuthUser extends User {

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

    public AdminAuthUser(String username, String password, Collection<? extends GrantedAuthority> authorities, Long  userId) {
        super(username, password, authorities);
        this.userId = userId;
    }

    public AdminAuthUser(String username, String password, boolean enabled, boolean accountNonExpired,
                         boolean credentialsNonExpired, boolean accountNonLocked,
                         Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }
}
