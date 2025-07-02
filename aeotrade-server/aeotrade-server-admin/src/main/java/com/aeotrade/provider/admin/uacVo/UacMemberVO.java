package com.aeotrade.provider.admin.uacVo;


import com.aeotrade.provider.admin.entiy.UacMember;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
public class UacMemberVO extends UacMember implements Serializable {
    //@ApiModelProperty(value="认证方式", allowEmptyValue=true)
    private List<String> sgsTypes;
    //@ApiModelProperty(value="认证详情", allowEmptyValue=true)
    private List<Object>  sgsDetails;
    //@ApiModelProperty(value="审核人", allowEmptyValue=true)
    private String  auditor= "系统";
}
