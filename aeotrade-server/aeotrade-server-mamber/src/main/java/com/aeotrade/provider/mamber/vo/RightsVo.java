package com.aeotrade.provider.mamber.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RightsVo {
   // @ApiModelProperty(value="权益类型id", allowEmptyValue=true,required = true)
    private Long  id ;
   // @ApiModelProperty(value="权益id数组", allowEmptyValue=true,required = true)
    private Long [] ids ;

}
