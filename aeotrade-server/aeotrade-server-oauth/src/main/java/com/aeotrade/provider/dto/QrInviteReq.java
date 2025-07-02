package com.aeotrade.provider.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QrInviteReq {
    // 邀请人
    private Long staffId;
    // 手机号
    private String phoneNumber;
    // 称呼
    private String name;
    // 企业标识
    private Long memberId;
    // 微信开放平台的唯一标识符
    private String unionid;
    // 用户唯一标识
    private String openid;
    // 角色
    private String roleId;

    private String deptId;

}
