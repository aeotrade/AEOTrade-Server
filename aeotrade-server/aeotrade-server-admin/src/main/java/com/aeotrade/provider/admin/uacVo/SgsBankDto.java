package com.aeotrade.provider.admin.uacVo;



import com.aeotrade.provider.admin.entiy.SgsBankInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper=true)
public class SgsBankDto extends SgsBankInfo implements Serializable {

    //@ApiModelProperty(value="认证方式名称", allowEmptyValue=true)
    private String sgsTypeName;

}
