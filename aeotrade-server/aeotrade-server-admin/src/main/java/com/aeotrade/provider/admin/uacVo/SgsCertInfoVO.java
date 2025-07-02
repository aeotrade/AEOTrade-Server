package com.aeotrade.provider.admin.uacVo;


import com.aeotrade.provider.admin.entiy.SgsCertInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=true)
public class SgsCertInfoVO extends SgsCertInfo implements Serializable {


//    @Column(name="sgs_type", length=10)
    //@ApiModelProperty(value="认证类型", allowEmptyValue=true)
    private Integer sgsType;

//    @Column(name="sgs_type_name")
    //@ApiModelProperty(value="认证方式名称", allowEmptyValue=true)
    private String sgsTypeName;
}
