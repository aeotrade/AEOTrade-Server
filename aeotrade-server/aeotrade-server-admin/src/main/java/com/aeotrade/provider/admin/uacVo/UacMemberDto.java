package com.aeotrade.provider.admin.uacVo;


import com.aeotrade.provider.admin.entiy.UacMember;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 企业添加
 * @Author: yewei
 * @Date: 2020/1/7 18:23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class UacMemberDto extends UacMember {

    private String code;
    private Long vipTypeId;


}
