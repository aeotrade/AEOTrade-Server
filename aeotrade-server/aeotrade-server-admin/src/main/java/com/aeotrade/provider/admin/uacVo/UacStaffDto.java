package com.aeotrade.provider.admin.uacVo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UacStaffDto implements Serializable {
    private static final long serialVersionUID = -7534447345705980678L;
    private Long staffId;
    private String wxLogo;
    private String wxOpenid;
    private String unionid;
    private Long memberId;
    private String memberName;
    private Long kindId;
    private String stasfTel;
    private String validateCode;
    private String email;
}
