package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author aeo
 * @since 2023-11-02
 */
@Getter
@Setter
@TableName("uac_user_connection")
public class UacUserConnection {

    private Long userId;

    private String providerId;

    private String providerUserId;

    private Long ranks;

    private String displayName;

    private String profileUrl;

    private String imageUrl;

    private String accessToken;

    private String secret;

    private String refreshToken;

    private String unionid;

    /**
     * 员工ID
     */
    private Long staffId;

    /**
     * 唯一标识
     */
    private Long id;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * TOKEN过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 公众平台openid
     */
    private String providerMpId;
}
