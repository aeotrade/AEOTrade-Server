package com.aeotrade.provider.admin.uacVo;


import com.aeotrade.provider.admin.entiy.UacMember;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 企业列表
 * @Author: yewei
 * @Date: 2020/1/7 17:02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class MemberVO extends UacMember implements Serializable {
     private String adminName;
     private String adminTel;
     private Integer settledStutas;
}
