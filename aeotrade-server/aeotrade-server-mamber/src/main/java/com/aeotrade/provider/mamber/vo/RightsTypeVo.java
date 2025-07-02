package com.aeotrade.provider.mamber.vo;


import com.aeotrade.provider.mamber.entity.UawRights;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RightsTypeVo {
    //  @ApiModelProperty(value="权益类型名称", allowEmptyValue=true)
    private String rightsTypeName;
    // @ApiModelProperty(value="权益类型图标", allowEmptyValue=true)
    private String ico;
    // @ApiModelProperty(value="权益类型id", allowEmptyValue=true)
    private Long id;
    //  @ApiModelProperty(value="权益集合", allowEmptyValue=true)
    private List<UawRights> ids;
    // @ApiModelProperty(value="权益集合", allowEmptyValue=true)
    private List<UawRights> rightsListList;
}
