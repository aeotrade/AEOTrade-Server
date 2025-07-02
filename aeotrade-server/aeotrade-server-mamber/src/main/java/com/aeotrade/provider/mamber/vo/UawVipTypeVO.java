package com.aeotrade.provider.mamber.vo;


import com.aeotrade.provider.mamber.entity.UawWorkbench;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UawVipTypeVO extends UawVipTypeDto implements Serializable {

  //  @ApiModelProperty(value="是否入驻", allowEmptyValue=true)
    private Integer isAptiudes;

    private String vipTypeName;

    private UawWorkbench uawWorkbench;
}
