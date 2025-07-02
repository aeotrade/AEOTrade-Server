package com.aeotrade.provider.admin.entiy;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

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
@TableName("uac_oauth_client_details")
public class UacOauthClientDetails {

    @TableId("client_id")
    private String clientId;

    private String resourceIds;

    private String clientSecret;

    private String scope;

    private String authorizedGrantTypes;

    private String webServerRedirectUri;

    private String authorities;

    private Long accessTokenValidity;

    private Long refreshTokenValidity;

    private String additionalInformation;

    private String autoapprove;
}
