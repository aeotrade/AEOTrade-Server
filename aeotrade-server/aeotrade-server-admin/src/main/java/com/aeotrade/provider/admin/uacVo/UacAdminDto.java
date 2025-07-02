package com.aeotrade.provider.admin.uacVo;


import com.aeotrade.provider.admin.entiy.UacAdmin;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author: yewei
 * @Date: 10:36 2020/12/22
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class UacAdminDto extends UacAdmin implements Serializable {

    private String loginType;
    private String staffName;
    private String wxOpenid;
    private String wxUnionid;
    private Long memberId;
    private String uscCode;
    private String memberName;
    private String memberMobile;








}
